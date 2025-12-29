package com.sample.sampleservice.feature.auth.infrastructure.secondary.repository;

import com.sample.sampleservice.feature.auth.domain.exception.UserErrorKey;
import com.sample.sampleservice.feature.auth.domain.model.*;
import com.sample.sampleservice.feature.auth.domain.repository.UserRepository;
import com.sample.sampleservice.feature.auth.infrastructure.secondary.domain.RoleEntity;
import com.sample.sampleservice.feature.auth.infrastructure.secondary.domain.UserEntity;
import com.sample.sampleservice.shared.authentication.application.AuthenticatedUser;
import com.sample.sampleservice.shared.authentication.infrastructure.primary.JwtTokenProvider;
import com.sample.sampleservice.shared.emailing.service.EmailService;
import com.sample.sampleservice.shared.error.domain.Assert;
import com.sample.sampleservice.shared.error.domain.GeneratorException;
import com.sample.sampleservice.shared.otp.application.OtpApplication;
import com.sample.sampleservice.shared.pagination.domain.Page;
import com.sample.sampleservice.shared.pagination.domain.Pageable;
import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Repository
@RequiredArgsConstructor
public class UserRepositoryServiceImpl implements UserRepository {

    private final UserEntityRepository userEntityRepository;
    private final RoleEntityRepository roleEntityRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final EmailService emailService;
    private final OtpApplication otpApplication;

    @Override
    @Transactional
    public UserDetails createUser(CreateUser createUser, String roleName, boolean verified) {
        if (userEntityRepository.existsByUsername(createUser.username())) {
            throw GeneratorException.badRequest(UserErrorKey.USER_ALREADY_EXISTS).message("Username already taken").build();
        }
        if (userEntityRepository.existsByEmail(createUser.email())) {
            throw GeneratorException.badRequest(UserErrorKey.EMAIL_ALREADY_EXISTS).message("Email already taken").build();
        }

        RoleEntity roleEntity = roleEntityRepository.findByName(roleName)
                .orElseThrow(() -> GeneratorException.badRequest(UserErrorKey.ROLE_NOT_FOUND).message("Role not found: " + roleName).build());

        UserEntity entity = UserEntity.builder()
                .username(createUser.username())
                .firstName(createUser.firstName())
                .lastName(createUser.lastName())
                .email(createUser.email())
                .phoneNumber(createUser.phoneNumber())
                .emailVerified(verified)
                .enabled(true)
                .createdTimestamp(System.currentTimeMillis())
                .password(passwordEncoder.encode(createUser.password()))
                .roles(new HashSet<>(Collections.singletonList(roleEntity)))
                .build();

        userEntityRepository.save(entity);
        emailService.sendRegistrationEmail(entity.getEmail(), entity.getUsername());
        sendOtp(entity.getEmail(), entity.getEmail());
        return mapEntityToUserDetails(entity);
    }

    @Override
    public Optional<OAuth2TokenResult> login(String username, String password) {
        UserEntity user = userEntityRepository.findByUsername(username)
                .orElseThrow(() -> GeneratorException.badRequest(UserErrorKey.USER_NOT_FOUND).message("User not found").build());

        if (!passwordEncoder.matches(password, user.getPassword())) {
             throw GeneratorException.badRequest(UserErrorKey.BAD_CREDENTIALS).message("Invalid Credentials").build();
        }

        if (!user.isEnabled()) {
            throw GeneratorException.badRequest(UserErrorKey.USER_LOCKED).message("Account is disabled").build();
        }
        
        if (!user.isEmailVerified()) {
            sendOtp(user.getUsername(), user.getEmail());
            throw GeneratorException.badRequest(UserErrorKey.VERIFY_EMAIL).message("You need to verify your email address to activate your account.").build();
        }

        return Optional.of(generateTokenForUser(user));
    }

    private void sendOtp(String identifier, String emailDestination) {
        String otp = otpApplication.generate(identifier);
        emailService.sendVerificationOtp(emailDestination, otp);        
    }

    @Override
    public Optional<OAuth2TokenResult> refresh(String refreshToken) {
        if (!jwtTokenProvider.validateToken(refreshToken)) {
             throw GeneratorException.badRequest(UserErrorKey.TOKEN_INACTIVE).message("Invalid or expired refresh token").build();
        }
        String username = jwtTokenProvider.getUsernameFromToken(refreshToken);

        UserEntity user = userEntityRepository.findByUsername(username)
                .orElseThrow(() -> GeneratorException.badRequest(UserErrorKey.USER_NOT_FOUND).message("User not found").build());

        if (!user.isEnabled()) {
            throw GeneratorException.badRequest(UserErrorKey.USER_LOCKED).message("User is disabled").build();
        }

        return Optional.of(generateTokenForUser(user));
    }

    private OAuth2TokenResult generateTokenForUser(UserEntity user) {
        List<SimpleGrantedAuthority> authorities = user.getRoles().stream()
                .map(r -> new SimpleGrantedAuthority(r.getName()))
                .collect(Collectors.toList());

        Authentication auth = new UsernamePasswordAuthenticationToken(user.getUsername(), null, authorities);
        return jwtTokenProvider.createToken(auth, user.getId());
    }

    @Override
    public UserDetails findByUsername(String username) {
        return userEntityRepository.findByUsername(username)
                .map(this::mapEntityToUserDetails)
                .orElseThrow(() -> GeneratorException.badRequest(UserErrorKey.USER_NOT_FOUND).message("User not found").build());
    }

    @Override
    public UserDetails findByEmail(String email) {
        return userEntityRepository.findByEmail(email)
                .map(this::mapEntityToUserDetails)
                .orElse(null);
    }

    @Override
    public Optional<UserDetails> getUserDetailById(String userId) {
        return userEntityRepository.findById(userId).map(this::mapEntityToUserDetails);
    }

    @Override
    @Transactional
    public void updatePassword(String username, ChangePassword changePassword) {
        UserEntity user = userEntityRepository.findByUsername(username)
                .orElseThrow(() -> GeneratorException.badRequest(UserErrorKey.USER_NOT_FOUND).message("User not found").build());

        // Check old password
        if (!passwordEncoder.matches(changePassword.oldPassword(), user.getPassword())) {
            throw GeneratorException.badRequest(UserErrorKey.BAD_CREDENTIALS).message("Old password does not match").build();
        }

        user.setPassword(passwordEncoder.encode(changePassword.newPassword()));
        userEntityRepository.save(user);
    }

    @Override
    @Transactional
    public void resetPassword(ChangePassword changePassword) {
        Assert.notNull("change password", changePassword);
        
        String userId = AuthenticatedUser.getUser().id(); 
        UserEntity user = userEntityRepository.findById(userId)
                .orElseThrow(() -> GeneratorException.badRequest(UserErrorKey.USER_NOT_FOUND).build());

        if(!passwordEncoder.matches(changePassword.oldPassword(), user.getPassword())) {
             throw GeneratorException.badRequest(UserErrorKey.BAD_CREDENTIALS).message("Invalid current password").build();
        }

        user.setPassword(passwordEncoder.encode(changePassword.newPassword()));
        userEntityRepository.save(user);
    }

    @Override
    public void forgotPassword(String email) {
        UserEntity user = userEntityRepository.findByEmail(email)
                .orElseThrow(() -> GeneratorException.badRequest(UserErrorKey.USER_NOT_FOUND).message("User not found").build());

        // TODO: Integrate NotificationService / EmailService here
        // Generate a random token, save it to DB (e.g. PasswordResetToken entity), and send email.
        log.info("Simulating sending forgot password email to: " + email);
    }

    @Override
    public boolean verifyOtp(String identifier, String code) {
        return otpApplication.verify(identifier, code);
    }

    @Override
    @Transactional
    public UserDetails setEmailVerified(UserDetails user) {
        var userEntity = userEntityRepository.findByEmail(user.getEmail()).orElseThrow(
            () -> GeneratorException.badRequest(UserErrorKey.USER_NOT_FOUND).message("User Was Not Found!").build()
        );
        userEntity.setEmailVerified(true);
        var savedUser = userEntityRepository.save(userEntity);
        return mapEntityToUserDetails(savedUser);
    }

    @Override
    public void sendVerify(String username, String password) {
        UserEntity user = userEntityRepository.findByUsername(username)
                .orElseThrow(() -> GeneratorException.badRequest(UserErrorKey.USER_NOT_FOUND).build());

        if (!passwordEncoder.matches(password, user.getPassword())) {
             throw GeneratorException.badRequest(UserErrorKey.BAD_CREDENTIALS).build();
        }

        // TODO: Integrate NotificationService
        log.info("Simulating sending verification email to: " + user.getEmail());
    }

    @Override
    @Transactional
    public UserDetails update(String userId, UserRequest request) {
        UserEntity user = userEntityRepository.findById(userId)
                .orElseThrow(() -> GeneratorException.badRequest(UserErrorKey.USER_NOT_FOUND).build());

        if (StringUtils.isNotBlank(request.getFirstName())) user.setFirstName(request.getFirstName());
        if (StringUtils.isNotBlank(request.getLastName())) user.setLastName(request.getLastName());
        if (StringUtils.isNotBlank(request.getEmail())) user.setEmail(request.getEmail());
        if (StringUtils.isNotBlank(request.getPhoneNumber())) user.setPhoneNumber(userId);
        
        userEntityRepository.save(user);
        return mapEntityToUserDetails(user);
    }

    @Override
    public UserDetails enableUser(String userId) {
        return updateUserStatus(userId, true);
    }

    @Override
    public UserDetails disableUser(String userId) {
        return updateUserStatus(userId, false);
    }

    private UserDetails updateUserStatus(String userId, boolean status) {
        UserEntity user = userEntityRepository.findById(userId)
                .orElseThrow(() -> GeneratorException.badRequest(UserErrorKey.USER_NOT_FOUND).build());
        user.setEnabled(status);
        userEntityRepository.save(user);
        return mapEntityToUserDetails(user);
    }

    @Override
    public List<String> myRoles(String userId) {
        return userEntityRepository.findById(userId)
                .map(user -> user.getRoles().stream()
                        .map(RoleEntity::getName)
                        .collect(Collectors.toList()))
                .orElse(Collections.emptyList());
    }


    @Override
    public Page<UserDetails> findAll(String role, Boolean emailVerified, Boolean enabled, Boolean exact, String searchTerm, Pageable customPageable) {
        Set<String> ALLOWED_SORT_FIELDS = Set.of("username", "createdTimestamp", "firstName", "lastName");
        String DEFAULT_SORT_FIELD = "createdTimestamp";
        String validatedSortBy = ALLOWED_SORT_FIELDS.contains(customPageable.getSortBy()) ? customPageable.getSortBy() : DEFAULT_SORT_FIELD;

        Sort.Direction direction = customPageable.getDirection() == Pageable.Direction.ASC ? Sort.Direction.ASC : Sort.Direction.DESC;
        PageRequest springPageable = PageRequest.of(customPageable.getPage() - 1, customPageable.getPageSize(), Sort.by(direction, validatedSortBy));

        Specification<UserEntity> spec = (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (StringUtils.isNotBlank(role)) {
                predicates.add(criteriaBuilder.equal(root.join("roles").get("name"), role));
            }
            if (emailVerified != null) {
                predicates.add(criteriaBuilder.equal(root.get("emailVerified"), emailVerified));
            }
            if (enabled != null) {
                predicates.add(criteriaBuilder.equal(root.get("enabled"), enabled));
            }
            if (StringUtils.isNotBlank(searchTerm)) {
                String likePattern = "%" + searchTerm.toLowerCase() + "%";
                Predicate searchPredicate = criteriaBuilder.or(
                    criteriaBuilder.like(criteriaBuilder.lower(root.get("firstName")), likePattern),
                    criteriaBuilder.like(criteriaBuilder.lower(root.get("lastName")), likePattern),
                    criteriaBuilder.like(criteriaBuilder.lower(root.get("username")), likePattern),
                    criteriaBuilder.like(criteriaBuilder.lower(root.get("email")), likePattern),
                    criteriaBuilder.like(root.get("phoneNumber"), "%" + searchTerm + "%")
                );
                predicates.add(searchPredicate);
            }
            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };

        org.springframework.data.domain.Page<UserEntity> userEntityPage = userEntityRepository.findAll(spec, springPageable);

        List<UserDetails> userDetailsContent = userEntityPage.getContent().stream()
                .map(this::mapEntityToUserDetails)
                .toList();

        return new Page<UserDetails>()
                .content(userDetailsContent)
                .currentPage(userEntityPage.getNumber() + 1)
                .total((int) userEntityPage.getTotalElements())
                .totalPages(userEntityPage.getTotalPages())
                .hasNext(userEntityPage.hasNext())
                .hasPrevious(userEntityPage.hasPrevious())
                .isLast(userEntityPage.isLast())
                .isEmpty(userEntityPage.isEmpty());
    }

    private UserDetails mapEntityToUserDetails(UserEntity entity) {
        if (entity == null) return null;
        
        return UserDetails.builder()
            .id(entity.getId())
            .username(entity.getUsername())
            .email(entity.getEmail())
            .firstName(entity.getFirstName())
            .lastName(entity.getLastName())
            .emailVerified(entity.isEmailVerified())
            .phoneNumber(entity.getPhoneNumber())
            .enabled(entity.isEnabled())
            .createdTimestamp(entity.getCreatedTimestamp())
            .roles(entity.getRoles().stream().map(RoleEntity::getName).collect(Collectors.toList()))
            .build();
    }
}

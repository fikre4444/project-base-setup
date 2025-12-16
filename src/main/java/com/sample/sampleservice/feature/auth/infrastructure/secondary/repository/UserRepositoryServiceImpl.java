package com.sample.sampleservice.feature.auth.infrastructure.secondary.repository;

import com.sample.sampleservice.feature.auth.domain.exception.UserErrorKey;
import com.sample.sampleservice.feature.auth.domain.model.*;
import com.sample.sampleservice.feature.auth.domain.repository.UserRepository;
import com.sample.sampleservice.feature.auth.infrastructure.secondary.config.KeycloakClientConfig;
import com.sample.sampleservice.feature.auth.infrastructure.secondary.domain.RoleEntity;
import com.sample.sampleservice.feature.auth.infrastructure.secondary.domain.UserEntity;
import com.sample.sampleservice.feature.auth.infrastructure.secondary.model.Credential;
import com.sample.sampleservice.feature.auth.infrastructure.secondary.model.RoleRepresentation;
import com.sample.sampleservice.feature.auth.infrastructure.secondary.model.UserRequestDto;
import com.sample.sampleservice.feature.auth.infrastructure.secondary.openfeign.KeycloakAdminClient;
import com.sample.sampleservice.feature.auth.infrastructure.secondary.openfeign.KeycloakAuthFeignClient;
import com.sample.sampleservice.shared.authentication.application.AuthenticatedUser;
import com.sample.sampleservice.shared.authentication.domain.Role;
import com.sample.sampleservice.shared.authentication.domain.User;
import com.sample.sampleservice.shared.error.domain.Assert;
import com.sample.sampleservice.shared.error.domain.GeneratorException;
import com.sample.sampleservice.shared.openfeign.infrastructure.secondary.OpenFeignErrorDecoder;
import com.sample.sampleservice.shared.pagination.domain.Page;
import com.sample.sampleservice.shared.pagination.domain.Pageable;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Repository;
import jakarta.persistence.criteria.Predicate;

import java.util.Locale;
import java.util.Objects;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Slf4j
@Repository
@RequiredArgsConstructor
public class UserRepositoryServiceImpl implements UserRepository {

    private final KeycloakAuthFeignClient keycloakAuthFeignClient;
    private final KeycloakClientConfig keycloakClientConfig;
    private final KeycloakAdminClient keycloakAdminClient;

    private final UserEntityRepository userEntityRepository;
    private final RoleEntityRepository roleEntityRepository;

    private UserRepositoryServiceImpl self;

    @Autowired
    public void setSelf(@Lazy UserRepositoryServiceImpl self) {
        this.self = self;
    }

    @Override
    public UserDetails findByUsername(String username) {
        final OAuth2TokenResult tokenResult = authenticateClient();
        final String bearerToken = "bearer " + tokenResult.getAccessToken();

        return keycloakAdminClient.getUserByUsername(bearerToken, username, null)
                .stream().findFirst()
                .orElseThrow(() -> GeneratorException.badRequest(UserErrorKey.USER_NOT_FOUND).message("User not found").build());

    }

    @Override
    public UserDetails findByEmail(String email) {
        final OAuth2TokenResult tokenResult = authenticateClient();
        final String bearerToken = "bearer " + tokenResult.getAccessToken();

        return keycloakAdminClient.getUserByUsername(bearerToken, null, email)
                .stream().findFirst()
                .orElse(null);
    }

    @Override
    public Optional<OAuth2TokenResult> login(String username, String password) {
        try {
            OAuth2TokenResult oAuth2TokenResult = keycloakAuthFeignClient.refresh(UserLogin.builder()
                    .username(username)
                    .password(password)
                    .client_id(keycloakClientConfig.getClient().getId())
                    .client_secret(keycloakClientConfig.getClient().getSecret())
                    .grant_type("password")
                    .build());
            return Optional.ofNullable(oAuth2TokenResult);
        } catch (OpenFeignErrorDecoder.OpenFeignException ex) {
            if (ex.getStatus() != 401) {
                final OAuth2TokenResult tokenResult = authenticateClient();
                final String bearerToken = "bearer " + tokenResult.getAccessToken();
                UserDetails userDetail = keycloakAdminClient.getUserByUsername(bearerToken, username, null)
                        .stream()
                        .findFirst()
                        .orElseThrow(() -> ex);
                if (userDetail.getRequiredActions().contains("UPDATE_PASSWORD")) {
                    throw GeneratorException.badRequest(UserErrorKey.UPDATE_PASSWORD).message("You need to change your password to activate your account.").build();
                } else if (userDetail.getRequiredActions().contains("VERIFY_EMAIL")) {
                    throw GeneratorException.badRequest(UserErrorKey.VERIFY_EMAIL).message("You need to verify your email address to activate your account.").build();
                }
            }
            throw ex;
        }
    }

    @Override
    public void updatePassword(String username, ChangePassword changePassword) {
        try {
            keycloakAuthFeignClient.refresh(UserLogin.builder()
                    .username(username)
                    .password(changePassword.oldPassword())
                    .client_id(keycloakClientConfig.getClient().getId())
                    .client_secret(keycloakClientConfig.getClient().getSecret())
                    .grant_type("password")
                    .build());
        } catch (OpenFeignErrorDecoder.OpenFeignException ex) {
            if (ex.getStatus() != 401) {
                final OAuth2TokenResult tokenResult = authenticateClient();
                final String bearerToken = "bearer " + tokenResult.getAccessToken();
                UserDetails userDetail = keycloakAdminClient.getUserByUsername(bearerToken, username, null)
                        .stream()
                        .findFirst()
                        .orElseThrow(() -> ex);
                if (userDetail.getRequiredActions().contains("UPDATE_PASSWORD")) {
                    keycloakAdminClient.resetPassword(bearerToken, userDetail.getId(), Credential.builder()
                            .type("password")
                            .value(changePassword.newPassword())
                            .temporary(false)
                            .build());
                    return;
                }
            }
            throw ex;
        }
    }

    @Override
    public void forgotPassword(String email) {
        final OAuth2TokenResult tokenResult = authenticateClient();
        final String bearerToken = "bearer " + tokenResult.getAccessToken();

        UserDetails userDetails = keycloakAdminClient.getUserByUsername(bearerToken, null, email)
                .stream().findFirst()
                .orElseThrow(() -> GeneratorException.badRequest(UserErrorKey.USER_NOT_FOUND).message("User not found").build());

        keycloakAdminClient.forgotPassword(bearerToken, userDetails.getId());
    }

    @Override
    public void sendVerify(String username, String password) {
        try {
            keycloakAuthFeignClient.refresh(UserLogin.builder()
                    .username(username)
                    .password(password)
                    .client_id(keycloakClientConfig.getClient().getId())
                    .client_secret(keycloakClientConfig.getClient().getSecret())
                    .grant_type("password")
                    .build());
        } catch (OpenFeignErrorDecoder.OpenFeignException ex) {
            if (ex.getStatus() != 401) {
                final OAuth2TokenResult tokenResult = authenticateClient();
                final String bearerToken = "bearer " + tokenResult.getAccessToken();
                UserDetails userDetail = keycloakAdminClient.getUserByUsername(bearerToken, username, null)
                        .stream()
                        .findFirst()
                        .orElseThrow(() -> ex);
                if (userDetail.getRequiredActions().contains("VERIFY_EMAIL")) {
                    keycloakAdminClient.sendVerifyEmail(bearerToken, userDetail.getId());
                    return;
                }
            }
            throw ex;
        }
    }

    @Override
    public Optional<OAuth2TokenResult> refresh(String refreshToken) {
        try {
             OAuth2TokenResult oAuth2TokenResult = keycloakAuthFeignClient.refresh(UserLogin.builder()
                .client_id(keycloakClientConfig.getClient().getId())
                .client_secret(keycloakClientConfig.getClient().getSecret())
                .refresh_token(refreshToken)
                .grant_type("refresh_token")
                .build());
            return Optional.ofNullable(oAuth2TokenResult);
        } catch (OpenFeignErrorDecoder.OpenFeignException ex) {
            if (ex.getStatus() == 400 && ex.getMessage().contains("Token is not active")) {
                throw GeneratorException.badRequest(UserErrorKey.TOKEN_INACTIVE)
                        .message("Token is not active")
                        .build();
            }
            throw ex;
        }
    }

    @Override
    public Optional<UserDetails> getUserDetailById(String userId) {

        return Optional.of(keycloakAdminClient.getUser(formatAuthentication(), userId));
    }

    @Override
    public UserDetails createUser(CreateUser createUser, String role, boolean verified) {
        final UserRequestDto userRequestDto = UserRequestDto.builder()
                .username(createUser.userName())
                .firstName(createUser.firstName())
                .lastName(createUser.lastName())
                .email(createUser.email())
                .realmRoles(List.of(role))
                .emailVerified(verified)
                .credentials(Collections.singletonList(Credential.builder().type("password").temporary(false).value(createUser.password()).build()))
                .attributes(Map.of("phoneNumber", List.of(createUser.phoneNumber())))
                .build();
        var token = formatAuthentication();
        keycloakAdminClient.registerUser(token, userRequestDto);

        UserDetails details = keycloakAdminClient.getUserByUsername(token, createUser.userName(), null)
                .stream().findFirst()
                .orElseThrow(() -> GeneratorException.badRequest(UserErrorKey.USER_NOT_FOUND).message("User not found").build());

        List<RoleRepresentation> roles = keycloakAdminClient.roles(token, details.getId())
                .stream()
                .filter(roleRepresentation -> StringUtils.equalsIgnoreCase(roleRepresentation.getName(), role))
                .toList();

        if (!roles.isEmpty()) {
            keycloakAdminClient.setRoles(token, details.getId(), roles);
        }
        synchronizeNewLocalUser(details, role);
        return details;
    }

    @Override
    public void resetPassword(ChangePassword changePassword) {
        Assert.notNull("change password", changePassword);

        User user = AuthenticatedUser.getUser();
        login(user.username(), changePassword.oldPassword()); // check if old password is correct (if not, an exception will be thrown)

        final OAuth2TokenResult tokenResult = authenticateClient();
        final String bearerToken = "bearer " + tokenResult.getAccessToken();
        keycloakAdminClient.resetPassword(bearerToken, user.id(), Credential.builder()
                .type("password")
                .value(changePassword.newPassword())
                .temporary(false)
                .build());
    }

    @Override
    public Page<UserDetails> findAll(String role, Boolean emailVerified, Boolean enabled, Boolean exact, String searchTerm, Pageable customPageable) {
        Set<String> ALLOWED_SORT_FIELDS = Set.of(
            "username",
            "createdTimestamp",
            "firstName",
            "lastName"
        );
        String DEFAULT_SORT_FIELD = "createdTimestamp";
        String validatedSortBy = ALLOWED_SORT_FIELDS.contains(customPageable.getSortBy())
            ? customPageable.getSortBy()
            : DEFAULT_SORT_FIELD;

        Sort.Direction direction = customPageable.getDirection() == Pageable.Direction.ASC ? Sort.Direction.ASC : Sort.Direction.DESC;
        PageRequest springPageable = PageRequest.of(
            customPageable.getPage() - 1, 
            customPageable.getPageSize(), 
            Sort.by(direction, validatedSortBy)
        );

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

    @Override
    public List<String> myRoles(String userId) {

        final OAuth2TokenResult tokenResult = authenticateClient();
        final String bearerToken = "bearer " + tokenResult.getAccessToken();

        return keycloakAdminClient.myRoles(bearerToken, userId)
                .stream()
                .map(RoleRepresentation::getName)
                .filter(role -> StringUtils.contains(role, "ROLE_"))
                .toList();
    }

    private UserDetails mapEntityToUserDetails(UserEntity entity) {
        if (entity == null) {
            return null;
        }

        UserDetails dto = new UserDetails();
        dto.setId(entity.getId());
        dto.setUsername(entity.getUsername());
        dto.setEmail(entity.getEmail());
        dto.setFirstName(entity.getFirstName());
        dto.setLastName(entity.getLastName());
        dto.setEnabled(entity.isEnabled());
        dto.setEmailVerified(entity.isEmailVerified());
        dto.setCreatedTimestamp(entity.getCreatedTimestamp());
        dto.setPhoneNumber(entity.getPhoneNumber());
        
        List<String> roleNames = entity.getRoles().stream()
                                    .map(RoleEntity::getName)
                                    .toList();
        dto.setRealmRoles(roleNames);

        if(entity.getPhoneNumber() != null){
            dto.setAttributes(Map.of("phoneNumber", List.of(entity.getPhoneNumber())));
        }
        
        return dto;
    }

    private OAuth2TokenResult authenticateClient() {
        return keycloakAuthFeignClient.refresh(UserLogin.builder()
                .client_id(keycloakClientConfig.getClient().getId())
                .client_secret(keycloakClientConfig.getClient().getSecret())
                .grant_type("client_credentials")
                .build());
    }

    private String formatAuthentication() {
        OAuth2TokenResult result = authenticateClient();

        return "Bearer " + result.getAccessToken();
    }

    @Override
    @Transactional
    public UserDetails update(final String userId, final UserRequest request) {
        request.clearCredentials();
        var token = authenticateClient();
        final String bearerToken = "bearer " + token.getAccessToken();

        keycloakAdminClient.updateUser(bearerToken, userId, request);
        UserDetails updatedUserDetails = keycloakAdminClient.getUser(bearerToken, userId, false);
        synchronizeLocalUser(updatedUserDetails);
        return updatedUserDetails;
    }

    @Override
    @Transactional
    public UserDetails updateUserFromDomainSource(String userId, UserRequest request) {
        //updates the user in keyclock and user table if the dmoain entity is updated.
        request.clearCredentials();
        var token = authenticateClient();
        final String bearerToken = "bearer " + token.getAccessToken();
        keycloakAdminClient.updateUser(bearerToken, userId, request);
        UserDetails updatedUserDetails = keycloakAdminClient.getUser(bearerToken, userId, false);
        synchronizeLocalUser(updatedUserDetails);
        return updatedUserDetails;
    }

    //synchronizes the keycloak user with our own user table
    private void synchronizeLocalUser(UserDetails userDetails) {
        UserEntity userEntity = userEntityRepository.findById(userDetails.getId())
                .orElseGet(() -> {
                    UserEntity newUser = new UserEntity();
                    newUser.setId(userDetails.getId());
                    newUser.setCreatedTimestamp(userDetails.getCreatedTimestamp());
                    return newUser;
                });

        userEntity.setId(userDetails.getId());
        userEntity.setUsername(userDetails.getUsername());
        userEntity.setFirstName(userDetails.getFirstName());
        userEntity.setLastName(userDetails.getLastName());
        userEntity.setEmail(userDetails.getEmail());
        userEntity.setEnabled(userDetails.isEnabled());
        userEntity.setEmailVerified(userDetails.isEmailVerified());
        if (userDetails.getAttributes() != null && userDetails.getAttributes().containsKey("phoneNumber")) {
             userEntity.setPhoneNumber(userDetails.getAttributes().get("phoneNumber").stream().findFirst().orElse(null));
        }
        

        List<String> keycloakRoleNames = myRoles(userDetails.getId());
        Set<RoleEntity> roles = keycloakRoleNames.stream()
                .map(roleName -> roleEntityRepository.findById(roleName)
                        .orElseGet(() -> roleEntityRepository.save(new RoleEntity(roleName))))
                .collect(Collectors.toSet());
        userEntity.setRoles(roles);

        userEntityRepository.save(userEntity);
    }

    @Override
    @Transactional
    public UserDetails enableUser(String userId) {

        final OAuth2TokenResult tokenResult = authenticateClient();
        final String bearerToken = "bearer " + tokenResult.getAccessToken();

        var user = this.keycloakAdminClient.getUser(bearerToken, userId, false);
        var request = UserRequest.builder()
                .email(user.getEmail())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .enabled(true)
                .emailVerified(user.isEmailVerified())
                .attributes(user.getAttributes() != null ? new HashMap<>(user.getAttributes()) : null)
                .build();
        return update(userId, request);
    }

    @Override
    @Transactional
    public UserDetails disableUser(String userId) {
        final OAuth2TokenResult tokenResult = authenticateClient();
        final String bearerToken = "bearer " + tokenResult.getAccessToken();

        var user = this.keycloakAdminClient.getUser(bearerToken, userId, false);
        var request = UserRequest.builder()
                .email(user.getEmail())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .enabled(false)
                .emailVerified(user.isEmailVerified())
                .attributes(user.getAttributes() != null ? new HashMap<>(user.getAttributes()) : null)
                .build();
        return update(userId, request);
    }

    private void updateKeycloakRoles(String bearerToken, String userId, List<String> requestedRoles) {
        List<String> roles = requestedRoles.stream().map(StringUtils::toRootUpperCase).toList();
        if(!roles.isEmpty()){
            List<RoleRepresentation> rolesRep = keycloakAdminClient.roles(bearerToken, null, null, null)
                    .stream()
                    .filter(roleRepresentation -> roles.contains(roleRepresentation.getName().toUpperCase()))
                    .toList();
            if (rolesRep.isEmpty()) {
                return;
            }
            
            if (!keycloakAdminClient.myRoles(bearerToken, userId)
                .stream()
                .filter(roleRepresentation -> roles.contains(roleRepresentation.getName().toUpperCase(Locale.ROOT)))
                .toList()
                .isEmpty()) 
            {
                List<RoleRepresentation> currentRoles = keycloakAdminClient.myRoles(bearerToken, userId);
                List<RoleRepresentation> currentAppRoles = currentRoles.stream()
                        .filter(role -> role.getName().startsWith("ROLE_"))
                        .toList();
                keycloakAdminClient.removeRoles(bearerToken, userId, currentAppRoles);
            }
            keycloakAdminClient.setRoles(bearerToken, userId, rolesRep);
        }
    }

    private void synchronizeNewLocalUser(UserDetails userDetails, String roleName) {
        log.debug("Synchronizing new user '{}' to local database.", userDetails.getUsername());
        
        if (userEntityRepository.existsById(userDetails.getId())) {
            log.warn("User with ID {} already exists in local DB. Skipping synchronization.", userDetails.getId());
            return;
        }

        RoleEntity roleEntity = roleEntityRepository.findById(roleName)
                .orElseGet(() -> roleEntityRepository.save(new RoleEntity(roleName)));

        UserEntity newUserEntity = UserEntity.builder()
                .id(userDetails.getId())
                .username(userDetails.getUsername())
                .email(userDetails.getEmail())
                .firstName(userDetails.getFirstName())
                .lastName(userDetails.getLastName())
                .enabled(userDetails.isEnabled())
                .emailVerified(userDetails.isEmailVerified())
                .createdTimestamp(userDetails.getCreatedTimestamp())
                .roles(Set.of(roleEntity)) // Associate the role
                .build();
        if (userDetails.getAttributes() != null && userDetails.getAttributes().containsKey("phoneNumber")) {
             newUserEntity.setPhoneNumber(userDetails.getAttributes().get("phoneNumber").stream().findFirst().orElse(null));
        }

        userEntityRepository.save(newUserEntity);
        log.info("Successfully synchronized new user '{}' with role '{}' to local database.", userDetails.getUsername(), roleName);
    }
    
    //this is for fetching the users initially
    @Override
    public void syncKeycloakUsers() {
        log.info("Starting optimized Keycloak user data backfill triggered by admin...");
        self.backfillCoreUserData();
        self.backfillUserRoleMappings();
        log.info("Keycloak user data backfill completed successfully.");
    }
    
    @Transactional
    public void backfillCoreUserData() {
        log.info("--- PHASE 1: Backfilling core user data ---");
        final int PAGE_SIZE = 50;
        int currentPage = 0;
        List<UserDetails> userPage;

        do {
            log.info("Fetching user page {}...", currentPage);
            int firstResult = currentPage * PAGE_SIZE;
            String token = formatAuthentication();
            userPage = keycloakAdminClient.getUsers(token, firstResult, PAGE_SIZE, false);

            List<UserEntity> userEntities = userPage.stream()
                .filter(kcUser -> !userEntityRepository.existsById(kcUser.getId()))
                .map(kcUser -> {
                    String phoneNumber = (kcUser.getAttributes() != null && kcUser.getAttributes().containsKey("phoneNumber"))
                        ? kcUser.getAttributes().get("phoneNumber").stream().findFirst().orElse(null)
                        : null;
                    
                    return UserEntity.builder()
                            .id(kcUser.getId())
                            .firstName(kcUser.getFirstName())
                            .lastName(kcUser.getLastName())
                            .username(kcUser.getUsername())
                            .createdTimestamp(kcUser.getCreatedTimestamp())
                            .email(kcUser.getEmail())
                            .emailVerified(kcUser.isEmailVerified())
                            .enabled(kcUser.isEnabled())
                            .phoneNumber(phoneNumber)
                            .build();
                })
                .toList();

            if (!userEntities.isEmpty()) {
                userEntityRepository.saveAll(userEntities);
                log.info("Saved {} new users to the database.", userEntities.size());
            }
            currentPage++;
        } while (!userPage.isEmpty());
        log.info("--- PHASE 1: Core user data backfill complete ---");
    }

    @Transactional
    public void backfillUserRoleMappings() {
        log.info("--- PHASE 2: Backfilling user role mappings ---");
        final int PAGE_SIZE = 50;

        String token = formatAuthentication();
        List<RoleRepresentation> appRoles = keycloakAdminClient.getAllRoles(token).stream()
            .filter(role -> role.getName().startsWith("ROLE_"))
            .toList();

        for (RoleRepresentation role : appRoles) {
            String roleName = role.getName();
            log.info("Processing role: {}", roleName);
            
            roleEntityRepository.findById(roleName).orElseGet(() -> roleEntityRepository.save(new RoleEntity(roleName)));

            int currentPage = 0;
            List<UserDetails> userPage;
            do {
                int firstResult = currentPage * PAGE_SIZE;
                String pageToken = formatAuthentication();
                userPage = keycloakAdminClient.getUsersInRole(pageToken, roleName, firstResult, PAGE_SIZE);

                for (UserDetails kcUser : userPage) {
                    userEntityRepository.findById(kcUser.getId()).ifPresent(userEntity -> {
                        boolean alreadyHasRole = userEntity.getRoles().stream()
                            .anyMatch(r -> r.getName().equals(roleName));
                        
                        if (!alreadyHasRole) {
                            userEntity.getRoles().add(new RoleEntity(roleName));
                            userEntityRepository.save(userEntity);
                        }
                    });
                }
                currentPage++;
            } while (!userPage.isEmpty());
        }
        log.info("--- PHASE 2: User role mappings backfill complete ---");
    }

    @Override
    public void checkUserLockoutStatus(String username) {
        try {
            final OAuth2TokenResult adminTokenResult = authenticateClient(); // Assuming you have this method in your class
            final String bearerToken = "Bearer " + adminTokenResult.getAccessToken();

            keycloakAdminClient.getUserByUsername(bearerToken, username, null)
                .stream()
                .findFirst()
                .ifPresent(user -> {
                    Map<String, Object> statusMap = keycloakAdminClient.getBruteForceStatus(
                            bearerToken,
                            user.getId()
                    );

                    if (statusMap != null && Boolean.TRUE.equals(statusMap.get("disabled"))) {
                        throw GeneratorException.badRequest(UserErrorKey.ACCOUNT_TEMPORARILY_LOCKED)
                            .message("Too many invalid login attempts. Account is temporarily locked. Try again later.")
                            .build();
                    }
                    // Boolean disabled = (Boolean) statusMap.get("disabled");
                    // Object failedLoginNotBeforeObj = statusMap.get("failedLoginNotBefore");

                    // Long failedLoginNotBefore = null;

                    // if (failedLoginNotBeforeObj instanceof Number number) {
                    //     // failedLoginNotBefore = number.longValue();
                    //     failedLoginNotBefore = number.longValue() * 1000L;
                    // }
                    // if (Boolean.TRUE.equals(disabled) && failedLoginNotBefore != null) {
                    //     long now = System.currentTimeMillis();
                    //     long remainingMs = failedLoginNotBefore - now;

                    //     if (remainingMs > 0) {
                    //         long remainingMinutes = TimeUnit.MILLISECONDS.toMinutes(remainingMs);
                    //         long remainingSeconds = (remainingMs / 1000) % 60;

                    //         String msg = String.format(
                    //             "Your account is temporarily locked. Try again in %d minutes %d seconds.",
                    //             remainingMinutes, remainingSeconds
                    //         );

                    //         throw GeneratorException.badRequest(UserErrorKey.ACCOUNT_TEMPORARILY_LOCKED)
                    //             .message(msg)
                    //             .build();
                    //     }
                    // }
                });
        } catch (GeneratorException ge) {
            throw ge;
        } catch (Exception e) {
            log.warn("Failed to check lockout status for user {}: {}", username, e.getMessage());
        }  
    }
}

package com.sample.sampleservice.feature.auth.domain.service;

import com.sample.sampleservice.feature.auth.domain.exception.UserErrorKey;
import com.sample.sampleservice.feature.auth.domain.model.ChangePassword;
import com.sample.sampleservice.feature.auth.domain.model.CreateUser;
import com.sample.sampleservice.feature.auth.domain.model.OAuth2TokenResult;
import com.sample.sampleservice.feature.auth.domain.model.UserDetails;
import com.sample.sampleservice.feature.auth.domain.model.UserRequest;
import com.sample.sampleservice.feature.auth.domain.repository.UserRepository;
import com.sample.sampleservice.shared.authentication.application.AuthenticatedUser;
import com.sample.sampleservice.shared.authentication.domain.Role;
import com.sample.sampleservice.shared.authentication.domain.User;
import com.sample.sampleservice.shared.error.domain.Assert;
import com.sample.sampleservice.shared.error.domain.GeneratorException;
import com.sample.sampleservice.shared.notification.application.NotificationApplicationService;
import com.sample.sampleservice.shared.notification.domain.model.Recipient;
import com.sample.sampleservice.shared.notification.domain.model.enums.NotificationType;
import com.sample.sampleservice.shared.openfeign.infrastructure.secondary.OpenFeignErrorDecoder;
import com.sample.sampleservice.shared.pagination.domain.Page;
import com.sample.sampleservice.shared.pagination.domain.Pageable;

import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;

import org.apache.commons.lang3.StringUtils;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Pattern;

@Slf4j
public class UserDomainService {

    private final UserRepository userRepository;
    // private final ShipperNotificationService shipperNotificationService;
    private final NotificationApplicationService notificationApplication;
    

    public UserDomainService(UserRepository userRepository, NotificationApplicationService notificationApplicationService) {
        this.userRepository = userRepository;
        this.notificationApplication = notificationApplicationService;
    }

    // public OAuth2TokenResult login(String username, String password) {
    //     var token = userRepository.login(username, password)
    //             .orElseThrow(() -> GeneratorException.badRequest(UserErrorKey.USER_NOT_FOUND).message("User not found").build());
    //     UserDetails user = null;
    //     if (ValidatorUtil.isEmail(username)) {
    //         user = userRepository.findByEmail(username);
    //     } else {
    //         user = userRepository.findByUsername(username);
    //     }

    //     if (!user.isEmailVerified()) {
    //         sendOtp(username);
    //         throw GeneratorException.badRequest(UserErrorKey.VERIFICATION_FAILED).message("Please Verify your account").build();
    //     }

    //     return token;
    // }

    public OAuth2TokenResult login(String username, String password) {
        OAuth2TokenResult token;
        String effectiveUsername = username;

        try {
            token = userRepository.login(username, password)
                .orElseThrow(() -> GeneratorException.badRequest(UserErrorKey.USER_NOT_FOUND).message("User not found").build());

        } catch (OpenFeignErrorDecoder.OpenFeignException ex) {
            if (ex.getStatus() == 400 || ex.getStatus() == 401) {
                userRepository.checkUserLockoutStatus(username);
            }
            if (ex.getStatus() == 401) {
                Optional<String> transformedUsernameOpt = transformIfLocalEthiopianNumber(username);
                if (transformedUsernameOpt.isPresent()) {
                    String transformedUsername = transformedUsernameOpt.get();
                    log.info("Initial login for '{}' failed. Retrying with transformed phone number '{}'.", username, transformedUsername);
                    try {
                        token = userRepository.login(transformedUsername, password)
                            .orElseThrow(() -> ex);        
                        effectiveUsername = transformedUsername;

                    } catch (OpenFeignErrorDecoder.OpenFeignException retryEx) {
                        if (retryEx.getStatus() == 400 || retryEx.getStatus() == 401) {
                            userRepository.checkUserLockoutStatus(transformedUsername);
                        }
                        throw retryEx;
                    }

                } else {
                    throw ex;
                }
            } else {
                throw ex;
            }
        }
        UserDetails user = null;
        if (ValidatorUtil.isEmail(effectiveUsername)) {
            user = userRepository.findByEmail(effectiveUsername);
        } else {
            user = userRepository.findByUsername(effectiveUsername);
        }
        
        if (user == null) {
            throw new IllegalStateException("Could not find user details for '" + effectiveUsername + "' after a successful login.");
        }

        if (!user.isEmailVerified()) {
            sendOtp(effectiveUsername);
            throw GeneratorException.badRequest(UserErrorKey.VERIFICATION_FAILED).message("Please Verify your account").build();
        }

        return token;
    }

    public OAuth2TokenResult refresh(String refreshToken) {
        return userRepository.refresh(refreshToken)
                .orElseThrow(() -> GeneratorException.badRequest(UserErrorKey.USER_NOT_FOUND).message("User not found").build());
    }

    public UserDetails getUserDetailById(String userId) {
        Assert.notBlank("User id", userId);

        var user = userRepository.getUserDetailById(userId)
                .orElseThrow(() -> GeneratorException.badRequest(UserErrorKey.USER_NOT_FOUND).message("User not found").build());
        var roles = userRepository.myRoles(userId);
        user.setRealmRoles(roles);
        return user;
    }

    public UserDetails getUserDetail() {
        User user = AuthenticatedUser.getUser();

        return userRepository.getUserDetailById(user.id())
                .orElseThrow(() -> GeneratorException.badRequest(UserErrorKey.USER_NOT_FOUND).message("User not found").build());
    }

    public UserDetails createUser(CreateUser createUser, String role, boolean verified) {
        return userRepository.createUser(createUser, role, verified);
    }

    public UserDetails findByUserName(String userName) {
        return userRepository.findByUsername(userName);
    }

    public UserDetails findByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    public void resetPassword(ChangePassword changePassword) {
        userRepository.resetPassword(changePassword);
    }

    public void forgotPassword(String email) {
        userRepository.forgotPassword(email);
    }

    public void updatePassword(String username, ChangePassword changePassword) {
        userRepository.updatePassword(username, changePassword);
    }

    public void sendVerify(String username, String password) {
        userRepository.sendVerify(username, password);
    }

    private void sendOtp(String username) {
        // notificationApplication.sendOtp(username);
    }

    public Page<UserDetails> getUsers(String role, Boolean emailVerified, Boolean enabled, Boolean exact, String search, Pageable pageable) {
        return userRepository.findAll(role, emailVerified, enabled, exact, search, pageable);
    }

    public UserDetails update(String userId, UserRequest request) {
        validatePhoneNumberIsPresent(request);
        var user = userRepository.update(userId, request);
        var roles = userRepository.myRoles(userId);

        notificationApplication.notify(NotificationType.PUSH, "change_status", List.of(
                new Recipient(roles.stream()
                        .map(StringUtils::toRootLowerCase)
                        .map(role -> role.replace("role_", ""))
                        .findAny()
                        .orElse(""), user.getId())), Map.ofEntries(
                Map.entry("UserName", user.getUsername())
        ));
        user.setRealmRoles(roles);
        return user;
    }

    public UserDetails updateUserFromDomainSource(String userId, UserRequest request) {
        return userRepository.updateUserFromDomainSource(userId, request);
    }

    public UserDetails enableUser(String userId) {
        var user = userRepository.enableUser(userId);
        var roles = userRepository.myRoles(userId);

        notificationApplication.notify(NotificationType.PUSH, "change_status", List.of(
                new Recipient(roles.stream()
                        .map(StringUtils::toRootLowerCase)
                        .map(role -> role.replace("role_", ""))
                        .findAny()
                        .orElse(""), user.getId())), Map.ofEntries(
                Map.entry("UserName", user.getUsername()),
                Map.entry("status", "enabled")
        ));

        user.setRealmRoles(roles);
        return user;
    }

    public UserDetails disableUser(String userId) {
        var user = userRepository.disableUser(userId);
        var roles = userRepository.myRoles(userId);

        notificationApplication.notify(NotificationType.PUSH, "change_status", List.of(
                new Recipient(roles.stream()
                        .map(StringUtils::toRootLowerCase)
                        .map(role -> role.replace("role_", ""))
                        .findAny()
                        .orElse(""), user.getId())), Map.ofEntries(
                Map.entry("UserName", user.getUsername()),
                Map.entry("status", "disabled")
        ));

        user.setRealmRoles(roles);
        return user;
    }

    public UserDetails disableSelfUser(String userId) {
        var user = userRepository.disableUser(userId);
        var roles = userRepository.myRoles(userId);

        user.setRealmRoles(roles);
        return user;
    }

    public void syncKeycloakUsers() {
        AuthenticatedUser.can(Role.ADMIN);
        userRepository.syncKeycloakUsers();
    }

    private void validatePhoneNumberIsPresent(UserRequest request) {
        log.debug("Validating that phone number is present in user update request.");
        
        Object phoneAttribute = (request.getAttributes() != null) 
            ? request.getAttributes().get("phoneNumber") 
            : null;

        if (phoneAttribute == null) {
            throw GeneratorException.badRequest(UserErrorKey.PHONE_NUMBER_REQUIRED)
                .message("Phone number is required and cannot be missing.")
                .build();
        }

        if (!(phoneAttribute instanceof List<?> phoneList)) {
            throw GeneratorException.badRequest(UserErrorKey.PHONE_NUMBER_REQUIRED)
                .message("Phone number attribute has an invalid format; expected a list of strings.")
                .build();
        }

        if (phoneList.isEmpty() || StringUtils.isBlank((String) phoneList.get(0))) {
            throw GeneratorException.badRequest(UserErrorKey.PHONE_NUMBER_REQUIRED)
                .message("Phone number is required and cannot be empty.")
                .build();
        }
        log.debug("Phone number validation passed.");
    }

    private Optional<String> transformIfLocalEthiopianNumber(String username) {
        if (username == null) {
            return Optional.empty();
        }
        
        // This regex matches a 10-digit number starting with either "09" or "07".
        if (username.matches("^0(9|7)\\d{8}$")) {
            return Optional.of("+251" + username.substring(1));
        }
        
        return Optional.empty();
    }

    // TODO: remove redundant code
    private static class ValidatorUtil {

        private static final Pattern EMAIL_PATTERN = Pattern.compile(
                "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$"
        );

        private static final Pattern PHONE_PATTERN = Pattern.compile(
                "^\\+?[0-9]{7,15}$" // Accepts optional + and 7–15 digits
        );

        public static boolean isEmail(String input) {
            return EMAIL_PATTERN.matcher(input).matches();
        }

        public static boolean isPhoneNumber(String input) {
            return PHONE_PATTERN.matcher(input).matches();
        }
    }
}

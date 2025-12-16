package com.sample.sampleservice.feature.auth.domain.service;

import com.sample.sampleservice.feature.auth.domain.exception.UserErrorKey;
import com.sample.sampleservice.feature.auth.domain.model.*;
import com.sample.sampleservice.feature.auth.domain.repository.UserRepository;
import com.sample.sampleservice.shared.authentication.application.AuthenticatedUser;
import com.sample.sampleservice.shared.authentication.domain.User;
import com.sample.sampleservice.shared.error.domain.Assert;
import com.sample.sampleservice.shared.error.domain.GeneratorException;
import com.sample.sampleservice.shared.notification.application.NotificationApplicationService;
import com.sample.sampleservice.shared.notification.domain.model.Recipient;
import com.sample.sampleservice.shared.notification.domain.model.enums.NotificationType;
import com.sample.sampleservice.shared.pagination.domain.Page;
import com.sample.sampleservice.shared.pagination.domain.Pageable;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.util.List;
import java.util.Map;

@Slf4j
public class UserDomainService {

    private final UserRepository userRepository;
    private final NotificationApplicationService notificationApplication;

    public UserDomainService(UserRepository userRepository, NotificationApplicationService notificationApplicationService) {
        this.userRepository = userRepository;
        this.notificationApplication = notificationApplicationService;
    }

    public OAuth2TokenResult login(String username, String password) {
       return userRepository.login(username, password)
                .orElseThrow(() -> GeneratorException.badRequest(UserErrorKey.USER_NOT_FOUND).message("User not found").build());
    }

    public OAuth2TokenResult refresh(String refreshToken) {
        return userRepository.refresh(refreshToken)
                .orElseThrow(() -> GeneratorException.badRequest(UserErrorKey.USER_NOT_FOUND).message("User not found").build());
    }

    public UserDetails getUserDetailById(String userId) {
        Assert.notBlank("User id", userId);

        var user = userRepository.getUserDetailById(userId)
                .orElseThrow(() -> GeneratorException.badRequest(UserErrorKey.USER_NOT_FOUND).message("User not found").build());
        
        // In the new Repo, roles are likely already mapped, but we ensure consistency here
        var roles = userRepository.myRoles(userId);
        user.setRoles(roles);
        return user;
    }

    public UserDetails getUserDetail() {
        User user = AuthenticatedUser.getUser();
        return getUserDetailById(user.id());
    }

    public UserDetails createUser(CreateUser createUser, String role, boolean verified) {
        return userRepository.createUser(createUser, role, verified);
    }

    public UserDetails findByUserName(String username) {
        return userRepository.findByUsername(username);
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

    public Page<UserDetails> getUsers(String role, Boolean emailVerified, Boolean enabled, Boolean exact, String search, Pageable pageable) {
        return userRepository.findAll(role, emailVerified, enabled, exact, search, pageable);
    }

    public UserDetails update(String userId, UserRequest request) {
        var user = userRepository.update(userId, request);
        
        notifyUserStatusChange(user, "change_status");
        
        return user;
    }

    public UserDetails updateUserFromDomainSource(String userId, UserRequest request) {
        return userRepository.update(userId, request);
    }

    public UserDetails enableUser(String userId) {
        var user = userRepository.enableUser(userId);
        notifyUserStatusChange(user, "enabled");
        return user;
    }

    public UserDetails disableUser(String userId) {
        var user = userRepository.disableUser(userId);
        notifyUserStatusChange(user, "disabled");
        return user;
    }

    public UserDetails disableSelfUser(String userId) {
        var user = userRepository.disableUser(userId);
        var roles = userRepository.myRoles(userId);
        user.setRoles(roles);
        return user;
    }

    private void notifyUserStatusChange(UserDetails user, String statusKey) {
        var roles = userRepository.myRoles(user.getId());
        
        // Helper to format roles for notification recipient
        String recipientRole = roles.stream()
                .map(StringUtils::toRootLowerCase)
                .map(role -> role.replace("role_", ""))
                .findAny()
                .orElse("");

        notificationApplication.notify(NotificationType.PUSH, "change_status", 
                List.of(new Recipient(recipientRole, user.getId())), 
                Map.ofEntries(
                    Map.entry("UserName", user.getUsername()),
                    Map.entry("status", statusKey)
                ));
        
        user.setRoles(roles);
    }
}
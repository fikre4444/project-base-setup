package com.sample.sampleservice.feature.auth.application.impl;

import com.sample.sampleservice.feature.auth.application.UserApplicationService;
import com.sample.sampleservice.feature.auth.domain.model.*;
import com.sample.sampleservice.feature.auth.domain.repository.UserRepository;
import com.sample.sampleservice.feature.auth.domain.service.UserDomainService;
import com.sample.sampleservice.shared.notification.application.NotificationApplicationService;
import com.sample.sampleservice.shared.pagination.domain.Page;
import com.sample.sampleservice.shared.pagination.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
public class UserApplicationServiceImpl implements UserApplicationService {

    private final UserDomainService userDomainService;

    public UserApplicationServiceImpl(UserRepository userRepository, NotificationApplicationService notificationApplicationService) {
        this.userDomainService = new UserDomainService(userRepository, notificationApplicationService);
    }

    @Override
    public OAuth2TokenResult login(String username, String password) {
        return userDomainService.login(username, password);
    }

    @Override
    public OAuth2TokenResult refresh(String refreshToken) {
        return userDomainService.refresh(refreshToken);
    }

    @Override
    public UserDetails getUserDetailById(String userId) {
        return userDomainService.getUserDetailById(userId);
    }

    @Override
    public void forgotPassword(String email) {
        userDomainService.forgotPassword(email);
    }

    @Override
    public UserDetails getUserDetail() {
        return userDomainService.getUserDetail();
    }

    @Override
    public UserDetails createUser(CreateUser createUser, String role, boolean verified) {
        return userDomainService.createUser(createUser, role, verified);
    }

    @Override
    public void changePassword(ChangePassword changePassword) {
        userDomainService.resetPassword(changePassword);
    }

    @Override
    public void updatePassword(String username, ChangePassword changePassword) {
        userDomainService.updatePassword(username, changePassword);
    }

    @Override
    public UserDetails verifyUser(String identifier, String code) {
        return userDomainService.verifyUser(identifier, code);
    }


    @Override
    public void sendVerify(String username, String password) {
        userDomainService.sendVerify(username, password);
    }

    @Override
    public UserDetails findByUserName(String username) {
        return userDomainService.findByUserName(username);
    }

    @Override
    public UserDetails findByEmail(String email) {
        return userDomainService.findByEmail(email);
    }

    @Override
    public UserDetails update(String userId, UserRequest request) {
        return userDomainService.update(userId, request);
    }

    @Override
    public UserDetails updateUserFromDomainSource(String userId, UserRequest request) {
        return userDomainService.updateUserFromDomainSource(userId, request);
    }

    @Override
    public Page<UserDetails> getUsers(String role, Boolean emailVerified, Boolean enabled, Boolean exact, String search, Pageable pageable) {
        return userDomainService.getUsers(role, emailVerified, enabled, exact, search, pageable);
    }

    @Override
    public UserDetails enableUser(String id) {
        return userDomainService.enableUser(id);
    }

    @Override
    public UserDetails disableUser(String id) {
        return userDomainService.disableUser(id);
    }

    @Override
    public UserDetails disableSelfUser(String userId) {
        return userDomainService.disableSelfUser(userId);
    }
}
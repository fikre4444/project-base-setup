package com.sample.sampleservice.feature.auth.application;

import com.sample.sampleservice.feature.auth.domain.model.*;
import com.sample.sampleservice.shared.pagination.domain.Page;
import com.sample.sampleservice.shared.pagination.domain.Pageable;

public interface UserApplicationService {
    OAuth2TokenResult login(String username, String password);
    OAuth2TokenResult refresh(String refreshToken);
    UserDetails getUserDetailById(String userId);
    void forgotPassword(String email);
    UserDetails getUserDetail();
    UserDetails createUser(CreateUser createUser, String role, boolean verified);
    void changePassword(ChangePassword changePassword);
    void updatePassword(String username, ChangePassword changePassword);
    void sendVerify(String username, String password);
    UserDetails findByUserName(String username);
    UserDetails findByEmail(String email);
    UserDetails update(String userId, UserRequest request);
    UserDetails updateUserFromDomainSource(String userId, UserRequest request);
    Page<UserDetails> getUsers(String role, Boolean emailVerified, Boolean enabled, Boolean exact, String search, Pageable pageable);
    UserDetails enableUser(String id);
    UserDetails disableUser(String id);
    UserDetails disableSelfUser(String userId);

    UserDetails verifyUser(String identifier, String code);
}
package com.sample.sampleservice.feature.auth.application;

import com.sample.sampleservice.feature.auth.domain.model.ChangePassword;
import com.sample.sampleservice.feature.auth.domain.model.CreateUser;
import com.sample.sampleservice.feature.auth.domain.model.OAuth2TokenResult;
import com.sample.sampleservice.feature.auth.domain.model.UserDetails;
import com.sample.sampleservice.feature.auth.domain.model.UserRequest;
import com.sample.sampleservice.shared.pagination.domain.Page;
import com.sample.sampleservice.shared.pagination.domain.Pageable;

public interface UserApplicationService {

    OAuth2TokenResult login(String username, String password);

    OAuth2TokenResult refresh(String refreshToken);

    UserDetails getUserDetailById(String userId);

    UserDetails findByUserName(String userName);

    UserDetails findByEmail(String email);

    void forgotPassword(String email);

    UserDetails getUserDetail();

    UserDetails createUser(CreateUser createUser, String role, boolean verified);

    void changePassword(ChangePassword changePassword);

    void updatePassword(String username, ChangePassword changePassword);

    void sendVerify(String username, String password);

    Page<UserDetails> getUsers(String role, Boolean emailVerified, Boolean enabled, Boolean exact, String search, Pageable pageable);

    UserDetails update(String userId, UserRequest request);

    UserDetails updateUserFromDomainSource(String userId, UserRequest request); //for syncing when domain entities are updated

    UserDetails enableUser(String id);

    UserDetails disableUser(String id);

    UserDetails disableSelfUser(String userId);

    void syncKeycloakUsers();

}

package com.sample.sampleservice.feature.auth.domain.repository;

import com.sample.sampleservice.feature.auth.domain.model.ChangePassword;
import com.sample.sampleservice.feature.auth.domain.model.CreateUser;
import com.sample.sampleservice.feature.auth.domain.model.OAuth2TokenResult;
import com.sample.sampleservice.feature.auth.domain.model.UserDetails;
import com.sample.sampleservice.feature.auth.domain.model.UserRequest;
import com.sample.sampleservice.shared.pagination.domain.Page;
import com.sample.sampleservice.shared.pagination.domain.Pageable;

import java.util.List;
import java.util.Optional;

public interface UserRepository {

    UserDetails findByUsername(String username);

    UserDetails findByEmail(String email);

    Optional<OAuth2TokenResult> login(String username, String password);

    UserDetails createUser(CreateUser createUser, String role, boolean verified);

    void updatePassword(String username, ChangePassword changePassword);
    
    void resetPassword(ChangePassword changePassword);

    void forgotPassword(String email);

    boolean verifyOtp(String identifier, String code);

    void sendVerify(String username, String password);

    Optional<OAuth2TokenResult> refresh(String refreshToken);

    Optional<UserDetails> getUserDetailById(String userId);

    Page<UserDetails> findAll(String role, Boolean emailVerified, Boolean enabled, Boolean exact, String search, Pageable pageable);
    
    UserDetails update(final String userId, final UserRequest request);
    
    List<String> myRoles(String userId);

    UserDetails enableUser(String userId);

    UserDetails disableUser(String userId);

    UserDetails setEmailVerified(UserDetails user);

}

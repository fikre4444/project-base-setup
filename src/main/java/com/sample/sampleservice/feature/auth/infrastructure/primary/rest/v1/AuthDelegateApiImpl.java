package com.sample.sampleservice.feature.auth.infrastructure.primary.rest.v1;

import com.google.common.base.CaseFormat;
import com.sample.sampleservice.feature.auth.api.rest.v1.AuthsApiDelegate;
import com.sample.sampleservice.feature.auth.api.rest.v1.model.RegisterUserRequest;
import com.sample.sampleservice.feature.auth.api.rest.v1.model.ResetPasswordRequest;
import com.sample.sampleservice.feature.auth.api.rest.v1.model.Token;
import com.sample.sampleservice.feature.auth.api.rest.v1.model.UserDetail;
import com.sample.sampleservice.feature.auth.api.rest.v1.model.UserDetailPaginated;
import com.sample.sampleservice.feature.auth.api.rest.v1.model.UserRequest;
import com.sample.sampleservice.feature.auth.application.UserApplicationService;
import com.sample.sampleservice.feature.auth.domain.exception.UserErrorKey;
import com.sample.sampleservice.feature.auth.domain.model.ChangePassword;
import com.sample.sampleservice.feature.auth.domain.model.CreateUser;
import com.sample.sampleservice.feature.auth.domain.model.UserDetails;
import com.sample.sampleservice.feature.auth.infrastructure.primary.mapper.ChangePasswordModelMapper;
import com.sample.sampleservice.feature.auth.infrastructure.primary.mapper.RegisterUserModelMapper;
import com.sample.sampleservice.feature.auth.infrastructure.primary.mapper.TokenModelMapper;
import com.sample.sampleservice.feature.auth.infrastructure.primary.mapper.UserDetailModelMapper;
import com.sample.sampleservice.feature.auth.infrastructure.primary.mapper.UserRequestModelMapper;
import com.sample.sampleservice.shared.authentication.application.AuthenticatedUser;
import com.sample.sampleservice.shared.authentication.domain.Role;
import com.sample.sampleservice.shared.error.domain.GeneratorException;
import com.sample.sampleservice.shared.pagination.domain.Page;
import com.sample.sampleservice.shared.pagination.domain.Pageable;

import lombok.RequiredArgsConstructor;
import java.util.Optional;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class AuthDelegateApiImpl implements AuthsApiDelegate {

    private final ChangePasswordModelMapper changePasswordModelMapper;
    private final UserApplicationService userApplicationService;
    private final UserDetailModelMapper userDetailModelMapper;
    private final TokenModelMapper tokenModelMapper;
    private final UserRequestModelMapper userRequestModelMapper;
    private final RegisterUserModelMapper registerUserModelMapper;

    @Override
    public ResponseEntity<Void> resetPassword(ResetPasswordRequest changePassword) {
        AuthenticatedUser.can(Role.ADMIN);
        userApplicationService.changePassword(changePasswordModelMapper.toBo(changePassword));
        return new ResponseEntity<>(HttpStatus.ACCEPTED);
    }

    @Override
    public ResponseEntity<Void> sendVerify(String username, String password) {
        userApplicationService.sendVerify(username, password);
        return new ResponseEntity<>(HttpStatus.ACCEPTED);
    }

    @Override
    public ResponseEntity<Void> updatePassword(String username, String password, String newPassword, String confirmPassword) {
        userApplicationService.updatePassword(username, new ChangePassword(password, newPassword, confirmPassword));
        return new ResponseEntity<>(HttpStatus.ACCEPTED);
    }

    @Override
    public ResponseEntity<UserDetail> currentUser() {
        return ResponseEntity.ok(userDetailModelMapper.toDto(userApplicationService.getUserDetail()));
    }

    @Override
    public ResponseEntity<Void> forgotPassword(String email) {
        userApplicationService.forgotPassword(email);
        return new ResponseEntity<>(HttpStatus.ACCEPTED);
    }

    @Override
    public ResponseEntity<Token> login(String username, String password) {
        var token = tokenModelMapper.toDto(userApplicationService.login(username, password));
        // ResponseCookie refreshCookie = ResponseCookie.from("refresh_token", token.getRefreshToken())
        // .httpOnly(true)
        // .secure(true)
        // .path("/api/v1/auth/refresh")   // Only sent to refresh endpoint
        // .sameSite("Strict")             // Best for security (or Lax if mobile app)
        // .build();
        // ResponseCookie accessToken = ResponseCookie.from("access_token", token.getAccessToken())
        // .httpOnly(true)
        // .secure(true)
        // .path("/")
        // .sameSite("Strict")
        // .build();               
        // return ResponseEntity.ok(token);

        ResponseCookie refreshCookie = ResponseCookie.from("refresh_token", token.getRefreshToken())
        .httpOnly(false)    // <--- ALLOWS JS TO READ IT (Debug only)
        .secure(false)      // <--- ALLOWS HTTP
        .path("/")          // <--- GLOBAL PATH (easier to find)
        .maxAge(24 * 60 * 60)
        .build();           // <--- NO SameSite defined (uses Browser Default)

    // 2. Access Token - Global Path, No Security
    ResponseCookie accessToken = ResponseCookie.from("access_token", token.getAccessToken())
        .httpOnly(false)    // <--- ALLOWS JS TO READ IT (Debug only)
        .secure(false)      // <--- ALLOWS HTTP
        .path("/")
        .maxAge(24 * 60 * 60)
        .build();
        return ResponseEntity.ok()
        .header(HttpHeaders.SET_COOKIE, refreshCookie.toString())
        .header(HttpHeaders.SET_COOKIE, accessToken.toString())
        .body(token);
    }

    // @Override
    // public ResponseEntity<SendOtpResponse> sendOtp(String identifier) {
    //     String phoneNumber = userApplicationService.sendOtp(identifier);
    //     String maskedPhoneNumber = phoneNumber.replaceAll(".(?=.{4})", "*");
    //     SendOtpResponse response = new SendOtpResponse();
    //     response.setPhoneNumber(maskedPhoneNumber);
    //     return ResponseEntity.ok(response);
    // }

    @Override
    public ResponseEntity<UserDetail> verifyUser(String identifier,
        String code) {
        var userDetails = userApplicationService.verifyUser(identifier, code);
        return ResponseEntity.ok(userDetailModelMapper.toDto(userDetails));
    }

    @Override
    public ResponseEntity<UserDetail> register(RegisterUserRequest registerUserRequest) {
        CreateUser createUserBo = registerUserModelMapper.toBo(registerUserRequest);

        var createdUser = userApplicationService.createUser(
            createUserBo, 
            Role.USER.roleName(),
            false
        );

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(userDetailModelMapper.toDto(createdUser));
    }

    @Override
    public ResponseEntity<Token> refresh(String refreshToken) {
        return ResponseEntity.ok(tokenModelMapper.toDto(userApplicationService.refresh(refreshToken)));
    }

    // public ResponseEntity<UserDetailPaginated> getAllUsers(String role, Optional<Boolean> emailVerified, Optional<Boolean> enabled, Optional<Boolean> exact, Optional<Integer> limit, Optional<Integer> page, Optional<String> sortBy, Optional<String> sortDir, Optional<String> searchTerm) {
    //     Page<UserDetails> users = userApplicationService.getUsers(role, emailVerified.orElse(null), enabled.orElse(null), exact.orElse(null), searchTerm.orElse(null), Pageable.builder().page(page.orElse(1)).pageSize(limit.orElse(20)).build());

    //     return ResponseEntity.ok(new UserDetailPaginated().contents(userDetailModelMapper.toDto(users.getContent())).isLast(users.isLast()).hasNext(users.isHasNext()).hasPrevious(users.isHasPrevious()).currentPage(users.getCurrentPage()).total(users.getTotal()).totalPages((long) users.getTotalPages()));
    // }

    public ResponseEntity<UserDetailPaginated> getAllUsers(String role, Optional<Boolean> emailVerified, Optional<Boolean> enabled, Optional<Boolean> exact, Optional<Integer> limit, Optional<Integer> page, Optional<String> sortBy, Optional<String> sortDir, Optional<String> searchTerm) {
        AuthenticatedUser.can(Role.ADMIN);
        String sortField = sortBy.map(s -> CaseFormat.LOWER_UNDERSCORE.to(CaseFormat.LOWER_CAMEL, s))
                                 .orElse("createdTimestamp");
        Pageable customPageable = new Pageable(
            page.orElse(1),
            limit.orElse(20),
            sortField,
            sortDir.map(dir -> "asc".equalsIgnoreCase(dir) ? Pageable.Direction.ASC : Pageable.Direction.DESC)
                   .orElse(Pageable.Direction.DESC)
        );

        Page<UserDetails> users = userApplicationService.getUsers(
            role, 
            emailVerified.orElse(null), 
            enabled.orElse(null), 
            exact.orElse(null),
            searchTerm.orElse(null), 
            customPageable
        );
        return ResponseEntity.ok(new UserDetailPaginated()
                .contents(userDetailModelMapper.toDto(users.getContent()))
                .isLast(users.isLast())
                .hasNext(users.isHasNext())
                .hasPrevious(users.isHasPrevious())
                .currentPage(users.getCurrentPage()) // Casting long to int
                .total((long) users.getTotal())
                .totalPages((long) users.getTotalPages()));
    }
    
    public ResponseEntity<UserDetail> updateUser(String id, UserRequest userRequest) {
        AuthenticatedUser.can(Role.ADMIN);
        return ResponseEntity.ok(userDetailModelMapper.toDto(userApplicationService.update(id, userRequestModelMapper.toBo(userRequest))));
    }

    @Override
    public ResponseEntity<UserDetail> getUserById(String id) {
        AuthenticatedUser.can(Role.ADMIN);
        return ResponseEntity.ok(userDetailModelMapper.toDto(userApplicationService.getUserDetailById(id)));
    }

    @Override
    public ResponseEntity<UserDetail> enableUserAvailability(String id, String command) {
        AuthenticatedUser.can(Role.ADMIN);
        if ("enable".equalsIgnoreCase(command)) {
            return ResponseEntity.ok(userDetailModelMapper.toDto(this.userApplicationService.enableUser(id)));
        } else if ("disable".equalsIgnoreCase(command)) {
            return ResponseEntity.ok(userDetailModelMapper.toDto(this.userApplicationService.disableUser(id)));
        }

        throw GeneratorException.badRequest(UserErrorKey.COMMAND).message("Command not found").build();

    }

    @Override
    public ResponseEntity<Void> disableSelfUser() {
        String userId = AuthenticatedUser.getUser().id();
        userApplicationService.disableSelfUser(userId);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @Override
    public ResponseEntity<String> healthCheck() {
        return ResponseEntity.ok("UP");
    }
}

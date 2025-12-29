package com.sample.sampleservice.feature.auth.domain.exception;

import com.sample.sampleservice.shared.error.domain.ErrorKey;

public enum UserErrorKey implements ErrorKey {

    UPDATE_PASSWORD("update.password"),
    CONFIRM_PASSWORD_MISMATCH("confirm.password.mismatch"),
    VERIFY_EMAIL("verify.email"),
    USER_NOT_FOUND("user.not.found"),
    USER_INSUFFICIENT_ROLE("user.insufficient.role"),
    EMAIL_EXISTS("user.email.exists"),
    VERIFICATION_FAILED("verification.failed"),
    COMMAND("command"),
    PHONE_NUMBER_REQUIRED("phone.number.required"),
    COMPLEXITY_INVALID("password.complexity.invalid"),
    TOKEN_INACTIVE("TOKEN_INACTIVE"),
    ACCOUNT_TEMPORARILY_LOCKED("account.temporarily.locked"),
    BAD_CREDENTIALS("bad.credentials"),
    USER_LOCKED("user.locked"),
    ROLE_NOT_FOUND("Role.not.found"),
    EMAIL_ALREADY_EXISTS("Email.already.exists"),
    USER_ALREADY_EXISTS("User.already.exists"),
    INVALID_OTP("invalid.otp");

    private final String key;

    UserErrorKey(String key) {
        this.key = key;
    }

    @Override
    public String get() {
        return key;
    }
}

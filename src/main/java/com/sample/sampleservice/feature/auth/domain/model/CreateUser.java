package com.sample.sampleservice.feature.auth.domain.model;

import com.sample.sampleservice.feature.auth.domain.exception.UserErrorKey;
import com.sample.sampleservice.shared.error.domain.Assert;
import com.sample.sampleservice.shared.error.domain.GeneratorException;
import org.apache.commons.lang3.StringUtils;

public record CreateUser(String password, String confirmPassword, String username, String firstName, String lastName, String email, String phoneNumber) {

    public CreateUser {
        Assert.field("Password", password)
                .minLength(8)
                .notBlank();
        Assert.field("Confirm password", confirmPassword)
                .minLength(8)
                .notBlank();
        Assert.notBlank("First name", firstName);
        Assert.notBlank("Last name", lastName);
//        Assert.notBlank("email", email);
        // Assert.notBlank("Phone number", phoneNumber);

        if (!StringUtils.equals(password, confirmPassword)) {
            throw GeneratorException.badRequest(UserErrorKey.CONFIRM_PASSWORD_MISMATCH).message("password and confirm password are not equal.").build();
        }
    }
}

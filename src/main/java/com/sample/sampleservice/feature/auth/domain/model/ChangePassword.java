package com.sample.sampleservice.feature.auth.domain.model;

import com.sample.sampleservice.feature.auth.domain.exception.UserErrorKey;
import com.sample.sampleservice.shared.error.domain.Assert;
import com.sample.sampleservice.shared.error.domain.GeneratorException;
import org.apache.commons.lang3.StringUtils;

public record ChangePassword(String oldPassword, String newPassword, String confirmPassword) {

    public ChangePassword {
        String passwordComplexityRegex = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&#^]).+$";
        Assert.field("Old Password", oldPassword)
                .minLength(8)
                .notBlank();
        Assert.field("New Password", newPassword)
                .minLength(8)
                .notBlank();
        if(!newPassword.matches(passwordComplexityRegex)){
            String complexityMessage = String.join(" & ",
                "At least 8 characters long",
                "At least 1 uppercase and 1 lowercase letter",
                "At least 1 special character (aq#$%^&*)"
            );
            throw GeneratorException.badRequest(UserErrorKey.COMPLEXITY_INVALID).message(complexityMessage).build();
        }
        Assert.field("Confirm password", confirmPassword)
                .minLength(8)
                .notBlank();

        if (!StringUtils.equals(newPassword, confirmPassword)) {
            throw GeneratorException.badRequest(UserErrorKey.CONFIRM_PASSWORD_MISMATCH).message("password and confirm password are not equal.").build();
        }
    }
}

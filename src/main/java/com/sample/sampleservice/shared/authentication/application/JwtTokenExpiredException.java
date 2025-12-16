package com.sample.sampleservice.shared.authentication.application;

import org.springframework.security.core.AuthenticationException;

public class JwtTokenExpiredException extends AuthenticationException {
    public JwtTokenExpiredException(String msg) {
        super(msg);
    }
}

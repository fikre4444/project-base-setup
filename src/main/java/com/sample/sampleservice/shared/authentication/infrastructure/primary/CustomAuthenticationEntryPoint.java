package com.sample.sampleservice.shared.authentication.infrastructure.primary;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sample.sampleservice.shared.authentication.application.JwtTokenExpiredException;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.oauth2.server.resource.web.BearerTokenAuthenticationEntryPoint;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class CustomAuthenticationEntryPoint implements AuthenticationEntryPoint {

    private final ObjectMapper objectMapper;

    private final AuthenticationEntryPoint delegate = new BearerTokenAuthenticationEntryPoint();

    @Override
    public void commence(
        HttpServletRequest request,
        HttpServletResponse response,
        AuthenticationException authException
    ) throws IOException, ServletException {

        if (authException instanceof JwtTokenExpiredException) {
            response.setStatus(HttpStatus.UNAUTHORIZED.value());
            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
            Map<String, String> errorDetails = Map.of(
                "code", "TOKEN_EXPIRED",
                "message", "Access token expired"
            );
            response.getWriter().write(objectMapper.writeValueAsString(errorDetails));

        } else {
            this.delegate.commence(request, response, authException);
        }
    }
}
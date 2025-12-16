package com.sample.sampleservice.shared.authentication.infrastructure.primary;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.converter.Converter;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.annotation.web.configurers.HeadersConfigurer.FrameOptionsConfig;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.mapping.GrantedAuthoritiesMapper;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.core.DelegatingOAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2TokenValidator;
import org.springframework.security.oauth2.core.oidc.user.OidcUserAuthority;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.*;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.csrf.CsrfFilter;
import org.springframework.security.web.header.writers.ReferrerPolicyHeaderWriter;
import org.springframework.security.web.servlet.util.matcher.MvcRequestMatcher;
import org.springframework.web.filter.CorsFilter;
import org.springframework.web.servlet.handler.HandlerMappingIntrospector;

import com.sample.sampleservice.shared.authentication.domain.Role;

import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;

import java.util.HashSet;
import java.util.Set;

import javax.crypto.SecretKey;

import static org.springframework.security.web.util.matcher.AntPathRequestMatcher.antMatcher;

@Slf4j
@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
@EnableMethodSecurity(securedEnabled = true)
class SecurityConfiguration {

    private final CorsFilter corsFilter;
    private final HandlerMappingIntrospector introspector;
    private final ApplicationSecurityProperties applicationSecurityProperties;
    private final CustomAuthenticationEntryPoint customAuthenticationEntryPoint; 
    private final UserEnabledTokenValidator userEnabledTokenValidator;

    @Value("${jwt.secret}")
    private String jwtSecret;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        // @formatter:off
    return http
      .csrf(AbstractHttpConfigurer::disable)
      .addFilterBefore(corsFilter, CsrfFilter.class)
      .headers(headers -> headers
        .contentSecurityPolicy(csp -> csp.policyDirectives(applicationSecurityProperties.getContentSecurityPolicy()))
        .frameOptions(FrameOptionsConfig::sameOrigin)
        .referrerPolicy(referrer -> referrer.policy(ReferrerPolicyHeaderWriter.ReferrerPolicy.STRICT_ORIGIN_WHEN_CROSS_ORIGIN))
        .permissionsPolicy(permissions ->
          permissions.policy("camera=(), fullscreen=(self), geolocation=(), gyroscope=(), magnetometer=(), microphone=(), midi=(), payment=(), sync-xhr=()"))
      )
      .authorizeHttpRequests(authz -> authz
        .requestMatchers(antMatcher("/api/v1/health-check")).permitAll() 
        .requestMatchers(antMatcher(HttpMethod.OPTIONS, "/**")).permitAll()
        .requestMatchers(antMatcher("/api/v1/auth/**")).permitAll()
        // .requestMatchers(antMatcher("/api/v1/sample/**")).permitAll()
          .requestMatchers(antMatcher("/api/v1/**")).authenticated()
        .requestMatchers(antMatcher("/app/**")).permitAll()
        .requestMatchers(antMatcher("/i18n/**")).permitAll()
        .requestMatchers(antMatcher("/content/**")).permitAll()
        .requestMatchers(antMatcher("/swagger-ui/**")).permitAll()
        .requestMatchers(antMatcher("/swagger-ui.html")).permitAll()
        .requestMatchers(antMatcher("/v3/api-docs/**")).permitAll()
        .requestMatchers(new MvcRequestMatcher(introspector, "/api/authenticate")).permitAll()
        .requestMatchers(new MvcRequestMatcher(introspector, "/api/auth-info")).permitAll()
        .requestMatchers(new MvcRequestMatcher(introspector, "/management/**")).hasAuthority(Role.ADMIN.key())
        .anyRequest().authenticated()
      )
      .oauth2ResourceServer(oauth2 -> oauth2
        .jwt(jwt -> jwt.jwtAuthenticationConverter(authenticationConverter()))
        .authenticationEntryPoint(customAuthenticationEntryPoint)
      )
      .build();
    }

    private Converter<Jwt, AbstractAuthenticationToken> authenticationConverter() {
        JwtAuthenticationConverter jwtAuthenticationConverter = new JwtAuthenticationConverter();
        jwtAuthenticationConverter.setJwtGrantedAuthoritiesConverter(new JwtGrantedAuthorityConverter());

        return jwtAuthenticationConverter;
    }

    @Bean
    public JwtDecoder jwtDecoder() {
        SecretKey key = Keys.hmacShaKeyFor(Decoders.BASE64.decode(jwtSecret));
        NimbusJwtDecoder jwtDecoder = NimbusJwtDecoder.withSecretKey(key).macAlgorithm(MacAlgorithm.HS256).build();

        OAuth2TokenValidator<Jwt> withTimestamp = new JwtTimestampValidator();
        //note the userEnabled token validator checks for the user for every endpoint request (either use caching or something to increase performance)
        OAuth2TokenValidator<Jwt> combinedValidator = new DelegatingOAuth2TokenValidator<>(
                withTimestamp, 
                userEnabledTokenValidator
        );
        jwtDecoder.setJwtValidator(combinedValidator);

        return jwtDecoder;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}

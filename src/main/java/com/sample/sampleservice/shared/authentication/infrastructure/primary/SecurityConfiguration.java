package com.sample.sampleservice.shared.authentication.infrastructure.primary;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
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
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.core.DelegatingOAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2TokenValidator;
import org.springframework.security.oauth2.core.oidc.user.OidcUserAuthority;
import org.springframework.security.oauth2.jwt.*;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.csrf.CsrfFilter;
import org.springframework.security.web.header.writers.ReferrerPolicyHeaderWriter;
import org.springframework.security.web.servlet.util.matcher.MvcRequestMatcher;
import org.springframework.web.filter.CorsFilter;
import org.springframework.web.servlet.handler.HandlerMappingIntrospector;

import com.sample.sampleservice.shared.authentication.domain.Role;

import java.time.Duration;
import java.util.HashSet;
import java.util.Set;

import static org.springframework.security.config.Customizer.withDefaults;
import static org.springframework.security.web.util.matcher.AntPathRequestMatcher.antMatcher;

@Slf4j
@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
@EnableMethodSecurity(securedEnabled = true)
class SecurityConfiguration {

    private static final int TIMEOUT = 2000;

    private final CorsFilter corsFilter;
    private final HandlerMappingIntrospector introspector;
    private final ApplicationSecurityProperties applicationSecurityProperties;
    private final CustomAuthenticationEntryPoint customAuthenticationEntryPoint; 


    @Value("${spring.security.oauth2.client.provider.oidc.issuer-uri}")
    private String issuerUri;

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
        .requestMatchers(antMatcher(HttpMethod.POST, "/api/v1/accounts")).permitAll()
          .requestMatchers(antMatcher("/api/v1/callback")).permitAll()
          .requestMatchers(antMatcher("/api/v1/chapa/callback")).permitAll()
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
      .oauth2Login(withDefaults())
      .oauth2ResourceServer(oauth2 -> oauth2
        .jwt(jwt -> jwt.jwtAuthenticationConverter(authenticationConverter()))
        .authenticationEntryPoint(customAuthenticationEntryPoint)
      )
      .oauth2Client(withDefaults())
      .build();
    // @formatter:on
    }

    private Converter<Jwt, AbstractAuthenticationToken> authenticationConverter() {
        JwtAuthenticationConverter jwtAuthenticationConverter = new JwtAuthenticationConverter();
        jwtAuthenticationConverter.setJwtGrantedAuthoritiesConverter(new JwtGrantedAuthorityConverter());

        return jwtAuthenticationConverter;
    }

    /**
     * Map authorities from "groups" or "roles" claim in ID Token.
     *
     * @return a {@link GrantedAuthoritiesMapper} that maps groups from the IdP to Spring Security Authorities.
     */
    @Bean
    public GrantedAuthoritiesMapper userAuthoritiesMapper() {
        return authorities -> {
            Set<GrantedAuthority> mappedAuthorities = new HashSet<>();

            authorities.forEach(authority -> {
                // Check for OidcUserAuthority because Spring Security 5.2 returns
                // each scope as a GrantedAuthority, which we don't care about.
                if (authority instanceof OidcUserAuthority oidcUserAuthority) {
                    mappedAuthorities.addAll(Claims.extractAuthorityFromClaims(oidcUserAuthority.getUserInfo().getClaims()));
                }
            });
            return mappedAuthorities;
        };
    }

    @Bean
    public JwtDecoder jwtDecoder(ClientRegistrationRepository clientRegistrationRepository, RestTemplateBuilder restTemplateBuilder) {
        NimbusJwtDecoder jwtDecoder = JwtDecoders.fromOidcIssuerLocation(issuerUri);

        OAuth2TokenValidator<Jwt> audienceValidator = new AudienceValidator(applicationSecurityProperties.getOauth2().getAudience());
        OAuth2TokenValidator<Jwt> withIssuer = JwtValidators.createDefaultWithIssuer(issuerUri);
        OAuth2TokenValidator<Jwt> withAudience = new DelegatingOAuth2TokenValidator<>(withIssuer, audienceValidator);

        jwtDecoder.setJwtValidator(withAudience);
        jwtDecoder.setClaimSetConverter(new CustomClaimConverter(clientRegistrationRepository.findByRegistrationId("oidc"), restTemplateBuilder.setConnectTimeout(Duration.ofMillis(TIMEOUT)).setReadTimeout(Duration.ofMillis(TIMEOUT)).build()));

        return jwtDecoder;
    }
}

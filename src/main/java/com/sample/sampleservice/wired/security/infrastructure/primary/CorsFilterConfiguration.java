package com.sample.sampleservice.wired.security.infrastructure.primary;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.CollectionUtils;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class CorsFilterConfiguration {

    private final CorsConfiguration corsConfiguration;

    @Bean
    public CorsFilter corsFilter() {
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        if (
                !CollectionUtils.isEmpty(corsConfiguration.getAllowedOrigins()) ||
                        !CollectionUtils.isEmpty(corsConfiguration.getAllowedOriginPatterns())
        ) {
            log.debug("Registering CORS filter");
            source.registerCorsConfiguration("/api/**", corsConfiguration);
            source.registerCorsConfiguration("/management/**", corsConfiguration);
            source.registerCorsConfiguration("/v2/api-docs", corsConfiguration);
            source.registerCorsConfiguration("/v3/api-docs", corsConfiguration);
            source.registerCorsConfiguration("/swagger-resources", corsConfiguration);
            source.registerCorsConfiguration("/swagger-ui/**", corsConfiguration);
        }
        return new CorsFilter(source);
    }
}

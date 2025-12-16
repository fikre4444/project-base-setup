package com.sample.sampleservice.shared.openfeign.infrastructure.secondary;

import lombok.RequiredArgsConstructor;
import org.springframework.cloud.openfeign.FeignClientsConfiguration;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@RequiredArgsConstructor
@Import(FeignClientsConfiguration.class)
public class SmsFeignClientConfiguration {

//    private final CustomProperties properties;
//
//    @Bean
//    public BasicAuthRequestInterceptor basicAuthRequestInterceptor() {
//        return new BasicAuthRequestInterceptor(properties.getSms().getUserName(), properties.getSms().getApiKey());
//    }
}

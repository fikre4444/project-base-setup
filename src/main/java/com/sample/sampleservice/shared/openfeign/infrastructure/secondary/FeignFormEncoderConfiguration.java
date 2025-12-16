package com.sample.sampleservice.shared.openfeign.infrastructure.secondary;

import feign.codec.Encoder;
import feign.form.FormEncoder;
import org.springframework.cloud.openfeign.FeignClientsConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Primary;

@Import(FeignClientsConfiguration.class)
public class FeignFormEncoderConfiguration {
    /**
     * Set the Feign specific log level to log client REST requests.
     */
    @Bean
    public feign.Logger.Level feignLoggerLevel() {
        return feign.Logger.Level.BASIC;
    }

    @Primary
    @Bean
    public Encoder encoder() {
        return new FormEncoder();
    }
}

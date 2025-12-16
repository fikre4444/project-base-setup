package com.sample.sampleservice.shared.openfeign.infrastructure.secondary;

//import feign.codec.Encoder;
//import feign.form.FormEncoder;

import org.springframework.cloud.openfeign.FeignClientsConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
//import org.springframework.context.annotation.Primary;

@Configuration
@Import(FeignClientsConfiguration.class)
public class FeignClientConfiguration {

    /**
     * Set the Feign specific log level to log client REST requests.
     */
    @Bean
    public feign.Logger.Level feignLoggerLevel() {
        return feign.Logger.Level.FULL;
    }
}

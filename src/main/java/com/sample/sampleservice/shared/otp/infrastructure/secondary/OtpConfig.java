package com.sample.sampleservice.shared.otp.infrastructure.secondary;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.sample.sampleservice.shared.otp.application.OtpApplication;
import com.sample.sampleservice.shared.otp.application.impl.OtpApplicationImpl;
import com.sample.sampleservice.shared.otp.domain.OtpGenerator;

@Configuration
public class OtpConfig {

    @Bean
    public OtpApplication otpApplication(OtpGenerator otpGenerator) {
        return new OtpApplicationImpl(otpGenerator);
    }
}

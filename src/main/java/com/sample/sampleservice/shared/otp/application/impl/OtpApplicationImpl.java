package com.sample.sampleservice.shared.otp.application.impl;

import com.sample.sampleservice.shared.otp.application.OtpApplication;
import com.sample.sampleservice.shared.otp.domain.OtpDomainService;
import com.sample.sampleservice.shared.otp.domain.OtpGenerator;

public class OtpApplicationImpl implements OtpApplication {

    private final OtpDomainService otpDomainService;

    public OtpApplicationImpl(OtpGenerator otpGenerator) {
        this.otpDomainService = new OtpDomainService(otpGenerator);
    }

    @Override
    public String generate(String identifier) {
        return this.otpDomainService.generate(identifier);
    }

    @Override
    public boolean verify(String phoneNumber, String code) {
        return this.otpDomainService.verify(phoneNumber, code);
    }
}

package com.sample.sampleservice.shared.otp.domain;

public class OtpDomainService {

    private final OtpGenerator otpGenerator;

    public OtpDomainService(OtpGenerator otpGenerator) {
        this.otpGenerator = otpGenerator;
    }

    public String generate(String identifier) {
        return otpGenerator.generate(identifier);
    }

    public boolean verify(String identifier, String code) {
        return otpGenerator.verify(identifier, code);
    }
}

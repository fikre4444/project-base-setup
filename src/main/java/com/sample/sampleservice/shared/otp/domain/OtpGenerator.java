package com.sample.sampleservice.shared.otp.domain;

public interface OtpGenerator {

    String generate(String identifier);

    boolean verify(String identifier, String code);
}

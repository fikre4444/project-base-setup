package com.sample.sampleservice.shared.otp.application;

public interface OtpApplication {

    String generate(String identifier);

    boolean verify(String identifier, String code);
}

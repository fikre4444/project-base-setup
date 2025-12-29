package com.sample.sampleservice.shared.emailing.service;

public interface EmailService {
    void sendRegistrationEmail(String to, String username);

    void sendVerificationOtp(String to, String otp);
}
package com.sample.sampleservice.shared.emailing.service.impl;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import com.sample.sampleservice.shared.emailing.service.EmailService;

import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailServiceImpl implements EmailService {

    private final JavaMailSender mailSender;
    private final ResourceLoader resourceLoader;

    @Value("${spring.mail.username}")
    private String from;

    @Async
    @Override
    public void sendRegistrationEmail(String to, String username) {
        try {
            log.info("Send from {} to {}", from, to);
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper =
                new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(from);
            helper.setTo(to);
            helper.setSubject("Welcome to Our Platform 🎉");

            String html = loadTemplate("templates/welcome-email.html");
            html = html
                .replace("{{username}}", username)
                .replace("{{loginUrl}}", "http://localhost:8085");

            helper.setText(html, true);

            mailSender.send(message);
        } catch (Exception e) {
            throw new RuntimeException("Failed to send registration email", e);
        }
    }

    @Async
    @Override
    public void sendVerificationOtp(String to, String otp) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper =
                new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(from);
            helper.setTo(to);
            helper.setSubject("Your OTP Code 🔒");

            String html = loadTemplate("templates/otp-email.html");
            html = html.replace("{{otp}}", otp);

            helper.setText(html, true);

            mailSender.send(message);
        } catch (Exception e) {
            throw new RuntimeException("Failed to send OTP email", e);
        }    
    }



    private String loadTemplate(String path) throws IOException {
        Resource resource = resourceLoader.getResource("classpath:" + path);
        return new String(
            resource.getInputStream().readAllBytes(),
            StandardCharsets.UTF_8
        );
    }
}

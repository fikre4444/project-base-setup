package com.sample.sampleservice.shared.otp.infrastructure.secondary;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.sample.sampleservice.shared.otp.domain.OtpGenerator;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Slf4j
@Service
public class OtpGeneratorImpl implements OtpGenerator {

    private final Cache<String, String> otpCache;

    public OtpGeneratorImpl() {
        this.otpCache = CacheBuilder.newBuilder()
            .expireAfterWrite(5, TimeUnit.MINUTES)
            .build();
    }

    public String generate(String identifier) {
        String otp = RandomStringUtils.random(6, false, true);
        otpCache.put(identifier, otp);
        return otp;
    }

    public boolean verify(String identifier, String code) {
        String cachedOtp = otpCache.getIfPresent(identifier);
        return cachedOtp != null && cachedOtp.equals(code);
    }
}

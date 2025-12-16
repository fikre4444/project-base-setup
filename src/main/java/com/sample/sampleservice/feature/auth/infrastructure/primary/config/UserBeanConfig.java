package com.sample.sampleservice.feature.auth.infrastructure.primary.config;

import com.sample.sampleservice.feature.auth.application.UserApplicationService;
import com.sample.sampleservice.feature.auth.application.impl.UserApplicationServiceImpl;
import com.sample.sampleservice.feature.auth.domain.repository.UserRepository;
import com.sample.sampleservice.shared.notification.application.NotificationApplicationService;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class UserBeanConfig {

    @Bean
    public UserApplicationService userApplicationService(UserRepository userRepository, NotificationApplicationService notificationApplicationService) {
        return new UserApplicationServiceImpl(userRepository, notificationApplicationService);
    }
}

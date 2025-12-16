package com.sample.sampleservice.shared.notification.infrastructure.primary.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.sample.sampleservice.shared.notification.application.NotificationApplicationService;
import com.sample.sampleservice.shared.notification.application.impl.NotificationApplicationServiceImpl;
import com.sample.sampleservice.shared.notification.domain.event.NotificationEvent;
import com.sample.sampleservice.shared.notification.domain.service.NotificationDomainService;

@Configuration
public class NotificationConfig {

    @Bean
    public NotificationApplicationService notificationApplicationService(NotificationEvent notificationEvent) {
        return new NotificationApplicationServiceImpl(new NotificationDomainService(notificationEvent));
    }
}

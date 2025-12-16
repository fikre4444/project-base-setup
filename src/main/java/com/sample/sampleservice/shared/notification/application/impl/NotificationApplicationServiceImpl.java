package com.sample.sampleservice.shared.notification.application.impl;

import java.util.List;
import java.util.Map;

import com.sample.sampleservice.shared.notification.application.NotificationApplicationService;
import com.sample.sampleservice.shared.notification.domain.model.Recipient;
import com.sample.sampleservice.shared.notification.domain.model.enums.NotificationType;
import com.sample.sampleservice.shared.notification.domain.service.NotificationDomainService;

public class NotificationApplicationServiceImpl implements NotificationApplicationService {

    private final NotificationDomainService notificationDomainService;

    public NotificationApplicationServiceImpl(NotificationDomainService notificationDomainService) {
        this.notificationDomainService = notificationDomainService;
    }

    @Override
    public void notify(NotificationType type, String templateName, List<Recipient> recipients, Map<String, String> payload) {
        this.notificationDomainService.notify(type, templateName, recipients, payload);
    }
}

package com.sample.sampleservice.shared.notification.domain.service;

import java.util.List;
import java.util.Map;

import com.sample.sampleservice.shared.notification.domain.event.NotificationEvent;
import com.sample.sampleservice.shared.notification.domain.model.Notification;
import com.sample.sampleservice.shared.notification.domain.model.Recipient;
import com.sample.sampleservice.shared.notification.domain.model.enums.NotificationType;

public class NotificationDomainService {

    public final NotificationEvent notificationEvent;

    public NotificationDomainService(NotificationEvent notificationEvent) {
        this.notificationEvent = notificationEvent;
    }

    public void notify(NotificationType type, String templateName, List<Recipient> recipients, Map<String, String> payload) {
        switch (type) {
            case SMS ->
                    this.notificationEvent.notifySMS(new Notification("sample_service", templateName, recipients, payload));
            case EMAIL ->
                    this.notificationEvent.notifyEmail(new Notification("sample_service", templateName, recipients, payload));
            case PUSH ->
                    this.notificationEvent.notifyPush(new Notification("sample_service", templateName, recipients, payload));
        }
    }
}

package com.sample.sampleservice.shared.notification.application;

import java.util.List;
import java.util.Map;

import com.sample.sampleservice.shared.notification.domain.model.Recipient;
import com.sample.sampleservice.shared.notification.domain.model.enums.NotificationType;

public interface NotificationApplicationService {

    void notify(NotificationType type, String templateName, List<Recipient> recipients, Map<String, String> payload);
}

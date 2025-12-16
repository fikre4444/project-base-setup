package com.sample.sampleservice.shared.notification.domain.model;

import java.util.List;
import java.util.Map;

public record Notification(String source, String templateName, List<Recipient> recipients, Map<String, String> payload) {
}

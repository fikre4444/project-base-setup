package com.sample.sampleservice.shared.notification.domain.event;

import com.sample.sampleservice.shared.notification.domain.model.Notification;

public interface NotificationEvent {

    void notifyPush(Notification notification);

    void notifySMS(Notification notification);

    void notifyEmail(Notification notification);
}

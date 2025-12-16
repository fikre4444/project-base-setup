package com.sample.sampleservice.shared.notification.infrastructure.secondary.publisher;

import lombok.RequiredArgsConstructor;

import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import com.sample.sampleservice.shared.notification.domain.event.NotificationEvent;
import com.sample.sampleservice.shared.notification.domain.model.Notification;

@Service
@RequiredArgsConstructor
public class NotificationPublisher implements NotificationEvent {

    public final KafkaProducer kafkaProducer;

    @Override
    @Async
    public void notifyPush(Notification notification) {
        kafkaProducer.sendPush(notification);
    }
    @Override
    @Async
    public void notifySMS(Notification notification) {
        kafkaProducer.sendSMS(notification);
    }
    @Override
    @Async
    public void notifyEmail(Notification notification) {
        kafkaProducer.sendEmail(notification);
    }
}

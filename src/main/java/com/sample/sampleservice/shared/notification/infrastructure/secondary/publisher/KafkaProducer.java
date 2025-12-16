package com.sample.sampleservice.shared.notification.infrastructure.secondary.publisher;

import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import com.sample.sampleservice.shared.notification.domain.model.Notification;

@Component
public class KafkaProducer {
   private final KafkaTemplate<String, Object> kafkaTemplate;

   public KafkaProducer(KafkaTemplate<String, Object> kafkaTemplate) {
       this.kafkaTemplate = kafkaTemplate;
   }

    public void sendEmail(Notification event) {
       kafkaTemplate.send("notification.email.sample", event);
    }

    public void sendSMS(Notification event) {
       kafkaTemplate.send("notification.sms.sample", event);
    }

    public void sendPush(Notification event) {
       kafkaTemplate.send("notification.push.sample", event);
    }

    public void notify(String topic, String message) {
        kafkaTemplate.send(topic, message);
    }

    // public void notify(String topic, TaskMetadata data) {
    //     kafkaTemplate.send(topic, data);
    // }
}


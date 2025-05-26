package com.synchrony.cloudinary.service;

import com.synchrony.cloudinary.dto.ImageUploadEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class KafkaProducer {
    private static final String TOPIC = "image-uploads";

    @Autowired
    KafkaTemplate<String, ImageUploadEvent> kafkaTemplate;

    public void sendImageUploadEvent(ImageUploadEvent event) {
        log.info("Publishing image upload event: {}", event);
        kafkaTemplate.send(TOPIC, event.getUsername(), event);
    }
}

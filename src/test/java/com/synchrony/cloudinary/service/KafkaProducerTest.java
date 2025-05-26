package com.synchrony.cloudinary.service;

import com.synchrony.cloudinary.dto.ImageUploadEvent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.kafka.core.KafkaTemplate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

public class KafkaProducerTest {

    private KafkaTemplate<String, ImageUploadEvent> kafkaTemplate;
    private KafkaProducer kafkaProducer;

    @BeforeEach
    void setUp() {
        kafkaTemplate = mock(KafkaTemplate.class);
        kafkaProducer = new KafkaProducer();
        kafkaProducer.kafkaTemplate = kafkaTemplate;
    }

    @Test
    void testSendImageUploadEvent() {
        ImageUploadEvent event = new ImageUploadEvent("alice", "photo.jpg");

        kafkaProducer.sendImageUploadEvent(event);

        ArgumentCaptor<String> keyCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<ImageUploadEvent> valueCaptor = ArgumentCaptor.forClass(ImageUploadEvent.class);

        verify(kafkaTemplate, times(1)).send(eq("image-uploads"), keyCaptor.capture(), valueCaptor.capture());

        assertThat(keyCaptor.getValue()).isEqualTo("alice");
        assertThat(valueCaptor.getValue()).isEqualTo(event);
    }
}

package com.synchrony.cloudinary.config;

import com.synchrony.cloudinary.dto.ImageUploadEvent;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringSerializer;
import org.junit.jupiter.api.Test;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.serializer.JsonSerializer;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

public class KafkaProducerConfigTest {

    private final KafkaProducerConfig kafkaProducerConfig = new KafkaProducerConfig();

    @Test
    void testProducerFactoryConfiguration() {
        ProducerFactory<String, ImageUploadEvent> producerFactory = kafkaProducerConfig.producerFactory();

        assertThat(producerFactory).isNotNull();

        // Extract config for assertions
        Map<String, Object> config = producerFactory.getConfigurationProperties();

        assertThat(config.get(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG)).isEqualTo("localhost:9092");
        assertThat(config.get(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG)).isEqualTo(StringSerializer.class);
        assertThat(config.get(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG)).isEqualTo(JsonSerializer.class);
    }

    @Test
    void testKafkaTemplateCreation() {
        KafkaTemplate<String, ImageUploadEvent> kafkaTemplate = kafkaProducerConfig.kafkaTemplate();
        assertThat(kafkaTemplate).isNotNull();
    }
}

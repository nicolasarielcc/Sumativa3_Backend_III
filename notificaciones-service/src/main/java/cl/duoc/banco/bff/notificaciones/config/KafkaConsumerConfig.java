package cl.duoc.banco.bff.notificaciones.config;

import java.util.HashMap;
import java.util.Map;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.listener.ContainerProperties;
import org.springframework.kafka.support.serializer.JsonDeserializer;

import cl.duoc.banco.bff.notificaciones.dto.RetiroEvent;

/**
 * Configuracion del consumidor Kafka. Ack manual inmediato y
 * {@code auto.offset.reset=earliest} para no perder eventos.
 */
@EnableKafka
@Configuration
public class KafkaConsumerConfig {

    public static final String TOPIC = "retiros";
    public static final String CONSUMER_GROUP_ID = "notificaciones-group";

    private final String bootstrapServers;

    public KafkaConsumerConfig(
            @Value("${app.kafka.bootstrap-servers:localhost:29092}") String bootstrapServers) {
        this.bootstrapServers = bootstrapServers;
    }

    @Bean
    ConsumerFactory<String, RetiroEvent> consumerFactory() {
        Map<String, Object> props = new HashMap<>();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        props.put(ConsumerConfig.GROUP_ID_CONFIG, CONSUMER_GROUP_ID);
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");

        return new DefaultKafkaConsumerFactory<>(props, new StringDeserializer(),
                new JsonDeserializer<>(RetiroEvent.class, false));
    }

    @Bean
    ConcurrentKafkaListenerContainerFactory<String, RetiroEvent> kafkaListenerContainerFactory() {
        ConcurrentKafkaListenerContainerFactory<String, RetiroEvent> factory =
                new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(consumerFactory());
        factory.getContainerProperties().setAckMode(ContainerProperties.AckMode.MANUAL_IMMEDIATE);
        return factory;
    }
}

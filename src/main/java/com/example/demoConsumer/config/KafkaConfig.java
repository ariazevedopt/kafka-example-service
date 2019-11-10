package com.example.demoConsumer.config;

import io.confluent.kafka.serializers.AbstractKafkaAvroSerDeConfig;
import io.confluent.kafka.serializers.KafkaAvroDeserializer;
import io.confluent.kafka.serializers.KafkaAvroSerializer;
import io.confluent.kafka.serializers.KafkaAvroSerializerConfig;
import io.confluent.kafka.serializers.subject.TopicRecordNameStrategy;
import org.apache.avro.generic.GenericRecord;
import org.apache.kafka.clients.CommonClientConfigs;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.config.SslConfigs;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.*;

import java.util.HashMap;
import java.util.Map;

@Configuration
@EnableKafka
public class KafkaConfig {

    @Autowired
    private KafkaProperties properties;

    @Value("${kafka.topic.request}")
    private String requestTopic;

    @Value("${kafka.request-reply.timeout-ms}")
    private Long replyTimeout;

    @Bean
    public ConsumerFactory<String, GenericRecord> consumerFactory() {
        Map<String, Object> props = new HashMap<>();

        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, properties.getBootstrapServers());
        props.put(AbstractKafkaAvroSerDeConfig.SCHEMA_REGISTRY_URL_CONFIG, properties.getSchemaRegistry());
        props.put(ConsumerConfig.GROUP_ID_CONFIG, properties.getConsumerGroupId());

        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, KafkaAvroDeserializer.class);

        props.put(ConsumerConfig.FETCH_MAX_BYTES_CONFIG, "50000000");
        props.put("max.request.size", 50000000);

        props.put(ConsumerConfig.SESSION_TIMEOUT_MS_CONFIG, 60000);
        props.put(ConsumerConfig.MAX_POLL_INTERVAL_MS_CONFIG, 60000);

        setSecurityProperties(props);

        return new DefaultKafkaConsumerFactory<>(props);
    }

    @Bean
    public Map<String, Object> producerConfig() {
        Map<String, Object> props = new HashMap<>();
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, properties.getBootstrapServers());
        props.put(AbstractKafkaAvroSerDeConfig.SCHEMA_REGISTRY_URL_CONFIG, properties.getSchemaRegistry());
        props.put(AbstractKafkaAvroSerDeConfig.AUTO_REGISTER_SCHEMAS, properties.getSchemaAutoregister());
        props.put(KafkaAvroSerializerConfig.VALUE_SUBJECT_NAME_STRATEGY, TopicRecordNameStrategy.class.getName());

        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, KafkaAvroSerializer.class);
        setSecurityProperties(props);

        return props;
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, GenericRecord> kafkaListenerContainerFactory() {
        ConcurrentKafkaListenerContainerFactory<String, GenericRecord> factory = new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(consumerFactory());
        factory.setReplyTemplate(kafkaTemplate());
        return factory;
    }

    @Bean
    public KafkaTemplate<String, GenericRecord> kafkaTemplate() {
        ProducerFactory<String, GenericRecord> pf = new DefaultKafkaProducerFactory<>(producerConfig());
        return new KafkaTemplate<>(pf);
    }

    private void setSecurityProperties(Map<String, Object> props) {
        KafkaPropertiesSecurity securityProperties = properties.getSsl();

        Boolean kafkaSecurityEnabled = securityProperties.getEnabled();
        if (kafkaSecurityEnabled) {
            props.put(CommonClientConfigs.SECURITY_PROTOCOL_CONFIG, securityProperties.getProtocol());
            props.put(SslConfigs.SSL_KEYSTORE_LOCATION_CONFIG, securityProperties.getKeyStoreLocation());
            props.put(SslConfigs.SSL_KEYSTORE_PASSWORD_CONFIG, securityProperties.getKeyStorePassword());
            props.put(SslConfigs.SSL_KEY_PASSWORD_CONFIG, securityProperties.getKeyPassword());
            props.put(SslConfigs.SSL_KEYSTORE_TYPE_CONFIG, securityProperties.getKeyStoreType());
            props.put(SslConfigs.SSL_TRUSTSTORE_LOCATION_CONFIG, securityProperties.getTrustStoreLocation());
            props.put(SslConfigs.SSL_TRUSTSTORE_PASSWORD_CONFIG, securityProperties.getTrustStorePassword());
            props.put(SslConfigs.SSL_ENDPOINT_IDENTIFICATION_ALGORITHM_CONFIG, securityProperties.getEndPointIdentificationAlgorithm());
        }
    }
}

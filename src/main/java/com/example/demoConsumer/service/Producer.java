package com.example.demoConsumer.service;


import com.example.demoConsumer.util.Constants;
import com.example.demoConsumer.util.KafkaHeaderUtil;
import org.apache.avro.specific.SpecificRecord;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.header.internals.RecordHeader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.MessageHeaders;
import org.springframework.stereotype.Service;


@Service
public class Producer {

    private static Logger logger = LoggerFactory.getLogger(Producer.class);

    @Autowired
    private KafkaTemplate kafkaTemplate;


    public void produce(SpecificRecord data, String key, MessageHeaders headers) {
        try {
            String replyTopic = KafkaHeaderUtil.getStringOrNullFromHeader(headers, KafkaHeaders.REPLY_TOPIC);

            ProducerRecord<String, SpecificRecord> producerRecord = new ProducerRecord<>(replyTopic, key, data);

            addCorrelationId(producerRecord, headers);
            //addHeaders(producerRecord, headers);

            kafkaTemplate.send(producerRecord);

            logger.info("Message produced to the topic {} ", replyTopic);
        } catch (Exception e) {
            logger.error("Producer error:", e);
        }
    }

    public static void addHeaders(ProducerRecord<String, SpecificRecord> producerRecord, MessageHeaders headers) {
        String auth = KafkaHeaderUtil.getStringOrNullFromHeader(headers, Constants.KafkaHeaders.TOKEN);
        String tracking = KafkaHeaderUtil.getStringOrNullFromHeader(headers, Constants.KafkaHeaders.TRACKING_ID);
        producerRecord.headers().add(new RecordHeader(Constants.KafkaHeaders.TOKEN, auth.getBytes()));
        if (tracking != null) {
            producerRecord.headers().add(new RecordHeader(Constants.KafkaHeaders.TRACKING_ID, tracking.getBytes()));
        }
    }

    private void addCorrelationId(ProducerRecord<String, SpecificRecord> producerRecord, MessageHeaders headers) {
        byte[] correlationId = (byte[]) headers.get(KafkaHeaders.CORRELATION_ID);
        producerRecord.headers().add(new RecordHeader(KafkaHeaders.CORRELATION_ID, correlationId));
    }

}

package com.example.demoConsumer.consumer;


import com.example.demoConsumer.service.Producer;
import com.example.demoConsumer.service.UserService;
import com.kafka.schema.UserExample;
import com.kafka.schema.UserGroup;
import com.kafka.schema.UserList;
import org.apache.avro.Schema;
import org.apache.avro.generic.GenericRecord;
import org.apache.avro.specific.SpecificData;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.messaging.MessageHeaders;
import org.springframework.messaging.handler.annotation.Headers;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class Consumer {

    static final Logger logger = LoggerFactory.getLogger(Consumer.class);

    @Autowired
    public ConcurrentKafkaListenerContainerFactory<String, GenericRecord> kafkaListenerContainerFactory;

    @Value("${kafka.topic.mock}")
    private String mockTopic;

    @Value("${kafka.topic.request}")
    private String requestTopic;

    @Autowired
    private UserService userService;

    @Autowired
    private Producer producer;

    @KafkaListener(topics = {"${kafka.topic.mock}", "${kafka.topic.request}"})
    public void listen(@Payload ConsumerRecord<String, GenericRecord> request, @Headers MessageHeaders headers) {

        Schema schema = request.value().getSchema();

        if (schema.equals(UserExample.getClassSchema())) {

            UserExample data = (UserExample) SpecificData.get().deepCopy(UserExample.SCHEMA$, request.value());
            userService.createMessageUser(data);
        }

        if (schema.equals(UserGroup.getClassSchema())) {

            UserGroup data = (UserGroup) SpecificData.get().deepCopy(UserGroup.SCHEMA$, request.value());
            UserList userList = userService.getUsersByGroup(data);

            producer.produce(userList, request.key(), headers);
        }
    }
}

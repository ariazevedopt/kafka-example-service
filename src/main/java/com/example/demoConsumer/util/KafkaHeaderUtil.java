package com.example.demoConsumer.util;

import org.springframework.messaging.MessageHeaders;

public class KafkaHeaderUtil {
    private KafkaHeaderUtil() {
    }

    public static String getStringOrNullFromHeader(MessageHeaders headers, String key) {
        byte[] rawValue = (byte[]) headers.get(key);

        if (rawValue == null) {
            return null;
        }

        return new String(rawValue);
    }

}

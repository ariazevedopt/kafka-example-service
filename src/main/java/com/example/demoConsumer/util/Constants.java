package com.example.demoConsumer.util;

public class Constants {

    private Constants() {
    }

    public static class KafkaHeaders {
        public static final String TOKEN = "api_access_token";
        public static final String TRACKING_ID = "request_id";

        private KafkaHeaders() {
        }
    }
}

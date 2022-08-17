package com.anacleto.ecommerce;

import java.util.UUID;
import java.util.concurrent.ExecutionException;

// stopped video 4.02

public class NewOderMain {

    public static void main(String[] args) throws ExecutionException, InterruptedException {
        try (var kafkaDispatcher = new KafkaDispatcher()) {

            for (var i = 0; i < 40; i++) {
                var key = UUID.randomUUID().toString();
                var value = key + ",67523,5200";
                kafkaDispatcher.send("ECOMMERCE_NEW_ORDER", key, value);

                var email = "Thank you! We are processing your order.";
                kafkaDispatcher.send("ECOMMERCE_SEND_EMAIL", key, email);
            }
        }
    }
}

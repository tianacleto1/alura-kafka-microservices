package com.anacleto.ecommerce;

import java.math.BigDecimal;
import java.util.UUID;
import java.util.concurrent.ExecutionException;

public class NewOderMain {

    public static void main(String[] args) throws ExecutionException, InterruptedException {
        try (var orderKafkaDispatcher = new KafkaDispatcher<Order>()) {
            try (var emailKafkaDispatcher = new KafkaDispatcher<String>()) {
                for (var i = 0; i < 40; i++) {
                    var userId = UUID.randomUUID().toString();
                    var orderId = UUID.randomUUID().toString();
                    var amount = BigDecimal.valueOf(Math.random() * 5000 + 1);

                    var order = new Order(userId, orderId, amount);

                    orderKafkaDispatcher.send("ECOMMERCE_NEW_ORDER", userId, order);

                    var email = "Thank you! We are processing your order.";
                    emailKafkaDispatcher.send("ECOMMERCE_SEND_EMAIL", userId, email);
                }
            }
        }
    }
}

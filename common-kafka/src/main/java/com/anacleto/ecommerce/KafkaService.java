package com.anacleto.ecommerce;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.serialization.StringDeserializer;

import java.io.Closeable;
import java.time.Duration;
import java.util.Collections;
import java.util.Map;
import java.util.Properties;
import java.util.UUID;
import java.util.regex.Pattern;

public class KafkaService<T> implements Closeable {

    private final KafkaConsumer<String, T> consumer;
    private final ConsumerFunction parse;

    public KafkaService(String groupId, String topic, ConsumerFunction parse, Class<T> type, Map<String, String> properties) {
        this(groupId, parse, type, properties);
        consumer.subscribe(Collections.singletonList(topic));
    }

    public KafkaService(String groupId, Pattern topicPattern, ConsumerFunction parse, Class<T> type, Map<String, String> properties) {
        this(groupId, parse, type, properties);
        consumer.subscribe(topicPattern);
    }

    private KafkaService(String groupId, ConsumerFunction parse, Class<T> type, Map<String, String> properties) {
        this.consumer = new KafkaConsumer<>(getProperties(groupId, type, properties));
        this.parse = parse;
    }

    public void run() {
        while (true) {
            var records = consumer.poll(Duration.ofMillis(100));

            if (!records.isEmpty()) {
                System.out.println("Found " + records.count() + " messages");
                for (var record : records) {
                    parse.consume(record);
                }
            }
        }
    }

    private Properties getProperties(String groupId, Class<T> type, Map<String, String> overrideProperties) {
        Properties properties = new Properties();
        properties.setProperty(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, "host.docker.internal:9094");
        properties.setProperty(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        properties.setProperty(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, GsonDeserializer.class.getName());
        properties.setProperty(ConsumerConfig.MAX_POLL_RECORDS_CONFIG, "1");
        properties.setProperty(ConsumerConfig.GROUP_ID_CONFIG, groupId);
        properties.setProperty(ConsumerConfig.CLIENT_ID_CONFIG, UUID.randomUUID().toString());
        properties.setProperty(GsonDeserializer.TYPE_CONFIG, type.getName());
        properties.putAll(overrideProperties);

        return properties;
    }

    @Override
    public void close() {
        this.consumer.close();
    }
}

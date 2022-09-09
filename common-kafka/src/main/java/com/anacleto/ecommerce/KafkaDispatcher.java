package com.anacleto.ecommerce;

import org.apache.kafka.clients.producer.Callback;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.StringSerializer;

import java.io.Closeable;
import java.util.Properties;
import java.util.concurrent.ExecutionException;

public class KafkaDispatcher<T> implements Closeable {

    private final KafkaProducer<String, T> producer;

    public KafkaDispatcher() {
        this.producer = new KafkaProducer<>(properties());
    }

    private static Properties properties() {
        Properties properties = new Properties();
        properties.setProperty(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, "host.docker.internal:9094");
        properties.setProperty(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        properties.setProperty(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, GsonSerializer.class.getName());

        return properties;
    }

    public void send(String topic, String key, T value) throws ExecutionException, InterruptedException {
        var msg = new ProducerRecord<>(topic, key, value);

        Callback callback = (data, ex) -> {
            if (ex != null) {
                ex.printStackTrace();
                return;
            }

            System.out.println("Sending success? " + data.topic() + " " + data.partition() + " " + data.offset());
        };

        producer.send(msg, callback).get();
    }

    @Override
    public void close()  {
        this.producer.close();
    }
}

package com.jh.kafka.vitalinfo;

import java.util.Properties;
import java.util.concurrent.ExecutionException;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;

/**
 * This class is a simple producer.
 */
public class App {
    public static void main(String[] args) throws ExecutionException, InterruptedException {
        for(int i = 0; i < args.length; i++) {
            String arg = args[i];

            System.out.println("ARG: " + arg);

            // Based on the definition in pom.xml for the execution task: kafkap
            if(arg.startsWith("--somestuff" + "=")) {
                System.out.println(arg.split("=")[1]);
            }
        }

        Properties props = new Properties();
        props.put("bootstrap.servers", "localhost:9093");
        props.put("acks", "all");
        props.put("delivery.timeout.ms", 30000);
        props.put("request.timeout.ms", 20000);
        props.put("batch.size", 16384);
        props.put("linger.ms", 1);
        props.put("buffer.memory", 33554432);
        props.put("key.serializer", "org.apache.kafka.common.serialization.StringSerializer");
        props.put("value.serializer", "org.apache.kafka.common.serialization.StringSerializer");

        Producer<String, String> producer = new KafkaProducer<>(props);
        for(int i = 0; i < 10; i++) {
            ProducerRecord<String, String> record = new ProducerRecord<>("titi", 0, Integer.toString(i), Integer.toString(i));
            producer.send(record);
            RecordMetadata metadata = producer.send(record).get();
            System.out.println("Record sent with key " + i + " to partition " + metadata.partition()
                    + " with offset " + metadata.offset());
            System.out.println("Sending record " + i);
        }
        producer.close();
    }
}

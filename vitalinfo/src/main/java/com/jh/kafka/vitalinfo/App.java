package com.jh.kafka.vitalinfo;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Properties;
import java.util.StringJoiner;
import java.util.TimeZone;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;

/**
 * This class is a simple producer.
 */
public class App {
    public static void main(String[] args) {
        for(int i = 0; i < args.length; i++) {
            String arg = args[i];

            System.out.println("ARG: " + arg);

            // Based on the definition in pom.xml for the execution task: kafkap
            if(arg.startsWith("--somestuff" + "=")) {
                System.out.println(arg.split("=")[1]);
            }
        }

        Properties props = new Properties();
        props.put("bootstrap.servers", "172.17.0.1:19092,172.17.0.1:29092,172.17.0.1:39092");
        props.put("acks", "all");
        props.put("delivery.timeout.ms", 2000);
        props.put("request.timeout.ms", 1000);
//        props.put("batch.size", 16384);
        props.put("linger.ms", 0);
        props.put("max.block.ms", 1000);
//        props.put("buffer.memory", 33554432);
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, "org.apache.kafka.common.serialization.StringSerializer");
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, "org.apache.kafka.common.serialization.StringSerializer");

        Producer<String, String> producer = new KafkaProducer<>(props);
        for(int i = 0; i < 30; i++) {
            try {
                System.out.println("Sending record " + i);
                InetAddress ip = InetAddress.getLocalHost();
                String hostname = ip.getHostName();


                Date date = new Date(System.currentTimeMillis());

// Conversion
                SimpleDateFormat sdf;
                sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSXXX");
                sdf.setTimeZone(TimeZone.getTimeZone("CET"));


                Runtime runtime = Runtime.getRuntime();

                NumberFormat format = NumberFormat.getInstance(Locale.ENGLISH);

                StringBuilder sb = new StringBuilder();
                long maxMemory = runtime.maxMemory();
                long allocatedMemory = runtime.totalMemory();
                long freeMemory = runtime.freeMemory();

                String msg = new StringJoiner(" - ")
                        .add(sdf.format(date))
                        .add(ip.getHostAddress())
                        .add(" #" + i)
                        .add(String.format("%s KB", format.format(allocatedMemory / 1024)))
                        .toString();
                ProducerRecord<String, String> record = new ProducerRecord<>("memory", 0, Integer.toString(i), msg);
                RecordMetadata metadata = producer.send(record).get(10, TimeUnit.SECONDS);
                System.out.println("Record sent with key " + i + " to partition " + metadata.partition()
                        + " with offset " + metadata.offset());

                Thread.sleep(2000);
            } catch(InterruptedException | ExecutionException | TimeoutException | UnknownHostException e) {
                e.printStackTrace();
            }
        }
        producer.flush();
        producer.close();
    }
}

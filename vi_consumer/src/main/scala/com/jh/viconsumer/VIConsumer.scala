package com.jh.viconsumer

import java.time.Duration
import java.util
import java.util.Properties

import org.apache.kafka.clients.consumer.{ConsumerRecord, KafkaConsumer}
import org.apache.kafka.common.serialization.StringDeserializer

import scala.collection.JavaConverters._

object VIConsumer {

  def main(args: Array[String]): Unit = {

    val properties = new Properties()
    properties.put("bootstrap.servers", "172.17.0.1:19092,172.17.0.1:29092,172.17.0.1:39092")
    properties.put("group.id", "consumer-tutorial")
    properties.put("key.deserializer", classOf[StringDeserializer])
    properties.put("value.deserializer", classOf[StringDeserializer])

    val kafkaConsumer = new KafkaConsumer[String, String](properties)
    kafkaConsumer.subscribe(util.Arrays.asList("memory"))

    while (true) {
      val results = kafkaConsumer.poll(Duration.ofSeconds(2)).asScala
      for (record: ConsumerRecord[String, String] <- results) {
        println(s"Topic: ${record.key()}, data: ${record.value()}")
      }
    }
  }
}

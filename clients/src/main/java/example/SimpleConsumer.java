package example;

import java.io.*;
import java.time.Duration;
import java.util.*;
import org.apache.kafka.clients.consumer.*;
import org.apache.kafka.common.serialization.StringDeserializer;

public class SimpleConsumer {

  public static void main(final String[] args) throws IOException {
    // Determine the config file path
    String configFile = args.length > 0 ? args[0] : "client.properties";
    File file = new File(configFile);
    if (!file.exists()) {
      System.out.printf("Config file '%s' not found. Using default 'client.properties'.%n", configFile);
      configFile = "client.properties";
    }

    // Load Kafka config
    Properties props = readConfig(configFile);

    // Read Group ID name form Environment
    String groupIdName = System.getenv("GROUP_ID_NAME");
    // Using PLAINTEXT for local Kafka
    System.out.println("Using PLAINTEXT (local Kafka)...");
    props.put("security.protocol", "PLAINTEXT");

    props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getCanonicalName());
    props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getCanonicalName());
    props.put(ConsumerConfig.GROUP_ID_CONFIG, groupIdName);

    final String topic = System.getenv("TOPIC_NAME");

    try (final KafkaConsumer<String, String> consumer = new KafkaConsumer<String, String>(props)) {
      consumer.subscribe(Arrays.asList(topic));
      System.out.println("Consuming from topic " + topic);

      while (true) {
        ConsumerRecords<String, String> records = consumer.poll(Duration.ofMillis(100));
        for (ConsumerRecord<String, String> record : records) {
          System.out.printf(
              "Consumed event from topic %s: key = %-10s value = %s%n",
              topic, record.key(), record.value());
        }
      }
    }
  }

  public static Properties readConfig(final String configFile) throws IOException {
    if (!new File(configFile).exists()) {
      throw new IOException(configFile + " not found.");
    }
    Properties config = new Properties();
    try (InputStream inputStream = new FileInputStream(configFile)) {
      config.load(inputStream);
    }
    return config;
  }
}

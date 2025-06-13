package example;

import com.github.javafaker.Faker;
import java.io.*;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.ExecutionException;

import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.clients.producer.*;
// import org.apache.kafka.common.serialization.StringSerializer;

public class SimpleProducer {
  public static void main(final String[] args) throws Exception {
    // Determine the config file path
    String configFile = args.length > 0 ? args[0] : "client.properties";
    File file = new File(configFile);
    if (!file.exists()) {
      System.out.printf("Config file '%s' not found. Using default 'client.properties'.%n", configFile);
      configFile = "client.properties";
    }

    // Load Kafka config
    Properties props = readConfig(configFile);

    System.out.println("Using PLAINTEXT (local Kafka)...");
    props.put("security.protocol", "PLAINTEXT");
    // props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getCanonicalName());
    // props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getCanonicalName());

    final String topic = "test-topic";

    ensureTopicExists(topic, props);

    Faker faker = new Faker();

    while (true) {
      try (final Producer<String, String> producer = new KafkaProducer<>(props)) {
        int numMessages = 10;
        for (int i = 0; i < numMessages; i++) {
          // Generate fake financial transaction data
          String transactionId = UUID.randomUUID().toString().substring(0, 10);
          String accountNumber = faker.finance().iban();
          double amount = faker.number().randomDouble(2, 10, 10000);
          String currency = faker.currency().code();
          String timestamp = Instant.now().toString();
          String transactionType =
              faker.options().option("payment", "refund", "withdrawal", "deposit");
          String status = faker.options().option("success", "failed", "pending");

          String message =
              String.format(
                  "{\"account_number\":\"%s\", \"amount\": %.2f,"
                      + " \"currency\":\"%s\", \"timestamp\":\"%s\", \"transaction_type\":\"%s\","
                      + " \"status\": \"%s\"}",
                  accountNumber, amount, currency, timestamp, transactionType, status);

          // Send message to Kafka
          producer.send(
              new ProducerRecord<>(topic, transactionId, message),
              (event, ex) -> {
                if (ex != null) {
                  ex.printStackTrace();
                } else {
                  System.out.printf(
                      "Produced event to topic %s: key = 'transactionId: %s', value = %s%n",
                      topic, transactionId, message);
                }
              });
        }
        System.out.printf("%d events were produced to topic %s%n", numMessages, topic);
        producer.flush();
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

    private static void ensureTopicExists(String topic, Properties props) throws Exception {
    // Create AdminClient with the same properties
    try (AdminClient adminClient = AdminClient.create(props)) {
      // Check if the topic exists
      Set<String> topics = adminClient.listTopics().names().get();
      if (!topics.contains(topic)) {
        System.out.printf("Topic '%s' does not exist. Creating it...%n", topic);

        // Define topic configuration
        NewTopic newTopic = new NewTopic(topic, 3, (short) 3);
        adminClient.createTopics(Collections.singleton(newTopic)).all().get();

        System.out.printf("Topic '%s' created successfully.%n", topic);
      } else {
        System.out.printf("Topic '%s' already exists.%n", topic);
      }
    } catch (ExecutionException | InterruptedException e) {
      throw new RuntimeException("Failed to check or create topic: " + topic, e);
    }
  }

}

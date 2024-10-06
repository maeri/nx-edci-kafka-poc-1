package eu.ec.empl.edci.async.poc.service;

import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.admin.AdminClientConfig;
import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.core.KafkaAdmin;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.kafka.KafkaContainer;
import org.testcontainers.utility.DockerImageName;

import java.time.Duration;
import java.util.*;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Testcontainers
@Slf4j
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class KafkaRequestReplyTest {

  @Container
  static KafkaContainer kafkaContainer = new KafkaContainer(DockerImageName.parse("apache/kafka:latest"));

  @Autowired
  private KafkaAdmin kafkaAdmin;

  @Autowired
  private ServiceOrchestrator orchestrator;

  @DynamicPropertySource
  static void kafkaProperties(DynamicPropertyRegistry registry) {
    registry.add("spring.kafka.bootstrap-servers", kafkaContainer::getBootstrapServers);
  }

  @Test
  @Order(1)
  void kafkaContainerIsRunning() {
    assertTrue(kafkaContainer.isRunning());
  }

  @Test
  @Order(2)
  void canGetBootstrapServers() {
    String bootstrapServers = kafkaContainer.getBootstrapServers();
    assertNotNull(bootstrapServers);
    log.debug("Bootstrap servers: {}", bootstrapServers);
    assertFalse(bootstrapServers.isEmpty());
  }

  @Test
  @Order(3)
  void canCreateTopic() throws Exception {
    String topicName = "test-topic";
    kafkaAdmin.createOrModifyTopics(new NewTopic(topicName, 1, (short) 1));

    try (
        AdminClient adminClient = AdminClient.create
            (Map.of(AdminClientConfig.BOOTSTRAP_SERVERS_CONFIG, kafkaContainer.getBootstrapServers())) ) {

      Set<String> topics = adminClient.listTopics().names().get();
      assertTrue(topics.contains(topicName));

    }
  }

  @Test
  @Order(4)
  void canProduceAndConsumeMessages() throws Exception {
    //given
    String topicName = "test-topic";
    String testMessage = "Hello, Kafka!";

    Properties props = new Properties();
    props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, kafkaContainer.getBootstrapServers());
    props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
    props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());

    //when
    try (KafkaProducer<String, String> producer = new KafkaProducer<>(props)) {
      producer.send(new ProducerRecord<>(topicName, testMessage)).get();
    }

    props.put(ConsumerConfig.GROUP_ID_CONFIG, "test-group");
    props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
    props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
    props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());

    try (KafkaConsumer<String, String> consumer = new KafkaConsumer<>(props)) {
      consumer.subscribe(Collections.singletonList(topicName));
      //then
      ConsumerRecords<String, String> records = consumer.poll(Duration.ofSeconds(10));
      assertFalse(records.isEmpty());
      assertEquals(testMessage, records.iterator().next().value());
    }
  }

  @Test
  @Order(5)
  void testRequestReply() throws Exception {
    //given
    String request = "Hello, Kafka!";
    String correlationId = UUID.randomUUID().toString();

    //when
    String response = orchestrator.callServiceBAndWaitForResponse(request, correlationId);

    //then
    assertThat(response).startsWith("Processed: " + request);
    assertThat(response).contains("Correlation ID: " + correlationId);
  }

}

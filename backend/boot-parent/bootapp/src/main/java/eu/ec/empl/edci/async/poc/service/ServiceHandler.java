package eu.ec.empl.edci.async.poc.service;

import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.header.internals.RecordHeader;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class ServiceHandler {

  private final String replyTopic;
  private final KafkaTemplate<String, String> kafkaTemplate;

  public ServiceHandler(@Value("${spring.kafka.reply-topic}") String replyTopic, KafkaTemplate<String, String> kafkaTemplate) {
    this.replyTopic = replyTopic;
    this.kafkaTemplate = kafkaTemplate;
  }

  @KafkaListener(topics = "${spring.kafka.request-topic}")
  public void handleRequest(ConsumerRecord<String, String> record) {
    String correlationId = new String(record.headers().lastHeader("kafka_correlationId").value());
    String response = processRequest(record.value(), correlationId);

    ProducerRecord<String, String> replyRecord = new ProducerRecord<>(replyTopic, response);
    replyRecord.headers().add(new RecordHeader("kafka_correlationId", correlationId.getBytes()));

    kafkaTemplate.send(replyRecord);
  }

  private String processRequest(String request, String correlationId) {
    log.info("Service Handler processed: {} , Correlation ID: {}", request, correlationId);
    return String.format("Processed: %s , Correlation ID: %s", request, correlationId);
  }

}

package eu.ec.empl.edci.async.poc.service;

import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.header.internals.RecordHeader;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.requestreply.ReplyingKafkaTemplate;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

@Service
@Slf4j
public class ServiceRequester {

  private final String requestTopic;
  private final ReplyingKafkaTemplate<String, String, String> replyingKafkaTemplate;
  private final Map<String, CompletableFuture<String>> pendingRequests = new ConcurrentHashMap<>();

  public ServiceRequester(@Value("${spring.kafka.request-topic}") String requestTopic,
                          ReplyingKafkaTemplate<String, String, String> replyingKafkaTemplate) {
    this.requestTopic = requestTopic;
    this.replyingKafkaTemplate = replyingKafkaTemplate;
  }

  public void sendRequest(String message, String correlationId) {
    log.info("Processed: {} , Correlation ID: {}", message, correlationId);
    ProducerRecord<String, String> record = new ProducerRecord<>(requestTopic, message);
    record.headers().add(new RecordHeader("kafka_correlationId", correlationId.getBytes()));
    replyingKafkaTemplate.send(record);
  }

  public CompletableFuture<String> waitForResponse(String correlationId) {
    CompletableFuture<String> future = new CompletableFuture<>();
    pendingRequests.put(correlationId, future);
    return future;
  }

  @KafkaListener(topics = "${spring.kafka.reply-topic}")
  public void handleResponse(ConsumerRecord<String, String> record) {
    String correlationId = new String(record.headers().lastHeader("kafka_correlationId").value());
    CompletableFuture<String> future = pendingRequests.remove(correlationId);
    if (future != null) {
      future.complete(record.value());
    }
  }

}

package eu.ec.empl.edci.async.poc;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class KafkaRequestReplyApplication {
  public static void main(String[] args) {
    SpringApplication.run(KafkaRequestReplyApplication.class, args);
  }
}

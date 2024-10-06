package eu.ec.empl.edci.async.poc.configuration;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Setter
@Getter
@Configuration
@ConfigurationProperties(prefix = "spring.kafka")
public class KafkaProperties {

  private String bootstrapServers;
  private String requestTopic;
  private String replyTopic;

}

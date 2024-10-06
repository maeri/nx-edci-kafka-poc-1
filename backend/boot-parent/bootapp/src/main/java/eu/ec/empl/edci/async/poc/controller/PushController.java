package eu.ec.empl.edci.async.poc.controller;

import eu.ec.empl.edci.async.poc.lib.LibService;
import eu.ec.empl.edci.async.poc.service.ServiceOrchestrator;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@AllArgsConstructor
@Slf4j
public class PushController {

  private static final String WELCOME_TEMPLATE = "Welcome, %s, from '%s'!";

  private final LibService libService;
  private final ServiceOrchestrator serviceOrchestrator;

  @PostMapping("/api")
  public String startProcessing(@RequestBody UserRequestDTO user) throws Exception {
    log.info("PushController : received request: {}", user);
    String correlationId = UUID.randomUUID().toString();
    return serviceOrchestrator.callServiceBAndWaitForResponse(String.format(WELCOME_TEMPLATE, user.getUser(), libService.message()), correlationId);
  }
}

package eu.ec.empl.edci.async.poc.controller;

import eu.ec.empl.edci.async.poc.lib.LibService;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController("/api")
@AllArgsConstructor
public class WelcomeController {

  private static final String WELCOME_TEMPLATE = "Welcome, %s, from '%s'!";

  private final LibService libService;

  @GetMapping("/welcome")
  public WelcomeMessage welcome(@RequestParam(value = "user", defaultValue = "School Nx Attendee") String user) {
    return new WelcomeMessage(user, String.format(WELCOME_TEMPLATE, user, libService.message()));
  }

  public record WelcomeMessage(String user, String message) {
  }
}

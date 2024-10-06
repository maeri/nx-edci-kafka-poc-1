package eu.ec.empl.edci.async.poc.controller;

import eu.ec.empl.edci.async.poc.lib.LibService;
import eu.ec.empl.edci.async.poc.service.ServiceOrchestrator;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


/**
 * curl -X POST http://localhost:8080/api \
 *   -H "Content-Type: application/json" \
 *   -d '{"user": "Test User"}'
 */

@ExtendWith(SpringExtension.class)
@SpringBootTest
@AutoConfigureMockMvc
class PushControllerTest {

  @Autowired
  private MockMvc mockMvc;

  @MockBean
  private LibService libService;

  @MockBean
  private ServiceOrchestrator serviceOrchestrator;

  @Test
  void startProcessing() throws Exception {
    // Given
    UserRequestDTO userRequest = new UserRequestDTO("test");
    String message = String.format("Welcome, %s, from 'Test Message'!", userRequest.getUser());
    String response = "Processed: " + message + ", Correlation ID: test-correlation-id";

    Mockito.when(libService.message()).thenReturn("Test Message");
    Mockito.when(serviceOrchestrator.callServiceBAndWaitForResponse(Mockito.anyString(), Mockito.anyString()))
      .thenReturn(response);

    // When & Then
    mockMvc.perform(post("/api")
        .contentType(MediaType.APPLICATION_JSON)
        .content("{\"user\":\"test\"}"))
      .andExpect(status().isOk())
      .andExpect(content().string(response));
  }
}

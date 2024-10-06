package eu.ec.empl.edci.async.poc.service;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.common.errors.TimeoutException;
import org.springframework.stereotype.Service;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@AllArgsConstructor
public class ServiceOrchestrator {

    private final ServiceRequester requester;

    public String callServiceBAndWaitForResponse(String message, String correlationId) throws Exception {
        log.info("Sending request with correlationId: {}", correlationId);

        CompletableFuture<String> responseFuture = requester.waitForResponse(correlationId);

        try {
            requester.sendRequest(message, correlationId);
            String response = responseFuture.get(3, TimeUnit.SECONDS);
            log.info("Received response for correlationId: {}", correlationId);
            return response;
        } catch (TimeoutException e) {
            log.error("Timeout waiting for response. CorrelationId: {}", correlationId);
            throw new RuntimeException("Timeout waiting for response", e);
        } catch (Exception e) {
            log.error("Error processing request-reply. CorrelationId: {}", correlationId, e);
            throw new RuntimeException("Error processing request-reply", e);
        }
    }
}

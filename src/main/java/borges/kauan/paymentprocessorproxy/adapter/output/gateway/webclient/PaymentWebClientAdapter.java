package borges.kauan.paymentprocessorproxy.adapter.output.gateway.webclient;

import borges.kauan.paymentprocessorproxy.adapter.output.gateway.PaymentRestClient;
import borges.kauan.paymentprocessorproxy.adapter.output.gateway.dto.HealthResponse;
import borges.kauan.paymentprocessorproxy.domain.payment.dto.PaymentRequest;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.web.reactive.function.client.WebClient;

public class PaymentWebClientAdapter implements PaymentRestClient {

    private final WebClient webClient;

    public PaymentWebClientAdapter(WebClient webClient) {
        this.webClient = webClient;
    }

    @Override
    public void processPayment(PaymentRequest paymentRequest) {
        String serializedValue = getSerializedValue(paymentRequest);

        webClient.post()
                .uri("/payments")
                .bodyValue(serializedValue)
                .retrieve()
                .bodyToMono(Void.class)
                .block();
    }

    @Override
    public HealthResponse getServiceHealthFallback() {
        return webClient.get()
                .uri("/payments/service-health")
                .retrieve()
                .bodyToMono(HealthResponse.class)
                .block();
    }

    private static String getSerializedValue(PaymentRequest paymentRequest) {
        String serializedValue;
        try {
            serializedValue = new ObjectMapper()
                    .registerModule(new JavaTimeModule())
                    .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
                    .writeValueAsString(paymentRequest);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
        return serializedValue;
    }
}

package borges.kauan.paymentprocessorproxy.adapter.output.gateway.webclient;

import borges.kauan.paymentprocessorproxy.adapter.output.gateway.PaymentRestClient;
import borges.kauan.paymentprocessorproxy.adapter.output.gateway.dto.HealthResponse;
import borges.kauan.paymentprocessorproxy.domain.payment.dto.PaymentRequest;
import org.springframework.web.reactive.function.client.WebClient;

import java.math.BigDecimal;
import java.time.Instant;

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
        StringBuilder sb = new StringBuilder();
        sb.append("{");

        sb.append("\"correlationId\":\"").append(paymentRequest.getCorrelationId()).append("\",");

        BigDecimal amount = paymentRequest.getAmount();
        sb.append("\"amount\":").append(amount != null ? amount.toPlainString() : "null").append(",");

        Instant requestedAt = paymentRequest.getRequestedAt();
        sb.append("\"requestedAt\":").append(requestedAt != null ? "\"" + requestedAt + "\"" : "null");

        sb.append("}");

        return sb.toString();
    }

}

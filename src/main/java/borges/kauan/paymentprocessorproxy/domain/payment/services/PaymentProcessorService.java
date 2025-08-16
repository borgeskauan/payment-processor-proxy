package borges.kauan.paymentprocessorproxy.domain.payment.services;

import borges.kauan.paymentprocessorproxy.domain.infra.MetricsRegister;
import borges.kauan.paymentprocessorproxy.domain.payment.dto.PaymentRequest;
import borges.kauan.paymentprocessorproxy.domain.payment.dto.ProcessedPaymentsSummaryResponse;
import borges.kauan.paymentprocessorproxy.domain.payment.entity.Payment;
import borges.kauan.paymentprocessorproxy.port.input.PaymentProcessorUseCase;
import borges.kauan.paymentprocessorproxy.port.output.PaymentGatewayPort;
import borges.kauan.paymentprocessorproxy.port.output.PaymentRepositoryPort;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

@Slf4j
@Service
public class PaymentProcessorService implements PaymentProcessorUseCase {

    private final PaymentGatewayPort paymentGatewayPort;
    private final PaymentRepositoryPort paymentRepositoryPort;

    private final MetricsRegister metricsRegister;

    private final ObjectMapper objectMapper;

    public PaymentProcessorService(PaymentGatewayPort paymentGatewayPort, PaymentRepositoryPort paymentRepositoryPort, MetricsRegister metricsRegister, ObjectMapper objectMapper) {
        this.paymentGatewayPort = paymentGatewayPort;
        this.paymentRepositoryPort = paymentRepositoryPort;
        this.metricsRegister = metricsRegister;
        this.objectMapper = objectMapper;
    }

    @Override
    public void processPayment(PaymentRequest paymentRequest) {
        Instant truncatedTimestamp = getTruncatedTimestamp(paymentRequest);
        var requestWithTimestamp = paymentRequest.withRequestedAt(truncatedTimestamp);

        var payment = Payment.builder()
                .correlationId(paymentRequest.getCorrelationId())
                .amount(paymentRequest.getAmount())
                .timestamp(truncatedTimestamp)
                .build();

        try {
            String processedBy = paymentGatewayPort.processPayment(requestWithTimestamp);
            paymentRepositoryPort.savePayment(
                    payment.withProcessedBy(processedBy)
            );

            metricsRegister.countPaymentProcessedSuccessfully();

        } catch (Exception e) {
            log.error("Error processing payment with correlationId '{}': {}", paymentRequest.getCorrelationId(), e.getMessage());

            metricsRegister.countPaymentDropped();

            throw new RuntimeException("Error processing payment with correlationId " + paymentRequest.getCorrelationId(), e);
        }
    }

    @Override
    public Mono<ProcessedPaymentsSummaryResponse> getPaymentsSummary(Instant from, Instant to) {
        return paymentRepositoryPort.getPaymentsSummary(from, to);
    }

    @Override
    public Mono<ProcessedPaymentsSummaryResponse> getStandalonePaymentsSummary(Instant from, Instant to) {
        return paymentRepositoryPort.getStandalonePaymentsSummary(from, to);
    }

    @Override
    public Mono<Void> purgePayments() {
        return paymentRepositoryPort.purgePayments();
    }

    @Override
    public void purgeStandalonePayments() {
        paymentRepositoryPort.purgeStandalonePayments();
    }

    @Override
    public void processPaymentRaw(String body) {
        try {
            var node = objectMapper.readTree(body);
            var paymentRequest = new PaymentRequest(
                    node.get("correlationId").asText(),
                    node.get("amount").decimalValue()
            );

            processPayment(paymentRequest);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to parse PaymentRequest", e);
        }
    }

    private static Instant getTruncatedTimestamp(PaymentRequest paymentRequest) {
        if (paymentRequest.getRequestedAt() == null) {
            return Instant.now().truncatedTo(ChronoUnit.MICROS);
        }

        return paymentRequest.getRequestedAt().truncatedTo(ChronoUnit.MICROS);
    }
}

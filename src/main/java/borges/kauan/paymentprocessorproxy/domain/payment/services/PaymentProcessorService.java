package borges.kauan.paymentprocessorproxy.domain.payment.services;

import borges.kauan.paymentprocessorproxy.domain.infra.MetricsRegister;
import borges.kauan.paymentprocessorproxy.domain.payment.dto.PaymentRequest;
import borges.kauan.paymentprocessorproxy.domain.payment.dto.ProcessedPaymentsSummaryResponse;
import borges.kauan.paymentprocessorproxy.domain.payment.entity.Payment;
import borges.kauan.paymentprocessorproxy.port.input.PaymentProcessorUseCase;
import borges.kauan.paymentprocessorproxy.port.output.PaymentGatewayPort;
import borges.kauan.paymentprocessorproxy.port.output.PaymentRepositoryPort;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

@Slf4j
@Service
public class PaymentProcessorService implements PaymentProcessorUseCase {

    private final PaymentGatewayPort paymentGatewayPort;
    private final PaymentRepositoryPort paymentRepositoryPort;

    private final MetricsRegister metricsRegister;

    public PaymentProcessorService(PaymentGatewayPort paymentGatewayPort, PaymentRepositoryPort paymentRepositoryPort, MetricsRegister metricsRegister) {
        this.paymentGatewayPort = paymentGatewayPort;
        this.paymentRepositoryPort = paymentRepositoryPort;
        this.metricsRegister = metricsRegister;
    }

    @Override
    public void processPayment(PaymentRequest paymentRequest) {
        Instant timestamp = Instant.now().truncatedTo(ChronoUnit.MILLIS);
        var requestWithTimestamp = paymentRequest.withRequestedAt(timestamp);

        var payment = Payment.builder()
                .correlationId(paymentRequest.getCorrelationId())
                .amount(paymentRequest.getAmount())
                .timestamp(timestamp)
                .build();

        String processedBy = paymentGatewayPort.processPayment(requestWithTimestamp);
        paymentRepositoryPort.savePayment(
                payment.withProcessedBy(processedBy)
        );

        metricsRegister.countPaymentProcessedSuccessfully();
    }

    @Override
    public ProcessedPaymentsSummaryResponse getPaymentsSummary(Instant from, Instant to) {
        return paymentRepositoryPort.getPaymentsSummary(from, to);
    }

    @Override
    public void purgePayments() {
        paymentRepositoryPort.purgePayments();
    }
}

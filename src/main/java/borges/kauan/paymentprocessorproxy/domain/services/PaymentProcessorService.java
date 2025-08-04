package borges.kauan.paymentprocessorproxy.domain.services;

import borges.kauan.paymentprocessorproxy.domain.dto.PaymentRequest;
import borges.kauan.paymentprocessorproxy.domain.dto.ProcessedPaymentsSummaryResponse;
import borges.kauan.paymentprocessorproxy.domain.entity.Payment;
import borges.kauan.paymentprocessorproxy.port.input.PaymentProcessorUseCase;
import borges.kauan.paymentprocessorproxy.port.output.PaymentRepositoryPort;
import borges.kauan.paymentprocessorproxy.port.output.PaymentGatewayPort;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.UUID;

@Service
public class PaymentProcessorService implements PaymentProcessorUseCase {

    private final PaymentGatewayPort paymentGatewayPort;
    private final PaymentRepositoryPort paymentRepositoryPort;

    public PaymentProcessorService(PaymentGatewayPort paymentGatewayPort, PaymentRepositoryPort paymentRepositoryPort) {
        this.paymentGatewayPort = paymentGatewayPort;
        this.paymentRepositoryPort = paymentRepositoryPort;
    }

    @Override
    public void processPayment(PaymentRequest paymentRequest) {
        String processedBy = paymentGatewayPort.processPayment(paymentRequest);

        var payment = Payment.builder()
                .id(UUID.randomUUID().toString())
                .correlationId(paymentRequest.getCorrelationId())
                .amount(paymentRequest.getAmount())
                .processedBy(processedBy)
                .build();

        paymentRepositoryPort.savePayment(payment);
    }

    @Override
    public ProcessedPaymentsSummaryResponse getPaymentsSummary(Instant from, Instant to) {
        return paymentRepositoryPort.getPaymentsSummary(from, to);
    }
}

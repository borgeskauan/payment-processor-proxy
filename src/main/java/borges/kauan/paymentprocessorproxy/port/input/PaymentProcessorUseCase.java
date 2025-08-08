package borges.kauan.paymentprocessorproxy.port.input;

import borges.kauan.paymentprocessorproxy.domain.payment.dto.PaymentRequest;
import borges.kauan.paymentprocessorproxy.domain.payment.dto.ProcessedPaymentsSummaryResponse;

import java.time.Instant;

public interface PaymentProcessorUseCase {

    void processPayment(PaymentRequest paymentRequest);

    ProcessedPaymentsSummaryResponse getPaymentsSummary(Instant from, Instant to);

    void purgePayments();
}

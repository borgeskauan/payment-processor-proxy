package borges.kauan.paymentprocessorproxy.port.output;

import borges.kauan.paymentprocessorproxy.domain.payment.dto.ProcessedPaymentsSummaryResponse;
import borges.kauan.paymentprocessorproxy.domain.payment.entity.Payment;

import java.time.Instant;

public interface PaymentRepositoryPort {
    void savePayment(Payment paymentRequest);

    ProcessedPaymentsSummaryResponse getPaymentsSummary(Instant from, Instant to);

    void purgePayments();
}

package borges.kauan.paymentprocessorproxy.ports.input;

import borges.kauan.paymentprocessorproxy.domain.dto.PaymentRequest;
import borges.kauan.paymentprocessorproxy.domain.dto.ProcessedPaymentsSummaryResponse;

public interface PaymentProcessorUseCase {

    void processPayment(PaymentRequest paymentRequest);

    ProcessedPaymentsSummaryResponse getPaymentsSummary();
}

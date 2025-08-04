package borges.kauan.paymentprocessorproxy.domain.services;

import borges.kauan.paymentprocessorproxy.domain.dto.PaymentRequest;
import borges.kauan.paymentprocessorproxy.domain.dto.ProcessedPaymentsSummaryResponse;
import borges.kauan.paymentprocessorproxy.ports.input.PaymentProcessorUseCase;
import org.springframework.stereotype.Service;

@Service
public class PaymentProcessorService implements PaymentProcessorUseCase {
    @Override
    public void processPayment(PaymentRequest paymentRequest) {

    }

    @Override
    public ProcessedPaymentsSummaryResponse getPaymentsSummary() {
        return null;
    }
}

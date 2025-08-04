package borges.kauan.paymentprocessorproxy.adapter.input;

import borges.kauan.paymentprocessorproxy.domain.dto.PaymentRequest;
import borges.kauan.paymentprocessorproxy.domain.dto.ProcessedPaymentsSummaryResponse;
import borges.kauan.paymentprocessorproxy.ports.input.PaymentProcessorUseCase;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class PaymentProcessorController {

    private final PaymentProcessorUseCase paymentProcessorUseCase;

    public PaymentProcessorController(PaymentProcessorUseCase paymentProcessorUseCase) {
        this.paymentProcessorUseCase = paymentProcessorUseCase;
    }

    @PostMapping("/payments")
    public void processPayment(@RequestBody PaymentRequest paymentRequest) {
        paymentProcessorUseCase.processPayment(paymentRequest);
    }

    @GetMapping("/payments-summary")
    public ProcessedPaymentsSummaryResponse getPaymentsSummary() {
        return paymentProcessorUseCase.getPaymentsSummary();
    }
}

package borges.kauan.paymentprocessorproxy.adapter.input;

import borges.kauan.paymentprocessorproxy.domain.dto.PaymentRequest;
import borges.kauan.paymentprocessorproxy.domain.dto.ProcessedPaymentsSummaryResponse;
import borges.kauan.paymentprocessorproxy.port.input.PaymentProcessorUseCase;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;

@Slf4j
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
    public ProcessedPaymentsSummaryResponse getPaymentsSummary(@RequestParam(required = false) Instant from,
                                                               @RequestParam(required = false) Instant to) {
        log.info("Fetching payments summary from {} to {}", from, to);

        return paymentProcessorUseCase.getPaymentsSummary(from, to);
    }

    @PostMapping("/purge-payments")
    public void purgePayments() {
        log.info("Purging all payments");

        paymentProcessorUseCase.purgePayments();
    }
}

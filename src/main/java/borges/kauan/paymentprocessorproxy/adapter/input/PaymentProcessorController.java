package borges.kauan.paymentprocessorproxy.adapter.input;

import borges.kauan.paymentprocessorproxy.domain.infra.MetricsRegister;
import borges.kauan.paymentprocessorproxy.domain.payment.dto.PaymentRequest;
import borges.kauan.paymentprocessorproxy.domain.payment.dto.ProcessedPaymentsSummaryResponse;
import borges.kauan.paymentprocessorproxy.domain.infra.WorkService;
import borges.kauan.paymentprocessorproxy.port.input.PaymentProcessorUseCase;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;

@Slf4j
@RestController
public class PaymentProcessorController {

    private final MetricsRegister metricsRegister;
    private final PaymentProcessorUseCase paymentProcessorUseCase;
    private final WorkService workService;

    public PaymentProcessorController(MetricsRegister metricsRegister,
                                      PaymentProcessorUseCase paymentProcessorUseCase,
                                      WorkService workService) {
        this.metricsRegister = metricsRegister;
        this.paymentProcessorUseCase = paymentProcessorUseCase;
        this.workService = workService;
    }

    @PostMapping("/payments")
    public void processPayment(@RequestBody PaymentRequest paymentRequest) {
        workService.doWork(() -> {
            metricsRegister.recordProcessingStart(paymentRequest.getCorrelationId());
            paymentProcessorUseCase.processPayment(paymentRequest);
        });

        metricsRegister.recordEnqueue(paymentRequest.getCorrelationId());
        metricsRegister.countPaymentReceived();
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

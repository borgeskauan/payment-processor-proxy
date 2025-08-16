package borges.kauan.paymentprocessorproxy.adapter.input;

import borges.kauan.paymentprocessorproxy.domain.infra.WorkService;
import borges.kauan.paymentprocessorproxy.domain.payment.dto.PaymentRequest;
import borges.kauan.paymentprocessorproxy.domain.payment.dto.ProcessedPaymentsSummaryResponse;
import borges.kauan.paymentprocessorproxy.port.input.PaymentProcessorUseCase;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.time.Instant;

@Slf4j
@RestController
public class PaymentProcessorController {

    private final PaymentProcessorUseCase paymentProcessorUseCase;
    private final WorkService workService;

    public PaymentProcessorController(PaymentProcessorUseCase paymentProcessorUseCase,
                                      WorkService workService) {
        this.paymentProcessorUseCase = paymentProcessorUseCase;
        this.workService = workService;
    }

    @PostMapping("/payments")
    public Mono<Void> processPayment(@RequestBody PaymentRequest paymentRequest) {
        var paymentRequestWithTimestamp = paymentRequest.withRequestedAt(Instant.now());

        workService.doWork(() -> paymentProcessorUseCase.processPayment(paymentRequestWithTimestamp));

        return Mono.empty();
    }

    @GetMapping("/payments-summary")
    public Mono<ProcessedPaymentsSummaryResponse> getPaymentsSummary(@RequestParam(required = false) Instant from,
                                                               @RequestParam(required = false) Instant to) {
        log.info("Fetching payments summary from {} to {}", from, to);

        return paymentProcessorUseCase.getPaymentsSummary(from, to);
    }

    @GetMapping("/payments-summary/standalone")
    public Mono<ProcessedPaymentsSummaryResponse> getStandalonePaymentsSummary(@RequestParam(required = false) Instant from,
                                                                         @RequestParam(required = false) Instant to) {
        log.info("Fetching standalone payments summary from {} to {}", from, to);

        return paymentProcessorUseCase.getStandalonePaymentsSummary(from, to);
    }

    @PostMapping("/purge-payments")
    public Mono<Void> purgePayments() {
        log.info("Purging all payments");

        return paymentProcessorUseCase.purgePayments();
    }

    @PostMapping("purge-payments/standalone")
    public void purgeStandalonePayments() {
        log.info("Purging standalone payments");
        paymentProcessorUseCase.purgeStandalonePayments();
    }
}

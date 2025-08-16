package borges.kauan.paymentprocessorproxy.port.input;

import borges.kauan.paymentprocessorproxy.domain.payment.dto.PaymentRequest;
import borges.kauan.paymentprocessorproxy.domain.payment.dto.ProcessedPaymentsSummaryResponse;
import reactor.core.publisher.Mono;

import java.time.Instant;

public interface PaymentProcessorUseCase {

    void processPayment(PaymentRequest paymentRequest);

    Mono<ProcessedPaymentsSummaryResponse> getPaymentsSummary(Instant from, Instant to);

    Mono<ProcessedPaymentsSummaryResponse> getStandalonePaymentsSummary(Instant from, Instant to);

    Mono<Void> purgePayments();

    void purgeStandalonePayments();

    void processPaymentRaw(String body);
}

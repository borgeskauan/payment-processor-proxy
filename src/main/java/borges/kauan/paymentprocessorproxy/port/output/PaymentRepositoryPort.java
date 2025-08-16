package borges.kauan.paymentprocessorproxy.port.output;

import borges.kauan.paymentprocessorproxy.domain.payment.dto.ProcessedPaymentsSummaryResponse;
import borges.kauan.paymentprocessorproxy.domain.payment.entity.Payment;
import reactor.core.publisher.Mono;

import java.time.Instant;

public interface PaymentRepositoryPort {
    void savePayment(Payment paymentRequest);

    Mono<ProcessedPaymentsSummaryResponse> getPaymentsSummary(Instant from, Instant to);

    Mono<ProcessedPaymentsSummaryResponse> getStandalonePaymentsSummary(Instant from, Instant to);

    Mono<Void> purgePayments();

    void purgeStandalonePayments();
}

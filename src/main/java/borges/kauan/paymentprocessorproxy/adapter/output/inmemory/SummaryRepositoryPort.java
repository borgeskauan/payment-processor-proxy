package borges.kauan.paymentprocessorproxy.adapter.output.inmemory;

import borges.kauan.paymentprocessorproxy.domain.payment.dto.ProcessedPaymentsSummaryResponse;
import reactor.core.publisher.Mono;

import java.time.Instant;

public interface SummaryRepositoryPort {

    ProcessedPaymentsSummaryResponse getPaymentsSummary(Instant from, Instant to);

    Mono<Void> purgePayments();
}

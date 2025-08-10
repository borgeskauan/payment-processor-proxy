package borges.kauan.paymentprocessorproxy.adapter.output.database;

import borges.kauan.paymentprocessorproxy.domain.infra.MetricsRegister;
import borges.kauan.paymentprocessorproxy.domain.payment.dto.ProcessedPaymentsSummaryResponse;
import borges.kauan.paymentprocessorproxy.domain.payment.entity.Payment;
import borges.kauan.paymentprocessorproxy.domain.payment.entity.ProcessedPaymentsSummary;
import borges.kauan.paymentprocessorproxy.port.output.PaymentRepositoryPort;
import io.micrometer.core.instrument.Timer;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentSkipListMap;

@Repository
@Primary
public class InMemoryPaymentDatabaseAdapter implements PaymentRepositoryPort {

    private final Map<String, ConcurrentSkipListMap<Long, List<Payment>>> payments = new ConcurrentHashMap<>();
    private final Timer addPaymentTimer;
    private final Timer calculateSummaryTime;

    public InMemoryPaymentDatabaseAdapter(MetricsRegister metricsRegister) {
        this.addPaymentTimer = metricsRegister.createTimer("redis.payment.add.time");
        this.calculateSummaryTime = metricsRegister.createTimer("redis.payment.summary.calculate.time");
    }

    @Override
    public void savePayment(Payment payment) {
        addPaymentTimer.record(() -> {
            var map = payments.computeIfAbsent(payment.getProcessedBy(), k -> new ConcurrentSkipListMap<>());

            // Use the timestamp as the key, and store a list of payments for that timestamp
            map.compute(payment.getTimestamp().toEpochMilli(), (ts, list) -> {
                if (list == null) {
                    list = new ArrayList<>(); // unsynchronized for max write speed
                }
                list.add(payment);
                return list;
            });
        });
    }

    @Override
    public ProcessedPaymentsSummaryResponse getPaymentsSummary(Instant from, Instant to) {
        return calculateSummaryTime.record(() -> {
            var defaultSummary = buildSummary(filterPayments(from, to, "default"));
            var fallbackSummary = buildSummary(filterPayments(from, to, "fallback"));

            return ProcessedPaymentsSummaryResponse.builder()
                    .defaultSummary(defaultSummary)
                    .fallback(fallbackSummary)
                    .build();
        });
    }

    @Override
    public void purgePayments() {
        payments.clear();
    }

    private FilteredCalculationResult filterPayments(Instant from, Instant to, String processedBy) {
        var map = payments.get(processedBy);
        if (map == null) return new FilteredCalculationResult(0L, BigDecimal.ZERO);

        long fromMillis = (from == null) ? Long.MIN_VALUE : from.toEpochMilli();
        long toMillis   = (to == null)   ? Long.MAX_VALUE : to.toEpochMilli();

        return map.subMap(fromMillis, true, toMillis, true)
                .values()
                .stream()
                .flatMap(List::stream)
                .map(Payment::getAmount)
                .reduce(new FilteredCalculationResult(0L, BigDecimal.ZERO),
                        (acc, amt) -> new FilteredCalculationResult(acc.size() + 1, acc.totalAmount().add(amt)),
                        (a, b) -> new FilteredCalculationResult(a.size() + b.size(), a.totalAmount().add(b.totalAmount())));
    }

    private ProcessedPaymentsSummary buildSummary(FilteredCalculationResult result) {
        return ProcessedPaymentsSummary.builder()
                .totalAmount(result.totalAmount())
                .totalRequests(result.size())
                .build();
    }

    private record FilteredCalculationResult(Long size, BigDecimal totalAmount) {}
}

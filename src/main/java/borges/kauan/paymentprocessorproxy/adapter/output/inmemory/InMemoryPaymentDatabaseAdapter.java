package borges.kauan.paymentprocessorproxy.adapter.output.inmemory;

import borges.kauan.paymentprocessorproxy.domain.infra.MetricsRegister;
import borges.kauan.paymentprocessorproxy.domain.payment.dto.ProcessedPaymentsSummaryResponse;
import borges.kauan.paymentprocessorproxy.domain.payment.entity.Payment;
import borges.kauan.paymentprocessorproxy.domain.payment.entity.ProcessedPaymentsSummary;
import borges.kauan.paymentprocessorproxy.port.output.PaymentRepositoryPort;
import io.micrometer.core.instrument.Timer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentSkipListMap;

@Slf4j
@Repository
@Primary
public class InMemoryPaymentDatabaseAdapter implements PaymentRepositoryPort {

    private final SummaryRepositoryPort summaryRepositoryPort;

    private final Map<String, ConcurrentSkipListMap<Long, List<Payment>>> payments = new ConcurrentHashMap<>();
    private final Timer addPaymentTimer;
    private final Timer calculateSummaryTime;

    public InMemoryPaymentDatabaseAdapter(SummaryRepositoryPort summaryRepositoryPort, MetricsRegister metricsRegister) {
        this.summaryRepositoryPort = summaryRepositoryPort;
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
                    list = Collections.synchronizedList(new ArrayList<>()); // unsynchronized for max write speed
                }

                list.add(payment);
                return list;
            });
        });
    }

    @Override
    public Mono<ProcessedPaymentsSummaryResponse> getPaymentsSummary(Instant from, Instant to) {
        return calculateSummaryTime.record(() -> getMergedSummary(from, to));
    }

    @Override
    public ProcessedPaymentsSummaryResponse getStandalonePaymentsSummary(Instant from, Instant to) {
        return calculateLocalSummary(from, to);
    }

    private Mono<ProcessedPaymentsSummaryResponse> getMergedSummary(Instant from, Instant to) {
        Mono<ProcessedPaymentsSummaryResponse> remoteMono = Mono.fromCallable(() ->
                callRemoteSummaryService(from, to)
        ).subscribeOn(Schedulers.boundedElastic());  // Offload blocking call

        Mono<ProcessedPaymentsSummaryResponse> localMono = Mono.fromCallable(() ->
                calculateLocalSummary(from, to)
        ).subscribeOn(Schedulers.boundedElastic());  // Offload blocking call if needed

        return Mono.zip(localMono, remoteMono, this::mergeSummaries);
    }

    // TODO: Convert return type to mono
    private ProcessedPaymentsSummaryResponse callRemoteSummaryService(Instant from, Instant to) {
        return summaryRepositoryPort.getPaymentsSummary(from, to);
    }

    // TODO: Convert return type to mono
    private ProcessedPaymentsSummaryResponse calculateLocalSummary(Instant from, Instant to) {
        var defaultSummary = buildSummary(filterPayments(from, to, "default"));
        var fallbackSummary = buildSummary(filterPayments(from, to, "fallback"));

        return ProcessedPaymentsSummaryResponse.builder()
                .defaultSummary(defaultSummary)
                .fallback(fallbackSummary)
                .build();
    }

    private ProcessedPaymentsSummaryResponse mergeSummaries(ProcessedPaymentsSummaryResponse local, ProcessedPaymentsSummaryResponse remote) {
        var defaultSummary = mergeSummaries(local.getDefaultSummary(), remote.getDefaultSummary());
        var fallbackSummary = mergeSummaries(local.getFallback(), remote.getFallback());

        return ProcessedPaymentsSummaryResponse.builder()
                .defaultSummary(defaultSummary)
                .fallback(fallbackSummary)
                .build();
    }

    private static ProcessedPaymentsSummary mergeSummaries(ProcessedPaymentsSummary local, ProcessedPaymentsSummary remote) {
        return ProcessedPaymentsSummary.builder()
                .totalAmount(local.getTotalAmount().add(remote.getTotalAmount()))
                .totalRequests(local.getTotalRequests() + remote.getTotalRequests())
                .build();
    }

    @Override
    public Mono<Void> purgePayments() {
        payments.clear();

        return summaryRepositoryPort.purgePayments();
    }

    @Override
    public void purgeStandalonePayments() {
        payments.clear();
    }

    private FilteredCalculationResult filterPayments(Instant from, Instant to, String processedBy) {
        var map = payments.get(processedBy);
        if (map == null) return new FilteredCalculationResult(0L, BigDecimal.ZERO);

        long fromMillis = (from == null) ? Long.MIN_VALUE : from.toEpochMilli();
        long toMillis = (to == null) ? Long.MAX_VALUE : to.toEpochMilli();

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

    private record FilteredCalculationResult(Long size, BigDecimal totalAmount) {
    }
}

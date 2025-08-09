package borges.kauan.paymentprocessorproxy.adapter.output.database;

import borges.kauan.paymentprocessorproxy.domain.infra.MetricsRegister;
import borges.kauan.paymentprocessorproxy.domain.payment.dto.ProcessedPaymentsSummaryResponse;
import borges.kauan.paymentprocessorproxy.domain.payment.entity.Payment;
import borges.kauan.paymentprocessorproxy.domain.payment.entity.ProcessedPaymentsSummary;
import borges.kauan.paymentprocessorproxy.port.output.PaymentRepositoryPort;
import io.micrometer.core.instrument.Timer;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Objects;
import java.util.Set;

@Repository
public class PaymentDatabaseAdapter implements PaymentRepositoryPort {

    private final RedisTemplate<String, String> redisTemplate;
    private final Timer addPaymentTimer;

    private final Timer calculateSummaryTime;
    private final Timer queryPaymentsFromRedisTime;

    public PaymentDatabaseAdapter(RedisTemplate<String, String> redisTemplate, MetricsRegister metricsRegister) {
        this.redisTemplate = redisTemplate;
        this.addPaymentTimer = metricsRegister.createTimer("redis.payment.add.time");
        this.calculateSummaryTime = metricsRegister.createTimer("redis.payment.summary.calculate.time");
        this.queryPaymentsFromRedisTime = metricsRegister.createTimer("redis.payment.query.time");
    }

    @Override
    public void savePayment(Payment payment) {
        addPaymentTimer.record(() -> savePaymentInternal(payment));
    }

    private void savePaymentInternal(Payment payment) {
        redisTemplate.opsForZSet().add(
                "payments:timestamps:" + payment.getProcessedBy(),
                payment.getId() + ":" + payment.getAmount().toString(),
                payment.getTimestamp().toEpochMilli()
        );
    }

    @Override
    public ProcessedPaymentsSummaryResponse getPaymentsSummary(Instant from, Instant to) {
        return calculateSummaryTime.record(() -> getPaymentsSummaryInternal(from, to));
    }

    private ProcessedPaymentsSummaryResponse getPaymentsSummaryInternal(Instant from, Instant to) {
        var defaultSummary = buildSummary(fiterPayments(from, to, "default"));
        var fallbackSummary = buildSummary(fiterPayments(from, to, "fallback"));

        return ProcessedPaymentsSummaryResponse.builder()
                .defaultSummary(defaultSummary)
                .fallback(fallbackSummary)
                .build();
    }

    @Override
    public void purgePayments() {
        Objects.requireNonNull(redisTemplate.getConnectionFactory())
                .getConnection()
                .serverCommands()
                .flushDb();
    }

    private FilteredCalculationResult fiterPayments(Instant from, Instant to, String processedBy) {
        long fromMillis = from == null ? Long.MIN_VALUE : from.toEpochMilli();
        long toMillis = to == null ? Long.MAX_VALUE : to.toEpochMilli();

        return queryPaymentsFromRedisTime.record(() -> {
            Set<String> paymentAmounts = redisTemplate.opsForZSet().rangeByScore(
                    "payments:timestamps:" + processedBy, fromMillis, toMillis);

            if (paymentAmounts == null || paymentAmounts.isEmpty()) {
                return new FilteredCalculationResult(0L, BigDecimal.ZERO);
            }

            var sum = paymentAmounts.stream()
                    .map(value -> new BigDecimal(value.split(":")[1]))
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            return new FilteredCalculationResult((long) paymentAmounts.size(), sum);
        });
    }

    private ProcessedPaymentsSummary buildSummary(FilteredCalculationResult calculationResult) {
        return ProcessedPaymentsSummary.builder()
                .totalAmount(calculationResult.totalAmount())
                .totalRequests(calculationResult.size())
                .build();
    }

    private record FilteredCalculationResult(Long size, BigDecimal totalAmount) {
    }
}

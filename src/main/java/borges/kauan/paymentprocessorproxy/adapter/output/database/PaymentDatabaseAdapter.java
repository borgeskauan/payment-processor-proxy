package borges.kauan.paymentprocessorproxy.adapter.output.database;

import borges.kauan.paymentprocessorproxy.domain.dto.ProcessedPaymentsSummaryResponse;
import borges.kauan.paymentprocessorproxy.domain.entity.Payment;
import borges.kauan.paymentprocessorproxy.domain.entity.ProcessedPaymentsSummary;
import borges.kauan.paymentprocessorproxy.port.output.PaymentRepositoryPort;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Repository
public class PaymentDatabaseAdapter implements PaymentRepositoryPort {

    private final RedisTemplate<String, String> redisTemplate;

    public PaymentDatabaseAdapter(RedisTemplate<String, String> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    @Override
    public void savePayment(Payment payment) {
        String key = "payment:" + payment.getId();
        redisTemplate.opsForHash().put(key, "id", payment.getId());
        redisTemplate.opsForHash().put(key, "correlationId", payment.getCorrelationId());
        redisTemplate.opsForHash().put(key, "processedBy", payment.getProcessedBy());
        redisTemplate.opsForHash().put(key, "amount", payment.getAmount().toString());
        redisTemplate.opsForHash().put(key, "timestamp", payment.getTimestamp().toString());

        // Add to sorted set for time-based queries
        redisTemplate.opsForZSet().add("payments:timestamps", payment.getId(),
                payment.getTimestamp().toEpochMilli());
    }

    @Override
    public ProcessedPaymentsSummaryResponse getPaymentsSummary(Instant from, Instant to) {
        List<Payment> filteredPayments = filterPayments(from, to);

        Map<String, List<Payment>> groupedPayments = filteredPayments.stream()
                .collect(Collectors.groupingBy(Payment::getProcessedBy));

        var defaultSummary = buildSummary(groupedPayments.get("default"));
        var fallbackSummary = buildSummary(groupedPayments.get("fallback"));

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

    private List<Payment> filterPayments(Instant from, Instant to) {
        long fromMillis = from == null ? Long.MIN_VALUE : from.toEpochMilli();
        long toMillis = to == null ? Long.MAX_VALUE : to.toEpochMilli();

        Set<String> paymentIds = redisTemplate.opsForZSet().rangeByScore(
                "payments:timestamps", fromMillis, toMillis);

        if (paymentIds == null || paymentIds.isEmpty()) {
            return List.of();
        }

        return paymentIds.stream()
                .map(id -> {
                    Map<Object, Object> entry = redisTemplate.opsForHash().entries("payment:" + id);
                    return Payment.builder()
                            .id((String) entry.get("id"))
                            .correlationId((String) entry.get("correlationId"))
                            .processedBy((String) entry.get("processedBy"))
                            .amount(new BigDecimal((String) entry.get("amount")))
                            .timestamp(Instant.parse((String) entry.get("timestamp")))
                            .build();
                })
                .collect(Collectors.toList());
    }

    private ProcessedPaymentsSummary buildSummary(List<Payment> payments) {
        if (payments == null || payments.isEmpty()) {
            return ProcessedPaymentsSummary.builder()
                    .totalAmount(BigDecimal.valueOf(0.0))
                    .totalRequests(0L)
                    .build();
        }

        BigDecimal totalAmount = payments.stream()
                .map(Payment::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        long totalCount = payments.size();

        return ProcessedPaymentsSummary.builder()
                .totalAmount(totalAmount)
                .totalRequests(totalCount)
                .build();
    }
}

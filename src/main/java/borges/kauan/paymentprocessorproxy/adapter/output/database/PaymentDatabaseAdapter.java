package borges.kauan.paymentprocessorproxy.adapter.output.database;

import borges.kauan.paymentprocessorproxy.domain.dto.ProcessedPaymentsSummaryResponse;
import borges.kauan.paymentprocessorproxy.domain.entity.Payment;
import borges.kauan.paymentprocessorproxy.domain.entity.ProcessedPaymentsSummary;
import borges.kauan.paymentprocessorproxy.port.output.PaymentRepositoryPort;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Repository
public class PaymentDatabaseAdapter implements PaymentRepositoryPort {

    private final List<Payment> paymentRequests = new ArrayList<>();

    @Override
    public void savePayment(Payment paymentRequest) {
        paymentRequests.add(paymentRequest);
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

    private List<Payment> filterPayments(Instant from, Instant to) {
        return paymentRequests.stream()
                .filter(payment -> (from == null || payment.getTimestamp().isAfter(from)) &&
                        (to == null || payment.getTimestamp().isBefore(to)))
                .toList();
    }
}

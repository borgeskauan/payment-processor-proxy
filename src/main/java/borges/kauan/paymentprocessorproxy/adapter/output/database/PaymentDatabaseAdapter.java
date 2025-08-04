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

@Repository
public class PaymentDatabaseAdapter implements PaymentRepositoryPort {

    private final List<Payment> paymentRequests = new ArrayList<>();

    @Override
    public void savePayment(Payment paymentRequest) {
        paymentRequests.add(paymentRequest);
    }

    @Override
    public ProcessedPaymentsSummaryResponse getPaymentsSummary(Instant from, Instant to) {
        var sampleSummary = ProcessedPaymentsSummary.builder()
                .totalAmount(BigDecimal.valueOf(1000.0))
                .totalRequests(10L)
                .build();

        return ProcessedPaymentsSummaryResponse.builder()
                .defaultSummary(sampleSummary)
                .fallback(sampleSummary)
                .build();
    }
}

package borges.kauan.paymentprocessorproxy.domain.payment.entity;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class ProcessedPaymentsSummary {
    private Long totalRequests;
    private BigDecimal totalAmount;
}

package borges.kauan.paymentprocessorproxy.domain.payment.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ProcessedPaymentsSummary {
    private Long totalRequests;
    private BigDecimal totalAmount;
}

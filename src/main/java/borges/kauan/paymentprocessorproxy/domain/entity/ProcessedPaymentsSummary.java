package borges.kauan.paymentprocessorproxy.domain.entity;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class ProcessedPaymentsSummary {
    private Long totalRequests;
    private BigDecimal totalAmount;
}

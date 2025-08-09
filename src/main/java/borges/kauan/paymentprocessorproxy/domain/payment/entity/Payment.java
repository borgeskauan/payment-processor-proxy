package borges.kauan.paymentprocessorproxy.domain.payment.entity;

import lombok.Builder;
import lombok.Data;
import lombok.With;

import java.math.BigDecimal;
import java.time.Instant;

@Data
@With
@Builder
public class Payment {
    private String correlationId;
    private String processedBy;

    private BigDecimal amount;

    private Instant timestamp;
}

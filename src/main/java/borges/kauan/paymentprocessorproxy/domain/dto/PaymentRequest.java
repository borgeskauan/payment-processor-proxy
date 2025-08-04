package borges.kauan.paymentprocessorproxy.domain.dto;

import lombok.Builder;
import lombok.Data;
import lombok.With;

import java.math.BigDecimal;
import java.time.Instant;

@Data
@With
@Builder
public class PaymentRequest {
    private String correlationId;
    private BigDecimal amount;
    private Instant requestedAt;
}

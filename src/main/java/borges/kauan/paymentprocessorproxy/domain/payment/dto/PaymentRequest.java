package borges.kauan.paymentprocessorproxy.domain.payment.dto;

import lombok.*;

import java.math.BigDecimal;
import java.time.Instant;

@Data
@With
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentRequest {
    private String correlationId;
    private BigDecimal amount;
    private Instant requestedAt;

    public PaymentRequest(String correlationId, BigDecimal amount) {
        this.correlationId = correlationId;
        this.amount = amount;
    }
}

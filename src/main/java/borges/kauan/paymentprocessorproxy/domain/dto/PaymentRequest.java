package borges.kauan.paymentprocessorproxy.domain.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.util.UUID;

@Data
public class PaymentRequest {
    private UUID correlationId;
    private BigDecimal amount;
}

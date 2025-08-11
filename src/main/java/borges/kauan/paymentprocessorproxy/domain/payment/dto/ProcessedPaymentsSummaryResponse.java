package borges.kauan.paymentprocessorproxy.domain.payment.dto;

import borges.kauan.paymentprocessorproxy.domain.payment.entity.ProcessedPaymentsSummary;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ProcessedPaymentsSummaryResponse {

    @JsonProperty("default")
    private ProcessedPaymentsSummary defaultSummary;
    private ProcessedPaymentsSummary fallback;
}

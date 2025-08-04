package borges.kauan.paymentprocessorproxy.domain.dto;

import borges.kauan.paymentprocessorproxy.domain.entity.ProcessedPaymentsSummary;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class ProcessedPaymentsSummaryResponse {

    @JsonProperty("default")
    private ProcessedPaymentsSummary defaultSummary;
    private ProcessedPaymentsSummary fallback;
}

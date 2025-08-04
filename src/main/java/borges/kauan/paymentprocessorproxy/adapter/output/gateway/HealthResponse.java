package borges.kauan.paymentprocessorproxy.adapter.output.gateway;

import lombok.Data;

@Data
public class HealthResponse {
    private Boolean failing;
    private Long minResponseTime;
}

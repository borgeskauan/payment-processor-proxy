package borges.kauan.paymentprocessorproxy.adapter.output.gateway;

import borges.kauan.paymentprocessorproxy.adapter.output.gateway.dto.HealthResponse;
import borges.kauan.paymentprocessorproxy.domain.payment.dto.PaymentRequest;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;

public interface PaymentRestClient {

    @PostMapping("/payments")
    void processPayment(PaymentRequest paymentRequest);

    @GetMapping("/payments/service-health")
    HealthResponse getServiceHealthFallback();
}

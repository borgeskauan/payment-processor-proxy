package borges.kauan.paymentprocessorproxy.adapter.output.gateway;

import borges.kauan.paymentprocessorproxy.domain.dto.PaymentRequest;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;

@FeignClient(name = "fallback-payment-rest-client", url = "${payment.gateway.fallback.url}")
public interface FallbackPaymentRestClient {

    @PostMapping("/payments")
    void processPayment(PaymentRequest paymentRequest);

    @GetMapping("/payments/service-health")
    HealthResponse getServiceHealthFallback();
}

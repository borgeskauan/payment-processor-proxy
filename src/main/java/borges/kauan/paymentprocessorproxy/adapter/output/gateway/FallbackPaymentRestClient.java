package borges.kauan.paymentprocessorproxy.adapter.output.gateway;

import org.springframework.cloud.openfeign.FeignClient;

@FeignClient(name = "fallback-payment-rest-client", url = "${payment.gateway.fallback.url}")
public interface FallbackPaymentRestClient extends PaymentRestClient {
}

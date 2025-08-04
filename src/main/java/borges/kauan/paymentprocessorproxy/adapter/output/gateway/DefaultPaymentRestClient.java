package borges.kauan.paymentprocessorproxy.adapter.output.gateway;

import org.springframework.cloud.openfeign.FeignClient;

@FeignClient(name = "default-payment-rest-client", url = "${payment.gateway.url}")
public interface DefaultPaymentRestClient extends PaymentRestClient {
}

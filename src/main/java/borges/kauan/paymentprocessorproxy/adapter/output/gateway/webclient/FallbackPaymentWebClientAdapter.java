package borges.kauan.paymentprocessorproxy.adapter.output.gateway.webclient;

import borges.kauan.paymentprocessorproxy.adapter.output.gateway.FallbackPaymentRestClient;
import borges.kauan.paymentprocessorproxy.adapter.output.gateway.dto.HealthResponse;
import borges.kauan.paymentprocessorproxy.domain.payment.dto.PaymentRequest;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Repository;
import org.springframework.web.reactive.function.client.WebClient;

@Repository
public class FallbackPaymentWebClientAdapter implements FallbackPaymentRestClient {

    private final PaymentWebClientAdapter paymentWebClientAdapter;

    public FallbackPaymentWebClientAdapter(@Qualifier("fallbackPaymentWebClient") WebClient webClient) {
        this.paymentWebClientAdapter = new PaymentWebClientAdapter(webClient);
    }

    @Override
    public void processPayment(PaymentRequest paymentRequest) {
        paymentWebClientAdapter.processPayment(paymentRequest);
    }

    @Override
    public HealthResponse getServiceHealthFallback() {
        return paymentWebClientAdapter.getServiceHealthFallback();
    }
}

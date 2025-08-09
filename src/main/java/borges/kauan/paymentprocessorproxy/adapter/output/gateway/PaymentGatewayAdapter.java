package borges.kauan.paymentprocessorproxy.adapter.output.gateway;

import borges.kauan.paymentprocessorproxy.domain.infra.MetricsRegister;
import borges.kauan.paymentprocessorproxy.domain.payment.dto.PaymentRequest;
import borges.kauan.paymentprocessorproxy.port.output.PaymentGatewayPort;
import io.micrometer.core.instrument.Timer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

import java.util.function.Supplier;

@Slf4j
@SuppressWarnings("ALL")
@Repository
public class PaymentGatewayAdapter implements PaymentGatewayPort {

    private final DefaultPaymentRestClient defaultPaymentRestClient;
    private final FallbackPaymentRestClient fallbackPaymentRestClient;

    private final MetricsRegister metricsRegister;

    private final Timer timer;

    public PaymentGatewayAdapter(DefaultPaymentRestClient defaultPaymentRestClient,
                                 FallbackPaymentRestClient fallbackPaymentRestClient,
                                 MetricsRegister metricsRegister) {
        this.defaultPaymentRestClient = defaultPaymentRestClient;
        this.fallbackPaymentRestClient = fallbackPaymentRestClient;
        this.metricsRegister = metricsRegister;
        this.timer = metricsRegister.createTimer("payment.processor.time");
    }

    @Override
    public String processPayment(PaymentRequest paymentRequest) {
        return timer.record(() -> processPaymentInternal(paymentRequest));
    }

    private String processPaymentInternal(PaymentRequest paymentRequest) {
        return run(
                () -> processPaymentDefault(paymentRequest),
                () -> processPaymentFallback(paymentRequest)
        );
    }

    private String processPaymentDefault(PaymentRequest paymentRequest) {
        defaultPaymentRestClient.processPayment(paymentRequest);
        return "default";
    }

    private String processPaymentFallback(PaymentRequest paymentRequest) {
        fallbackPaymentRestClient.processPayment(paymentRequest);
        return "fallback";
    }

    private <T> T run(Supplier<T> supplier, Supplier<T> fallback) {
        try {
            return supplier.get();
        } catch (Exception e) {
            log.error("Error in supplier, executing fallback: " + e.getMessage(), e);
            return fallback.get();
        }
    }
}

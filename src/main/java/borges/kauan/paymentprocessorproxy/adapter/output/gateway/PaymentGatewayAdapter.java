package borges.kauan.paymentprocessorproxy.adapter.output.gateway;

import borges.kauan.paymentprocessorproxy.domain.infra.MetricsRegister;
import borges.kauan.paymentprocessorproxy.domain.infra.PaymentGatewayCircuitBreaker;
import borges.kauan.paymentprocessorproxy.domain.payment.dto.PaymentRequest;
import borges.kauan.paymentprocessorproxy.port.output.PaymentGatewayPort;
import io.micrometer.core.instrument.Timer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

@Slf4j
@SuppressWarnings("ALL")
@Repository
public class PaymentGatewayAdapter implements PaymentGatewayPort {

    private final PaymentGatewayCircuitBreaker circuitBreaker;

    private final DefaultPaymentRestClient defaultPaymentRestClient;
    private final FallbackPaymentRestClient fallbackPaymentRestClient;

    private final MetricsRegister metricsRegister;

    private final Timer timer;

    public PaymentGatewayAdapter(PaymentGatewayCircuitBreaker circuitBreaker,
                                 DefaultPaymentRestClient defaultPaymentRestClient,
                                 FallbackPaymentRestClient fallbackPaymentRestClient,
                                 MetricsRegister metricsRegister) {
        this.circuitBreaker = circuitBreaker;
        this.defaultPaymentRestClient = defaultPaymentRestClient;
        this.fallbackPaymentRestClient = fallbackPaymentRestClient;
        this.metricsRegister = metricsRegister;
        this.timer = metricsRegister.createTimer("payment.processor.time");

        circuitBreaker.onDefaultProcessorBecomesResponsiveAgain(() -> {
            log.info("Circuit breaker transitioned from HALF_OPEN to CLOSED");
        });
    }

    @Override
    public String processPayment(PaymentRequest paymentRequest) {
        return timer.record(() -> processPaymentInternal(paymentRequest));
    }

    private String processPaymentInternal(PaymentRequest paymentRequest) {
        try {
            return circuitBreaker.run(
                    () -> processPaymentDefault(paymentRequest),
                    throwable -> processPaymentFallback(throwable, paymentRequest)
            );
        } catch (Exception e) {
            log.error("Error processing payment: {}", e.getMessage());

            metricsRegister.countPaymentDropped();

            throw e;
        }
    }

    private String processPaymentDefault(PaymentRequest paymentRequest) {
        defaultPaymentRestClient.processPayment(paymentRequest);
        return "default";
    }

    private String processPaymentFallback(Throwable throwable, PaymentRequest paymentRequest) {
        log.warn("Error processing payment, falling back: " + throwable.getMessage());

        fallbackPaymentRestClient.processPayment(paymentRequest);
        return "fallback";
    }
}

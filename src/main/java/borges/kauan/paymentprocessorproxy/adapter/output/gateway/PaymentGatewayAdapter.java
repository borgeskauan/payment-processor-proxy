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

    private final static int MAX_RETRIES = 2;

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
        return runWithRetries(
                () -> processPaymentDefault(paymentRequest),
                () -> processPaymentFallback(paymentRequest),
                MAX_RETRIES
        );
    }

    private <T> T runWithRetries(Supplier<T> supplier, Supplier<T> fallback, int maxRetries) {
        int attempt = 0;
        while (attempt < maxRetries) {
            try {
                return supplier.get();
            } catch (Exception e) {
                attempt++;
                log.error("Attempt {} failed: {}", attempt, e.getMessage());
                if (attempt >= maxRetries) {
                    log.error("Max retries reached, executing fallback");
                    return fallback.get();
                }
            }
        }

        throw new RuntimeException("Failed to process payment after " + maxRetries + " attempts");
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

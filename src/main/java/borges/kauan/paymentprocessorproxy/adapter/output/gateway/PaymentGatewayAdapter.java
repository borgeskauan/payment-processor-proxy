package borges.kauan.paymentprocessorproxy.adapter.output.gateway;

import borges.kauan.paymentprocessorproxy.domain.PaymentAlreadyProcessedException;
import borges.kauan.paymentprocessorproxy.domain.dto.PaymentRequest;
import borges.kauan.paymentprocessorproxy.port.output.PaymentGatewayPort;
import feign.FeignException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.client.circuitbreaker.CircuitBreakerFactory;
import org.springframework.stereotype.Repository;

@Slf4j
@SuppressWarnings("ALL")
@Repository
public class PaymentGatewayAdapter implements PaymentGatewayPort {

    private final CircuitBreakerFactory circuitBreakerFactory;

    private final DefaultPaymentRestClient defaultPaymentRestClient;
    private final FallbackPaymentRestClient fallbackPaymentRestClient;

    public PaymentGatewayAdapter(CircuitBreakerFactory circuitBreakerFactory,
                                 DefaultPaymentRestClient defaultPaymentRestClient,
                                 FallbackPaymentRestClient fallbackPaymentRestClient) {
        this.circuitBreakerFactory = circuitBreakerFactory;
        this.defaultPaymentRestClient = defaultPaymentRestClient;
        this.fallbackPaymentRestClient = fallbackPaymentRestClient;
    }

    @Override
    public String processPayment(PaymentRequest paymentRequest) {
        try {
            return circuitBreakerFactory.create("paymentCircuitBreaker").run(
                    () -> processPaymentDefault(paymentRequest),
                    throwable -> processPaymentFallback(throwable, paymentRequest)
            );
        } catch (PaymentAlreadyProcessedException e) {
            log.warn("Payment already processed for correlation ID: {}", paymentRequest.getCorrelationId());
            throw e; // Re-throw the exception to be handled by the circuit breaker
        } catch (Exception e) {
            log.error("Error processing payment: {}", e.getMessage());
            return "none";
        }
    }

    private String processPaymentDefault(PaymentRequest paymentRequest) {
        try {
            // Call the default payment service
            defaultPaymentRestClient.processPayment(paymentRequest);
            return "default";
        } catch (FeignException.UnprocessableEntity e) {
            log.warn("Payment processing failed with 422 Unprocessable Entity: {}", e.getMessage());

            throw new PaymentAlreadyProcessedException("Payment already processed for correlation ID: " + paymentRequest.getCorrelationId(), e);
        }
    }

    private String processPaymentFallback(Throwable throwable, PaymentRequest paymentRequest) {
        try {
            // Log the error or handle it as needed.
            log.warn("Error processing payment, falling back: " + throwable.getMessage());

            // Call the fallback payment service
            fallbackPaymentRestClient.processPayment(paymentRequest);
            return "fallback";
        } catch (FeignException.UnprocessableEntity e) {
            log.warn("Payment processing failed with 422 Unprocessable Entity: {}", e.getMessage());

            throw new PaymentAlreadyProcessedException("Payment already processed for correlation ID: " + paymentRequest.getCorrelationId(), e);
        }
    }
}

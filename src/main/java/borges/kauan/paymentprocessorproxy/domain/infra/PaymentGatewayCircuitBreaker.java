package borges.kauan.paymentprocessorproxy.domain.infra;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.function.Function;
import java.util.function.Supplier;

@Slf4j
@Service
public class PaymentGatewayCircuitBreaker {

    public <T> T run(Supplier<T> supplier, Function<Throwable, T> fallback) {
        try {
            return supplier.get();
        } catch (Exception e) {
            return fallback.apply(e);
        }
    }

    public void onDefaultProcessorBecomesResponsiveAgain(Runnable action) {
    }
}
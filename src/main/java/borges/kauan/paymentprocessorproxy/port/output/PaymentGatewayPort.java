package borges.kauan.paymentprocessorproxy.port.output;

import borges.kauan.paymentprocessorproxy.domain.dto.PaymentRequest;

import java.util.Optional;

public interface PaymentGatewayPort {
    Optional<String> processPayment(PaymentRequest paymentRequest);
}

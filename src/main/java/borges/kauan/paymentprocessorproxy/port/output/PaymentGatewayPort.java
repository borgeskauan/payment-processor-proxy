package borges.kauan.paymentprocessorproxy.port.output;

import borges.kauan.paymentprocessorproxy.domain.dto.PaymentRequest;

public interface PaymentGatewayPort {
    String processPayment(PaymentRequest paymentRequest);
}

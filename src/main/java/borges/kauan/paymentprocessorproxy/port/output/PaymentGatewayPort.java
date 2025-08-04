package borges.kauan.paymentprocessorproxy.port.output;

import borges.kauan.paymentprocessorproxy.domain.dto.PaymentRequest;

public interface PaymentGatewayPort {
    void processPayment(PaymentRequest paymentRequest);
}

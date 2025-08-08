package borges.kauan.paymentprocessorproxy.port.output;

import borges.kauan.paymentprocessorproxy.domain.payment.dto.PaymentRequest;

public interface PaymentGatewayPort {
    String processPayment(PaymentRequest paymentRequest);
}

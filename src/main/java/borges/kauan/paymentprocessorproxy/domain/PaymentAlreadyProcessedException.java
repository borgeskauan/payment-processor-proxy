package borges.kauan.paymentprocessorproxy.domain;

public class PaymentAlreadyProcessedException extends RuntimeException {
    public PaymentAlreadyProcessedException(String message, Throwable cause) {
        super(message, cause);
    }
}

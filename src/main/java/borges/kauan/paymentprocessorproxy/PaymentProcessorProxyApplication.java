package borges.kauan.paymentprocessorproxy;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@EnableFeignClients
public class PaymentProcessorProxyApplication {

    public static void main(String[] args) {
        SpringApplication.run(PaymentProcessorProxyApplication.class, args);
    }

}

package borges.kauan.paymentprocessorproxy.adapter.output.inmemory;

import borges.kauan.paymentprocessorproxy.domain.payment.dto.ProcessedPaymentsSummaryResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.Instant;

@FeignClient(
        name = "summaryRepository",
        url = "${summary-service.url}"
)
public interface SummaryRepositoryPort {

    @GetMapping("/payments-summary/standalone")
    ProcessedPaymentsSummaryResponse getPaymentsSummary(@RequestParam Instant from, @RequestParam Instant to);

    @PostMapping("/purge-payments/standalone")
    void purgePayments();
}

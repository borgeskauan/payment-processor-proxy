package borges.kauan.paymentprocessorproxy.adapter.output.inmemory;

import borges.kauan.paymentprocessorproxy.domain.payment.dto.ProcessedPaymentsSummaryResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;
import reactor.netty.http.client.HttpClient;

import java.time.Instant;

@Repository
public class SummaryUdsAdapter implements SummaryRepositoryPort {

    private final HttpClient httpClient;

    public SummaryUdsAdapter(@Qualifier("summaryUdsHttpClient") HttpClient httpClient) {
        this.httpClient = httpClient;
    }

    public ProcessedPaymentsSummaryResponse getPaymentsSummary(Instant from, Instant to) {
        String url = buildQueryUrl(from, to);

        String json = httpClient
                .get()
                .uri(url)
                .responseContent()
                .aggregate()
                .asString()
                .block();

        try {
            return new ObjectMapper().readValue(json, ProcessedPaymentsSummaryResponse.class);
        } catch (Exception e) {
            throw new RuntimeException("Failed to parse JSON response", e);
        }
    }

    @Override
    public Mono<Void> purgePayments() {
        return httpClient
                .post()
                .uri("/purge-payments/standalone")
                .responseContent()
                .aggregate()
                .then();
    }

    private static String buildQueryUrl(Instant from, Instant to) {
        String baseUrl = "/payments-summary/standalone";
        String fromParam = from != null ? from.toString() : "";
        String toParam = to != null ? to.toString() : "";
        return String.format("%s?from=%s&to=%s", baseUrl, fromParam, toParam);
    }
}

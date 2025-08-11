package borges.kauan.paymentprocessorproxy.adapter.output.gateway.webclient;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class PaymentWebClientConfiguration {

    @Value("${payment.gateway.url}")
    private String defaultUrl;

    @Value("${payment.gateway.fallback.url}")
    private String fallbackUrl;

    @Bean("defaultPaymentWebClient")
    public WebClient webClient() {
        return createWebClient(defaultUrl);
    }

    @Bean("fallbackPaymentWebClient")
    public WebClient fallbackWebClient() {
        return createWebClient(fallbackUrl);
    }

    private WebClient createWebClient(String url) {
        return WebClient.builder()
                .baseUrl(url)
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .build();
    }
}

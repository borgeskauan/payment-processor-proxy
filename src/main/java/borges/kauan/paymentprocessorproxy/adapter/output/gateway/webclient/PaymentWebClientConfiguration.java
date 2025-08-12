package borges.kauan.paymentprocessorproxy.adapter.output.gateway.webclient;

import io.netty.channel.ChannelOption;
import io.netty.handler.timeout.ReadTimeoutHandler;
import io.netty.handler.timeout.WriteTimeoutHandler;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.netty.http.client.HttpClient;
import reactor.netty.resources.ConnectionProvider;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

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
        // Connection pool configuration
        ConnectionProvider provider = ConnectionProvider.builder("custom-pool")
                .maxConnections(500) // adjust based on load
                .pendingAcquireMaxCount(-1) // queue if pool is busy
                .maxIdleTime(Duration.ofSeconds(120)) // close idle connections
                .maxLifeTime(Duration.ofMinutes(5)) // max lifetime before recycling
                .build();

        HttpClient httpClient = HttpClient.create(provider)
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 5000) // connect timeout
                .responseTimeout(Duration.ofSeconds(5)) // read/write timeout
                .doOnConnected(conn -> conn
                        .addHandlerLast(new ReadTimeoutHandler(5, TimeUnit.SECONDS))
                        .addHandlerLast(new WriteTimeoutHandler(5, TimeUnit.SECONDS)));

        return WebClient.builder()
                .baseUrl(url)
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .defaultHeader(HttpHeaders.ACCEPT_ENCODING, "gzip")
                .clientConnector(new ReactorClientHttpConnector(httpClient))
                .build();
    }
}

package borges.kauan.paymentprocessorproxy.adapter.output.gateway.webclient;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Slf4j
@Component
public class ConnectionPoolWarmup {

    private final WebClient defaultClient;
    private final WebClient fallbackClient;

    private static final int DEFAULT_POOL_SIZE = 500;
    private static final int FALLBACK_POOL_SIZE = 500;

    public ConnectionPoolWarmup(
            @Qualifier("defaultPaymentWebClient") WebClient defaultClient,
            @Qualifier("fallbackPaymentWebClient") WebClient fallbackClient) {
        this.defaultClient = defaultClient;
        this.fallbackClient = fallbackClient;
    }

    @EventListener(ApplicationReadyEvent.class)
    public void warmUpPools() {
        log.info("Warming up connection pools...");

        warmUp(defaultClient, DEFAULT_POOL_SIZE);
        warmUp(fallbackClient, FALLBACK_POOL_SIZE);
    }

    private void warmUp(WebClient client, int connectionCount) {
        Flux.range(0, connectionCount)
                .flatMap(i -> client.get()
                        .uri("/payments/service-health")
                        .retrieve()
                        .bodyToMono(String.class)
                        .onErrorResume(e -> Mono.empty()))
                .blockLast(); // block so pool is ready before serving traffic
    }
}

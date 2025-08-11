package borges.kauan.paymentprocessorproxy.adapter.output.inmemory;

import io.netty.channel.epoll.Epoll;
import io.netty.channel.unix.DomainSocketAddress;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import reactor.netty.http.client.HttpClient;

@Configuration
public class SummaryUdsHttpClientConfiguration {

    @Value("${summary-service.url}")
    private String socketPath;

    @Bean("summaryUdsHttpClient")
    public HttpClient createUdsClient() {
        if (!Epoll.isAvailable()) {
            throw new IllegalStateException("Epoll is not available on this system.");
        }

        return HttpClient.create()
                .remoteAddress(() -> new DomainSocketAddress(socketPath));
    }
}

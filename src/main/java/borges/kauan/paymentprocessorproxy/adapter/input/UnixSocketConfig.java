package borges.kauan.paymentprocessorproxy.adapter.input;

import io.netty.channel.unix.DomainSocketAddress;
import org.springframework.boot.web.embedded.netty.NettyReactiveWebServerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class UnixSocketConfig {

    private final String socketPath = "/tmp/spring-boot.sock";

    @Bean
    public NettyReactiveWebServerFactory nettyReactiveWebServerFactory() {
        NettyReactiveWebServerFactory factory = new NettyReactiveWebServerFactory();
        factory.addServerCustomizers(
                httpServer -> httpServer.bindAddress(() -> new DomainSocketAddress(socketPath))
        );

        return factory;
    }
}
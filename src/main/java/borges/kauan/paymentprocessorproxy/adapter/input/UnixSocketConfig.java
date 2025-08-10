package borges.kauan.paymentprocessorproxy.adapter.input;

import io.netty.channel.unix.DomainSocketAddress;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.embedded.netty.NettyReactiveWebServerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Slf4j
@Configuration
public class UnixSocketConfig {

    @Value("${unix.socket.path:/tmp/spring-boot.sock}")
    private String socketPath;

    @Bean
    public NettyReactiveWebServerFactory nettyReactiveWebServerFactory() {
        NettyReactiveWebServerFactory factory = new NettyReactiveWebServerFactory();
        factory.addServerCustomizers(httpServer -> httpServer.bindAddress(() -> new DomainSocketAddress(socketPath)));

        return factory;
    }
}
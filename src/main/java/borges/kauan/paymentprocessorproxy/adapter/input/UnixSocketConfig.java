package borges.kauan.paymentprocessorproxy.adapter.input;

import io.netty.channel.epoll.Epoll;
import io.netty.channel.unix.DomainSocketAddress;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.embedded.netty.NettyReactiveWebServerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.attribute.PosixFilePermissions;

@Slf4j
@Configuration
public class UnixSocketConfig {

    @Value("${unix.socket.path}")
    private String socketPath;

    @Bean
    public NettyReactiveWebServerFactory nettyReactiveWebServerFactory() {
        log.info("Configuring Netty server to use Unix socket at: {}", socketPath);

        if (!Epoll.isAvailable()) {
            throw new IllegalStateException("Epoll not available: " + Epoll.unavailabilityCause());
        }

        NettyReactiveWebServerFactory factory = new NettyReactiveWebServerFactory();
        factory.addServerCustomizers(httpServer -> httpServer.bindAddress(() -> new DomainSocketAddress(socketPath))
                .doOnBound(serverBootstrap -> {
                    log.info("Netty server bound to Unix socket at: {}", socketPath);
                    try {
                        Files.setPosixFilePermissions(Paths.get(socketPath), PosixFilePermissions.fromString("rwxrwxrwx"));
                    } catch (IOException e) {
                        log.error("Failed to set socket permissions", e);
                    }
                }));

        return factory;
    }
}
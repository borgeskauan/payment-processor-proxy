package borges.kauan.paymentprocessorproxy.adapter.input;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

@Slf4j
@EnableAsync
@Configuration
public class AsyncConfig {

    @Bean("workExecutor")
    public ThreadPoolTaskExecutor internalExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();

        executor.setCorePoolSize(2);
        executor.setThreadNamePrefix("WorkExecutor-");
        executor.setPrestartAllCoreThreads(true);

        executor.initialize();
        return executor;
    }
}

package borges.kauan.paymentprocessorproxy.adapter.input;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;

@EnableAsync
@Configuration
public class AsyncConfig {

    @Bean(name = "workExecutor")
    public Executor workExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();

        executor.setCorePoolSize(100);      // number of threads
        executor.setThreadNamePrefix("WorkExecutor-");
        executor.setPrestartAllCoreThreads(true); // prestart all core threads

        executor.initialize();
        return executor;
    }
}

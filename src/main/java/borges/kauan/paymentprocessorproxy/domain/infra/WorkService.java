package borges.kauan.paymentprocessorproxy.domain.infra;

import io.micrometer.core.instrument.Timer;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.stereotype.Service;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

@Service
public class WorkService implements DisposableBean {

    private final ThreadPoolExecutor executor;
    private final Timer timer;

    public WorkService(MetricsRegister metricsRegister) {
        int poolSize = 2;      // tune this
        int queueSize = 5000;  // tune this (pre-allocated)

        this.executor = new ThreadPoolExecutor(
                poolSize, poolSize,
                0L, TimeUnit.MILLISECONDS,
                new ArrayBlockingQueue<>(queueSize), // fixed size, preallocated
                r -> {
                    Thread t = new Thread(r);
                    t.setName("WorkExecutor-" + t.threadId());
                    t.setDaemon(true);
                    return t;
                },
                new ThreadPoolExecutor.CallerRunsPolicy() // backpressure when full
        );

        this.timer = metricsRegister.createTimer("work.processing.time");
    }

    public void doWork(Runnable task) {
        executor.execute(() -> timer.record(task));
    }

    @Override
    public void destroy() {
        executor.shutdownNow();
    }
}

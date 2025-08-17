package borges.kauan.paymentprocessorproxy.domain.infra;

import org.springframework.beans.factory.DisposableBean;
import org.springframework.stereotype.Service;

import java.util.concurrent.*;

@Service
public class WorkService implements DisposableBean {

    private final ExecutorService executor;

    public WorkService() {
        int maxConcurrentTasks = 50;
        int queueSize = 5000;

        // Create a virtual-thread executor
        this.executor = new ThreadPoolExecutor(
                maxConcurrentTasks,
                maxConcurrentTasks,
                0L, TimeUnit.MILLISECONDS,
                new LinkedBlockingQueue<>(queueSize), // Bounded queue
                Thread.ofVirtual().factory(),
                new ThreadPoolExecutor.CallerRunsPolicy() // Handle queue full
        );

        // Pre-warm with a few dummy tasks (optional)
        int preWarmCount = 100; // Adjust based on expected concurrency
        CountDownLatch latch = new CountDownLatch(preWarmCount);
        for (int i = 0; i < preWarmCount; i++) {
            executor.submit(() -> {
                try {
                    Thread.sleep(10);
                } catch (InterruptedException ignored) {
                }
                latch.countDown();
            });
        }

        try {
            latch.await(); // Ensure warmup completes
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    public void doWork(Runnable task) {
        executor.execute(task);
    }

    @Override
    public void destroy() {
        executor.shutdownNow();
    }
}

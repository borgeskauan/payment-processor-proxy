package borges.kauan.paymentprocessorproxy.domain.infra;

import org.springframework.beans.factory.DisposableBean;
import org.springframework.stereotype.Service;

import java.util.concurrent.*;

@Service
public class WorkService implements DisposableBean {

    private final ExecutorService executor;

    public WorkService() {
        int maxConcurrentTasks = 35;

        // Create a virtual-thread executor
        this.executor = new ThreadPoolExecutor(
                maxConcurrentTasks,
                maxConcurrentTasks,
                0L, TimeUnit.MILLISECONDS,
                new LinkedBlockingQueue<>(), // Unbounded queue
                Thread.ofVirtual().factory(),
                new ThreadPoolExecutor.CallerRunsPolicy() // Handle queue full
        );

        // Pre-warm with a few dummy tasks (optional)
        CountDownLatch latch = new CountDownLatch(maxConcurrentTasks);
        for (int i = 0; i < maxConcurrentTasks; i++) {
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

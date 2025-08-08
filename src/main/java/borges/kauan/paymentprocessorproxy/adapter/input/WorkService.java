package borges.kauan.paymentprocessorproxy.adapter.input;

import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class WorkService {

    @Async("workExecutor") // use the custom executor
    public void doWork(Runnable task) {
        log.info("Starting work in thread: {}", Thread.currentThread().getName());

        task.run();

        log.info("Work done by {}", Thread.currentThread().getName());
    }
}
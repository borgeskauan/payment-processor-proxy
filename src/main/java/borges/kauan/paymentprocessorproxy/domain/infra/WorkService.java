package borges.kauan.paymentprocessorproxy.domain.infra;

import io.micrometer.core.instrument.Timer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class WorkService {

    private final Timer timer;

    public WorkService(MetricsRegister metricsRegister) {
        this.timer = metricsRegister.createTimer("work.processing.time");
    }

    @Async("workExecutor") // use the custom executor
    public void doWork(Runnable task) {
        timer.record(task);
    }
}
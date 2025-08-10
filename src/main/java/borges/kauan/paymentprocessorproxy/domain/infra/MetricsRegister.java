package borges.kauan.paymentprocessorproxy.domain.infra;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.DistributionSummary;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import org.springframework.stereotype.Service;

@Service
public class MetricsRegister {

    private final MeterRegistry meterRegistry;

    private final Counter paymentReceivedCounter;
    private final Counter paymentProcessedSucessfullyCounter;
    private final Counter paymentDroppedCounter;

    private final DistributionSummary queueSizeStats;
    private final Long queueSize = 0L;

    public MetricsRegister(MeterRegistry meterRegistry) {
        this.meterRegistry = meterRegistry;

        this.paymentReceivedCounter = Counter
                .builder("payment.received")
                .description("Number of payments received")
                .register(meterRegistry);

        this.paymentProcessedSucessfullyCounter = Counter
                .builder("payment.processed.successfully")
                .description("Number of payments processed successfully")
                .register(meterRegistry);

        this.paymentDroppedCounter = Counter
                .builder("payment.dropped")
                .description("Number of payments dropped due to processing errors")
                .register(meterRegistry);

        this.queueSizeStats = DistributionSummary.builder("executor.queue.stats")
                .description("Stats of the executor queue size")
                .register(meterRegistry);
    }

    public Timer createTimer(String name) {
        return meterRegistry.timer(name);
    }

    public void countPaymentReceived() {
        paymentReceivedCounter.increment();
    }

    public void countPaymentProcessedSuccessfully() {
        paymentProcessedSucessfullyCounter.increment();
    }

    public void countPaymentDropped() {
        paymentDroppedCounter.increment();
    }
}

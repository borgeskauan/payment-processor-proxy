package borges.kauan.paymentprocessorproxy.domain.infra;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.DistributionSummary;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class MetricsRegister {

    private final MeterRegistry meterRegistry;

    private final DistributionSummary queueDelaySummary;
    private final Map<String, Long> enqueueTimestamps = new ConcurrentHashMap<>();

    private final Counter paymentReceivedCounter;
    private final Counter paymentProcessedSucessfullyCounter;
    private final Counter paymentDroppedCounter;

    public MetricsRegister(MeterRegistry meterRegistry) {
        this.meterRegistry = meterRegistry;

        this.queueDelaySummary = DistributionSummary
                .builder("queue.processing.delay")
                .description("Time between message enqueue and processing start")
                .baseUnit("seconds")
                .register(meterRegistry);

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

    public void recordEnqueue(String messageId) {
        enqueueTimestamps.put(messageId, System.currentTimeMillis());
    }

    public void recordProcessingStart(String messageId) {
        Long enqueueTime = enqueueTimestamps.remove(messageId);
        if (enqueueTime != null) {
            long delay = (System.currentTimeMillis() - enqueueTime) / 1000;
            queueDelaySummary.record(delay);
        }
    }
}

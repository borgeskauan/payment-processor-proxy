package borges.kauan.paymentprocessorproxy.domain.infra;

import io.micrometer.core.instrument.*;
import io.micrometer.core.instrument.distribution.ValueAtPercentile;
import org.springframework.stereotype.Service;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Service
public class MetricsFormattedOutputService {
    private final MeterRegistry meterRegistry;

    public MetricsFormattedOutputService(MeterRegistry meterRegistry) {
        this.meterRegistry = meterRegistry;
    }

    public Map<String, Map<String, Object>> getDetailedMetrics() {
        Map<String, Map<String, Object>> metrics = new LinkedHashMap<>();

        // 1. Queue metrics
        DistributionSummary queueDelay = meterRegistry.find("queue.processing.delay").summary();
        if (queueDelay != null) {
            metrics.put("queue_processing_delay", createDistributionSummaryMap(queueDelay));
        }

        Gauge queueSize = meterRegistry.find("executor.queued").gauge();
        if (queueSize != null) {
            metrics.put("queue_size", createGaugeMap(queueSize));
        }

        // 2. Processing time metrics
        Timer processingTime = meterRegistry.find("request.processing.time").timer();
        if (processingTime != null) {
            metrics.put("request_processing_time", createTimerMap(processingTime));
        }

        Timer paymentProcessorTime = meterRegistry.find("payment.processor.time").timer();
        if (paymentProcessorTime != null) {
            metrics.put("payment_processor_time", createTimerMap(paymentProcessorTime));
        }

        Timer redisAddTime = meterRegistry.find("redis.payment.add.time").timer();
        if (redisAddTime != null) {
            metrics.put("redis_add_time", createTimerMap(redisAddTime));
        }

        // 3. Thread pool metrics - grouped under "threads"
        Map<String, Object> threadMetrics = new LinkedHashMap<>();

        Gauge poolSize = meterRegistry.find("executor.pool.size").gauge();
        if (poolSize != null) {
            threadMetrics.put("current_count", poolSize.value());
        }

        Gauge poolCore = meterRegistry.find("executor.pool.core").gauge();
        if (poolCore != null) {
            threadMetrics.put("core_count", poolCore.value());
        }

        Gauge poolMax = meterRegistry.find("executor.pool.max").gauge();
        if (poolMax != null) {
            threadMetrics.put("max_count", poolMax.value());
        }

        if (!threadMetrics.isEmpty()) {
            metrics.put("threads", threadMetrics);
        }

        // 4. Payment metrics - grouped under "payments"
        Map<String, Object> paymentMetrics = new LinkedHashMap<>();

        Counter paymentReceived = meterRegistry.find("payment.received").counter();
        if (paymentReceived != null) {
            paymentMetrics.put("received_count", paymentReceived.count());
        }

        Counter paymentProcessed = meterRegistry.find("payment.processed.successfully").counter();
        if (paymentProcessed != null) {
            paymentMetrics.put("processed_successfully_count", paymentProcessed.count());
        }

        Counter paymentDropped = meterRegistry.find("payment.dropped").counter();
        if (paymentDropped != null) {
            paymentMetrics.put("dropped_count", paymentDropped.count());
        }

        if (!paymentMetrics.isEmpty()) {
            metrics.put("payments", paymentMetrics);
        }

        return metrics;
    }

    private Map<String, Object> createTimerMap(Timer timer) {
        ValueAtPercentile[] percentiles = timer.takeSnapshot().percentileValues();
        Map<String, Object> timerMap = new LinkedHashMap<>();
        timerMap.put("count", timer.count());
        timerMap.put("total_time_seconds", timer.totalTime(TimeUnit.SECONDS));
        timerMap.put("mean_seconds", timer.mean(TimeUnit.SECONDS));
        timerMap.put("max_seconds", timer.max(TimeUnit.SECONDS));

        // Add percentiles from snapshot
        for (ValueAtPercentile percentile : percentiles) {
            String key = "p" + (int) (percentile.percentile() * 100) + "_seconds";
            timerMap.put(key, percentile.value(TimeUnit.SECONDS));
        }

        return timerMap;
    }

    private Map<String, Object> createDistributionSummaryMap(DistributionSummary summary) {
        ValueAtPercentile[] percentiles = summary.takeSnapshot().percentileValues();
        Map<String, Object> summaryMap = new LinkedHashMap<>();
        summaryMap.put("count", summary.count());
        summaryMap.put("total_seconds", summary.totalAmount());
        summaryMap.put("mean", summary.mean());
        summaryMap.put("max", summary.max());

        // Add percentiles from snapshot
        for (ValueAtPercentile percentile : percentiles) {
            String key = "p" + (int) (percentile.percentile() * 100);
            summaryMap.put(key, percentile.value());
        }

        return summaryMap;
    }

    private Map<String, Object> createGaugeMap(Gauge gauge) {
        Map<String, Object> gaugeMap = new LinkedHashMap<>();
        gaugeMap.put("value", gauge.value());
        return gaugeMap;
    }

    private Map<String, Object> createCounterMap(Counter counter) {
        Map<String, Object> counterMap = new LinkedHashMap<>();
        counterMap.put("count", counter.count());
        return counterMap;
    }
}

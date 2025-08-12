package borges.kauan.paymentprocessorproxy.domain.infra;

import io.micrometer.core.instrument.*;
import io.micrometer.core.instrument.distribution.ValueAtPercentile;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Service
public class MetricsFormattedOutputService {
    private final MeterRegistry meterRegistry;

    @Value("${unix.socket.path}")
    private String unixSocketPath;

    public MetricsFormattedOutputService(MeterRegistry meterRegistry) {
        this.meterRegistry = meterRegistry;
    }

    public Map<String, Map<String, Object>> getDetailedMetrics() {
        Map<String, Map<String, Object>> metrics = new LinkedHashMap<>();

        metrics.put("socket_path", Map.of("value", unixSocketPath));

        // 1. Queue metrics
        Gauge queueSize = meterRegistry.find("executor.queued").gauge();
        if (queueSize != null) {
            metrics.put("queue_size", createGaugeMap(queueSize));
        }

        // 2. Processing time metrics
        Timer processingTime = meterRegistry.find("work.processing.time").timer();
        if (processingTime != null) {
            metrics.put("work_processing_time", createTimerMap(processingTime));
        }

        Timer paymentProcessorTime = meterRegistry.find("payment.processor.time").timer();
        if (paymentProcessorTime != null) {
            metrics.put("payment_processor_time", createTimerMap(paymentProcessorTime));
        }

        // 3. Redis metrics - grouped under "redis"
        Map<String, Object> redisMetrics = new LinkedHashMap<>();

        Timer redisAddTime = meterRegistry.find("redis.payment.add.time").timer();
        if (redisAddTime != null) {
            redisMetrics.put("add_time", createTimerMap(redisAddTime));
        }

        Timer calculateSummaryTime = meterRegistry.find("redis.payment.summary.calculate.time").timer();
        if (calculateSummaryTime != null) {
            redisMetrics.put("calculate_summary_time", createTimerMap(calculateSummaryTime));
        }

        if (!redisMetrics.isEmpty()) {
            metrics.put("redis", redisMetrics);
        }

        // 4. Thread pool metrics - grouped under "threads"
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

        // 5. Payment metrics - grouped under "payments"
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

    private Map<String, Object> createGaugeMap(Gauge gauge) {
        Map<String, Object> gaugeMap = new LinkedHashMap<>();
        gaugeMap.put("value", gauge.value());
        return gaugeMap;
    }
}

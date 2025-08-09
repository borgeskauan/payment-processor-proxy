package borges.kauan.paymentprocessorproxy.adapter.input;

import borges.kauan.paymentprocessorproxy.domain.infra.MetricsFormattedOutputService;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/diagnostics")
public class DiagnosticController {

    private final MetricsFormattedOutputService outputService;

    private final ThreadPoolTaskExecutor workExecutor; // TODO: Move to a service if needed

    public DiagnosticController(MetricsFormattedOutputService outputService, @Qualifier("workExecutor") ThreadPoolTaskExecutor workExecutor) {
        this.outputService = outputService;
        this.workExecutor = workExecutor;
    }

    @GetMapping
    public Map<String, Map<String, Object>> getDiagnostics() {
        return outputService.getDetailedMetrics();
    }

    @PostMapping("/stop")
    public void stop() {
        workExecutor.shutdown();
    }
}

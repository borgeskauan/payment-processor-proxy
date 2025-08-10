package borges.kauan.paymentprocessorproxy.adapter.input;

import borges.kauan.paymentprocessorproxy.domain.infra.MetricsFormattedOutputService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/diagnostics")
public class DiagnosticController {

    private final MetricsFormattedOutputService outputService;

    public DiagnosticController(MetricsFormattedOutputService outputService) {
        this.outputService = outputService;
    }

    @GetMapping
    public Map<String, Map<String, Object>> getDiagnostics() {
        return outputService.getDetailedMetrics();
    }
}

package gr.aueb.cf.restbankapp.api;

import gr.aueb.cf.restbankapp.core.exceptions.EntityNotFoundException;
import gr.aueb.cf.restbankapp.dto.JobStatusDTO;
import gr.aueb.cf.restbankapp.service.IEligibleService;
import gr.aueb.cf.restbankapp.service.ICustomerService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/eligible")
public class EligibleRestController {

    private final IEligibleService eligibleService;

    // 1. Trigger the async report generation
    @PostMapping("/report")
    public ResponseEntity<Map<String, String>> startReport() {
        String jobId = UUID.randomUUID().toString();
        eligibleService.generateReport(jobId);
        return ResponseEntity.accepted().body(Map.of("jobId", jobId));
    }

    // 2. Poll for the result
    @GetMapping("/report/{jobId}")
    public ResponseEntity<JobStatusDTO> getReport(@PathVariable String jobId) {
        JobStatusDTO status = eligibleService.getJobStatus(jobId);

        if (status == null) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(status);
    }
}

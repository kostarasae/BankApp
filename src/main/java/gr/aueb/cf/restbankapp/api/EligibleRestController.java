package gr.aueb.cf.restbankapp.api;

import gr.aueb.cf.restbankapp.dto.JobStatusDTO;
import gr.aueb.cf.restbankapp.service.IEligibleService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/eligible")
@Tag(name = "Reports", description = "Long-running reports, generated in the background. Administrators only.")
@SecurityRequirement(name = "Bearer Authentication")
public class EligibleRestController {

    private final IEligibleService eligibleService;

    @Operation(
            summary = "Start generating the eligible-customers report",
            description = """
                    Returns immediately with a job id instead of waiting for the report;
                    the work continues in the background. Poll GET /eligible/report/{jobId}
                    with that id until the status says the report is ready."""
    )
    @ApiResponses({
            @ApiResponse(responseCode = "202", description = "Report accepted for processing; the body carries the jobId",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(example = "{\"jobId\": \"3f2a...\"}")))
    })
    @PostMapping("/report")
    public ResponseEntity<Map<String, String>> startReport() {
        String jobId = UUID.randomUUID().toString();
        eligibleService.generateReport(jobId);
        return ResponseEntity.accepted().body(Map.of("jobId", jobId));
    }

    @Operation(
            summary = "Check on a report",
            description = """
                    Returns the current state of a job started by POST /eligible/report.
                    Keep polling while it is still running; the result is included once finished."""
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Current status of the job",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = JobStatusDTO.class))),
            @ApiResponse(responseCode = "404", description = "No job with this id — it was never started, or the server restarted")
    })
    @GetMapping("/report/{jobId}")
    public ResponseEntity<JobStatusDTO> getReport(@PathVariable String jobId) {
        JobStatusDTO status = eligibleService.getJobStatus(jobId);

        if (status == null) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(status);
    }
}

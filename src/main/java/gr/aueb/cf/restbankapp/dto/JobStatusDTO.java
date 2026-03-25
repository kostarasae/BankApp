package gr.aueb.cf.restbankapp.dto;

import java.util.List;

public record JobStatusDTO(
        String jobId,
        String status,
        List<CustomerStatusReportView> data
) {
    // Compact constructor for IN_PROGRESS / FAILED (no data)
    public static JobStatusDTO withoutData(String jobId, String status) {
        return new JobStatusDTO(jobId, status, null);
    }
}

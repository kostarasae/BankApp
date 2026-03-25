package gr.aueb.cf.restbankapp.service;

import gr.aueb.cf.restbankapp.core.exceptions.EntityNotFoundException;
import gr.aueb.cf.restbankapp.dto.JobStatusDTO;

public interface IEligibleService {
    void generateReport(String jobId);
    JobStatusDTO getJobStatus(String jobId);
}

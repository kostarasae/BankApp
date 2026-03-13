package gr.aueb.cf.restbankapp.repository;

import gr.aueb.cf.restbankapp.model.Capability;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CapabilityRepository extends JpaRepository<Capability, Long> {
}

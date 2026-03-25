package gr.aueb.cf.restbankapp.repository;

import gr.aueb.cf.restbankapp.model.Customer;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.Optional;
import java.util.UUID;

public interface CustomerRepository extends JpaRepository<Customer, Long>,
        JpaSpecificationExecutor<Customer> {

    Optional<Customer> findByUuid(UUID uuid);
    Optional<Customer> findByVat(String vat);

    @EntityGraph(attributePaths = {"personalInfo", "region"})
    Page<Customer> findAllByDeletedFalse(Pageable pageable);

    Optional<Customer> findByUuidAndDeletedFalse(UUID uuid);
    Optional<Customer> findByVatAndDeletedFalse(String vat);

    boolean existsByUuidAndUser_Uuid(UUID customerUuid, UUID userUuid);
}

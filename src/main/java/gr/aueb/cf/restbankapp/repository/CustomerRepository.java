package gr.aueb.cf.restbankapp.repository;

import gr.aueb.cf.restbankapp.dto.CustomerStatusReportView;
import gr.aueb.cf.restbankapp.model.Customer;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface CustomerRepository extends JpaRepository<Customer, Long>,
        JpaSpecificationExecutor<Customer> {

    Optional<Customer> findByUuid(UUID uuid);
    Optional<Customer> findByVat(String vat);
    Optional<Customer> findByPersonalInfo_IdNumber(String idNumber);

    @EntityGraph(attributePaths = {"personalInfo", "region"})
    Page<Customer> findAllByDeletedFalse(Pageable pageable);

    Optional<Customer> findByUuidAndDeletedFalse(UUID uuid);
    Optional<Customer> findByVatAndDeletedFalse(String vat);

    boolean existsByUuidAndUser_Uuid(UUID customerUuid, UUID userUuid);

    Optional<Customer> findByPhone(String phone);

    @Query(value = """
    SELECT
        r.name AS periochi,
        t.firstname AS onoma,
        t.lastname AS eponymo,
        t.vat AS vat,
        'ΕΝΕΡΓΟΣ' AS katastasi
    FROM customers t
    JOIN regions r ON t.region_id = r.id
    WHERE t.deleted = 0
    ORDER BY r.name
    """, nativeQuery = true)
    List<CustomerStatusReportView> findAllCustomersReport();
}

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
    Optional<Customer> findByPersonalInfo_Afm(String afm);

    @EntityGraph(attributePaths = {"personalInfo", "region"})
    Page<Customer> findAllByDeletedFalse(Pageable pageable);

    Optional<Customer> findByUuidAndDeletedFalse(UUID uuid);
    Optional<Customer> findByVatAndDeletedFalse(String vat);

    boolean existsByUuidAndUser_Uuid(UUID customerUuid, UUID userUuid);

    @Query(value = """
    SELECT
        r.name AS periochi,
        t.firstname AS onoma,
        t.lastname AS eponymo,
        pi.afm AS afm,
        t.vat AS vat,
        CASE WHEN t.deleted = 1 THEN 'ΔΙΕΓΡΑΜΜΕΝΟΣ' ELSE 'ΕΝΕΡΓΟΣ' END AS katastasi,
        CASE 
            WHEN t.created_at > '2025-01-01' THEN 'ΝΕΟΣ'
            WHEN t.created_at > '2023-01-01' THEN 'ΜΕΣΑΙΟΣ'
            WHEN t.created_at > '2020-01-01' THEN 'ΕΜΠΕΙΡΟΣ'
            ELSE 'ΠΑΛΙΟΣ'
        END AS empeiria
    FROM customers t
    JOIN personal_information pi ON t.personal_info_id = pi.id
    JOIN regions r ON t.region_id = r.id
    WHERE t.deleted = 0
    ORDER BY t.deleted DESC, r.name
    """, nativeQuery = true)
    List<CustomerStatusReportView> findAllCustomersReport();
}

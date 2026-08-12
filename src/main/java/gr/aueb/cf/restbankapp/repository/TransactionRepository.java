package gr.aueb.cf.restbankapp.repository;

import gr.aueb.cf.restbankapp.model.Transaction;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TransactionRepository extends JpaRepository<Transaction, Long> {

    List<Transaction> findByIbanOrderByCreatedAtDesc(String iban);

    Page<Transaction> findByIban(String iban, Pageable pageable);
}

package gr.aueb.cf.restbankapp.repository;

import gr.aueb.cf.restbankapp.model.Attachment;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AttachmentRepository extends JpaRepository<Attachment, Long> {
}

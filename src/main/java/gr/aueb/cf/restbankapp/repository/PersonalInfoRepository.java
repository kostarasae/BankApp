package gr.aueb.cf.restbankapp.repository;

import gr.aueb.cf.restbankapp.model.PersonalInfo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.Optional;

public interface PersonalInfoRepository extends JpaRepository<PersonalInfo, Long>,
        JpaSpecificationExecutor<PersonalInfo> {

    Optional<PersonalInfo> findByAfm(String afm);
    Optional<PersonalInfo> findByIdentityNumber(String identityNumber);
}

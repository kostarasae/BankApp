package gr.aueb.cf.restbankapp.repository;

import gr.aueb.cf.restbankapp.model.static_data.Region;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RegionRepository extends JpaRepository<Region, Long> {

    List<Region> findAllByOrderByNameAsc();
}

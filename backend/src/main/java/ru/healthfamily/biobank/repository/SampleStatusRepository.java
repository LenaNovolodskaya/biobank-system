package ru.healthfamily.biobank.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.healthfamily.biobank.model.SampleStatus;

import java.util.Optional;

@Repository
public interface SampleStatusRepository extends JpaRepository<SampleStatus, Long> {

    Optional<SampleStatus> findBySampleStatusNameIgnoreCase(String sampleStatusName);
}

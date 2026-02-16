package ru.healthfamily.biobank.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import ru.healthfamily.biobank.model.Specimen;

import java.util.List;

@Repository
public interface SpecimenRepository extends JpaRepository<Specimen, Long> {

    List<Specimen> findBySampleSampleIdOrderBySpecimenId(Long sampleId);

    @Query("SELECT DISTINCT s.sample.sampleId FROM Specimen s WHERE s.sampleStatusId = :statusId")
    List<Long> findSampleIdsBySampleStatusId(Long statusId);

    @Query("SELECT s FROM Specimen s LEFT JOIN FETCH s.sample")
    List<Specimen> findAllWithSample();
}

package ru.healthfamily.biobank.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import ru.healthfamily.biobank.model.Aliquot;

import java.util.List;
import java.util.Set;

@Repository
public interface AliquotRepository extends JpaRepository<Aliquot, Long> {

    List<Aliquot> findBySampleSampleIdOrderByAliquotId(Long sampleId);

    @Query("SELECT DISTINCT a.sample.sampleId FROM Aliquot a WHERE a.sampleStatusId = :statusId")
    Set<Long> findSampleIdsBySampleStatusId(Long statusId);

    @Query("SELECT a FROM Aliquot a LEFT JOIN FETCH a.sample")
    List<Aliquot> findAllWithSample();
}

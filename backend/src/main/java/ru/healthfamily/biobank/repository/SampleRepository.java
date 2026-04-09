package ru.healthfamily.biobank.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.healthfamily.biobank.model.Sample;
import java.util.Collection;

@Repository
public interface SampleRepository extends JpaRepository<Sample, Long> {
    boolean existsByBarcode(String barcode);

    void deleteByVisit_VisitId(Long visitId);

    void deleteByVisit_VisitIdIn(Collection<Long> visitIds);
}

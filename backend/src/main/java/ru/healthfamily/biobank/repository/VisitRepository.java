package ru.healthfamily.biobank.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.healthfamily.biobank.model.Visit;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface VisitRepository extends JpaRepository<Visit, Long> {
    boolean existsByPatientIdAndVisitNumber(Long patientId, Integer visitNumber);

    boolean existsByPatientIdAndVisitNumberAndVisitIdNot(
            Long patientId,
            Integer visitNumber,
            Long visitId
    );

    boolean existsByPatientIdAndCollectionDate(Long patientId, LocalDateTime collectionDate);

    boolean existsByPatientIdAndCollectionDateAndVisitIdNot(
            Long patientId,
            LocalDateTime collectionDate,
            Long visitId
    );

    List<Visit> findByPatientId(Long patientId);

    List<Visit> findByResearchId(Long researchId);

    void deleteByPatientId(Long patientId);

    void deleteByResearchId(Long researchId);
}

package ru.healthfamily.biobank.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.healthfamily.biobank.model.Visit;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface VisitRepository extends JpaRepository<Visit, Long> {
    boolean existsByPatient_PatientIdAndVisitNumber(Long patientId, Integer visitNumber);

    boolean existsByPatient_PatientIdAndVisitNumberAndVisitIdNot(
            Long patientId,
            Integer visitNumber,
            Long visitId
    );

    boolean existsByPatient_PatientIdAndCollectionDate(Long patientId, LocalDateTime collectionDate);

    boolean existsByPatient_PatientIdAndCollectionDateAndVisitIdNot(
            Long patientId,
            LocalDateTime collectionDate,
            Long visitId
    );

    List<Visit> findByPatient_PatientId(Long patientId);

    List<Visit> findByResearch_ResearchId(Long researchId);

    void deleteByPatient_PatientId(Long patientId);

    void deleteByResearch_ResearchId(Long researchId);
}

package ru.healthfamily.biobank.service;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import ru.healthfamily.biobank.dto.CreateVisitRequest;
import ru.healthfamily.biobank.dto.VisitDTO;
import ru.healthfamily.biobank.model.Visit;
import ru.healthfamily.biobank.repository.SampleRepository;
import ru.healthfamily.biobank.repository.VisitRepository;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class VisitService {

    private final VisitRepository visitRepository;
    private final SampleRepository sampleRepository;

    @Transactional
    public VisitDTO createVisit(CreateVisitRequest request) {
        validateVisitNumber(request.getPatientId(), request.getVisitNumber());
        validateVisitDate(request.getPatientId(), request.getCollectionDate());
        Visit visit = new Visit();
        applyRequest(visit, request);
        return toDTO(visitRepository.save(visit));
    }

    @Transactional
    public VisitDTO updateVisit(Long visitId, CreateVisitRequest request) {
        Visit visit = visitRepository.findById(visitId)
                .orElseThrow(() -> new RuntimeException("Визит не найден"));
        validateVisitNumber(request.getPatientId(), request.getVisitNumber(), visitId);
        validateVisitDate(request.getPatientId(), request.getCollectionDate(), visitId);
        applyRequest(visit, request);
        return toDTO(visitRepository.save(visit));
    }

    @Transactional
    public void deleteVisit(Long visitId) {
        Visit visit = visitRepository.findById(visitId)
                .orElseThrow(() -> new RuntimeException("Визит не найден"));
        sampleRepository.deleteByVisitId(visit.getVisitId());
        visitRepository.delete(visit);
    }

    @Transactional(readOnly = true)
    public List<VisitDTO> getAllVisits() {
        return visitRepository.findAll().stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    private void applyRequest(Visit visit, CreateVisitRequest request) {
        visit.setPatientId(request.getPatientId());
        visit.setResearchId(request.getResearchId());
        visit.setVisitNumber(request.getVisitNumber());
        visit.setCollectionDate(request.getCollectionDate());
        visit.setAgeAtCollection(request.getAgeAtCollection());
        visit.setDiagnosisId(request.getDiagnosisId());
        List<Long> comorbid = request.getComorbidDiagnosisIds();
        visit.setComorbidDiagnosisIds(
                comorbid == null ? new ArrayList<>() : new ArrayList<>(comorbid)
        );
    }

    private void validateVisitNumber(Long patientId, Integer visitNumber) {
        if (patientId == null || visitNumber == null) {
            return;
        }
        if (visitRepository.existsByPatientIdAndVisitNumber(patientId, visitNumber)) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "У пациента уже есть визит с таким номером"
            );
        }
    }

    private void validateVisitNumber(Long patientId, Integer visitNumber, Long visitId) {
        if (patientId == null || visitNumber == null || visitId == null) {
            return;
        }
        if (visitRepository.existsByPatientIdAndVisitNumberAndVisitIdNot(
                patientId,
                visitNumber,
                visitId
        )) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "У пациента уже есть визит с таким номером"
            );
        }
    }

    private void validateVisitDate(Long patientId, java.time.LocalDateTime collectionDate) {
        if (patientId == null || collectionDate == null) {
            return;
        }
        if (visitRepository.existsByPatientIdAndCollectionDate(patientId, collectionDate)) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "У пациента уже есть визит на эту дату"
            );
        }
    }

    private void validateVisitDate(
            Long patientId,
            java.time.LocalDateTime collectionDate,
            Long visitId
    ) {
        if (patientId == null || collectionDate == null || visitId == null) {
            return;
        }
        if (visitRepository.existsByPatientIdAndCollectionDateAndVisitIdNot(
                patientId,
                collectionDate,
                visitId
        )) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "У пациента уже есть визит на эту дату"
            );
        }
    }

    private VisitDTO toDTO(Visit visit) {
        return new VisitDTO(
                visit.getVisitId(),
                visit.getPatientId(),
                visit.getResearchId(),
                visit.getVisitNumber(),
                visit.getCollectionDate(),
                visit.getAgeAtCollection(),
                visit.getDiagnosisId(),
                visit.getComorbidDiagnosisIds() == null
                        ? List.of()
                        : new ArrayList<>(visit.getComorbidDiagnosisIds())
        );
    }
}

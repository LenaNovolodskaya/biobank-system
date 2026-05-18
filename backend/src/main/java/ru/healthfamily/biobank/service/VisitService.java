package ru.healthfamily.biobank.service;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import ru.healthfamily.biobank.dto.CreateVisitRequest;
import ru.healthfamily.biobank.dto.DeleteCheckResponse;
import ru.healthfamily.biobank.dto.VisitDTO;
import ru.healthfamily.biobank.model.Sample;
import ru.healthfamily.biobank.model.Visit;
import ru.healthfamily.biobank.repository.PatientRepository;
import ru.healthfamily.biobank.repository.ResearchRepository;
import ru.healthfamily.biobank.repository.SampleRepository;
import ru.healthfamily.biobank.repository.VisitRepository;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class VisitService {

    private final VisitRepository visitRepository;
    private final SampleRepository sampleRepository;
    private final PatientRepository patientRepository;
    private final ResearchRepository researchRepository;

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

    @Transactional(readOnly = true)
    public DeleteCheckResponse checkCanDeleteVisit(Long visitId) {
        Visit visit = visitRepository.findById(visitId)
                .orElseThrow(() -> new RuntimeException("Визит не найден"));
        List<String> barcodes = sampleRepository.findByVisit_VisitId(visitId).stream()
                .sorted(Comparator.comparingLong(sample -> sample.getSampleId()))
                .map(Sample::getBarcode)
                .collect(Collectors.toList());

        if (!barcodes.isEmpty()) {
            return new DeleteCheckResponse(
                    false,
                    "К данному визиту привязаны следующие образцы: " + barcodes +
                            ". Прежде чем удалять визит, необходимо удалить или отвязать образцы.",
                    null
            );
        }
        return new DeleteCheckResponse(true, null, null);
    }

    @Transactional
    public void deleteVisit(Long visitId) {
        Visit visit = visitRepository.findById(visitId)
                .orElseThrow(() -> new RuntimeException("Визит не найден"));
        List<String> barcodes = sampleRepository.findByVisit_VisitId(visitId).stream()
                .sorted(Comparator.comparingLong(sample -> sample.getSampleId()))
                .map(Sample::getBarcode)
                .collect(Collectors.toList());
        if (!barcodes.isEmpty()) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "К данному визиту привязаны следующие образцы: " + barcodes +
                            ". Прежде чем удалять визит, необходимо удалить или отвязать образцы."
            );
        }
        visitRepository.delete(visit);
    }

    @Transactional(readOnly = true)
    public List<VisitDTO> getAllVisits() {
        return visitRepository.findAll().stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    private void applyRequest(Visit visit, CreateVisitRequest request) {
        visit.setPatient(patientRepository.findById(request.getPatientId())
                .orElseThrow(() -> new RuntimeException("Пациент не найден")));
        visit.setResearch(researchRepository.findById(request.getResearchId())
                .orElseThrow(() -> new RuntimeException("Исследование не найдено")));
        visit.setVisitNumber(request.getVisitNumber());
        visit.setCollectionDate(request.getCollectionDate() != null
                ? request.getCollectionDate()
                : LocalDate.now().atTime(8, 0));
        visit.setAgeAtCollection(request.getAgeAtCollection());
    }

    private void validateVisitNumber(Long patientId, Integer visitNumber) {
        if (patientId == null || visitNumber == null) {
            return;
        }
        if (visitRepository.existsByPatient_PatientIdAndVisitNumber(patientId, visitNumber)) {
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
        if (visitRepository.existsByPatient_PatientIdAndVisitNumberAndVisitIdNot(
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
        if (visitRepository.existsByPatient_PatientIdAndCollectionDate(patientId, collectionDate)) {
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
        if (visitRepository.existsByPatient_PatientIdAndCollectionDateAndVisitIdNot(
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
        List<Long> comorbidIds = List.of();
        Long diagnosisId = null;
        Long patientId = visit.getPatient() != null ? visit.getPatient().getPatientId() : null;
        Long researchId = visit.getResearch() != null ? visit.getResearch().getResearchId() : null;
        if (patientId != null) {
            var patientOpt = patientRepository.findById(patientId);
            comorbidIds = patientOpt
                    .map(p -> p.getComorbidDiagnoses() == null
                            ? List.<Long>of()
                            : p.getComorbidDiagnoses().stream()
                                    .map(d -> d.getDiagnosisId())
                                    .collect(Collectors.toList()))
                    .orElse(List.of());
            diagnosisId = patientOpt.map(p -> p.getMainDiagnosis() != null
                    ? p.getMainDiagnosis().getDiagnosisId() : null).orElse(null);
        }
        return new VisitDTO(
                visit.getVisitId(),
                patientId,
                researchId,
                visit.getVisitNumber(),
                visit.getCollectionDate(),
                visit.getAgeAtCollection(),
                diagnosisId,
                comorbidIds
        );
    }
}

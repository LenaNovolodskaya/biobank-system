package ru.healthfamily.biobank.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.healthfamily.biobank.dto.CreatePatientRequest;
import ru.healthfamily.biobank.dto.PatientDTO;
import ru.healthfamily.biobank.model.Nationality;
import ru.healthfamily.biobank.model.Patient;
import ru.healthfamily.biobank.repository.NationalityRepository;
import ru.healthfamily.biobank.repository.PatientRepository;
import ru.healthfamily.biobank.repository.SampleRepository;
import ru.healthfamily.biobank.repository.VisitRepository;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PatientService {
    
    private final PatientRepository patientRepository;
    private final NationalityRepository nationalityRepository;
    private final VisitRepository visitRepository;
    private final SampleRepository sampleRepository;
    
    @Transactional
    public PatientDTO createPatient(CreatePatientRequest request) {
        // Проверяем уникальность штрихкода
        if (patientRepository.existsByPatientBarcode(request.getPatientBarcode())) {
            throw new RuntimeException("Пациент с таким штрихкодом уже существует");
        }
        
        Patient patient = new Patient();
        patient.setPatientBarcode(request.getPatientBarcode());
        patient.setGender(request.getGender());
        patient.setBirthDate(request.getBirthDate());
        patient.setInformedConsent(request.getInformedConsent());
        patient.setMainDiagnosisId(request.getMainDiagnosisId());
        List<Long> comorbid = request.getComorbidDiagnosisIds();
        patient.setComorbidDiagnosisIds(
                comorbid == null ? new ArrayList<>() : new ArrayList<>(comorbid)
        );
        patient.setCreatedAtPatient(
                request.getCreatedAtPatient() != null
                        ? request.getCreatedAtPatient()
                        : LocalDate.now().atTime(8, 0)
        );
        
        // Если указана национальность, находим её
        if (request.getNationalityId() != null) {
            Nationality nationality = nationalityRepository.findById(request.getNationalityId())
                    .orElseThrow(() -> new RuntimeException("Национальность не найдена"));
            patient.setNationality(nationality);
        }
        
        Patient savedPatient = patientRepository.save(patient);
        return convertToDTO(savedPatient);
    }
    
    @Transactional(readOnly = true)
    public List<PatientDTO> getAllPatients() {
        return patientRepository.findAll().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }
    
    @Transactional(readOnly = true)
    public PatientDTO getPatientById(Long id) {
        Patient patient = patientRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Пациент не найден"));
        return convertToDTO(patient);
    }
    
    @Transactional(readOnly = true)
    public PatientDTO getPatientByBarcode(String barcode) {
        Patient patient = patientRepository.findByPatientBarcode(barcode)
                .orElseThrow(() -> new RuntimeException("Пациент не найден"));
        return convertToDTO(patient);
    }

    @Transactional
    public PatientDTO updatePatient(Long id, CreatePatientRequest request) {
        Patient patient = patientRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Пациент не найден"));

        String newBarcode = request.getPatientBarcode();
        if (!patient.getPatientBarcode().equals(newBarcode)
                && patientRepository.existsByPatientBarcode(newBarcode)) {
            throw new RuntimeException("Пациент с таким штрихкодом уже существует");
        }

        patient.setPatientBarcode(newBarcode);
        patient.setGender(request.getGender());
        patient.setBirthDate(request.getBirthDate());
        patient.setInformedConsent(request.getInformedConsent());
        patient.setMainDiagnosisId(request.getMainDiagnosisId());
        List<Long> comorbid = request.getComorbidDiagnosisIds();
        patient.setComorbidDiagnosisIds(
                comorbid == null ? new ArrayList<>() : new ArrayList<>(comorbid)
        );
        if (request.getCreatedAtPatient() != null) {
            patient.setCreatedAtPatient(request.getCreatedAtPatient());
        }

        if (request.getNationalityId() != null) {
            Nationality nationality = nationalityRepository.findById(request.getNationalityId())
                    .orElseThrow(() -> new RuntimeException("Национальность не найдена"));
            patient.setNationality(nationality);
        } else {
            patient.setNationality(null);
        }

        return convertToDTO(patientRepository.save(patient));
    }

    @Transactional
    public void deletePatient(Long id) {
        if (!patientRepository.existsById(id)) {
            throw new RuntimeException("Пациент не найден");
        }
        List<Long> visitIds = visitRepository.findByPatientId(id).stream()
                .map(visit -> visit.getVisitId())
                .collect(Collectors.toList());
        if (!visitIds.isEmpty()) {
            sampleRepository.deleteByVisitIdIn(visitIds);
        }
        visitRepository.deleteByPatientId(id);
        patientRepository.deleteById(id);
    }
    
    private PatientDTO convertToDTO(Patient patient) {
        PatientDTO dto = new PatientDTO();
        dto.setPatientId(patient.getPatientId());
        dto.setPatientBarcode(patient.getPatientBarcode());
        dto.setGender(patient.getGender());
        dto.setBirthDate(patient.getBirthDate());
        dto.setCreatedAtPatient(patient.getCreatedAtPatient());
        dto.setInformedConsent(patient.getInformedConsent());
        dto.setMainDiagnosisId(patient.getMainDiagnosisId());
        dto.setComorbidDiagnosisIds(
                patient.getComorbidDiagnosisIds() == null
                        ? List.of()
                        : new ArrayList<>(patient.getComorbidDiagnosisIds())
        );
        
        if (patient.getNationality() != null) {
            dto.setNationalityId(patient.getNationality().getNationalityId());
            dto.setNationalityName(patient.getNationality().getNationalityName());
        }
        
        return dto;
    }
}
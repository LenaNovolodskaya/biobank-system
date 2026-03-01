package ru.healthfamily.biobank.service;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.healthfamily.biobank.dto.CreateSampleTransactionRequest;
import ru.healthfamily.biobank.dto.SampleTransactionDTO;
import ru.healthfamily.biobank.dto.SpecimenActionRequest;
import ru.healthfamily.biobank.model.*;
import ru.healthfamily.biobank.repository.*;
import ru.healthfamily.biobank.security.CustomUserDetails;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SampleTransactionService {

    private final SampleTransactionRepository sampleTransactionRepository;
    private final SampleRepository sampleRepository;
    private final SpecimenRepository specimenRepository;
    private final UserRepository userRepository;
    private final TransactionTypeRepository transactionTypeRepository;
    private final DepartmentRepository departmentRepository;
    private final SampleStatusRepository sampleStatusRepository;

    @Transactional(readOnly = true)
    public List<SampleTransactionDTO> getAllTransactions(Long sampleId, Long specimenId) {
        if (specimenId != null) {
            return sampleTransactionRepository.findBySpecimenIdWithDetails(specimenId).stream()
                    .map(this::toDTO).collect(Collectors.toList());
        }
        List<SampleTransaction> list = sampleId != null
                ? sampleTransactionRepository.findBySampleIdWithDetails(sampleId)
                : sampleTransactionRepository.findAllWithDetails();
        return list.stream().map(this::toDTO).collect(Collectors.toList());
    }

    private SampleTransactionDTO toDTO(SampleTransaction t) {
        SampleTransactionDTO dto = new SampleTransactionDTO();
        dto.setTransactionId(t.getTransactionId());
        dto.setSampleId(t.getSample() != null ? t.getSample().getSampleId() : null);
        dto.setSpecimenId(t.getSpecimen() != null ? t.getSpecimen().getSpecimenId() : null);
        dto.setSampleBarcode(t.getSampleBarcode() != null ? t.getSampleBarcode() : (t.getSample() != null ? t.getSample().getBarcode() : null));
        dto.setSpecimenBarcode(t.getSpecimenBarcode() != null ? t.getSpecimenBarcode() : (t.getSpecimen() != null ? t.getSpecimen().getBarcode() : null));
        dto.setUserId(t.getUser() != null ? t.getUser().getUserId() : null);
        dto.setUserFullName(t.getUser() != null ? t.getUser().getFullName() : null);
        dto.setTransactionTypeId(t.getTransactionType() != null ? t.getTransactionType().getTransactionTypeId() : null);
        dto.setTransactionTypeName(t.getTransactionType() != null ? t.getTransactionType().getTransactionTypeName() : null);
        dto.setOperationSign(resolveOperationSign(t.getTransactionType() != null ? t.getTransactionType().getTransactionTypeName() : null));
        dto.setTransactionDate(t.getTransactionDate());
        dto.setDepartmentId(t.getDepartment() != null ? t.getDepartment().getDepartmentId() : null);
        dto.setDepartmentName(t.getDepartment() != null ? t.getDepartment().getDepartmentName() : null);
        dto.setPurpose(t.getPurpose());
        dto.setNotes(t.getNotes());
        return dto;
    }

    private String resolveOperationSign(String typeName) {
        if (typeName == null) return "—";
        String lower = typeName.trim().toLowerCase();
        if (lower.contains("создан") || lower.contains("create") || lower.contains("добавлен")) return "+";
        if (lower.contains("возвращён") || lower.contains("return")) return "+";
        if (lower.contains("изъят") || lower.contains("изъвлеч") || lower.contains("withdraw")) return "-";
        if (lower.contains("исчерпан") || lower.contains("exhaust")) return "-";
        return "—";
    }

    @Transactional
    public SampleTransactionDTO createTransaction(CreateSampleTransactionRequest request) {
        Long userId = getCurrentUserId();
        Sample sample = sampleRepository.findById(request.getSampleId())
                .orElseThrow(() -> new IllegalArgumentException("Образец не найден: " + request.getSampleId()));
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("Пользователь не найден"));
        TransactionType transactionType = transactionTypeRepository.findById(request.getTransactionTypeId())
                .orElseThrow(() -> new IllegalArgumentException("Тип операции не найден: " + request.getTransactionTypeId()));
        Department department = request.getDepartmentId() != null
                ? departmentRepository.findById(request.getDepartmentId()).orElse(null)
                : null;

        SampleTransaction t = new SampleTransaction();
        t.setSample(sample);
        t.setSampleBarcode(sample.getBarcode());
        t.setUser(user);
        t.setTransactionType(transactionType);
        t.setTransactionDate(request.getTransactionDate());
        t.setDepartment(department);
        t.setPurpose(request.getPurpose());
        t.setNotes(request.getNotes());
        t = sampleTransactionRepository.save(t);

        t.setSample(sample);
        t.setUser(user);
        t.setTransactionType(transactionType);
        t.setDepartment(department);
        return toDTO(t);
    }

    private Long getCurrentUserId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() instanceof CustomUserDetails) {
            return ((CustomUserDetails) auth.getPrincipal()).getUserId();
        }
        throw new IllegalStateException("Пользователь не авторизован");
    }

    @Transactional
    public void recordSampleCreated(Sample sample) {
        User user = userRepository.findById(getCurrentUserId()).orElse(null);
        java.time.LocalDateTime txDate = sample.getCreatedAtSample() != null
                ? sample.getCreatedAtSample()
                : java.time.LocalDateTime.now();
        transactionTypeRepository.findByTransactionTypeNameIgnoreCase("Создан")
                .ifPresent(type -> {
                    for (Specimen specimen : sample.getSpecimens()) {
                        SampleTransaction t = new SampleTransaction();
                        t.setSample(sample);
                        t.setSpecimen(specimen);
                        t.setSampleBarcode(sample.getBarcode());
                        t.setSpecimenBarcode(specimen.getBarcode());
                        t.setUser(user);
                        t.setTransactionType(type);
                        t.setTransactionDate(txDate);
                        sampleTransactionRepository.save(t);
                    }
                });
    }

    @Transactional
    public void recordSampleWithdrawn(Sample sample) {
        transactionTypeRepository.findByTransactionTypeNameIgnoreCase("Изъвлечён из хранилища")
                .ifPresent(type -> {
                    SampleTransaction t = new SampleTransaction();
                    t.setSample(sample);
                    t.setSampleBarcode(sample.getBarcode());
                    t.setUser(userRepository.findById(getCurrentUserId()).orElse(null));
                    t.setTransactionType(type);
                    t.setTransactionDate(java.time.LocalDateTime.now());
                    sampleTransactionRepository.save(t);
                    // Clear sample reference before sample is deleted to avoid FK violation
                    t.setSample(null);
                    sampleTransactionRepository.save(t);
                });
    }

    @Transactional
    public SampleTransactionDTO withdrawSpecimen(Long specimenId, SpecimenActionRequest request) {
        Specimen specimen = specimenRepository.findById(specimenId)
                .orElseThrow(() -> new IllegalArgumentException("Проба не найдена: " + specimenId));
        Long withdrawnStatusId = sampleStatusRepository.findBySampleStatusNameIgnoreCase("Изъят")
                .map(s -> s.getSampleStatusId()).orElseThrow(() -> new IllegalArgumentException("Статус «Изъят» не найден"));
        TransactionType type = transactionTypeRepository.findByTransactionTypeNameIgnoreCase("Изъвлечён из хранилища")
                .orElseThrow(() -> new IllegalArgumentException("Тип операции «Изъвлечён из хранилища» не найден"));

        specimen.setSampleStatusId(withdrawnStatusId);
        specimenRepository.save(specimen);

        return recordSpecimenTransaction(specimen, type, request);
    }

    @Transactional
    public SampleTransactionDTO returnSpecimen(Long specimenId, SpecimenActionRequest request) {
        Specimen specimen = specimenRepository.findById(specimenId)
                .orElseThrow(() -> new IllegalArgumentException("Проба не найдена: " + specimenId));
        Long inStorageStatusId = sampleStatusRepository.findBySampleStatusNameIgnoreCase("В хранилище")
                .map(s -> s.getSampleStatusId()).orElseThrow(() -> new IllegalArgumentException("Статус «В хранилище» не найден"));
        TransactionType type = transactionTypeRepository.findByTransactionTypeNameIgnoreCase("Возвращён в хранилище")
                .orElseThrow(() -> new IllegalArgumentException("Тип операции «Возвращён в хранилище» не найден"));

        specimen.setSampleStatusId(inStorageStatusId);
        specimen.setContainerId(specimen.getSample() != null ? specimen.getSample().getContainerId() : null);
        specimenRepository.save(specimen);

        return recordSpecimenTransaction(specimen, type, request);
    }

    @Transactional
    public SampleTransactionDTO exhaustSpecimen(Long specimenId, SpecimenActionRequest request) {
        Specimen specimen = specimenRepository.findById(specimenId)
                .orElseThrow(() -> new IllegalArgumentException("Проба не найдена: " + specimenId));
        Long exhaustedStatusId = sampleStatusRepository.findBySampleStatusNameIgnoreCase("Исчерпан")
                .map(s -> s.getSampleStatusId()).orElseThrow(() -> new IllegalArgumentException("Статус «Исчерпан» не найден"));
        TransactionType type = transactionTypeRepository.findByTransactionTypeNameIgnoreCase("Исчерпан")
                .orElseThrow(() -> new IllegalArgumentException("Тип операции «Исчерпан» не найден"));

        specimen.setSampleStatusId(exhaustedStatusId);
        specimen.setContainerId(null);
        specimen.setPositionInContainer(null);
        specimenRepository.save(specimen);

        return recordSpecimenTransaction(specimen, type, request);
    }

    private SampleTransactionDTO recordSpecimenTransaction(Specimen specimen, TransactionType type, SpecimenActionRequest request) {
        User user = userRepository.findById(getCurrentUserId())
                .orElseThrow(() -> new IllegalStateException("Пользователь не найден"));
        Department department = request.getDepartmentId() != null
                ? departmentRepository.findById(request.getDepartmentId()).orElse(null)
                : null;

        SampleTransaction t = new SampleTransaction();
        t.setSample(specimen.getSample());
        t.setSpecimen(specimen);
        t.setSampleBarcode(specimen.getSample() != null ? specimen.getSample().getBarcode() : null);
        t.setSpecimenBarcode(specimen.getBarcode());
        t.setUser(user);
        t.setTransactionType(type);
        t.setTransactionDate(request.getTransactionDate());
        t.setDepartment(department);
        t.setPurpose(request.getPurpose());
        t.setNotes(request.getNotes());
        t = sampleTransactionRepository.save(t);
        return toDTO(t);
    }
}

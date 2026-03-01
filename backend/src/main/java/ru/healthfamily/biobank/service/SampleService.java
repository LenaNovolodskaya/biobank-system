package ru.healthfamily.biobank.service;

import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.healthfamily.biobank.dto.SpecimenDTO;
import ru.healthfamily.biobank.dto.CreateSampleRequest;
import ru.healthfamily.biobank.dto.SampleDTO;
import ru.healthfamily.biobank.model.Specimen;
import ru.healthfamily.biobank.model.Sample;
import ru.healthfamily.biobank.model.SampleStatus;
import ru.healthfamily.biobank.model.Sample.ExpiryStatus;
import ru.healthfamily.biobank.repository.SpecimenRepository;
import ru.healthfamily.biobank.repository.SampleRepository;
import ru.healthfamily.biobank.repository.SampleStatusRepository;
import ru.healthfamily.biobank.repository.StorageContainerRepository;
import ru.healthfamily.biobank.repository.VisitRepository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Period;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SampleService {

    private static final String IN_STORAGE_STATUS_NAME = "В хранилище";

    private final SampleRepository sampleRepository;
    private final SpecimenRepository specimenRepository;
    private final SampleStatusRepository sampleStatusRepository;
    private final StorageContainerRepository containerRepository;
    private final VisitRepository visitRepository;
    private final EntityManager entityManager;
    private final SampleTransactionService sampleTransactionService;

    @Transactional
    public List<SampleDTO> getSamples(
            String search,
            Long containerId,
            Long sampleTypeId,
            Long sampleStatusId,
            Long visitId,
            ExpiryStatus expiryStatus
    ) {
        List<Sample> samples = sampleRepository.findAll();
        List<Sample> toUpdate = new ArrayList<>();
        for (Sample sample : samples) {
            Integer previousMonths = sample.getActualStorageMonths();
            ExpiryStatus previousStatus = sample.getExpiryStatus();
            applyStorageStatus(sample);
            if (!Objects.equals(previousMonths, sample.getActualStorageMonths())
                    || !Objects.equals(previousStatus, sample.getExpiryStatus())) {
                toUpdate.add(sample);
            }
        }
        if (!toUpdate.isEmpty()) {
            sampleRepository.saveAll(toUpdate);
        }
        List<Specimen> allSpecimens = specimenRepository.findAllWithSample();
        java.util.Map<Long, List<Specimen>> specimensBySampleId = allSpecimens.stream()
                .filter(s -> s.getSample() != null)
                .collect(Collectors.groupingBy(s -> s.getSample().getSampleId()));
        java.util.Set<Long> sampleIdsWithStatus = sampleStatusId != null
                ? new java.util.HashSet<>(specimenRepository.findSampleIdsBySampleStatusId(sampleStatusId)) : null;
        return samples.stream()
                .filter(sample -> matchesSearch(sample, search, specimensBySampleId.getOrDefault(sample.getSampleId(), List.of())))
                .filter(sample -> containerId == null || containerId.equals(sample.getContainerId()))
                .filter(sample -> sampleTypeId == null || sampleTypeId.equals(sample.getSampleTypeId()))
                .filter(sample -> sampleStatusId == null || (sampleIdsWithStatus != null && sampleIdsWithStatus.contains(sample.getSampleId())))
                .filter(sample -> visitId == null || visitId.equals(sample.getVisitId()))
                .filter(sample -> expiryStatus == null || expiryStatus == sample.getExpiryStatus())
                .map(sample -> toDTO(sample, specimensBySampleId.getOrDefault(sample.getSampleId(), List.of())))
                .collect(Collectors.toList());
    }

    @Transactional
    public SampleDTO createSample(CreateSampleRequest request) {
        if (request.getVisitId() == null || !visitRepository.existsById(request.getVisitId())) {
            throw new IllegalArgumentException("Нельзя создать образец: данного визита не существует.");
        }
        Sample sample = new Sample();
        applyRequest(sample, request);
        Sample saved = sampleRepository.save(sample);
        updateContainerCountsFromSpecimens(saved.getSpecimens());
        try {
            sampleTransactionService.recordSampleCreated(saved);
        } catch (Exception e) {
            // ignore - journal is optional
        }
        return toDTO(saved);
    }

    @Transactional
    public SampleDTO updateSample(Long sampleId, CreateSampleRequest request) {
        if (request.getVisitId() == null || !visitRepository.existsById(request.getVisitId())) {
            throw new IllegalArgumentException("Нельзя обновить образец: данного визита не существует.");
        }
        Sample sample = sampleRepository.findById(sampleId)
                .orElseThrow(() -> new RuntimeException("Образец не найден"));
        java.util.Set<Long> oldContainerIds = sample.getSpecimens().stream()
                .map(Specimen::getContainerId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
        applyRequest(sample, request);
        Sample saved = sampleRepository.save(sample);
        java.util.Set<Long> newContainerIds = saved.getSpecimens().stream()
                .map(Specimen::getContainerId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
        java.util.Set<Long> affected = new java.util.HashSet<>(oldContainerIds);
        affected.addAll(newContainerIds);
        affected.forEach(this::recalculateContainerCount);
        return toDTO(saved);
    }

    @Transactional
    public void deleteSample(Long sampleId) {
        Sample sample = sampleRepository.findById(sampleId)
                .orElseThrow(() -> new RuntimeException("Образец не найден"));
        try {
            sampleTransactionService.recordSampleWithdrawn(sample);
        } catch (Exception e) {
            // ignore - journal is optional
        }
        java.util.Set<Long> containerIds = sample.getSpecimens().stream()
                .map(Specimen::getContainerId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
        sampleRepository.delete(sample);
        containerIds.forEach(this::recalculateContainerCount);
    }

    private void updateContainerCountsFromSpecimens(List<Specimen> specimens) {
        specimens.stream()
                .map(Specimen::getContainerId)
                .filter(Objects::nonNull)
                .distinct()
                .forEach(this::recalculateContainerCount);
    }

    private void recalculateContainerCount(Long containerId) {
        containerRepository.findById(containerId).ifPresent(container -> {
            long count = specimenRepository.countByContainerId(containerId);
            container.setCurrentSamplesCount((int) count);
            containerRepository.save(container);
        });
    }

    private boolean matchesSearch(Sample sample, String search, List<Specimen> sampleSpecimens) {
        if (search == null || search.isBlank()) {
            return true;
        }
        String normalized = search.toLowerCase(Locale.ROOT);
        if (sample.getBarcode() != null && sample.getBarcode().toLowerCase(Locale.ROOT).contains(normalized)) {
            return true;
        }
        if (sample.getSampleId() != null && sample.getSampleId().toString().contains(normalized)) {
            return true;
        }
        return sampleSpecimens.stream()
                .anyMatch(s -> (s.getBarcode() != null && s.getBarcode().toLowerCase(Locale.ROOT).contains(normalized))
                        || (s.getPositionInContainer() != null && s.getPositionInContainer().toLowerCase(Locale.ROOT).contains(normalized)));
    }

    private void applyRequest(Sample sample, CreateSampleRequest request) {
        sample.setVisitId(request.getVisitId());
        sample.setBarcode(request.getBarcode());
        sample.setSampleTypeId(request.getSampleTypeId());
        sample.setInitialQuantity(request.getInitialQuantity());
        sample.setRecommendedStorageMonths(request.getRecommendedStorageMonths());
        if (request.getCollectionDate() != null) {
            sample.setCreatedAtSample(request.getCollectionDate());
        } else if (sample.getCreatedAtSample() == null) {
            visitRepository.findById(request.getVisitId())
                    .map(visit -> visit.getCollectionDate())
                    .ifPresentOrElse(sample::setCreatedAtSample,
                            () -> sample.setCreatedAtSample(LocalDate.now().atTime(8, 0)));
        }
        sample.setContainerId(request.getContainerId());
        applyStorageStatus(sample);

        sample.getSpecimens().clear();
        entityManager.flush();
        Long inStorageId = getInStorageStatusId();
        if (request.getSpecimens() != null && !request.getSpecimens().isEmpty()) {
            for (CreateSampleRequest.SpecimenItem item : request.getSpecimens()) {
                Specimen specimen = new Specimen();
                specimen.setSample(sample);
                specimen.setBarcode(item.getBarcode());
                specimen.setSampleStatusId(item.getSampleStatusId() != null ? item.getSampleStatusId() : inStorageId);
                specimen.setContainerId(item.getContainerId() != null ? item.getContainerId() : request.getContainerId());
                specimen.setPositionInContainer(item.getPositionInContainer());
                sample.getSpecimens().add(specimen);
            }
        }
        int currentQty = (int) sample.getSpecimens().stream()
                .filter(s -> inStorageId != null && inStorageId.equals(s.getSampleStatusId()))
                .count();
        sample.setCurrentQuantity(currentQty);
    }

    private Long getInStorageStatusId() {
        return sampleStatusRepository.findBySampleStatusNameIgnoreCase(IN_STORAGE_STATUS_NAME)
                .map(SampleStatus::getSampleStatusId)
                .orElse(null);
    }

    private void applyStorageStatus(Sample sample) {
        if (sample.getCreatedAtSample() == null) {
            sample.setActualStorageMonths(null);
            sample.setExpiryStatus(ExpiryStatus.GREEN);
            return;
        }
        LocalDate startDate = sample.getCreatedAtSample().toLocalDate();
        LocalDate today = LocalDate.now();
        int months = Period.between(startDate, today).getYears() * 12
                + Period.between(startDate, today).getMonths();
        sample.setActualStorageMonths(months);

        Integer recommended = sample.getRecommendedStorageMonths();
        if (recommended == null) {
            sample.setExpiryStatus(ExpiryStatus.GREEN);
            return;
        }
        int remaining = recommended - months;
        if (remaining < 0) {
            sample.setExpiryStatus(ExpiryStatus.RED);
        } else if (remaining <= 2) {
            sample.setExpiryStatus(ExpiryStatus.YELLOW);
        } else {
            sample.setExpiryStatus(ExpiryStatus.GREEN);
        }
    }

    private SampleDTO toDTO(Sample sample, List<Specimen> specimens) {
        List<SpecimenDTO> specimenDTOs = specimens.stream()
                .map(s -> new SpecimenDTO(
                        s.getSpecimenId(),
                        s.getBarcode(),
                        s.getSample() != null ? s.getSample().getSampleId() : null,
                        s.getSampleStatusId(),
                        s.getContainerId(),
                        s.getPositionInContainer()
                ))
                .collect(Collectors.toList());
        Long inStorageStatusId = getInStorageStatusId();
        int currentQuantity = (int) specimens.stream()
                .filter(s -> inStorageStatusId != null && inStorageStatusId.equals(s.getSampleStatusId()))
                .count();
        return new SampleDTO(
                sample.getSampleId(),
                sample.getVisitId(),
                sample.getBarcode(),
                sample.getSampleTypeId(),
                sample.getInitialQuantity(),
                currentQuantity,
                sample.getRecommendedStorageMonths(),
                sample.getActualStorageMonths(),
                sample.getExpiryStatus(),
                sample.getContainerId(),
                sample.getCreatedAtSample(),
                specimenDTOs
        );
    }

    private SampleDTO toDTO(Sample sample) {
        List<Specimen> specimens = sample.getSpecimens() != null ? sample.getSpecimens() : List.of();
        return toDTO(sample, specimens);
    }
}

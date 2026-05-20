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
import ru.healthfamily.biobank.model.Visit;
import ru.healthfamily.biobank.model.SampleStatus;
import ru.healthfamily.biobank.model.Sample.ExpiryStatus;
import ru.healthfamily.biobank.repository.SpecimenRepository;
import ru.healthfamily.biobank.repository.SampleRepository;
import ru.healthfamily.biobank.repository.SampleStatusRepository;
import ru.healthfamily.biobank.repository.SampleTypeRepository;
import ru.healthfamily.biobank.repository.StorageContainerRepository;
import ru.healthfamily.biobank.repository.VisitRepository;

import java.time.LocalDate;
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
    private final SampleTypeRepository sampleTypeRepository;
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
                .filter(sample -> containerId == null || (sample.getContainer() != null && containerId.equals(sample.getContainer().getContainerId())))
                .filter(sample -> sampleTypeId == null || (sample.getSampleType() != null && sampleTypeId.equals(sample.getSampleType().getSampleTypeId())))
                .filter(sample -> sampleStatusId == null || (sampleIdsWithStatus != null && sampleIdsWithStatus.contains(sample.getSampleId())))
                .filter(sample -> visitId == null || (sample.getVisit() != null && visitId.equals(sample.getVisit().getVisitId())))
                .filter(sample -> expiryStatus == null || expiryStatus == sample.getExpiryStatus())
                .map(sample -> toDTO(sample, specimensBySampleId.getOrDefault(sample.getSampleId(), List.of())))
                .collect(Collectors.toList());
    }

    @Transactional
    public SampleDTO createSample(CreateSampleRequest request) {
        if (request.getVisitId() == null || !visitRepository.existsById(request.getVisitId())) {
            throw new IllegalArgumentException("Нельзя создать образец: данного визита не существует.");
        }
        if (request.getBarcode() != null && sampleRepository.existsByBarcode(request.getBarcode())) {
            throw new IllegalArgumentException("Образец с таким штрихкодом уже существует: " + request.getBarcode());
        }
        Sample sample = new Sample();
        applyRequest(sample, request);
        Sample saved = sampleRepository.save(sample);
        updateContainerCountsFromSpecimens(saved.getSpecimens());
        try {
            sampleTransactionService.recordSampleCreated(saved);
        } catch (Exception e) {}
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
                .map(s -> s.getContainer() != null ? s.getContainer().getContainerId() : null)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
        try {
            sampleTransactionService.clearSampleAndSpecimenReferences(sampleId);
        } catch (Exception e) {}
        applyRequest(sample, request);
        Sample saved = sampleRepository.save(sample);
        java.util.Set<Long> newContainerIds = saved.getSpecimens().stream()
                .map(s -> s.getContainer() != null ? s.getContainer().getContainerId() : null)
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
            sampleTransactionService.clearSampleAndSpecimenReferences(sampleId);
            sampleTransactionService.recordSampleWithdrawn(sample);
        } catch (Exception e) {}
        java.util.Set<Long> containerIds = sample.getSpecimens().stream()
                .map(s -> s.getContainer() != null ? s.getContainer().getContainerId() : null)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
        sampleRepository.delete(sample);
        containerIds.forEach(this::recalculateContainerCount);
    }

    private void updateContainerCountsFromSpecimens(List<Specimen> specimens) {
        specimens.stream()
                .map(s -> s.getContainer() != null ? s.getContainer().getContainerId() : null)
                .filter(Objects::nonNull)
                .distinct()
                .forEach(this::recalculateContainerCount);
    }

    private void recalculateContainerCount(Long containerId) {
        containerRepository.findById(containerId).ifPresent(container -> {
            long count = specimenRepository.countByContainer_ContainerId(containerId);
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
        sample.setVisit(visitRepository.findById(request.getVisitId())
                .orElseThrow(() -> new IllegalArgumentException("Визит не найден")));
        sample.setBarcode(request.getBarcode());
        sample.setSampleType(request.getSampleTypeId() != null
                ? sampleTypeRepository.findById(request.getSampleTypeId()).orElse(null)
                : null);
        sample.setInitialQuantity(request.getInitialQuantity());
        sample.setRecommendedStorageMonths(request.getRecommendedStorageMonths());
        if (request.getCollectionDate() != null) {
            sample.setCreatedAtSample(request.getCollectionDate());
        } else if (sample.getCreatedAtSample() == null) {
            visitRepository.findById(request.getVisitId())
                    .map(Visit::getCollectionDate)
                    .ifPresentOrElse(sample::setCreatedAtSample,
                            () -> sample.setCreatedAtSample(LocalDate.now().atTime(8, 0)));
        }
        sample.setContainer(request.getContainerId() != null
            ? containerRepository.findById(request.getContainerId()).orElse(null)
            : null);
        if (request.getContainerId() != null && sample.getContainer() == null) {
            throw new IllegalArgumentException("Контейнера с ID " + request.getContainerId() + " нет в хранилище");
        }
        applyStorageStatus(sample);

        sample.getSpecimens().clear();
        entityManager.flush();
        Long inStorageId = getInStorageStatusId();
        SampleStatus inStorageStatus = inStorageId != null ? sampleStatusRepository.findById(inStorageId).orElse(null) : null;
        if (request.getSpecimens() != null && !request.getSpecimens().isEmpty()) {
            // Validate container existence and position occupancy before adding specimens
            // Map containerId -> (position -> list of specimen barcodes requesting this position)
            java.util.Map<Long, java.util.Map<String, java.util.List<String>>> posMap = new java.util.HashMap<>();
            java.util.List<String> duplicatePositionsWithinRequest = new java.util.ArrayList<>();
            for (CreateSampleRequest.SpecimenItem item : request.getSpecimens()) {
                Long containerIdToUse = item.getContainerId() != null ? item.getContainerId() : request.getContainerId();
                if (containerIdToUse != null) {
                    if (!containerRepository.existsById(containerIdToUse)) {
                        throw new IllegalArgumentException("Контейнера с ID " + containerIdToUse + " нет в хранилище");
                    }
                }
                String pos = item.getPositionInContainer() != null ? item.getPositionInContainer().trim() : null;
                String specimenBarcode = item.getBarcode() != null ? item.getBarcode() : "";
                if (containerIdToUse != null && pos != null && !pos.isEmpty()) {
                    posMap.computeIfAbsent(containerIdToUse, k -> new java.util.HashMap<>());
                    java.util.Map<String, java.util.List<String>> inner = posMap.get(containerIdToUse);
                    if (!inner.containsKey(pos)) {
                        inner.put(pos, new java.util.ArrayList<>());
                    }
                    java.util.List<String> list = inner.get(pos);
                    if (list.contains(specimenBarcode)) {
                        // same barcode repeated for same position
                        duplicatePositionsWithinRequest.add(specimenBarcode);
                    }
                    list.add(specimenBarcode);
                }
            }

            java.util.Map<String, java.util.List<String>> occupiedPosToBarcodes = new java.util.HashMap<>();
            for (java.util.Map.Entry<Long, java.util.Map<String, java.util.List<String>>> e : posMap.entrySet()) {
                Long containerId = e.getKey();
                for (String pos : e.getValue().keySet()) {
                    boolean exists = specimenRepository.existsByContainer_ContainerIdAndPositionInContainer(containerId, pos);
                    if (exists) {
                        occupiedPosToBarcodes.put(pos, e.getValue().get(pos));
                    }
                }
            }
            if (!duplicatePositionsWithinRequest.isEmpty()) {
                throw new IllegalArgumentException("Дублирующиеся штрихкоды проб в запросе: " + String.join(", ", duplicatePositionsWithinRequest));
            }
            if (!occupiedPosToBarcodes.isEmpty()) {
                // Build message listing specimen barcodes and positions
                java.util.List<String> parts = new java.util.ArrayList<>();
                for (java.util.Map.Entry<String, java.util.List<String>> en : occupiedPosToBarcodes.entrySet()) {
                    String pos = en.getKey();
                    String barcodes = String.join(", ", en.getValue());
                    parts.add(barcodes + " (позиция: " + pos + ")");
                }
                throw new IllegalArgumentException("Не удалось разместить пробы: " + String.join("; ", parts) + ". Позиции уже заняты.");
            }

            for (CreateSampleRequest.SpecimenItem item : request.getSpecimens()) {
                Specimen specimen = new Specimen();
                specimen.setSample(sample);
                specimen.setBarcode(item.getBarcode());
                specimen.setSampleStatus(item.getSampleStatusId() != null
                        ? sampleStatusRepository.findById(item.getSampleStatusId()).orElse(inStorageStatus)
                        : inStorageStatus);
                Long containerIdToUse = item.getContainerId() != null ? item.getContainerId() : request.getContainerId();
                specimen.setContainer(containerIdToUse != null ? containerRepository.findById(containerIdToUse).orElse(null) : null);
                specimen.setPositionInContainer(item.getPositionInContainer());
                sample.getSpecimens().add(specimen);
            }
        }
        int currentQty = (int) sample.getSpecimens().stream()
                .filter(s -> inStorageId != null && s.getSampleStatus() != null && inStorageId.equals(s.getSampleStatus().getSampleStatusId()))
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
                        s.getSampleStatus() != null ? s.getSampleStatus().getSampleStatusId() : null,
                        s.getContainer() != null ? s.getContainer().getContainerId() : null,
                        s.getPositionInContainer()
                ))
                .collect(Collectors.toList());
        Long inStorageStatusId = getInStorageStatusId();
        int currentQuantity = (int) specimens.stream()
                .filter(s -> inStorageStatusId != null && s.getSampleStatus() != null && inStorageStatusId.equals(s.getSampleStatus().getSampleStatusId()))
                .count();
        return new SampleDTO(
                sample.getSampleId(),
                sample.getVisit() != null ? sample.getVisit().getVisitId() : null,
                sample.getBarcode(),
                sample.getSampleType() != null ? sample.getSampleType().getSampleTypeId() : null,
                sample.getInitialQuantity(),
                currentQuantity,
                sample.getRecommendedStorageMonths(),
                sample.getActualStorageMonths(),
                sample.getExpiryStatus(),
                sample.getContainer() != null ? sample.getContainer().getContainerId() : null,
                sample.getCreatedAtSample(),
                specimenDTOs
        );
    }

    private SampleDTO toDTO(Sample sample) {
        List<Specimen> specimens = sample.getSpecimens() != null ? sample.getSpecimens() : List.of();
        return toDTO(sample, specimens);
    }
}

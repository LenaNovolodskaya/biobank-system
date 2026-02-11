package ru.healthfamily.biobank.service;

import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.healthfamily.biobank.dto.AliquotDTO;
import ru.healthfamily.biobank.dto.CreateSampleRequest;
import ru.healthfamily.biobank.dto.SampleDTO;
import ru.healthfamily.biobank.model.Aliquot;
import ru.healthfamily.biobank.model.Sample;
import ru.healthfamily.biobank.model.SampleStatus;
import ru.healthfamily.biobank.model.Sample.ExpiryStatus;
import ru.healthfamily.biobank.repository.AliquotRepository;
import ru.healthfamily.biobank.repository.SampleRepository;
import ru.healthfamily.biobank.repository.SampleStatusRepository;
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
    private final AliquotRepository aliquotRepository;
    private final SampleStatusRepository sampleStatusRepository;
    private final VisitRepository visitRepository;
    private final EntityManager entityManager;

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
        List<Aliquot> allAliquots = aliquotRepository.findAllWithSample();
        java.util.Map<Long, List<Aliquot>> aliquotsBySampleId = allAliquots.stream()
                .filter(a -> a.getSample() != null)
                .collect(Collectors.groupingBy(a -> a.getSample().getSampleId()));
        java.util.Set<Long> sampleIdsWithStatus = sampleStatusId != null
                ? aliquotRepository.findSampleIdsBySampleStatusId(sampleStatusId) : null;
        return samples.stream()
                .filter(sample -> matchesSearch(sample, search, aliquotsBySampleId.getOrDefault(sample.getSampleId(), List.of())))
                .filter(sample -> containerId == null || containerId.equals(sample.getContainerId()))
                .filter(sample -> sampleTypeId == null || sampleTypeId.equals(sample.getSampleTypeId()))
                .filter(sample -> sampleStatusId == null || (sampleIdsWithStatus != null && sampleIdsWithStatus.contains(sample.getSampleId())))
                .filter(sample -> visitId == null || visitId.equals(sample.getVisitId()))
                .filter(sample -> expiryStatus == null || expiryStatus == sample.getExpiryStatus())
                .map(sample -> toDTO(sample, aliquotsBySampleId.getOrDefault(sample.getSampleId(), List.of())))
                .collect(Collectors.toList());
    }

    @Transactional
    public SampleDTO createSample(CreateSampleRequest request) {
        Sample sample = new Sample();
        applyRequest(sample, request);
        return toDTO(sampleRepository.save(sample));
    }

    @Transactional
    public SampleDTO updateSample(Long sampleId, CreateSampleRequest request) {
        Sample sample = sampleRepository.findById(sampleId)
                .orElseThrow(() -> new RuntimeException("Образец не найден"));
        applyRequest(sample, request);
        return toDTO(sampleRepository.save(sample));
    }

    @Transactional
    public void deleteSample(Long sampleId) {
        Sample sample = sampleRepository.findById(sampleId)
                .orElseThrow(() -> new RuntimeException("Образец не найден"));
        sampleRepository.delete(sample);
    }

    private boolean matchesSearch(Sample sample, String search, List<Aliquot> sampleAliquots) {
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
        return sampleAliquots.stream()
                .anyMatch(a -> (a.getBarcode() != null && a.getBarcode().toLowerCase(Locale.ROOT).contains(normalized))
                        || (a.getPositionInContainer() != null && a.getPositionInContainer().toLowerCase(Locale.ROOT).contains(normalized)));
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
                            () -> sample.setCreatedAtSample(LocalDateTime.now()));
        }
        sample.setContainerId(request.getContainerId());
        applyStorageStatus(sample);

        sample.getAliquots().clear();
        entityManager.flush();
        if (request.getAliquots() != null && !request.getAliquots().isEmpty()) {
            for (CreateSampleRequest.AliquotItem item : request.getAliquots()) {
                Aliquot aliquot = new Aliquot();
                aliquot.setSample(sample);
                aliquot.setBarcode(item.getBarcode());
                aliquot.setSampleStatusId(item.getSampleStatusId());
                aliquot.setContainerId(item.getContainerId() != null ? item.getContainerId() : request.getContainerId());
                aliquot.setPositionInContainer(item.getPositionInContainer());
                sample.getAliquots().add(aliquot);
            }
        }
        Long inStorageStatusId = getInStorageStatusId();
        int currentQty = (int) sample.getAliquots().stream()
                .filter(a -> inStorageStatusId != null && inStorageStatusId.equals(a.getSampleStatusId()))
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

    private SampleDTO toDTO(Sample sample, List<Aliquot> aliquots) {
        List<AliquotDTO> aliquotDTOs = aliquots.stream()
                .map(a -> new AliquotDTO(
                        a.getAliquotId(),
                        a.getBarcode(),
                        a.getSample() != null ? a.getSample().getSampleId() : null,
                        a.getSampleStatusId(),
                        a.getContainerId(),
                        a.getPositionInContainer()
                ))
                .collect(Collectors.toList());
        Long inStorageStatusId = getInStorageStatusId();
        int currentQuantity = (int) aliquots.stream()
                .filter(a -> inStorageStatusId != null && inStorageStatusId.equals(a.getSampleStatusId()))
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
                aliquotDTOs
        );
    }

    private SampleDTO toDTO(Sample sample) {
        List<Aliquot> aliquots = sample.getAliquots() != null ? sample.getAliquots() : List.of();
        return toDTO(sample, aliquots);
    }
}

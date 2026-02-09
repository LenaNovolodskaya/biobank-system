package ru.healthfamily.biobank.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.healthfamily.biobank.dto.CreateSampleRequest;
import ru.healthfamily.biobank.dto.SampleDTO;
import ru.healthfamily.biobank.model.Sample;
import ru.healthfamily.biobank.model.Sample.ExpiryStatus;
import ru.healthfamily.biobank.repository.SampleRepository;
import ru.healthfamily.biobank.repository.VisitRepository;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Period;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SampleService {

    private final SampleRepository sampleRepository;
    private final VisitRepository visitRepository;

    @Transactional(readOnly = true)
    public List<SampleDTO> getSamples(
            String search,
            Long containerId,
            Long sampleTypeId,
            Long sampleStatusId,
            Long visitId,
            ExpiryStatus expiryStatus
    ) {
        return sampleRepository.findAll().stream()
                .filter(sample -> matchesSearch(sample, search))
                .filter(sample -> containerId == null || containerId.equals(sample.getContainerId()))
                .filter(sample -> sampleTypeId == null || sampleTypeId.equals(sample.getSampleTypeId()))
                .filter(sample -> sampleStatusId == null || sampleStatusId.equals(sample.getSampleStatusId()))
                .filter(sample -> visitId == null || visitId.equals(sample.getVisitId()))
                .filter(sample -> expiryStatus == null || expiryStatus == sample.getExpiryStatus())
                .map(this::toDTO)
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

    private boolean matchesSearch(Sample sample, String search) {
        if (search == null || search.isBlank()) {
            return true;
        }
        String normalized = search.toLowerCase(Locale.ROOT);
        return (sample.getBarcode() != null && sample.getBarcode().toLowerCase(Locale.ROOT).contains(normalized))
                || (sample.getSampleId() != null && sample.getSampleId().toString().contains(normalized))
                || (sample.getPositionInContainer() != null
                && sample.getPositionInContainer().toLowerCase(Locale.ROOT).contains(normalized));
    }

    private void applyRequest(Sample sample, CreateSampleRequest request) {
        sample.setVisitId(request.getVisitId());
        sample.setBarcode(request.getBarcode());
        sample.setSampleTypeId(request.getSampleTypeId());
        sample.setInitialQuantity(request.getInitialQuantity());
        sample.setCurrentQuantity(request.getCurrentQuantity());
        sample.setRecommendedStorageMonths(request.getRecommendedStorageMonths());
        if (request.getCollectionDate() != null) {
            sample.setCreatedAtSample(request.getCollectionDate());
        } else if (sample.getCreatedAtSample() == null) {
            visitRepository.findById(request.getVisitId())
                    .map(visit -> visit.getCollectionDate())
                    .ifPresentOrElse(sample::setCreatedAtSample,
                            () -> sample.setCreatedAtSample(LocalDateTime.now()));
        }
        sample.setSampleStatusId(request.getSampleStatusId());
        sample.setTubeStatusIds(request.getTubeStatusIds());
        sample.setContainerId(request.getContainerId());
        sample.setPositionInContainer(request.getPositionInContainer());
        applyStorageStatus(sample);
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

    private SampleDTO toDTO(Sample sample) {
        return new SampleDTO(
                sample.getSampleId(),
                sample.getVisitId(),
                sample.getBarcode(),
                sample.getSampleTypeId(),
                sample.getInitialQuantity(),
                sample.getCurrentQuantity(),
                sample.getRecommendedStorageMonths(),
                sample.getActualStorageMonths(),
                sample.getExpiryStatus(),
                sample.getSampleStatusId(),
                sample.getTubeStatusIds(),
                sample.getContainerId(),
                sample.getPositionInContainer(),
                sample.getCreatedAtSample()
        );
    }
}

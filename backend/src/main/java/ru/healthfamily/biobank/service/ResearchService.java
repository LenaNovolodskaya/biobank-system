package ru.healthfamily.biobank.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.healthfamily.biobank.dto.CreateResearchRequest;
import ru.healthfamily.biobank.dto.ResearchDTO;
import ru.healthfamily.biobank.model.Research;
import ru.healthfamily.biobank.repository.ResearchRepository;
import ru.healthfamily.biobank.repository.SampleRepository;
import ru.healthfamily.biobank.repository.VisitRepository;
import java.util.List;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ResearchService {

    private final ResearchRepository researchRepository;
    private final VisitRepository visitRepository;
    private final SampleRepository sampleRepository;

    @Transactional
    public ResearchDTO createResearch(CreateResearchRequest request) {
        Research research = new Research();
        applyRequest(research, request);
        return toDTO(researchRepository.save(research));
    }

    @Transactional
    public ResearchDTO updateResearch(Long researchId, CreateResearchRequest request) {
        Research research = researchRepository.findById(researchId)
                .orElseThrow(() -> new RuntimeException("Исследование не найдено"));
        applyRequest(research, request);
        return toDTO(researchRepository.save(research));
    }

    @Transactional
    public void deleteResearch(Long researchId) {
        Research research = researchRepository.findById(researchId)
                .orElseThrow(() -> new RuntimeException("Исследование не найдено"));
        List<Long> visitIds = visitRepository.findByResearchId(researchId).stream()
                .map(visit -> visit.getVisitId())
                .collect(Collectors.toList());
        if (!visitIds.isEmpty()) {
            sampleRepository.deleteByVisitIdIn(visitIds);
        }
        visitRepository.deleteByResearchId(researchId);
        researchRepository.delete(research);
    }

    @Transactional(readOnly = true)
    public List<ResearchDTO> getAllResearches() {
        return researchRepository.findAll().stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    private void applyRequest(Research research, CreateResearchRequest request) {
        research.setResearchNumber(request.getResearchNumber());
        research.setResearchName(request.getResearchName());
        research.setResearchGroupId(request.getResearchGroupId());
        research.setFinancingSourceId(request.getFinancingSourceId());
        research.setDepartmentId(request.getDepartmentId());
        research.setIsActive(request.getIsActive() == null ? true : request.getIsActive());
    }

    private ResearchDTO toDTO(Research research) {
        return new ResearchDTO(
                research.getResearchId(),
                research.getResearchNumber(),
                research.getResearchName(),
                research.getResearchGroupId(),
                research.getFinancingSourceId(),
                research.getDepartmentId(),
                research.getIsActive()
        );
    }
}

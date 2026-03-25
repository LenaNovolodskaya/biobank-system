package ru.healthfamily.biobank.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.healthfamily.biobank.dto.CreateResearchRequest;
import ru.healthfamily.biobank.dto.ResearchDTO;
import ru.healthfamily.biobank.model.Department;
import ru.healthfamily.biobank.model.FinancingSource;
import ru.healthfamily.biobank.model.Research;
import ru.healthfamily.biobank.model.ResearchGroup;
import ru.healthfamily.biobank.repository.DepartmentRepository;
import ru.healthfamily.biobank.repository.FinancingSourceRepository;
import ru.healthfamily.biobank.repository.ResearchGroupRepository;
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
    private final ResearchGroupRepository researchGroupRepository;
    private final FinancingSourceRepository financingSourceRepository;
    private final DepartmentRepository departmentRepository;
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
        List<Long> visitIds = visitRepository.findByResearch_ResearchId(researchId).stream()
                .map(visit -> visit.getVisitId())
                .collect(Collectors.toList());
        if (!visitIds.isEmpty()) {
            sampleRepository.deleteByVisit_VisitIdIn(visitIds);
        }
        visitRepository.deleteByResearch_ResearchId(researchId);
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
        research.setResearchGroup(request.getResearchGroupId() != null
                ? researchGroupRepository.findById(request.getResearchGroupId()).orElse(null) : null);
        research.setFinancingSource(request.getFinancingSourceId() != null
                ? financingSourceRepository.findById(request.getFinancingSourceId()).orElse(null) : null);
        research.setDepartment(request.getDepartmentId() != null
                ? departmentRepository.findById(request.getDepartmentId()).orElse(null) : null);
        research.setIsActive(request.getIsActive() == null ? true : request.getIsActive());
    }

    private ResearchDTO toDTO(Research research) {
        return new ResearchDTO(
                research.getResearchId(),
                research.getResearchNumber(),
                research.getResearchName(),
                research.getResearchGroup() != null ? research.getResearchGroup().getResearchGroupId() : null,
                research.getFinancingSource() != null ? research.getFinancingSource().getFinancingSourceId() : null,
                research.getDepartment() != null ? research.getDepartment().getDepartmentId() : null,
                research.getIsActive()
        );
    }
}

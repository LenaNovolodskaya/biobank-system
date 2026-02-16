package ru.healthfamily.biobank.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.healthfamily.biobank.model.ContainerTypeTemplate;
import ru.healthfamily.biobank.model.Nationality;
import ru.healthfamily.biobank.model.UnitType;
import ru.healthfamily.biobank.model.Department;
import ru.healthfamily.biobank.model.Diagnosis;
import ru.healthfamily.biobank.model.FinancingSource;
import ru.healthfamily.biobank.model.ResearchGroup;
import ru.healthfamily.biobank.model.SampleStatus;
import ru.healthfamily.biobank.model.SampleType;
import ru.healthfamily.biobank.repository.DepartmentRepository;
import ru.healthfamily.biobank.repository.DiagnosisRepository;
import ru.healthfamily.biobank.repository.FinancingSourceRepository;
import ru.healthfamily.biobank.repository.ContainerTypeTemplateRepository;
import ru.healthfamily.biobank.repository.NationalityRepository;
import ru.healthfamily.biobank.repository.UnitTypeRepository;
import ru.healthfamily.biobank.repository.ResearchGroupRepository;
import ru.healthfamily.biobank.repository.SampleStatusRepository;
import ru.healthfamily.biobank.repository.SampleTypeRepository;
import java.util.Map;
import java.util.List;

@RestController
@RequestMapping("/references")
@RequiredArgsConstructor
public class ReferenceController {
    
    private final ContainerTypeTemplateRepository containerTypeTemplateRepository;
    private final UnitTypeRepository unitTypeRepository;
    private final NationalityRepository nationalityRepository;
    private final SampleTypeRepository sampleTypeRepository;
    private final SampleStatusRepository sampleStatusRepository;
    private final ResearchGroupRepository researchGroupRepository;
    private final FinancingSourceRepository financingSourceRepository;
    private final DepartmentRepository departmentRepository;
    private final DiagnosisRepository diagnosisRepository;
    
    @GetMapping("/nationalities")
    public ResponseEntity<List<Nationality>> getAllNationalities() {
        List<Nationality> nationalities = nationalityRepository.findAllByOrderByNationalityNameAsc();
        return ResponseEntity.ok(nationalities);
    }

    @PostMapping("/nationalities")
    public ResponseEntity<Nationality> createNationality(@RequestBody Map<String, String> body) {
        String name = body.getOrDefault("name", "").trim();
        Nationality nationality = new Nationality();
        nationality.setNationalityName(name);
        return ResponseEntity.ok(nationalityRepository.save(nationality));
    }

    @PutMapping("/nationalities/{id}")
    public ResponseEntity<Nationality> updateNationality(
            @PathVariable Long id,
            @RequestBody Map<String, String> body
    ) {
        Nationality nationality = nationalityRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Национальность не найдена"));
        nationality.setNationalityName(body.getOrDefault("name", "").trim());
        return ResponseEntity.ok(nationalityRepository.save(nationality));
    }

    @DeleteMapping("/nationalities/{id}")
    public ResponseEntity<Void> deleteNationality(@PathVariable Long id) {
        nationalityRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }
    
    @GetMapping("/container-templates")
    public ResponseEntity<List<ContainerTypeTemplate>> getContainerTemplates() {
        return ResponseEntity.ok(containerTypeTemplateRepository.findAll());
    }

    @PostMapping("/container-templates")
    public ResponseEntity<ContainerTypeTemplate> createContainerTemplate(@RequestBody java.util.Map<String, Object> body) {
        String name = String.valueOf(body.getOrDefault("templateName", "")).trim();
        if (name.isEmpty()) {
            throw new RuntimeException("Название шаблона обязательно");
        }
        Integer rows = body.get("rowsCount") != null ? ((Number) body.get("rowsCount")).intValue() : 1;
        Integer cols = body.get("columnsCount") != null ? ((Number) body.get("columnsCount")).intValue() : 1;
        String numbering = body.get("numberingType") != null ? String.valueOf(body.get("numberingType")) : "SEQUENTIAL";
        ContainerTypeTemplate t = new ContainerTypeTemplate();
        t.setTemplateName(name);
        t.setRowsCount(rows);
        t.setColumnsCount(cols);
        t.setMaxSamplesCount(rows * cols);
        t.setNumberingType(numbering);
        return ResponseEntity.ok(containerTypeTemplateRepository.save(t));
    }

    @PutMapping("/container-templates/{id}")
    public ResponseEntity<ContainerTypeTemplate> updateContainerTemplate(
            @PathVariable Long id,
            @RequestBody java.util.Map<String, Object> body
    ) {
        ContainerTypeTemplate t = containerTypeTemplateRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Шаблон контейнера не найден"));
        String name = String.valueOf(body.getOrDefault("templateName", "")).trim();
        if (name.isEmpty()) {
            throw new RuntimeException("Название шаблона обязательно");
        }
        Integer rows = body.get("rowsCount") != null ? ((Number) body.get("rowsCount")).intValue() : t.getRowsCount();
        Integer cols = body.get("columnsCount") != null ? ((Number) body.get("columnsCount")).intValue() : t.getColumnsCount();
        String numbering = body.get("numberingType") != null ? String.valueOf(body.get("numberingType")) : t.getNumberingType();
        t.setTemplateName(name);
        t.setRowsCount(rows);
        t.setColumnsCount(cols);
        t.setMaxSamplesCount(rows * cols);
        t.setNumberingType(numbering);
        return ResponseEntity.ok(containerTypeTemplateRepository.save(t));
    }

    @DeleteMapping("/container-templates/{id}")
    public ResponseEntity<Void> deleteContainerTemplate(@PathVariable Long id) {
        containerTypeTemplateRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/genders")
    public ResponseEntity<List<String>> getAllGenders() {
        List<String> genders = List.of("UNKNOWN", "MALE", "FEMALE");
        return ResponseEntity.ok(genders);
    }

    @GetMapping("/unit-types")
    public ResponseEntity<List<UnitType>> getUnitTypes() {
        return ResponseEntity.ok(unitTypeRepository.findAll());
    }

    @PostMapping("/unit-types")
    public ResponseEntity<UnitType> createUnitType(@RequestBody Map<String, String> body) {
        String name = body.getOrDefault("name", "").trim();
        UnitType unitType = new UnitType();
        unitType.setUnitTypeName(name);
        return ResponseEntity.ok(unitTypeRepository.save(unitType));
    }

    @PutMapping("/unit-types/{id}")
    public ResponseEntity<UnitType> updateUnitType(
            @PathVariable Long id,
            @RequestBody Map<String, String> body
    ) {
        UnitType unitType = unitTypeRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Тип хранилища не найден"));
        unitType.setUnitTypeName(body.getOrDefault("name", "").trim());
        return ResponseEntity.ok(unitTypeRepository.save(unitType));
    }

    @DeleteMapping("/unit-types/{id}")
    public ResponseEntity<Void> deleteUnitType(@PathVariable Long id) {
        unitTypeRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/sample-types")
    public ResponseEntity<List<SampleType>> getSampleTypes() {
        return ResponseEntity.ok(sampleTypeRepository.findAll());
    }

    @PostMapping("/sample-types")
    public ResponseEntity<SampleType> createSampleType(@RequestBody Map<String, String> body) {
        String name = body.getOrDefault("name", "").trim();
        String iconPath = body.getOrDefault("iconPath", "").trim();
        SampleType type = new SampleType();
        type.setSampleTypeName(name);
        type.setIconPath(iconPath.isEmpty() ? null : iconPath);
        return ResponseEntity.ok(sampleTypeRepository.save(type));
    }

    @PutMapping("/sample-types/{id}")
    public ResponseEntity<SampleType> updateSampleType(
            @PathVariable Long id,
            @RequestBody Map<String, String> body
    ) {
        SampleType type = sampleTypeRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Тип образца не найден"));
        type.setSampleTypeName(body.getOrDefault("name", "").trim());
        type.setIconPath(body.getOrDefault("iconPath", "").trim().isEmpty() ? null : body.getOrDefault("iconPath", "").trim());
        return ResponseEntity.ok(sampleTypeRepository.save(type));
    }

    @DeleteMapping("/sample-types/{id}")
    public ResponseEntity<Void> deleteSampleType(@PathVariable Long id) {
        sampleTypeRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/sample-statuses")
    public ResponseEntity<List<SampleStatus>> getSampleStatuses() {
        return ResponseEntity.ok(sampleStatusRepository.findAll());
    }

    @GetMapping("/research-groups")
    public ResponseEntity<List<ResearchGroup>> getResearchGroups() {
        return ResponseEntity.ok(researchGroupRepository.findAll());
    }

    @PostMapping("/research-groups")
    public ResponseEntity<ResearchGroup> createResearchGroup(@RequestBody Map<String, String> body) {
        String name = body.getOrDefault("name", "").trim();
        ResearchGroup group = new ResearchGroup();
        group.setResearchGroupName(name);
        return ResponseEntity.ok(researchGroupRepository.save(group));
    }

    @PutMapping("/research-groups/{id}")
    public ResponseEntity<ResearchGroup> updateResearchGroup(
            @PathVariable Long id,
            @RequestBody Map<String, String> body
    ) {
        ResearchGroup group = researchGroupRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Группа исследования не найдена"));
        group.setResearchGroupName(body.getOrDefault("name", "").trim());
        return ResponseEntity.ok(researchGroupRepository.save(group));
    }

    @DeleteMapping("/research-groups/{id}")
    public ResponseEntity<Void> deleteResearchGroup(@PathVariable Long id) {
        researchGroupRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/financing-sources")
    public ResponseEntity<List<FinancingSource>> getFinancingSources() {
        return ResponseEntity.ok(financingSourceRepository.findAll());
    }

    @PostMapping("/financing-sources")
    public ResponseEntity<FinancingSource> createFinancingSource(@RequestBody Map<String, String> body) {
        String name = body.getOrDefault("name", "").trim();
        FinancingSource source = new FinancingSource();
        source.setFinancingSourceName(name);
        return ResponseEntity.ok(financingSourceRepository.save(source));
    }

    @PutMapping("/financing-sources/{id}")
    public ResponseEntity<FinancingSource> updateFinancingSource(
            @PathVariable Long id,
            @RequestBody Map<String, String> body
    ) {
        FinancingSource source = financingSourceRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Источник финансирования не найден"));
        source.setFinancingSourceName(body.getOrDefault("name", "").trim());
        return ResponseEntity.ok(financingSourceRepository.save(source));
    }

    @DeleteMapping("/financing-sources/{id}")
    public ResponseEntity<Void> deleteFinancingSource(@PathVariable Long id) {
        financingSourceRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/departments")
    public ResponseEntity<List<Department>> getDepartments() {
        return ResponseEntity.ok(departmentRepository.findAll());
    }

    @PostMapping("/departments")
    public ResponseEntity<Department> createDepartment(@RequestBody Map<String, String> body) {
        String name = body.getOrDefault("name", "").trim();
        Department department = new Department();
        department.setDepartmentName(name);
        return ResponseEntity.ok(departmentRepository.save(department));
    }

    @PutMapping("/departments/{id}")
    public ResponseEntity<Department> updateDepartment(
            @PathVariable Long id,
            @RequestBody Map<String, String> body
    ) {
        Department department = departmentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Подразделение не найдено"));
        department.setDepartmentName(body.getOrDefault("name", "").trim());
        return ResponseEntity.ok(departmentRepository.save(department));
    }

    @DeleteMapping("/departments/{id}")
    public ResponseEntity<Void> deleteDepartment(@PathVariable Long id) {
        departmentRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/diagnoses")
    public ResponseEntity<List<Diagnosis>> getDiagnoses() {
        return ResponseEntity.ok(diagnosisRepository.findAll());
    }

    @PostMapping("/diagnoses")
    public ResponseEntity<Diagnosis> createDiagnosis(@RequestBody Map<String, String> body) {
        Diagnosis diagnosis = new Diagnosis();
        diagnosis.setIcd10Code(body.getOrDefault("icd10Code", "").trim());
        diagnosis.setDiagnosisName(body.getOrDefault("diagnosisName", "").trim());
        return ResponseEntity.ok(diagnosisRepository.save(diagnosis));
    }

    @PutMapping("/diagnoses/{id}")
    public ResponseEntity<Diagnosis> updateDiagnosis(
            @PathVariable Long id,
            @RequestBody Map<String, String> body
    ) {
        Diagnosis diagnosis = diagnosisRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Диагноз не найден"));
        diagnosis.setIcd10Code(body.getOrDefault("icd10Code", "").trim());
        diagnosis.setDiagnosisName(body.getOrDefault("diagnosisName", "").trim());
        return ResponseEntity.ok(diagnosisRepository.save(diagnosis));
    }

    @DeleteMapping("/diagnoses/{id}")
    public ResponseEntity<Void> deleteDiagnosis(@PathVariable Long id) {
        diagnosisRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}
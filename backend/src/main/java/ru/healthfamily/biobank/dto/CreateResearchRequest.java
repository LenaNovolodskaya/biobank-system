package ru.healthfamily.biobank.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class CreateResearchRequest {

    @NotBlank(message = "Номер исследования обязателен")
    private String researchNumber;

    @NotBlank(message = "Название исследования обязательно")
    private String researchName;

    private Long researchGroupId;
    private Long financingSourceId;
    private Long departmentId;
    private Boolean isActive;
}

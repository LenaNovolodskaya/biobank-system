package ru.healthfamily.biobank.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ResearchDTO {
    private Long researchId;
    private String researchNumber;
    private String researchName;
    private Long researchGroupId;
    private Long financingSourceId;
    private Long departmentId;
    private Boolean isActive;
}

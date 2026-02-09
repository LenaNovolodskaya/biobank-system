package ru.healthfamily.biobank.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class VisitDTO {
    private Long visitId;
    private Long patientId;
    private Long researchId;
    private Integer visitNumber;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime collectionDate;

    private Integer ageAtCollection;
    private Long diagnosisId;
    private List<Long> comorbidDiagnosisIds;
}

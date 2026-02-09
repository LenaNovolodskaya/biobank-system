package ru.healthfamily.biobank.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class CreateVisitRequest {

    @NotNull(message = "Пациент обязателен")
    private Long patientId;

    @NotNull(message = "Исследование обязательно")
    private Long researchId;

    @NotNull(message = "Номер визита обязателен")
    @Min(value = 1, message = "Номер визита должен быть больше 0")
    private Integer visitNumber;

    @NotNull(message = "Дата забора обязательна")
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm")
    private LocalDateTime collectionDate;

    @NotNull(message = "Возраст на момент забора обязателен")
    @Min(value = 0, message = "Возраст не может быть отрицательным")
    private Integer ageAtCollection;

    private Long diagnosisId;

    private List<Long> comorbidDiagnosisIds;
}

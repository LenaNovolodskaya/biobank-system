package ru.healthfamily.biobank.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class SpecimenActionRequest {
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm")
    @NotNull(message = "Дата и время операции обязательны")
    private LocalDateTime transactionDate;

    private Long departmentId;
    private String purpose;
    private String notes;
}

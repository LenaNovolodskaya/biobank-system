package ru.healthfamily.biobank.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class CreateSampleTransactionRequest {
    @NotNull(message = "Образец обязателен")
    private Long sampleId;

    @NotNull(message = "Тип операции обязателен")
    private Long transactionTypeId;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm")
    @NotNull(message = "Дата операции обязательна")
    private LocalDateTime transactionDate;

    private Long departmentId;
    private String purpose;
    private String notes;
}

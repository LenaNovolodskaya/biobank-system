package ru.healthfamily.biobank.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SampleTransactionDTO {
    private Long transactionId;
    private Long sampleId;
    private Long specimenId;
    private String sampleBarcode;
    private String specimenBarcode;
    private Long userId;
    private String userFullName;
    private Long transactionTypeId;
    private String transactionTypeName;
    private String operationSign; // "+" for created, "-" for withdrawn

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime transactionDate;

    private Long departmentId;
    private String departmentName;
    private String purpose;
    private String notes;
}

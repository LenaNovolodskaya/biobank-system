package ru.healthfamily.biobank.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;
import lombok.Data;
import ru.healthfamily.biobank.model.Patient.Gender;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class CreatePatientRequest {
    
    @NotBlank(message = "Штрихкод пациента обязателен")
    private String patientBarcode;
    
    @NotNull(message = "Пол пациента обязателен")
    private Gender gender;
    
    @NotNull(message = "Дата рождения обязательна")
    @Past(message = "Дата рождения должна быть в прошлом")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate birthDate;
    
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm")
    private LocalDateTime createdAtPatient;

    private Long nationalityId;

    private Long mainDiagnosisId;
    
    private Boolean informedConsent;
}
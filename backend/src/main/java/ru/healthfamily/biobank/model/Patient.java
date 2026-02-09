package ru.healthfamily.biobank.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "patients")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Patient {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "patient_id")
    private Long patientId;
    
    @Column(name = "patient_barcode", unique = true, nullable = false)
    private String patientBarcode;
    
    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    @Column(name = "gender", columnDefinition = "gender_code_type")
    private Gender gender = Gender.UNKNOWN;
    
    @Column(name = "birth_date", nullable = false)
    private LocalDate birthDate;
    
    @ManyToOne
    @JoinColumn(name = "nationality_id")
    private Nationality nationality;

    @Column(name = "main_diagnosis_id")
    private Long mainDiagnosisId;
    
    @Column(name = "created_at_patient")
    private LocalDateTime createdAtPatient;
    
    @Column(name = "informed_consent")
    private Boolean informedConsent;
    
    public enum Gender {
        UNKNOWN, MALE, FEMALE
    }
}
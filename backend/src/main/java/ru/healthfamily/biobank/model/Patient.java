package ru.healthfamily.biobank.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

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
    @Column(name = "gender", length = 20)
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

    @ElementCollection
    @CollectionTable(name = "patient_comorbid_diagnoses", joinColumns = @JoinColumn(name = "patient_id"))
    @Column(name = "diagnosis_id")
    private List<Long> comorbidDiagnosisIds = new ArrayList<>();
    
    public enum Gender {
        UNKNOWN, MALE, FEMALE
    }
}
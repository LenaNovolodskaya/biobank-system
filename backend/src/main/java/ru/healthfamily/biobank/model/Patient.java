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
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "nationality_id")
    private Nationality nationality;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "main_diagnosis_id")
    private Diagnosis mainDiagnosis;

    @Column(name = "created_at_patient")
    private LocalDateTime createdAtPatient;

    @Column(name = "informed_consent")
    private Boolean informedConsent;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "patient_comorbid_diagnoses",
            joinColumns = @JoinColumn(name = "patient_id"),
            inverseJoinColumns = @JoinColumn(name = "diagnosis_id")
    )
    private List<Diagnosis> comorbidDiagnoses = new ArrayList<>();

    @OneToMany(mappedBy = "patient", fetch = FetchType.LAZY)
    private List<Visit> visits = new ArrayList<>();

    public enum Gender {
        UNKNOWN, MALE, FEMALE
    }
}
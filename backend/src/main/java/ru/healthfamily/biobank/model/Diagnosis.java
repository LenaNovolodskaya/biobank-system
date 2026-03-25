package ru.healthfamily.biobank.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "diagnoses")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Diagnosis {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "diagnosis_id")
    private Long diagnosisId;

    @Column(name = "icd10_code")
    private String icd10Code;

    @Column(name = "diagnosis_name", nullable = false)
    private String diagnosisName;

    /** Пациенты с этим диагнозом как основным */
    @OneToMany(mappedBy = "mainDiagnosis", fetch = FetchType.LAZY)
    @JsonIgnore
    private List<Patient> patientsWithMainDiagnosis = new ArrayList<>();

    /** Пациенты с этим диагнозом как сопутствующим (через patient_comorbid_diagnoses) */
    @ManyToMany(mappedBy = "comorbidDiagnoses", fetch = FetchType.LAZY)
    @JsonIgnore
    private List<Patient> patientsWithComorbidDiagnosis = new ArrayList<>();
}

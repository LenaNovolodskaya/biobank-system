package ru.healthfamily.biobank.model;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "visits")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Visit {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "visit_id")
    private Long visitId;

    @Column(name = "patient_id", nullable = false)
    private Long patientId;

    @Column(name = "research_id", nullable = false)
    private Long researchId;

    @Column(name = "visit_number", nullable = false)
    private Integer visitNumber;

    @Column(name = "collection_date", nullable = false)
    private LocalDateTime collectionDate;

    @Column(name = "age_at_collection", nullable = false)
    private Integer ageAtCollection;

    @Column(name = "diagnosis_id")
    private Long diagnosisId;

    @ElementCollection
    @CollectionTable(name = "visit_comorbid_diagnoses", joinColumns = @JoinColumn(name = "visit_id"))
    @Column(name = "diagnosis_id")
    private List<Long> comorbidDiagnosisIds = new ArrayList<>();
}

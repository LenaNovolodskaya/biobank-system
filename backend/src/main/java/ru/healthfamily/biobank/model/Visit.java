package ru.healthfamily.biobank.model;

import jakarta.persistence.*;
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

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "patient_id", nullable = false)
    private Patient patient;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "research_id", nullable = false)
    private Research research;

    @Column(name = "visit_number", nullable = false)
    private Integer visitNumber;

    @Column(name = "collection_date", nullable = false)
    private LocalDateTime collectionDate;

    @Column(name = "age_at_collection", nullable = false)
    private Integer ageAtCollection;

    @OneToMany(mappedBy = "visit", fetch = FetchType.LAZY)
    private List<Sample> samples = new ArrayList<>();
}

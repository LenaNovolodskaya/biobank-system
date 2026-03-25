package ru.healthfamily.biobank.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "financing_sources")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class FinancingSource {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "financing_source_id")
    private Long financingSourceId;

    @Column(name = "financing_source_name", nullable = false)
    private String financingSourceName;

    @OneToMany(mappedBy = "financingSource", fetch = FetchType.LAZY)
    @JsonIgnore
    private List<Research> researches = new ArrayList<>();
}

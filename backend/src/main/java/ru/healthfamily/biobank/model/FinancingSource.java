package ru.healthfamily.biobank.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

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
}

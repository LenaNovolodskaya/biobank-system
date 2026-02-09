package ru.healthfamily.biobank.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Entity
@Table(name = "sample_types")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SampleType {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "sample_type_id")
    private Long sampleTypeId;

    @Column(name = "sample_type_name", nullable = false)
    private String sampleTypeName;
}

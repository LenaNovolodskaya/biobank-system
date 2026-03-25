package ru.healthfamily.biobank.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.util.ArrayList;
import java.util.List;

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

    @Column(name = "sample_type_name", nullable = false, unique = true)
    private String sampleTypeName;

    @Column(name = "icon_path")
    private String iconPath;

    @OneToMany(mappedBy = "sampleType", fetch = FetchType.LAZY)
    @JsonIgnore
    private List<Sample> samples = new ArrayList<>();
}

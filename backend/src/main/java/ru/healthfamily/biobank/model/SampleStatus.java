package ru.healthfamily.biobank.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "sample_statuses")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SampleStatus {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "sample_status_id")
    private Long sampleStatusId;

    @Column(name = "sample_status_name", nullable = false, unique = true)
    private String sampleStatusName;

    @OneToMany(mappedBy = "sampleStatus", fetch = FetchType.LAZY)
    @JsonIgnore
    private List<Specimen> specimens = new ArrayList<>();
}

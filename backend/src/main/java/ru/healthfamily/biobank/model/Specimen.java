package ru.healthfamily.biobank.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "specimens")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Specimen {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "specimen_id")
    private Long specimenId;

    @Column(name = "barcode", nullable = false, unique = true)
    private String barcode;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sample_id", nullable = false)
    private Sample sample;

    @Column(name = "sample_status_id")
    private Long sampleStatusId;

    @Column(name = "container_id")
    private Long containerId;

    @Column(name = "position_in_container")
    private String positionInContainer;
}

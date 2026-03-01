package ru.healthfamily.biobank.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Entity
@Table(name = "storage_containers")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class StorageContainer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "container_id")
    private Long containerId;

    @Column(name = "container_number", length = 255)
    private String containerNumber;

    @Column(name = "current_samples_count", nullable = false)
    private Integer currentSamplesCount = 0;

    @ManyToOne(optional = false)
    @JoinColumn(name = "unit_id", nullable = false)
    private StorageUnit unit;

    @Column(name = "shelf_number")
    private Integer shelfNumber;

    /** Позиция на полке (порядок отображения). null = сортировать по container_number */
    @Column(name = "shelf_position")
    private Integer shelfPosition;

    @ManyToOne(optional = false)
    @JoinColumn(name = "template_id", nullable = false)
    private ContainerTypeTemplate template;
}

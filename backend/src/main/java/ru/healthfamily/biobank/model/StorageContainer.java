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

    @ManyToOne(optional = false)
    @JoinColumn(name = "unit_id", nullable = false)
    private StorageUnit unit;

    @Column(name = "shelf_number")
    private String shelfNumber;

    @Column(name = "container_type")
    private String containerType;

    @Column(name = "container_number")
    private Integer containerNumber;

    @Column(name = "rows_count")
    private Integer rowsCount;

    @Column(name = "columns_count")
    private Integer columnsCount;

    @Column(name = "max_samples_count", nullable = false)
    private Integer maxSamplesCount;

    @Column(name = "current_samples_count", nullable = false)
    private Integer currentSamplesCount = 0;

    /** Тип нумерации: LETTER_DIGIT (A1), DIGIT_LETTER (1A), DIGIT_DIGIT (1/1), SEQUENTIAL (1-25) */
    @Column(name = "numbering_type", length = 20)
    private String numberingType;
}

package ru.healthfamily.biobank.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "samples")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Sample {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "sample_id")
    private Long sampleId;

    @Column(name = "visit_id", nullable = false)
    private Long visitId;

    @Column(name = "barcode", nullable = false, unique = true)
    private String barcode;

    @Column(name = "sample_type_id")
    private Long sampleTypeId;

    @Column(name = "initial_quantity", nullable = false)
    private Integer initialQuantity;

    @Column(name = "current_quantity", nullable = false)
    private Integer currentQuantity;

    @Column(name = "recommended_storage_months")
    private Integer recommendedStorageMonths;

    @Column(name = "actual_storage_months")
    private Integer actualStorageMonths;

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    @Column(name = "expiry_status", columnDefinition = "expirystatus")
    private ExpiryStatus expiryStatus = ExpiryStatus.GREEN;

    @Column(name = "container_id")
    private Long containerId;

    @Column(name = "created_at_sample")
    private LocalDateTime createdAtSample;

    @OneToMany(mappedBy = "sample", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<Aliquot> aliquots = new ArrayList<>();

    public enum ExpiryStatus {
        GREEN, YELLOW, RED
    }
}

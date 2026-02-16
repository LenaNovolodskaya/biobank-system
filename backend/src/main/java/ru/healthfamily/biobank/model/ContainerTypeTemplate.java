package ru.healthfamily.biobank.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

/**
 * Шаблон типа контейнера.
 * Типы нумерации: LETTER_DIGIT (A1..E5), DIGIT_LETTER (1A..5E),
 * DIGIT_DIGIT (1/1..5/5), SEQUENTIAL (1..25).
 */
@Entity
@Table(name = "container_type_templates", uniqueConstraints = {
    @UniqueConstraint(columnNames = "template_name")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ContainerTypeTemplate {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "template_id")
    private Long templateId;

    @Column(name = "template_name", nullable = false)
    private String templateName;

    @Column(name = "rows_count", nullable = false)
    private Integer rowsCount;

    @Column(name = "columns_count", nullable = false)
    private Integer columnsCount;

    @Column(name = "max_samples_count", nullable = false)
    private Integer maxSamplesCount;

    @Column(name = "numbering_type", nullable = false, length = 20)
    private String numberingType; // LETTER_DIGIT, DIGIT_LETTER, DIGIT_DIGIT, SEQUENTIAL
}

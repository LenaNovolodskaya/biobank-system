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
@Table(name = "researches")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Research {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "research_id")
    private Long researchId;

    @Column(name = "research_number", nullable = false)
    private String researchNumber;

    @Column(name = "research_name", nullable = false)
    private String researchName;

    @Column(name = "research_group_id")
    private Long researchGroupId;

    @Column(name = "financing_source_id")
    private Long financingSourceId;

    @Column(name = "department_id")
    private Long departmentId;

    @Column(name = "is_active")
    private Boolean isActive = true;
}

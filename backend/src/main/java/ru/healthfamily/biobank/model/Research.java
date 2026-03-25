package ru.healthfamily.biobank.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

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

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "research_group_id")
    private ResearchGroup researchGroup;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "financing_source_id")
    private FinancingSource financingSource;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "department_id")
    private Department department;

    @Column(name = "is_active")
    private Boolean isActive = true;

    @OneToMany(mappedBy = "research", fetch = FetchType.LAZY)
    private List<Visit> visits = new ArrayList<>();
}

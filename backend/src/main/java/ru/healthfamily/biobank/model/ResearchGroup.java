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
@Table(name = "research_groups")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ResearchGroup {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "research_group_id")
    private Long researchGroupId;

    @Column(name = "research_group_name", nullable = false)
    private String researchGroupName;
}

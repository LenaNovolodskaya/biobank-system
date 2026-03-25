package ru.healthfamily.biobank.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

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

    @OneToMany(mappedBy = "researchGroup", fetch = FetchType.LAZY)
    @JsonIgnore
    private List<Research> researches = new ArrayList<>();
}

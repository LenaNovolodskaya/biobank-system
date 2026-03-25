package ru.healthfamily.biobank.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "nationalities")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Nationality {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "nationality_id")
    private Long nationalityId;

    @Column(name = "nationality_name", unique = true, nullable = false)
    private String nationalityName;

    @OneToMany(mappedBy = "nationality", fetch = FetchType.LAZY)
    @JsonIgnore
    private List<Patient> patients = new ArrayList<>();
}
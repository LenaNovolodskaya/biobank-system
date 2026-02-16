package ru.healthfamily.biobank.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Entity
@Table(name = "unit_types")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UnitType {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "unit_type_id")
    private Long unitTypeId;

    @Column(name = "unit_type_name", nullable = false, unique = true)
    private String unitTypeName;
}

package ru.healthfamily.biobank.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.healthfamily.biobank.model.SampleType;

@Repository
public interface SampleTypeRepository extends JpaRepository<SampleType, Long> {
}

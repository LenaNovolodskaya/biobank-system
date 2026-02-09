package ru.healthfamily.biobank.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.healthfamily.biobank.model.StorageUnit;

@Repository
public interface StorageUnitRepository extends JpaRepository<StorageUnit, Long> {
}

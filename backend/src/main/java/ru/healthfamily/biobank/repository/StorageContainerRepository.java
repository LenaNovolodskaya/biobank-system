package ru.healthfamily.biobank.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.healthfamily.biobank.model.StorageContainer;

@Repository
public interface StorageContainerRepository extends JpaRepository<StorageContainer, Long> {
}

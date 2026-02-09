package ru.healthfamily.biobank.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.healthfamily.biobank.model.FinancingSource;

@Repository
public interface FinancingSourceRepository extends JpaRepository<FinancingSource, Long> {
}

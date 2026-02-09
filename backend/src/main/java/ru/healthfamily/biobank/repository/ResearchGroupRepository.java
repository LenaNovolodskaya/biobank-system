package ru.healthfamily.biobank.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.healthfamily.biobank.model.ResearchGroup;

@Repository
public interface ResearchGroupRepository extends JpaRepository<ResearchGroup, Long> {
}

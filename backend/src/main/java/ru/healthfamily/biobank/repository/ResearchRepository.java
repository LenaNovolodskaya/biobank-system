package ru.healthfamily.biobank.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.healthfamily.biobank.model.Research;

@Repository
public interface ResearchRepository extends JpaRepository<Research, Long> {
}

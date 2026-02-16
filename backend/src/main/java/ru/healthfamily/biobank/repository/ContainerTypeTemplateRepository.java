package ru.healthfamily.biobank.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.healthfamily.biobank.model.ContainerTypeTemplate;

import java.util.Optional;

@Repository
public interface ContainerTypeTemplateRepository extends JpaRepository<ContainerTypeTemplate, Long> {

    Optional<ContainerTypeTemplate> findByTemplateNameIgnoreCase(String templateName);
}

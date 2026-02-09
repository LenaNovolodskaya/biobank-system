package ru.healthfamily.biobank.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.healthfamily.biobank.model.Department;

@Repository
public interface DepartmentRepository extends JpaRepository<Department, Long> {
}

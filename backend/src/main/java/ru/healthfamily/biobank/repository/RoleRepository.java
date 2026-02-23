package ru.healthfamily.biobank.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.healthfamily.biobank.model.Role;

import java.util.Optional;

public interface RoleRepository extends JpaRepository<Role, Long> {
    Optional<Role> findByRoleName(String roleName);
}

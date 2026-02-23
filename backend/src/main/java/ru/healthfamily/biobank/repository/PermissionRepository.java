package ru.healthfamily.biobank.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.healthfamily.biobank.model.Permission;

import java.util.Optional;

public interface PermissionRepository extends JpaRepository<Permission, Long> {
    Optional<Permission> findByPermissionName(String permissionName);
}

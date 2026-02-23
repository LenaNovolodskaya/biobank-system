package ru.healthfamily.biobank.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.healthfamily.biobank.model.UserPermissionOverride;

import java.util.List;

public interface UserPermissionOverrideRepository extends JpaRepository<UserPermissionOverride, Long> {
    List<UserPermissionOverride> findByUserUserId(Long userId);
    void deleteByUserUserId(Long userId);
}


package ru.healthfamily.biobank.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import ru.healthfamily.biobank.model.UserPermissionOverride;

import java.util.List;

public interface UserPermissionOverrideRepository extends JpaRepository<UserPermissionOverride, Long> {
    List<UserPermissionOverride> findByUserUserId(Long userId);

    @Modifying
    @Query("DELETE FROM UserPermissionOverride o WHERE o.user.userId = :userId")
    void deleteByUserUserId(Long userId);
}


package ru.healthfamily.biobank.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import ru.healthfamily.biobank.model.Permission;
import ru.healthfamily.biobank.model.Role;
import ru.healthfamily.biobank.repository.PermissionRepository;
import ru.healthfamily.biobank.repository.RoleRepository;

import java.util.Set;
import java.util.stream.Collectors;

/**
 * Добавляет недостающие разрешения в БД и обновляет роль Администратор.
 * Выполняется после AuthDataInitializer.
 */
@Component
@RequiredArgsConstructor
@Slf4j
@Order(2)
public class PermissionMigration implements CommandLineRunner {

    private final PermissionRepository permissionRepository;
    private final RoleRepository roleRepository;

    private static final Set<String> ALL_PERMISSIONS = Set.of(
            "patient.view", "patient.create", "patient.update", "patient.delete",
            "visit.view", "visit.create", "visit.update", "visit.delete",
            "sample.view", "sample.create", "sample.update", "sample.delete",
            "research.view", "research.create", "research.update", "research.delete",
            "storage.view", "storage.create", "storage.update", "storage.delete",
            "reference.view", "reference.create", "reference.update", "reference.delete",
            "reference.manage",
            "role.view", "role.create", "role.delete", "role.permissions.manage", "role.manage",
            "user.view", "user.create", "user.delete", "user.permissions.manage", "user.roles.assign", "user.manage"
    );

    @Override
    @Transactional
    public void run(String... args) {
        var existing = permissionRepository.findAll().stream()
                .map(Permission::getPermissionName)
                .collect(Collectors.toSet());

        int added = 0;
        for (String name : ALL_PERMISSIONS) {
            if (!existing.contains(name)) {
                Permission p = new Permission();
                p.setPermissionName(name);
                permissionRepository.save(p);
                added++;
            }
        }
        if (added > 0) {
            log.info("Добавлено {} новых разрешений", added);
        }

        roleRepository.findByRoleName("Администратор").ifPresent(admin -> {
            var allPerms = permissionRepository.findAll();
            var currentNames = admin.getPermissions().stream()
                    .map(Permission::getPermissionName)
                    .collect(Collectors.toSet());
            boolean updated = false;
            for (Permission p : allPerms) {
                if (!currentNames.contains(p.getPermissionName())) {
                    admin.getPermissions().add(p);
                    updated = true;
                }
            }
            if (updated) {
                roleRepository.save(admin);
                log.info("Роль Администратор обновлена: добавлены новые разрешения");
            }
        });

        // Роль "Лаборант" — справочники только для просмотра (reference.view)
        roleRepository.findByRoleName("Лаборант").ifPresent(lab -> {
            var refPermsToRemove = Set.of(
                    "reference.create", "reference.update", "reference.delete", "reference.manage"
            );
            boolean updated = lab.getPermissions().removeIf(
                    p -> refPermsToRemove.contains(p.getPermissionName())
            );
            if (updated) {
                roleRepository.save(lab);
                log.info("Роль Лаборант обновлена: справочники — только просмотр");
            }
        });
    }
}

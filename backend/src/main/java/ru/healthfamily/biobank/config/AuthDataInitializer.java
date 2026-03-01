package ru.healthfamily.biobank.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import ru.healthfamily.biobank.model.Permission;
import ru.healthfamily.biobank.model.Role;
import ru.healthfamily.biobank.model.User;
import ru.healthfamily.biobank.repository.PermissionRepository;
import ru.healthfamily.biobank.repository.RoleRepository;
import ru.healthfamily.biobank.repository.UserRepository;

import java.util.Set;
import java.util.stream.Collectors;

/**
 * Инициализация справочников авторизации при первом запуске:
 * - Разрешения (permissions)
 * - Роли (roles) с привязкой разрешений
 * - Пользователь-администратор по умолчанию (admin/admin123)
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class AuthDataInitializer implements CommandLineRunner {

    private final PermissionRepository permissionRepository;
    private final RoleRepository roleRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public void run(String... args) {
        if (permissionRepository.count() > 0) {
            log.debug("Данные авторизации уже инициализированы");
            return;
        }

        log.info("Инициализация справочников авторизации...");

        // Разрешения
        var permissions = createPermissions();
        var permMap = permissionRepository.findAll().stream()
                .collect(Collectors.toMap(Permission::getPermissionName, p -> p));

        // Роль "Пользователь" - только просмотр
        Role viewerRole = createRole("Пользователь", Set.of(
                "patient.view", "visit.view", "sample.view", "research.view", "storage.view", "reference.view"
        ), permMap);

        // Роль "Администратор" - полный доступ
        Role adminRole = createRole("Администратор", permMap.keySet(), permMap);

        // Роль "Лаборант" - просмотр + создание/обновление образцов, справочники только для просмотра
        Role labRole = createRole("Лаборант", Set.of(
                "patient.view", "patient.create", "patient.update",
                "visit.view", "visit.create", "visit.update",
                "sample.view", "sample.create", "sample.update",
                "research.view", "storage.view", "storage.create", "storage.update",
                "reference.view"
        ), permMap);

        // Администратор по умолчанию
        if (!userRepository.existsByUsername("admin")) {
            User admin = new User();
            admin.setUsername("admin");
            admin.setPassword(passwordEncoder.encode("admin123"));
            admin.setFullName("Администратор системы");
            admin.setIsActive(true);
            admin.getRoles().add(adminRole);
            userRepository.save(admin);
            log.info("Создан пользователь-администратор: admin / admin123");
        }

        log.info("Инициализация авторизации завершена");
    }

    private Set<Permission> createPermissions() {
        var names = Set.of(
                "patient.view", "patient.create", "patient.update", "patient.delete",
                "visit.view", "visit.create", "visit.update", "visit.delete",
                "sample.view", "sample.create", "sample.update", "sample.delete",
                "research.view", "research.create", "research.update", "research.delete",
                "storage.view", "storage.create", "storage.update", "storage.delete",
                "reference.view", "reference.create", "reference.update", "reference.delete", "reference.manage",
                "role.view", "role.create", "role.delete", "role.permissions.manage", "role.manage",
                "user.view", "user.create", "user.delete", "user.permissions.manage", "user.roles.assign", "user.manage"
        );
        for (String name : names) {
            Permission p = new Permission();
            p.setPermissionName(name);
            permissionRepository.save(p);
        }
        return permissionRepository.findAll().stream().collect(Collectors.toSet());
    }

    private Role createRole(String roleName, Set<String> permissionNames, java.util.Map<String, Permission> permMap) {
        Role role = new Role();
        role.setRoleName(roleName);
        for (String pName : permissionNames) {
            Permission p = permMap.get(pName);
            if (p != null) {
                role.getPermissions().add(p);
            }
        }
        return roleRepository.save(role);
    }
}

package ru.healthfamily.biobank.service;

import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.healthfamily.biobank.dto.*;
import ru.healthfamily.biobank.model.Permission;
import ru.healthfamily.biobank.model.Role;
import ru.healthfamily.biobank.model.User;
import ru.healthfamily.biobank.model.UserPermissionOverride;
import ru.healthfamily.biobank.repository.PermissionRepository;
import ru.healthfamily.biobank.repository.RoleRepository;
import ru.healthfamily.biobank.repository.UserPermissionOverrideRepository;
import ru.healthfamily.biobank.repository.UserRepository;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PermissionRepository permissionRepository;
    private final UserPermissionOverrideRepository userPermissionOverrideRepository;
    private final EntityManager entityManager;
    private final PasswordEncoder passwordEncoder;

    public List<UserDTO> getAllUsers() {
        return userRepository.findAll().stream()
                .map(this::toUserDTO)
                .collect(Collectors.toList());
    }

    public UserDTO getUserById(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("Пользователь не найден: " + userId));
        return toUserDTO(user);
    }

    @Transactional
    public UserDTO createUser(CreateUserRequest request) {
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new IllegalArgumentException("Пользователь с таким логином уже существует");
        }
        User user = new User();
        user.setUsername(request.getUsername());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setFullName(request.getFullName());
        user.setIsActive(true);
        for (Long roleId : request.getRoleIds()) {
            Role role = roleRepository.findById(roleId)
                    .orElseThrow(() -> new IllegalArgumentException("Роль не найдена: " + roleId));
            user.getRoles().add(role);
        }
        return toUserDTO(userRepository.save(user));
    }

    @Transactional
    public void deleteUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("Пользователь не найден: " + userId));
        userRepository.delete(user);
    }

    @Transactional
    public UserDTO updateUser(Long userId, UpdateUserRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("Пользователь не найден: " + userId));

        if (request.getFullName() != null) {
            user.setFullName(request.getFullName());
        }
        if (request.getIsActive() != null) {
            user.setIsActive(request.getIsActive());
        }

        return toUserDTO(userRepository.save(user));
    }

    @Transactional
    public UserDTO assignRoles(Long userId, AssignRolesRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("Пользователь не найден: " + userId));

        Set<Role> roles = new HashSet<>();
        for (Long roleId : request.getRoleIds()) {
            Role role = roleRepository.findById(roleId)
                    .orElseThrow(() -> new IllegalArgumentException("Роль не найдена: " + roleId));
            roles.add(role);
        }

        user.setRoles(roles);
        return toUserDTO(userRepository.save(user));
    }

    private static final Set<String> DEFAULT_VIEW_ONLY = Set.of(
            "patient.view", "visit.view", "sample.view", "research.view",
            "storage.view", "reference.view"
    );

    public List<UserPermissionMatrixItemDTO> getUserPermissionsMatrix(Long userId, Set<Long> roleIds) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("Пользователь не найден: " + userId));

        Set<Role> rolesForMatrix;
        if (roleIds != null && !roleIds.isEmpty()) {
            rolesForMatrix = new HashSet<>();
            for (Long roleId : roleIds) {
                Role role = roleRepository.findById(roleId)
                        .orElseThrow(() -> new IllegalArgumentException("Роль не найдена: " + roleId));
                rolesForMatrix.add(role);
            }
        } else {
            rolesForMatrix = user.getRoles();
        }

        Set<String> inherited = rolesForMatrix.isEmpty()
                ? new HashSet<>(DEFAULT_VIEW_ONLY)
                : rolesForMatrix.stream()
                        .flatMap(r -> r.getPermissions().stream())
                        .map(Permission::getPermissionName)
                        .collect(Collectors.toSet());

        Map<String, Boolean> overrides = user.getPermissionOverrides().stream()
                .collect(Collectors.toMap(
                        o -> o.getPermission().getPermissionName(),
                        UserPermissionOverride::getAllowed,
                        (a, b) -> b
                ));

        return permissionRepository.findAll().stream()
                .map(p -> {
                    boolean inh = inherited.contains(p.getPermissionName());
                    boolean eff = overrides.containsKey(p.getPermissionName())
                            ? Boolean.TRUE.equals(overrides.get(p.getPermissionName()))
                            : inh;
                    return new UserPermissionMatrixItemDTO(
                            p.getPermissionId(),
                            p.getPermissionName(),
                            p.getPermissionLabel(),
                            inh,
                            eff
                    );
                })
                .sorted(Comparator.comparing(UserPermissionMatrixItemDTO::getPermissionName))
                .collect(Collectors.toList());
    }

    @Transactional
    public void setUserPermissionOverrides(Long userId, SetUserPermissionOverridesRequest request) {
        userPermissionOverrideRepository.deleteByUserUserId(userId);
        entityManager.flush();
        entityManager.clear();

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("Пользователь не найден: " + userId));

        if (request.getOverrides() == null || request.getOverrides().isEmpty()) {
            return;
        }

        Set<Long> seenPermissionIds = new HashSet<>();
        for (SetUserPermissionOverridesRequest.Item item : request.getOverrides()) {
            if (!seenPermissionIds.add(item.getPermissionId())) {
                continue;
            }
            Permission permission = permissionRepository.findById(item.getPermissionId())
                    .orElseThrow(() -> new IllegalArgumentException("Разрешение не найдено: " + item.getPermissionId()));
            UserPermissionOverride o = new UserPermissionOverride();
            o.setUser(user);
            o.setPermission(permission);
            o.setAllowed(Boolean.TRUE.equals(item.getAllowed()));
            user.getPermissionOverrides().add(o);
        }

        userRepository.save(user);
    }

    private UserDTO toUserDTO(User user) {
        UserDTO dto = new UserDTO();
        dto.setUserId(user.getUserId());
        dto.setUsername(user.getUsername());
        dto.setFullName(user.getFullName());
        dto.setIsActive(user.getIsActive());
        dto.setRoleNames(user.getRoles().stream()
                .map(Role::getRoleName)
                .collect(Collectors.toSet()));
        return dto;
    }
}

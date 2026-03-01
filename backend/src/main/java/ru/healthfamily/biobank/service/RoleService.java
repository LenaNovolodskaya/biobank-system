package ru.healthfamily.biobank.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.healthfamily.biobank.dto.CreateRoleRequest;
import ru.healthfamily.biobank.dto.RoleDTO;
import ru.healthfamily.biobank.dto.UpdateRolePermissionsRequest;
import ru.healthfamily.biobank.model.Permission;
import ru.healthfamily.biobank.model.Role;
import ru.healthfamily.biobank.repository.PermissionRepository;
import ru.healthfamily.biobank.repository.RoleRepository;

import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RoleService {

    private final RoleRepository roleRepository;
    private final PermissionRepository permissionRepository;
    private final ru.healthfamily.biobank.repository.UserRepository userRepository;

    public List<RoleDTO> getAllRoles() {
        return roleRepository.findAll().stream()
                .map(this::toRoleDTO)
                .collect(Collectors.toList());
    }

    public RoleDTO getRoleById(Long roleId) {
        Role role = roleRepository.findById(roleId)
                .orElseThrow(() -> new IllegalArgumentException("Роль не найдена: " + roleId));
        return toRoleDTO(role);
    }

    @Transactional
    public RoleDTO createRole(CreateRoleRequest request) {
        if (roleRepository.findByRoleName(request.getRoleName()).isPresent()) {
            throw new IllegalArgumentException("Роль с таким названием уже существует");
        }

        Role role = new Role();
        role.setRoleName(request.getRoleName());
        role.setPermissions(new HashSet<>());

        for (Long permId : request.getPermissionIds()) {
            Permission p = permissionRepository.findById(permId)
                    .orElseThrow(() -> new IllegalArgumentException("Разрешение не найдено: " + permId));
            role.getPermissions().add(p);
        }

        return toRoleDTO(roleRepository.save(role));
    }

    @Transactional
    public void deleteRole(Long roleId) {
        Role role = roleRepository.findById(roleId)
                .orElseThrow(() -> new IllegalArgumentException("Роль не найдена: " + roleId));
        userRepository.findAll().forEach(u -> {
            if (u.getRoles().removeIf(r -> r.getRoleId().equals(roleId))) {
                userRepository.save(u);
            }
        });
        role.getPermissions().clear();
        roleRepository.save(role);
        roleRepository.delete(role);
    }

    @Transactional
    public RoleDTO updateRolePermissions(Long roleId, UpdateRolePermissionsRequest request) {
        Role role = roleRepository.findById(roleId)
                .orElseThrow(() -> new IllegalArgumentException("Роль не найдена: " + roleId));

        role.getPermissions().clear();
        for (Long permId : request.getPermissionIds()) {
            Permission p = permissionRepository.findById(permId)
                    .orElseThrow(() -> new IllegalArgumentException("Разрешение не найдено: " + permId));
            role.getPermissions().add(p);
        }

        return toRoleDTO(roleRepository.save(role));
    }

    private RoleDTO toRoleDTO(Role role) {
        RoleDTO dto = new RoleDTO();
        dto.setRoleId(role.getRoleId());
        dto.setRoleName(role.getRoleName());
        dto.setPermissionNames(role.getPermissions().stream()
                .map(Permission::getPermissionName)
                .collect(Collectors.toSet()));
        return dto;
    }
}

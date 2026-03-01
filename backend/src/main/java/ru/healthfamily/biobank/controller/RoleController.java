package ru.healthfamily.biobank.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import ru.healthfamily.biobank.dto.CreateRoleRequest;
import ru.healthfamily.biobank.dto.RoleDTO;
import ru.healthfamily.biobank.dto.UpdateRolePermissionsRequest;
import ru.healthfamily.biobank.service.RoleService;

import java.util.List;

@RestController
@RequestMapping("/roles")
@RequiredArgsConstructor
public class RoleController {

    private final RoleService roleService;

    @GetMapping
    @PreAuthorize("hasAnyAuthority('role.view','role.manage','user.view','user.manage','user.roles.assign')")
    public ResponseEntity<List<RoleDTO>> getAllRoles() {
        return ResponseEntity.ok(roleService.getAllRoles());
    }

    @GetMapping("/{roleId}")
    @PreAuthorize("hasAnyAuthority('role.view','role.manage','user.roles.assign','user.manage')")
    public ResponseEntity<RoleDTO> getRoleById(@PathVariable Long roleId) {
        return ResponseEntity.ok(roleService.getRoleById(roleId));
    }

    @PostMapping
    @PreAuthorize("hasAnyAuthority('role.create','role.manage')")
    public ResponseEntity<RoleDTO> createRole(@Valid @RequestBody CreateRoleRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(roleService.createRole(request));
    }

    @DeleteMapping("/{roleId}")
    @PreAuthorize("hasAnyAuthority('role.delete','role.manage')")
    public ResponseEntity<Void> deleteRole(@PathVariable Long roleId) {
        roleService.deleteRole(roleId);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{roleId}/permissions")
    @PreAuthorize("hasAnyAuthority('role.permissions.manage','role.manage')")
    public ResponseEntity<RoleDTO> updateRolePermissions(
            @PathVariable Long roleId,
            @Valid @RequestBody UpdateRolePermissionsRequest request
    ) {
        return ResponseEntity.ok(roleService.updateRolePermissions(roleId, request));
    }
}

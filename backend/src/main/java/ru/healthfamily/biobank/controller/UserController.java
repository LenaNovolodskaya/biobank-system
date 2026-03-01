package ru.healthfamily.biobank.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import ru.healthfamily.biobank.dto.AssignRolesRequest;
import ru.healthfamily.biobank.dto.CreateUserRequest;
import ru.healthfamily.biobank.dto.SetUserPermissionOverridesRequest;
import ru.healthfamily.biobank.dto.UpdateUserRequest;
import ru.healthfamily.biobank.dto.UserDTO;
import ru.healthfamily.biobank.dto.UserPermissionMatrixItemDTO;
import ru.healthfamily.biobank.service.UserService;

import java.util.List;
import java.util.Set;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping
    @PreAuthorize("hasAnyAuthority('user.view','user.manage')")
    public ResponseEntity<List<UserDTO>> getAllUsers() {
        return ResponseEntity.ok(userService.getAllUsers());
    }

    @GetMapping("/{userId}")
    @PreAuthorize("hasAnyAuthority('user.view','user.manage')")
    public ResponseEntity<UserDTO> getUserById(@PathVariable Long userId) {
        return ResponseEntity.ok(userService.getUserById(userId));
    }

    @PostMapping
    @PreAuthorize("hasAnyAuthority('user.create','user.manage')")
    public ResponseEntity<UserDTO> createUser(@Valid @RequestBody CreateUserRequest request) {
        return ResponseEntity.status(org.springframework.http.HttpStatus.CREATED)
                .body(userService.createUser(request));
    }

    @DeleteMapping("/{userId}")
    @PreAuthorize("hasAnyAuthority('user.delete','user.manage')")
    public ResponseEntity<Void> deleteUser(@PathVariable Long userId) {
        userService.deleteUser(userId);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{userId}")
    @PreAuthorize("hasAnyAuthority('user.view','user.manage')")
    public ResponseEntity<UserDTO> updateUser(
            @PathVariable Long userId,
            @Valid @RequestBody UpdateUserRequest request
    ) {
        return ResponseEntity.ok(userService.updateUser(userId, request));
    }

    @PutMapping("/{userId}/roles")
    @PreAuthorize("hasAnyAuthority('user.roles.assign','user.permissions.manage','user.manage')")
    public ResponseEntity<UserDTO> assignRoles(
            @PathVariable Long userId,
            @Valid @RequestBody AssignRolesRequest request
    ) {
        return ResponseEntity.ok(userService.assignRoles(userId, request));
    }

    @GetMapping("/{userId}/permissions-matrix")
    @PreAuthorize("hasAnyAuthority('user.view','user.permissions.manage','user.manage')")
    public ResponseEntity<List<UserPermissionMatrixItemDTO>> getUserPermissionsMatrix(
            @PathVariable Long userId,
            @RequestParam(required = false) Set<Long> roleIds
    ) {
        return ResponseEntity.ok(userService.getUserPermissionsMatrix(userId, roleIds));
    }

    @PutMapping("/{userId}/permission-overrides")
    @PreAuthorize("hasAnyAuthority('user.permissions.manage','user.manage')")
    public ResponseEntity<Void> setUserPermissionOverrides(
            @PathVariable Long userId,
            @Valid @RequestBody SetUserPermissionOverridesRequest request
    ) {
        userService.setUserPermissionOverrides(userId, request);
        return ResponseEntity.noContent().build();
    }
}

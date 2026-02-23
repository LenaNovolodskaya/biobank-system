package ru.healthfamily.biobank.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import ru.healthfamily.biobank.dto.AssignRolesRequest;
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
@PreAuthorize("hasAuthority('user.manage')")
public class UserController {

    private final UserService userService;

    @GetMapping
    public ResponseEntity<List<UserDTO>> getAllUsers() {
        return ResponseEntity.ok(userService.getAllUsers());
    }

    @GetMapping("/{userId}")
    public ResponseEntity<UserDTO> getUserById(@PathVariable Long userId) {
        return ResponseEntity.ok(userService.getUserById(userId));
    }

    @PutMapping("/{userId}")
    public ResponseEntity<UserDTO> updateUser(
            @PathVariable Long userId,
            @Valid @RequestBody UpdateUserRequest request
    ) {
        return ResponseEntity.ok(userService.updateUser(userId, request));
    }

    @PutMapping("/{userId}/roles")
    public ResponseEntity<UserDTO> assignRoles(
            @PathVariable Long userId,
            @Valid @RequestBody AssignRolesRequest request
    ) {
        return ResponseEntity.ok(userService.assignRoles(userId, request));
    }

    @GetMapping("/{userId}/permissions-matrix")
    public ResponseEntity<List<UserPermissionMatrixItemDTO>> getUserPermissionsMatrix(
            @PathVariable Long userId,
            @RequestParam(required = false) Set<Long> roleIds
    ) {
        return ResponseEntity.ok(userService.getUserPermissionsMatrix(userId, roleIds));
    }

    @PutMapping("/{userId}/permission-overrides")
    public ResponseEntity<Void> setUserPermissionOverrides(
            @PathVariable Long userId,
            @Valid @RequestBody SetUserPermissionOverridesRequest request
    ) {
        userService.setUserPermissionOverrides(userId, request);
        return ResponseEntity.noContent().build();
    }
}

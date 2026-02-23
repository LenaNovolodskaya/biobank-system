package ru.healthfamily.biobank.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.healthfamily.biobank.dto.PermissionDTO;
import ru.healthfamily.biobank.repository.PermissionRepository;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/permissions")
@RequiredArgsConstructor
@PreAuthorize("hasAuthority('role.manage')")
public class PermissionController {

    private final PermissionRepository permissionRepository;

    @GetMapping
    public ResponseEntity<List<PermissionDTO>> getAllPermissions() {
        return ResponseEntity.ok(
                permissionRepository.findAll().stream()
                        .map(p -> {
                            PermissionDTO dto = new PermissionDTO();
                            dto.setPermissionId(p.getPermissionId());
                            dto.setPermissionName(p.getPermissionName());
                            dto.setPermissionLabel(p.getPermissionLabel());
                            return dto;
                        })
                        .collect(Collectors.toList())
        );
    }
}

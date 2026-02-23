package ru.healthfamily.biobank.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.Set;

@Data
public class UpdateRolePermissionsRequest {
    @NotNull(message = "Список разрешений обязателен")
    private Set<Long> permissionIds;
}

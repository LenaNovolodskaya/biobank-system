package ru.healthfamily.biobank.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.Set;

@Data
public class CreateRoleRequest {
    @NotBlank(message = "Название роли обязательно")
    @Size(max = 100)
    private String roleName;

    private Set<Long> permissionIds = java.util.Collections.emptySet();
}

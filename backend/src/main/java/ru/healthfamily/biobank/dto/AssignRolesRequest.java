package ru.healthfamily.biobank.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.Set;

@Data
public class AssignRolesRequest {
    @NotNull(message = "Список ролей обязателен")
    private Set<Long> roleIds;
}

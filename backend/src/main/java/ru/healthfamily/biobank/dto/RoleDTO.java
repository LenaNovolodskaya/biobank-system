package ru.healthfamily.biobank.dto;

import lombok.Data;

import java.util.Set;

@Data
public class RoleDTO {
    private Long roleId;
    private String roleName;
    private Set<String> permissionNames;
}

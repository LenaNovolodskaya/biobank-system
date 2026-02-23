package ru.healthfamily.biobank.dto;

import lombok.Data;

@Data
public class PermissionDTO {
    private Long permissionId;
    private String permissionName;
    private String permissionLabel;
}

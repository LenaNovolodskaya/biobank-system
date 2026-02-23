package ru.healthfamily.biobank.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class UserPermissionMatrixItemDTO {
    private Long permissionId;
    private String permissionName;
    private String permissionLabel;
    private boolean inherited;
    private boolean effective;
}


package ru.healthfamily.biobank.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

@Data
public class SetUserPermissionOverridesRequest {

    @NotNull
    private List<Item> overrides;

    @Data
    public static class Item {
        @NotNull
        private Long permissionId;

        @NotNull
        private Boolean allowed;
    }
}


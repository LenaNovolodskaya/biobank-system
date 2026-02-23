package ru.healthfamily.biobank.dto;

import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UpdateUserRequest {
    @Size(max = 255)
    private String fullName;

    private Boolean isActive;
}

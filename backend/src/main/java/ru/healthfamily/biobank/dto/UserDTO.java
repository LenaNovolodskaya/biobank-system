package ru.healthfamily.biobank.dto;

import lombok.Data;

import java.util.Set;

@Data
public class UserDTO {
    private Long userId;
    private String username;
    private String fullName;
    private Boolean isActive;
    private Set<String> roleNames;
}

package ru.healthfamily.biobank.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.Set;

@Data
@AllArgsConstructor
public class CurrentUserResponse {
    private String username;
    private String fullName;
    private Long userId;
    private Set<String> permissions;
}

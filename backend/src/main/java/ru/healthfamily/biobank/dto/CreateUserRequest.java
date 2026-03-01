package ru.healthfamily.biobank.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.Set;

@Data
public class CreateUserRequest {
    @NotBlank(message = "Логин обязателен")
    @Size(min = 3, max = 100)
    private String username;

    @NotBlank(message = "Пароль обязателен")
    @Size(min = 6, message = "Пароль должен содержать минимум 6 символов")
    private String password;

    @NotBlank(message = "ФИО обязательно")
    @Size(max = 255)
    private String fullName;

    private Set<Long> roleIds = java.util.Collections.emptySet();
}

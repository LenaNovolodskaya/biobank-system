package ru.healthfamily.biobank.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class CreateStorageUnitRequest {

    @NotBlank(message = "Тип хранилища обязателен")
    private String unitType;

    @NotBlank(message = "Название хранилища обязательно")
    private String unitName;

    private Integer shelvesCount;
}

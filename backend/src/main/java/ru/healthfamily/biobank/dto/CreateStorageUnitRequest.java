package ru.healthfamily.biobank.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CreateStorageUnitRequest {

    @NotNull(message = "Тип хранилища обязателен")
    private Long unitTypeId;

    @NotNull(message = "Название хранилища обязательно")
    private String unitName;

    private Integer shelvesCount;
}

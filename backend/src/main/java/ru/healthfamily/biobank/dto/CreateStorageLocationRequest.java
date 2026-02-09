package ru.healthfamily.biobank.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class CreateStorageLocationRequest {

    @NotBlank(message = "Название локации обязательно")
    private String locationName;

    @NotBlank(message = "Адрес обязателен")
    private String address;

    @NotBlank(message = "Номер помещения обязателен")
    private String roomNumber;

    private String description;
}

package ru.healthfamily.biobank.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CreateStorageContainerRequest {

    private String shelfNumber;

    private String containerType;

    private Integer containerNumber;

    /** LETTER_DIGIT, DIGIT_LETTER, DIGIT_DIGIT, SEQUENTIAL */
    private String numberingType;

    @Min(value = 1, message = "Количество строк должно быть больше 0")
    private Integer rowsCount;

    @Min(value = 1, message = "Количество столбцов должно быть больше 0")
    private Integer columnsCount;

    @NotNull(message = "Максимальное количество образцов обязательно")
    @Min(value = 1, message = "Максимальное количество должно быть больше 0")
    private Integer maxSamplesCount;

    @Min(value = 0, message = "Текущее количество не может быть меньше 0")
    private Integer currentSamplesCount;
}

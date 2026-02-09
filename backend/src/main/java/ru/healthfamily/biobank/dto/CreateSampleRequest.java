package ru.healthfamily.biobank.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import ru.healthfamily.biobank.model.Sample.ExpiryStatus;
import java.time.LocalDateTime;

@Data
public class CreateSampleRequest {

    @NotNull(message = "Визит обязателен")
    private Long visitId;

    @NotBlank(message = "Штрихкод обязателен")
    private String barcode;

    private Long sampleTypeId;

    @NotNull(message = "Начальное количество обязательно")
    @Min(value = 1, message = "Начальное количество должно быть больше 0")
    private Integer initialQuantity;

    @NotNull(message = "Текущее количество обязательно")
    @Min(value = 0, message = "Текущее количество не может быть меньше 0")
    private Integer currentQuantity;

    private Integer recommendedStorageMonths;

    private Integer actualStorageMonths;

    private ExpiryStatus expiryStatus;

    private Long sampleStatusId;

    private String tubeStatusIds;

    private Long containerId;

    private String positionInContainer;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm")
    private LocalDateTime collectionDate;
}

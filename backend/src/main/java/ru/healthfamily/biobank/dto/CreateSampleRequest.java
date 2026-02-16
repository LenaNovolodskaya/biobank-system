package ru.healthfamily.biobank.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

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

    private Long containerId;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm")
    private LocalDateTime collectionDate;

    @Valid
    private List<SpecimenItem> specimens = new ArrayList<>();

    @Data
    public static class SpecimenItem {
        @NotBlank(message = "Штрихкод образца обязателен")
        private String barcode;
        private Long sampleStatusId;
        private Long containerId;
        private String positionInContainer;
    }
}

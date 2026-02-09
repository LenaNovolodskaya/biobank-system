package ru.healthfamily.biobank.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class StorageUnitDTO {
    private Long unitId;
    private Long locationId;
    private String unitType;
    private String unitName;
    private Integer shelvesCount;
    private List<StorageContainerDTO> containers;
}

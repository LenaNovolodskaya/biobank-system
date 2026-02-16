package ru.healthfamily.biobank.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class StorageUnitDTO {
    private Long unitId;
    private Long locationId;
    private Long unitTypeId;
    private String unitTypeName;
    private String unitName;
    private Integer shelvesCount;
    private List<StorageContainerDTO> containers = new ArrayList<>();
}

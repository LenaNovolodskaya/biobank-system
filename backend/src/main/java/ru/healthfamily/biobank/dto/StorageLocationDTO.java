package ru.healthfamily.biobank.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class StorageLocationDTO {
    private Long locationId;
    private String locationName;
    private String address;
    private String roomNumber;
    private String description;
    private List<StorageUnitDTO> units;
}

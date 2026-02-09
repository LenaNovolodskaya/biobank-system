package ru.healthfamily.biobank.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class StorageContainerDTO {
    private Long containerId;
    private String shelfNumber;
    private String containerType;
    private Integer containerNumber;
    private Integer rowsCount;
    private Integer columnsCount;
    private Integer maxSamplesCount;
    private Integer currentSamplesCount;
}

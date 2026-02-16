package ru.healthfamily.biobank.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class StorageContainerDTO {
    private Long containerId;
    private String containerNumber;
    private Integer currentSamplesCount;
    private Long unitId;
    private Integer shelfNumber;
    private Long templateId;
    private String templateName;
    private Integer rowsCount;
    private Integer columnsCount;
    private Integer maxSamplesCount;
    private String numberingType;
}

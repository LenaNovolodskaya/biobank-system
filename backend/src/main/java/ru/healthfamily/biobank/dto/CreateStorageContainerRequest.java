package ru.healthfamily.biobank.dto;

import lombok.Data;

@Data
public class CreateStorageContainerRequest {

    private String containerNumber;
    private Long templateId;
    private String templateName;
    private Integer rowsCount;
    private Integer columnsCount;
    private String numberingType;
    private Integer shelfNumber;
    private Integer shelfPosition;
}

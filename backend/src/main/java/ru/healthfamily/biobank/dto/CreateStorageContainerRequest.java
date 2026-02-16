package ru.healthfamily.biobank.dto;

import lombok.Data;

@Data
public class CreateStorageContainerRequest {

    /** Номер/название контейнера (например "A1", "Криобокс-1") */
    private String containerNumber;

    /** ID существующего шаблона (если указан — используется он) */
    private Long templateId;

    /** Название нового шаблона (если templateId не указан и шаблона нет — создаётся новый) */
    private String templateName;

    /** Параметры для нового шаблона (если создаётся) */
    private Integer rowsCount;
    private Integer columnsCount;
    private String numberingType;

    /** Номер полки (только цифра) */
    private Integer shelfNumber;
}

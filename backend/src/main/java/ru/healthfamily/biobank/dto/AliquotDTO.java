package ru.healthfamily.biobank.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AliquotDTO {
    private Long aliquotId;
    private String barcode;
    private Long sampleId;
    private Long sampleStatusId;
    private Long containerId;
    private String positionInContainer;
}

package ru.healthfamily.biobank.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.healthfamily.biobank.model.Sample.ExpiryStatus;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SampleDTO {
    private Long sampleId;
    private Long visitId;
    private String barcode;
    private Long sampleTypeId;
    private Integer initialQuantity;
    private Integer currentQuantity;
    private Integer recommendedStorageMonths;
    private Integer actualStorageMonths;
    private ExpiryStatus expiryStatus;
    private Long sampleStatusId;
    private String tubeStatusIds;
    private Long containerId;
    private String positionInContainer;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAtSample;
}

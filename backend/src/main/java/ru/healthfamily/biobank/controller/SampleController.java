package ru.healthfamily.biobank.controller;

import lombok.RequiredArgsConstructor;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.RestController;
import ru.healthfamily.biobank.dto.CreateSampleRequest;
import ru.healthfamily.biobank.dto.SampleDTO;
import ru.healthfamily.biobank.model.Sample.ExpiryStatus;
import ru.healthfamily.biobank.service.SampleService;
import java.util.List;

@RestController
@RequestMapping("/samples")
@RequiredArgsConstructor
public class SampleController {

    private final SampleService sampleService;

    @GetMapping
    @PreAuthorize("hasAuthority('sample.view')")
    public ResponseEntity<List<SampleDTO>> getSamples(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) Long containerId,
            @RequestParam(required = false) Long sampleTypeId,
            @RequestParam(required = false) Long sampleStatusId,
            @RequestParam(required = false) Long visitId,
            @RequestParam(required = false) ExpiryStatus expiryStatus
    ) {
        return ResponseEntity.ok(sampleService.getSamples(
                search,
                containerId,
                sampleTypeId,
                sampleStatusId,
                visitId,
                expiryStatus
        ));
    }

    @PostMapping
    @PreAuthorize("hasAuthority('sample.create')")
    public ResponseEntity<SampleDTO> createSample(
            @Valid @RequestBody CreateSampleRequest request) {
        SampleDTO sample = sampleService.createSample(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(sample);
    }

    @PutMapping("/{sampleId}")
    @PreAuthorize("hasAuthority('sample.update')")
    public ResponseEntity<SampleDTO> updateSample(
            @PathVariable Long sampleId,
            @Valid @RequestBody CreateSampleRequest request) {
        return ResponseEntity.ok(sampleService.updateSample(sampleId, request));
    }

    @DeleteMapping("/{sampleId}")
    @PreAuthorize("hasAuthority('sample.delete')")
    public ResponseEntity<Void> deleteSample(@PathVariable Long sampleId) {
        sampleService.deleteSample(sampleId);
        return ResponseEntity.noContent().build();
    }
}

package ru.healthfamily.biobank.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import ru.healthfamily.biobank.dto.SampleTransactionDTO;
import ru.healthfamily.biobank.dto.SpecimenActionRequest;
import ru.healthfamily.biobank.service.SampleTransactionService;

import java.util.List;

@RestController
@RequestMapping("/specimens")
@RequiredArgsConstructor
public class SpecimenController {

    private final SampleTransactionService sampleTransactionService;

    @PostMapping("/{specimenId}/withdraw")
    @PreAuthorize("hasAnyAuthority('sample.update','sample.manage')")
    public ResponseEntity<SampleTransactionDTO> withdrawSpecimen(
            @PathVariable Long specimenId,
            @Valid @RequestBody SpecimenActionRequest request
    ) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(sampleTransactionService.withdrawSpecimen(specimenId, request));
    }

    @PostMapping("/{specimenId}/return")
    @PreAuthorize("hasAnyAuthority('sample.update','sample.manage')")
    public ResponseEntity<SampleTransactionDTO> returnSpecimen(
            @PathVariable Long specimenId,
            @Valid @RequestBody SpecimenActionRequest request
    ) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(sampleTransactionService.returnSpecimen(specimenId, request));
    }

    @PostMapping("/{specimenId}/exhaust")
    @PreAuthorize("hasAnyAuthority('sample.update','sample.manage')")
    public ResponseEntity<SampleTransactionDTO> exhaustSpecimen(
            @PathVariable Long specimenId,
            @Valid @RequestBody SpecimenActionRequest request
    ) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(sampleTransactionService.exhaustSpecimen(specimenId, request));
    }

    @GetMapping("/{specimenId}/transactions")
    @PreAuthorize("hasAuthority('sample.view')")
    public ResponseEntity<List<SampleTransactionDTO>> getSpecimenTransactions(
            @PathVariable Long specimenId
    ) {
        return ResponseEntity.ok(sampleTransactionService.getAllTransactions(null, specimenId));
    }
}

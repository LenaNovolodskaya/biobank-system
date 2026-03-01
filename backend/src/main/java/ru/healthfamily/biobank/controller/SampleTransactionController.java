package ru.healthfamily.biobank.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import ru.healthfamily.biobank.dto.CreateSampleTransactionRequest;
import ru.healthfamily.biobank.dto.SampleTransactionDTO;
import ru.healthfamily.biobank.service.SampleTransactionService;

import java.util.List;

@RestController
@RequestMapping("/sample-transactions")
@RequiredArgsConstructor
public class SampleTransactionController {

    private final SampleTransactionService sampleTransactionService;

    @GetMapping
    @PreAuthorize("hasAuthority('sample.view')")
    public ResponseEntity<List<SampleTransactionDTO>> getTransactions(
            @RequestParam(required = false) Long sampleId,
            @RequestParam(required = false) Long specimenId
    ) {
        return ResponseEntity.ok(sampleTransactionService.getAllTransactions(sampleId, specimenId));
    }

    @PostMapping
    @PreAuthorize("hasAuthority('sample.create')")
    public ResponseEntity<SampleTransactionDTO> createTransaction(
            @Valid @RequestBody CreateSampleTransactionRequest request
    ) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(sampleTransactionService.createTransaction(request));
    }
}

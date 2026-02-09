package ru.healthfamily.biobank.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.healthfamily.biobank.dto.CreateVisitRequest;
import ru.healthfamily.biobank.dto.VisitDTO;
import ru.healthfamily.biobank.service.VisitService;
import java.util.List;

@RestController
@RequestMapping("/visits")
@RequiredArgsConstructor
public class VisitController {

    private final VisitService visitService;

    @GetMapping
    public ResponseEntity<List<VisitDTO>> getAllVisits() {
        return ResponseEntity.ok(visitService.getAllVisits());
    }

    @PostMapping
    public ResponseEntity<VisitDTO> createVisit(
            @Valid @RequestBody CreateVisitRequest request) {
        VisitDTO visit = visitService.createVisit(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(visit);
    }

    @PutMapping("/{visitId}")
    public ResponseEntity<VisitDTO> updateVisit(
            @PathVariable Long visitId,
            @Valid @RequestBody CreateVisitRequest request) {
        return ResponseEntity.ok(visitService.updateVisit(visitId, request));
    }

    @DeleteMapping("/{visitId}")
    public ResponseEntity<Void> deleteVisit(@PathVariable Long visitId) {
        visitService.deleteVisit(visitId);
        return ResponseEntity.noContent().build();
    }
}

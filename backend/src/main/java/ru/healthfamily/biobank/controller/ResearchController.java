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
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.RestController;
import ru.healthfamily.biobank.dto.CreateResearchRequest;
import ru.healthfamily.biobank.dto.ResearchDTO;
import ru.healthfamily.biobank.service.ResearchService;
import java.util.List;

@RestController
@RequestMapping("/researches")
@RequiredArgsConstructor
public class ResearchController {

    private final ResearchService researchService;

    @GetMapping
    @PreAuthorize("hasAuthority('research.view')")
    public ResponseEntity<List<ResearchDTO>> getAllResearches() {
        return ResponseEntity.ok(researchService.getAllResearches());
    }

    @PostMapping
    @PreAuthorize("hasAuthority('research.create')")
    public ResponseEntity<ResearchDTO> createResearch(
            @Valid @RequestBody CreateResearchRequest request) {
        ResearchDTO research = researchService.createResearch(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(research);
    }

    @PutMapping("/{researchId}")
    @PreAuthorize("hasAuthority('research.update')")
    public ResponseEntity<ResearchDTO> updateResearch(
            @PathVariable Long researchId,
            @Valid @RequestBody CreateResearchRequest request) {
        return ResponseEntity.ok(researchService.updateResearch(researchId, request));
    }

    @DeleteMapping("/{researchId}")
    @PreAuthorize("hasAuthority('research.delete')")
    public ResponseEntity<Void> deleteResearch(@PathVariable Long researchId) {
        researchService.deleteResearch(researchId);
        return ResponseEntity.noContent().build();
    }
}

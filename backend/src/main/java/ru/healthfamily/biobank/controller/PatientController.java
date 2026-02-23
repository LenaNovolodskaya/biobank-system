package ru.healthfamily.biobank.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import ru.healthfamily.biobank.dto.CreatePatientRequest;
import ru.healthfamily.biobank.dto.PatientDTO;
import ru.healthfamily.biobank.service.PatientService;
import java.util.List;

@RestController
@RequestMapping("/patients")
@RequiredArgsConstructor
public class PatientController {
    
    private final PatientService patientService;
    
    @GetMapping
    @PreAuthorize("hasAuthority('patient.view')")
    public ResponseEntity<List<PatientDTO>> getAllPatients() {
        List<PatientDTO> patients = patientService.getAllPatients();
        return ResponseEntity.ok(patients);
    }

    @PostMapping
    @PreAuthorize("hasAuthority('patient.create')")
    public ResponseEntity<PatientDTO> createPatient(@Valid @RequestBody CreatePatientRequest request) {
        PatientDTO patient = patientService.createPatient(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(patient);
    }
    
    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('patient.view')")
    public ResponseEntity<PatientDTO> getPatientById(@PathVariable Long id) {
        PatientDTO patient = patientService.getPatientById(id);
        return ResponseEntity.ok(patient);
    }
    
    @GetMapping("/barcode/{barcode}")
    @PreAuthorize("hasAuthority('patient.view')")
    public ResponseEntity<PatientDTO> getPatientByBarcode(@PathVariable String barcode) {
        PatientDTO patient = patientService.getPatientByBarcode(barcode);
        return ResponseEntity.ok(patient);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('patient.update')")
    public ResponseEntity<PatientDTO> updatePatient(
            @PathVariable Long id,
            @Valid @RequestBody CreatePatientRequest request
    ) {
        PatientDTO patient = patientService.updatePatient(id, request);
        return ResponseEntity.ok(patient);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('patient.delete')")
    public ResponseEntity<Void> deletePatient(@PathVariable Long id) {
        patientService.deletePatient(id);
        return ResponseEntity.noContent().build();
    }
}
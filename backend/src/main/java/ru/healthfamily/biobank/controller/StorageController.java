package ru.healthfamily.biobank.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.healthfamily.biobank.dto.CreateStorageContainerRequest;
import ru.healthfamily.biobank.dto.CreateStorageLocationRequest;
import ru.healthfamily.biobank.dto.CreateStorageUnitRequest;
import ru.healthfamily.biobank.dto.StorageContainerDTO;
import ru.healthfamily.biobank.dto.StorageLocationDTO;
import ru.healthfamily.biobank.dto.StorageUnitDTO;
import ru.healthfamily.biobank.service.StorageService;
import java.util.List;

@RestController
@RequestMapping("/storage")
@RequiredArgsConstructor
public class StorageController {

    private final StorageService storageService;

    @PostMapping("/locations")
    public ResponseEntity<StorageLocationDTO> createLocation(
            @Valid @RequestBody CreateStorageLocationRequest request) {
        StorageLocationDTO location = storageService.createLocation(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(location);
    }

    @PostMapping("/locations/{locationId}/units")
    public ResponseEntity<StorageUnitDTO> addUnitToLocation(
            @PathVariable Long locationId,
            @Valid @RequestBody CreateStorageUnitRequest request) {
        StorageUnitDTO unit = storageService.addUnitToLocation(locationId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(unit);
    }

    @PostMapping("/units/{unitId}/containers")
    public ResponseEntity<StorageContainerDTO> addContainerToUnit(
            @PathVariable Long unitId,
            @Valid @RequestBody CreateStorageContainerRequest request) {
        StorageContainerDTO container = storageService.addContainerToUnit(unitId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(container);
    }

    @PutMapping("/locations/{locationId}")
    public ResponseEntity<StorageLocationDTO> updateLocation(
            @PathVariable Long locationId,
            @Valid @RequestBody CreateStorageLocationRequest request) {
        return ResponseEntity.ok(storageService.updateLocation(locationId, request));
    }

    @PutMapping("/units/{unitId}")
    public ResponseEntity<StorageUnitDTO> updateUnit(
            @PathVariable Long unitId,
            @Valid @RequestBody CreateStorageUnitRequest request) {
        return ResponseEntity.ok(storageService.updateUnit(unitId, request));
    }

    @PutMapping("/containers/{containerId}")
    public ResponseEntity<StorageContainerDTO> updateContainer(
            @PathVariable Long containerId,
            @Valid @RequestBody CreateStorageContainerRequest request) {
        return ResponseEntity.ok(storageService.updateContainer(containerId, request));
    }

    @GetMapping("/locations")
    public ResponseEntity<List<StorageLocationDTO>> getAllLocations() {
        return ResponseEntity.ok(storageService.getAllLocations());
    }

    @GetMapping("/units")
    public ResponseEntity<List<StorageUnitDTO>> getAllUnits() {
        return ResponseEntity.ok(storageService.getAllUnits());
    }

    @GetMapping("/containers")
    public ResponseEntity<List<StorageContainerDTO>> getAllContainers() {
        return ResponseEntity.ok(storageService.getAllContainers());
    }

    @DeleteMapping("/locations/{locationId}")
    public ResponseEntity<Void> deleteLocation(@PathVariable Long locationId) {
        storageService.deleteLocation(locationId);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/units/{unitId}")
    public ResponseEntity<Void> deleteUnit(@PathVariable Long unitId) {
        storageService.deleteUnit(unitId);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/containers/{containerId}")
    public ResponseEntity<Void> deleteContainer(@PathVariable Long containerId) {
        storageService.deleteContainer(containerId);
        return ResponseEntity.noContent().build();
    }
}

package ru.healthfamily.biobank.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.healthfamily.biobank.dto.CreateStorageContainerRequest;
import ru.healthfamily.biobank.dto.CreateStorageLocationRequest;
import ru.healthfamily.biobank.dto.CreateStorageUnitRequest;
import ru.healthfamily.biobank.dto.StorageContainerDTO;
import ru.healthfamily.biobank.dto.StorageLocationDTO;
import ru.healthfamily.biobank.dto.StorageUnitDTO;
import ru.healthfamily.biobank.model.StorageContainer;
import ru.healthfamily.biobank.model.StorageLocation;
import ru.healthfamily.biobank.model.StorageUnit;
import ru.healthfamily.biobank.repository.StorageContainerRepository;
import ru.healthfamily.biobank.repository.StorageLocationRepository;
import ru.healthfamily.biobank.repository.StorageUnitRepository;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class StorageService {

    private final StorageLocationRepository locationRepository;
    private final StorageUnitRepository unitRepository;
    private final StorageContainerRepository containerRepository;

    @Transactional
    public StorageLocationDTO createLocation(CreateStorageLocationRequest request) {
        StorageLocation location = new StorageLocation();
        location.setLocationName(request.getLocationName());
        location.setAddress(request.getAddress());
        location.setRoomNumber(request.getRoomNumber());
        location.setDescription(request.getDescription());
        return convertLocationToDTO(locationRepository.save(location));
    }

    @Transactional
    public StorageUnitDTO addUnitToLocation(Long locationId, CreateStorageUnitRequest request) {
        StorageLocation location = locationRepository.findById(locationId)
                .orElseThrow(() -> new RuntimeException("Локация не найдена"));
        StorageUnit unit = new StorageUnit();
        unit.setLocation(location);
        unit.setUnitType(request.getUnitType());
        unit.setUnitName(request.getUnitName());
        unit.setShelvesCount(request.getShelvesCount());
        return convertUnitToDTO(unitRepository.save(unit));
    }

    @Transactional
    public StorageContainerDTO addContainerToUnit(Long unitId, CreateStorageContainerRequest request) {
        StorageUnit unit = unitRepository.findById(unitId)
                .orElseThrow(() -> new RuntimeException("Хранилище не найдено"));
        StorageContainer container = new StorageContainer();
        container.setUnit(unit);
        container.setShelfNumber(request.getShelfNumber());
        container.setContainerType(request.getContainerType());
        container.setContainerNumber(request.getContainerNumber());
        container.setNumberingType(request.getNumberingType());
        applyContainerLayout(container, request);
        Integer current = request.getCurrentSamplesCount();
        container.setCurrentSamplesCount(current == null ? 0 : current);
        validateContainerCapacity(container);
        return convertContainerToDTO(containerRepository.save(container));
    }

    @Transactional(readOnly = true)
    public List<StorageLocationDTO> getAllLocations() {
        return locationRepository.findAll().stream()
                .map(this::convertLocationToDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<StorageUnitDTO> getAllUnits() {
        return unitRepository.findAll().stream()
                .map(this::convertUnitToDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<StorageContainerDTO> getAllContainers() {
        return containerRepository.findAll().stream()
                .map(this::convertContainerToDTO)
                .collect(Collectors.toList());
    }

    @Transactional
    public StorageLocationDTO updateLocation(Long locationId, CreateStorageLocationRequest request) {
        StorageLocation location = locationRepository.findById(locationId)
                .orElseThrow(() -> new RuntimeException("Локация не найдена"));
        location.setLocationName(request.getLocationName());
        location.setAddress(request.getAddress());
        location.setRoomNumber(request.getRoomNumber());
        location.setDescription(request.getDescription());
        return convertLocationToDTO(locationRepository.save(location));
    }

    @Transactional
    public StorageUnitDTO updateUnit(Long unitId, CreateStorageUnitRequest request) {
        StorageUnit unit = unitRepository.findById(unitId)
                .orElseThrow(() -> new RuntimeException("Хранилище не найдено"));
        unit.setUnitType(request.getUnitType());
        unit.setUnitName(request.getUnitName());
        unit.setShelvesCount(request.getShelvesCount());
        return convertUnitToDTO(unitRepository.save(unit));
    }

    @Transactional
    public StorageContainerDTO updateContainer(Long containerId, CreateStorageContainerRequest request) {
        StorageContainer container = containerRepository.findById(containerId)
                .orElseThrow(() -> new RuntimeException("Контейнер не найден"));
        container.setShelfNumber(request.getShelfNumber());
        container.setContainerType(request.getContainerType());
        container.setContainerNumber(request.getContainerNumber());
        if (request.getNumberingType() != null) {
            container.setNumberingType(request.getNumberingType());
        }
        applyContainerLayout(container, request);
        Integer current = request.getCurrentSamplesCount();
        if (current != null) {
            container.setCurrentSamplesCount(current);
        }
        validateContainerCapacity(container);
        return convertContainerToDTO(containerRepository.save(container));
    }

    @Transactional
    public void deleteLocation(Long locationId) {
        StorageLocation location = locationRepository.findById(locationId)
                .orElseThrow(() -> new RuntimeException("Локация не найдена"));
        locationRepository.delete(location);
    }

    @Transactional
    public void deleteUnit(Long unitId) {
        StorageUnit unit = unitRepository.findById(unitId)
                .orElseThrow(() -> new RuntimeException("Хранилище не найдено"));
        unitRepository.delete(unit);
    }

    @Transactional
    public void deleteContainer(Long containerId) {
        StorageContainer container = containerRepository.findById(containerId)
                .orElseThrow(() -> new RuntimeException("Контейнер не найден"));
        containerRepository.delete(container);
    }

    private StorageLocationDTO convertLocationToDTO(StorageLocation location) {
        List<StorageUnitDTO> units = location.getUnits().stream()
                .map(this::convertUnitToDTO)
                .collect(Collectors.toList());
        return new StorageLocationDTO(
                location.getLocationId(),
                location.getLocationName(),
                location.getAddress(),
                location.getRoomNumber(),
                location.getDescription(),
                units
        );
    }

    private StorageUnitDTO convertUnitToDTO(StorageUnit unit) {
        List<StorageContainerDTO> containers = unit.getContainers().stream()
                .map(this::convertContainerToDTO)
                .collect(Collectors.toList());
        return new StorageUnitDTO(
                unit.getUnitId(),
                unit.getLocation().getLocationId(),
                unit.getUnitType(),
                unit.getUnitName(),
                unit.getShelvesCount(),
                containers
        );
    }

    private StorageContainerDTO convertContainerToDTO(StorageContainer container) {
        return new StorageContainerDTO(
                container.getContainerId(),
                container.getShelfNumber(),
                container.getContainerType(),
                container.getContainerNumber(),
                container.getRowsCount(),
                container.getColumnsCount(),
                container.getMaxSamplesCount(),
                container.getCurrentSamplesCount(),
                container.getNumberingType()
        );
    }

    private void applyContainerLayout(StorageContainer container, CreateStorageContainerRequest request) {
        Integer rows = request.getRowsCount();
        Integer columns = request.getColumnsCount();
        container.setRowsCount(rows);
        container.setColumnsCount(columns);
        if (rows != null && columns != null) {
            container.setMaxSamplesCount(rows * columns);
        } else {
            container.setMaxSamplesCount(request.getMaxSamplesCount());
        }
    }

    private void validateContainerCapacity(StorageContainer container) {
        Integer current = container.getCurrentSamplesCount();
        Integer max = container.getMaxSamplesCount();
        if (current != null && max != null && current > max) {
            throw new RuntimeException("Текущее количество превышает вместимость контейнера");
        }
    }
}

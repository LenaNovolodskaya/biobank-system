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
import ru.healthfamily.biobank.model.ContainerTypeTemplate;
import ru.healthfamily.biobank.model.StorageContainer;
import ru.healthfamily.biobank.model.StorageLocation;
import ru.healthfamily.biobank.model.StorageUnit;
import ru.healthfamily.biobank.model.UnitType;
import ru.healthfamily.biobank.repository.ContainerTypeTemplateRepository;
import ru.healthfamily.biobank.repository.StorageContainerRepository;
import ru.healthfamily.biobank.repository.StorageLocationRepository;
import ru.healthfamily.biobank.repository.StorageUnitRepository;
import ru.healthfamily.biobank.repository.UnitTypeRepository;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class StorageService {

    private final StorageLocationRepository locationRepository;
    private final StorageUnitRepository unitRepository;
    private final StorageContainerRepository containerRepository;
    private final ContainerTypeTemplateRepository templateRepository;
    private final UnitTypeRepository unitTypeRepository;

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
        UnitType unitType = unitTypeRepository.findById(request.getUnitTypeId())
                .orElseThrow(() -> new RuntimeException("Тип хранилища не найден"));
        StorageUnit unit = new StorageUnit();
        unit.setLocation(location);
        unit.setUnitType(unitType);
        unit.setUnitName(request.getUnitName());
        unit.setShelvesCount(request.getShelvesCount());
        return convertUnitToDTO(unitRepository.save(unit));
    }

    @Transactional
    public StorageContainerDTO addContainerToUnit(Long unitId, CreateStorageContainerRequest request) {
        StorageUnit unit = unitRepository.findById(unitId)
                .orElseThrow(() -> new RuntimeException("Хранилище не найдено"));
        ContainerTypeTemplate template = resolveOrCreateTemplate(request);
        StorageContainer container = new StorageContainer();
        container.setUnit(unit);
        container.setTemplate(template);
        container.setContainerNumber(request.getContainerNumber());
        container.setShelfNumber(request.getShelfNumber());
        container.setCurrentSamplesCount(0);
        return convertContainerToDTO(containerRepository.save(container));
    }

    /** Находит шаблон по ID или создаёт новый, если передан templateName и шаблона ещё нет */
    private ContainerTypeTemplate resolveOrCreateTemplate(CreateStorageContainerRequest request) {
        if (request.getTemplateId() != null) {
            return templateRepository.findById(request.getTemplateId())
                    .orElseThrow(() -> new RuntimeException("Шаблон контейнера не найден"));
        }
        String name = request.getTemplateName();
        if (name == null || name.isBlank()) {
            throw new RuntimeException("Укажите templateId или templateName для шаблона контейнера");
        }
        return templateRepository.findByTemplateNameIgnoreCase(name.trim())
                .orElseGet(() -> {
                    Integer rows = request.getRowsCount() != null ? request.getRowsCount() : 1;
                    Integer cols = request.getColumnsCount() != null ? request.getColumnsCount() : 1;
                    String numbering = request.getNumberingType() != null && !request.getNumberingType().isBlank()
                            ? request.getNumberingType() : "SEQUENTIAL";
                    ContainerTypeTemplate t = new ContainerTypeTemplate();
                    t.setTemplateName(name.trim());
                    t.setRowsCount(rows);
                    t.setColumnsCount(cols);
                    t.setMaxSamplesCount(rows * cols);
                    t.setNumberingType(numbering);
                    return templateRepository.save(t);
                });
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
        UnitType unitType = unitTypeRepository.findById(request.getUnitTypeId())
                .orElseThrow(() -> new RuntimeException("Тип хранилища не найден"));
        unit.setUnitType(unitType);
        unit.setUnitName(request.getUnitName());
        unit.setShelvesCount(request.getShelvesCount());
        return convertUnitToDTO(unitRepository.save(unit));
    }

    @Transactional
    public StorageContainerDTO updateContainer(Long containerId, CreateStorageContainerRequest request) {
        StorageContainer container = containerRepository.findById(containerId)
                .orElseThrow(() -> new RuntimeException("Контейнер не найден"));
        ContainerTypeTemplate template = resolveOrCreateTemplate(request);
        container.setTemplate(template);
        container.setContainerNumber(request.getContainerNumber());
        container.setShelfNumber(request.getShelfNumber());
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
                unit.getUnitType().getUnitTypeId(),
                unit.getUnitType().getUnitTypeName(),
                unit.getUnitName(),
                unit.getShelvesCount(),
                containers
        );
    }

    private StorageContainerDTO convertContainerToDTO(StorageContainer container) {
        ContainerTypeTemplate t = container.getTemplate();
        return new StorageContainerDTO(
                container.getContainerId(),
                container.getContainerNumber(),
                container.getCurrentSamplesCount(),
                container.getUnit().getUnitId(),
                container.getShelfNumber(),
                t.getTemplateId(),
                t.getTemplateName(),
                t.getRowsCount(),
                t.getColumnsCount(),
                t.getMaxSamplesCount(),
                t.getNumberingType()
        );
    }
}

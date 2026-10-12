package com.smartbuilding.controller;

import com.smartbuilding.model.Unit;
import com.smartbuilding.repository.BuildingRepository;
import com.smartbuilding.repository.UnitRepository;
import com.smartbuilding.repository.UserRepository;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/units")
public class UnitController {

    private final UnitRepository unitRepository;
    private final BuildingRepository buildingRepository;
    private final UserRepository userRepository;

    public UnitController(UnitRepository unitRepository, BuildingRepository buildingRepository, UserRepository userRepository) {
        this.unitRepository = unitRepository;
        this.buildingRepository = buildingRepository;
        this.userRepository = userRepository;
    }

    @GetMapping
    public List<Map<String, Object>> getAllUnits(@RequestParam(required = false) Long buildingId) {
        List<Unit> units = buildingId != null ? 
                unitRepository.findByBuildingId(buildingId) : unitRepository.findAll();
        return units.stream().map(this::toResponse).collect(Collectors.toList());
    }

    @GetMapping("/vacant")
    public List<Map<String, Object>> getVacantUnits(@RequestParam Long buildingId) {
        return unitRepository.findVacantUnitsByBuilding(buildingId).stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @GetMapping("/{id}")
    public Map<String, Object> getUnitById(@PathVariable Long id) {
        Unit unit = unitRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Unit not found: " + id));
        return toResponse(unit);
    }

    @GetMapping("/by-number/{unitNumber}")
    public Map<String, Object> getUnitByNumber(@PathVariable String unitNumber) {
        Unit unit = unitRepository.findByUnitNumber(unitNumber)
                .orElseThrow(() -> new RuntimeException("Unit not found: " + unitNumber));
        return toResponse(unit);
    }

    @PostMapping
    public Map<String, Object> createUnit(@RequestBody Map<String, Object> request) {
        Unit unit = new Unit();
        unit.setUnitNumber((String) request.get("unitNumber"));
        unit.setBuildingId(Long.valueOf(request.get("buildingId").toString()));
        unit.setFloorNumber(request.get("floorNumber") != null ? 
                Integer.valueOf(request.get("floorNumber").toString()) : 0);
        unit.setUnitType((String) request.get("unitType"));
        unit.setAreaSqft(request.get("areaSqft") != null ? 
                Double.valueOf(request.get("areaSqft").toString()) : 0);
        unit.setMonthlyMaintenance(request.get("monthlyMaintenance") != null ? 
                Double.valueOf(request.get("monthlyMaintenance").toString()) : 0);
        unit.setStatus("VACANT");

        // Validate building exists
        buildingRepository.findById(unit.getBuildingId())
                .orElseThrow(() -> new RuntimeException("Building not found"));

        Unit saved = unitRepository.save(unit);
        return toResponse(saved);
    }

    @PutMapping("/{id}")
    public Map<String, Object> updateUnit(@PathVariable Long id, @RequestBody Map<String, Object> request) {
        Unit unit = unitRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Unit not found: " + id));

        if (request.get("unitNumber") != null) unit.setUnitNumber((String) request.get("unitNumber"));
        if (request.get("buildingId") != null) {
            Long buildingId = Long.valueOf(request.get("buildingId").toString());
            buildingRepository.findById(buildingId)
                    .orElseThrow(() -> new RuntimeException("Building not found"));
            unit.setBuildingId(buildingId);
        }
        if (request.get("floorNumber") != null) unit.setFloorNumber(Integer.valueOf(request.get("floorNumber").toString()));
        if (request.get("unitType") != null) unit.setUnitType((String) request.get("unitType"));
        if (request.get("areaSqft") != null) unit.setAreaSqft(Double.valueOf(request.get("areaSqft").toString()));
        if (request.get("monthlyMaintenance") != null) unit.setMonthlyMaintenance(Double.valueOf(request.get("monthlyMaintenance").toString()));
        if (request.get("status") != null) unit.setStatus((String) request.get("status"));

        Unit saved = unitRepository.save(unit);
        return toResponse(saved);
    }

    @PutMapping("/{id}/assign-resident")
    public Map<String, Object> assignResident(@PathVariable Long id, @RequestBody Map<String, Object> request) {
        Unit unit = unitRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Unit not found: " + id));

        Long residentId = Long.valueOf(request.get("residentId").toString());
        userRepository.findById(residentId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        unit.setResidentId(residentId);
        unit.setStatus("OCCUPIED");

        Unit saved = unitRepository.save(unit);
        return toResponse(saved);
    }

    @PutMapping("/{id}/vacate")
    public Map<String, Object> vacateUnit(@PathVariable Long id) {
        Unit unit = unitRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Unit not found: " + id));

        unit.setResidentId(null);
        unit.setStatus("VACANT");

        Unit saved = unitRepository.save(unit);
        return toResponse(saved);
    }

    @DeleteMapping("/{id}")
    public void deleteUnit(@PathVariable Long id) {
        unitRepository.deleteById(id);
    }

    private Map<String, Object> toResponse(Unit unit) {
        Map<String, Object> response = new java.util.HashMap<>();
        response.put("id", unit.getId());
        response.put("unitNumber", unit.getUnitNumber());
        response.put("floorNumber", unit.getFloorNumber());
        response.put("unitType", unit.getUnitType());
        response.put("areaSqft", unit.getAreaSqft());
        response.put("buildingId", unit.getBuildingId());
        response.put("buildingName", unit.getBuildingId() != null ?
                buildingRepository.findById(unit.getBuildingId()).map(b -> b.getName()).orElse(null) : null);
        response.put("residentId", unit.getResidentId());
        response.put("residentName", unit.getResidentId() != null ?
                userRepository.findById(unit.getResidentId())
                        .map(u -> u.getFirstName() + " " + u.getLastName()).orElse(null) : null);
        response.put("ownerId", unit.getOwnerId());
        response.put("monthlyMaintenance", unit.getMonthlyMaintenance());
        response.put("status", unit.getStatus());
        response.put("createdAt", unit.getCreatedAt());
        return response;
    }
}
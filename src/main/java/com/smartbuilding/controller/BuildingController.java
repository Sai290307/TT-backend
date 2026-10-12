package com.smartbuilding.controller;

import com.smartbuilding.model.Building;
import com.smartbuilding.repository.BuildingRepository;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/buildings")
public class BuildingController {

    private final BuildingRepository buildingRepository;

    public BuildingController(BuildingRepository buildingRepository) {
        this.buildingRepository = buildingRepository;
    }

    @GetMapping
    public List<Map<String, Object>> getAllBuildings() {
        return buildingRepository.findAll().stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @GetMapping("/{id}")
    public Map<String, Object> getBuildingById(@PathVariable Long id) {
        Building building = buildingRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Building not found: " + id));
        return toResponse(building);
    }

    @PostMapping
    public Map<String, Object> createBuilding(@RequestBody Map<String, Object> request) {
        Building building = new Building();
        building.setName((String) request.get("name"));
        building.setAddress((String) request.get("address"));
        building.setTotalFloors(request.get("totalFloors") != null ? 
                Integer.valueOf(request.get("totalFloors").toString()) : 0);
        building.setTotalUnits(request.get("totalUnits") != null ? 
                Integer.valueOf(request.get("totalUnits").toString()) : 0);

        Building saved = buildingRepository.save(building);
        return toResponse(saved);
    }

    @PutMapping("/{id}")
    public Map<String, Object> updateBuilding(@PathVariable Long id, @RequestBody Map<String, Object> request) {
        Building building = buildingRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Building not found: " + id));

        if (request.get("name") != null) building.setName((String) request.get("name"));
        if (request.get("address") != null) building.setAddress((String) request.get("address"));
        if (request.get("totalFloors") != null) building.setTotalFloors(Integer.valueOf(request.get("totalFloors").toString()));
        if (request.get("totalUnits") != null) building.setTotalUnits(Integer.valueOf(request.get("totalUnits").toString()));

        Building saved = buildingRepository.save(building);
        return toResponse(saved);
    }

    @DeleteMapping("/{id}")
    public void deleteBuilding(@PathVariable Long id) {
        buildingRepository.deleteById(id);
    }

    private Map<String, Object> toResponse(Building building) {
        Map<String, Object> response = new java.util.HashMap<>();
        response.put("id", building.getId());
        response.put("name", building.getName());
        response.put("address", building.getAddress());
        response.put("totalFloors", building.getTotalFloors());
        response.put("totalUnits", building.getTotalUnits());
        response.put("createdAt", building.getCreatedAt());
        return response;
    }
}
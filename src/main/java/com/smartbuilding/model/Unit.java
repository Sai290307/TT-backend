package com.smartbuilding.model;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "units")
public class Unit {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "unit_number", nullable = false, unique = true)
    private String unitNumber;

    @Column(name = "floor_number")
    private Integer floorNumber;

    @Column(name = "unit_type")
    private String unitType; // 1BHK, 2BHK, 3BHK, etc.

    @Column(name = "area_sqft")
    private Double areaSqft;

    @Column(name = "building_id", nullable = false)
    private Long buildingId;

    @Column(name = "resident_id")
    private Long residentId; // User who lives here

    @Column(name = "owner_id")
    private Long ownerId; // User who owns this (can be different from resident)

    @Column(name = "monthly_maintenance")
    private Double monthlyMaintenance;

    @Column(name = "status")
    private String status = "VACANT"; // VACANT, OCCUPIED, MAINTENANCE

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @PrePersist
    public void onCreate() {
        this.createdAt = LocalDateTime.now();
    }

    public Unit() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getUnitNumber() { return unitNumber; }
    public void setUnitNumber(String unitNumber) { this.unitNumber = unitNumber; }
    public Integer getFloorNumber() { return floorNumber; }
    public void setFloorNumber(Integer floorNumber) { this.floorNumber = floorNumber; }
    public String getUnitType() { return unitType; }
    public void setUnitType(String unitType) { this.unitType = unitType; }
    public Double getAreaSqft() { return areaSqft; }
    public void setAreaSqft(Double areaSqft) { this.areaSqft = areaSqft; }
    public Long getBuildingId() { return buildingId; }
    public void setBuildingId(Long buildingId) { this.buildingId = buildingId; }
    public Long getResidentId() { return residentId; }
    public void setResidentId(Long residentId) { this.residentId = residentId; }
    public Long getOwnerId() { return ownerId; }
    public void setOwnerId(Long ownerId) { this.ownerId = ownerId; }
    public Double getMonthlyMaintenance() { return monthlyMaintenance; }
    public void setMonthlyMaintenance(Double monthlyMaintenance) { this.monthlyMaintenance = monthlyMaintenance; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
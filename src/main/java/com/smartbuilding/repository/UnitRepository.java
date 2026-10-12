package com.smartbuilding.repository;

import com.smartbuilding.model.Unit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface UnitRepository extends JpaRepository<Unit, Long> {

    List<Unit> findByBuildingId(Long buildingId);

    Optional<Unit> findByUnitNumber(String unitNumber);

    List<Unit> findByResidentId(Long residentId);

    List<Unit> findByOwnerId(Long ownerId);

    List<Unit> findByStatus(String status);

    @Query("SELECT u FROM Unit u WHERE u.buildingId = :buildingId AND u.status = 'VACANT'")
    List<Unit> findVacantUnitsByBuilding(@Param("buildingId") Long buildingId);
}
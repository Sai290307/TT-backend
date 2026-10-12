package com.smartbuilding.repository;

import com.smartbuilding.model.Announcement;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface AnnouncementRepository extends JpaRepository<Announcement, Long> {

    List<Announcement> findByBuildingId(Long buildingId);

    List<Announcement> findByAuthorId(Long authorId);

    @Query("SELECT a FROM Announcement a WHERE (a.buildingId = :buildingId OR a.buildingId IS NULL) " +
           "AND a.isPublished = true " +
           "AND (a.expiresAt IS NULL OR a.expiresAt > :now) " +
           "ORDER BY a.priority DESC, a.publishedAt DESC")
    List<Announcement> findActiveForBuilding(@Param("buildingId") Long buildingId, @Param("now") LocalDateTime now);

    @Query("SELECT a FROM Announcement a WHERE a.isPublished = true " +
           "AND (a.expiresAt IS NULL OR a.expiresAt > :now) " +
           "ORDER BY a.priority DESC, a.publishedAt DESC")
    List<Announcement> findAllActiveGlobal(@Param("now") LocalDateTime now);
}
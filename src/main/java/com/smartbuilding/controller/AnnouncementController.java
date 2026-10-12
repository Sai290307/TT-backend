package com.smartbuilding.controller;

import com.smartbuilding.model.Announcement;
import com.smartbuilding.model.AnnouncementType;
import com.smartbuilding.repository.AnnouncementRepository;
import com.smartbuilding.repository.BuildingRepository;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/announcements")
public class AnnouncementController {

    private final AnnouncementRepository announcementRepository;
    private final BuildingRepository buildingRepository;

    public AnnouncementController(AnnouncementRepository announcementRepository, BuildingRepository buildingRepository) {
        this.announcementRepository = announcementRepository;
        this.buildingRepository = buildingRepository;
    }

    @GetMapping
    public List<Map<String, Object>> getAllAnnouncements(@RequestParam(required = false) Long buildingId) {
        List<Announcement> announcements = buildingId != null ? 
                announcementRepository.findByBuildingId(buildingId) : announcementRepository.findAll();
        return announcements.stream().map(this::toResponse).collect(Collectors.toList());
    }

    @GetMapping("/active")
    public List<Map<String, Object>> getActiveAnnouncements(@RequestParam(required = false) Long buildingId) {
        List<Announcement> announcements = buildingId != null ? 
                announcementRepository.findActiveForBuilding(buildingId, LocalDateTime.now()) :
                announcementRepository.findAllActiveGlobal(LocalDateTime.now());
        return announcements.stream().map(this::toResponse).collect(Collectors.toList());
    }

    @GetMapping("/{id}")
    public Map<String, Object> getAnnouncementById(@PathVariable Long id) {
        Announcement announcement = announcementRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Announcement not found: " + id));
        return toResponse(announcement);
    }

    @PostMapping
    public Map<String, Object> createAnnouncement(@RequestAttribute Long userId, @RequestBody Map<String, Object> request) {
        Announcement announcement = new Announcement();
        announcement.setTitle((String) request.get("title"));
        announcement.setContent((String) request.get("content"));
        announcement.setAuthorId(userId);
        
        if (request.get("buildingId") != null) {
            announcement.setBuildingId(Long.valueOf(request.get("buildingId").toString()));
        }
        
        if (request.get("type") != null) {
            announcement.setType(AnnouncementType.valueOf(((String) request.get("type")).toUpperCase()));
        }
        
        if (request.get("priority") != null) {
            announcement.setPriority(Integer.valueOf(request.get("priority").toString()));
        }
        
        if (request.get("expiresAt") != null) {
            announcement.setExpiresAt(LocalDateTime.parse((String) request.get("expiresAt")));
        }

        Announcement saved = announcementRepository.save(announcement);
        return toResponse(saved);
    }

    @PutMapping("/{id}")
    public Map<String, Object> updateAnnouncement(@PathVariable Long id, @RequestBody Map<String, Object> request) {
        Announcement announcement = announcementRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Announcement not found: " + id));

        if (request.get("title") != null) announcement.setTitle((String) request.get("title"));
        if (request.get("content") != null) announcement.setContent((String) request.get("content"));
        if (request.get("type") != null) announcement.setType(AnnouncementType.valueOf(((String) request.get("type")).toUpperCase()));
        if (request.get("priority") != null) announcement.setPriority(Integer.valueOf(request.get("priority").toString()));
        if (request.get("expiresAt") != null) announcement.setExpiresAt(LocalDateTime.parse((String) request.get("expiresAt")));

        Announcement saved = announcementRepository.save(announcement);
        return toResponse(saved);
    }

    @PutMapping("/{id}/publish")
    public Map<String, Object> publishAnnouncement(@PathVariable Long id) {
        Announcement announcement = announcementRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Announcement not found: " + id));

        announcement.setIsPublished(true);
        announcement.setPublishedAt(LocalDateTime.now());

        Announcement saved = announcementRepository.save(announcement);
        return toResponse(saved);
    }

    @PutMapping("/{id}/unpublish")
    public Map<String, Object> unpublishAnnouncement(@PathVariable Long id) {
        Announcement announcement = announcementRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Announcement not found: " + id));

        announcement.setIsPublished(false);

        Announcement saved = announcementRepository.save(announcement);
        return toResponse(saved);
    }

    @DeleteMapping("/{id}")
    public void deleteAnnouncement(@PathVariable Long id) {
        announcementRepository.deleteById(id);
    }

    private Map<String, Object> toResponse(Announcement announcement) {
        Map<String, Object> response = new java.util.HashMap<>();
        response.put("id", announcement.getId());
        response.put("title", announcement.getTitle());
        response.put("content", announcement.getContent());
        response.put("buildingId", announcement.getBuildingId());
        response.put("authorId", announcement.getAuthorId());
        response.put("type", announcement.getType().name());
        response.put("priority", announcement.getPriority());
        response.put("publishedAt", announcement.getPublishedAt());
        response.put("expiresAt", announcement.getExpiresAt());
        response.put("isPublished", announcement.getIsPublished());
        response.put("createdAt", announcement.getCreatedAt());
        return response;
    }
}
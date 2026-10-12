package com.smartbuilding.controller;

import com.smartbuilding.model.Issue;
import com.smartbuilding.model.IssuePriority;
import com.smartbuilding.model.IssueStatus;
import com.smartbuilding.repository.IssueRepository;
import com.smartbuilding.repository.UserRepository;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/issues")
public class IssueController {

    private final IssueRepository issueRepository;
    private final UserRepository userRepository;

    public IssueController(IssueRepository issueRepository, UserRepository userRepository) {
        this.issueRepository = issueRepository;
        this.userRepository = userRepository;
    }

    @GetMapping
    public List<Map<String, Object>> getAllIssues() {
        return issueRepository.findAll().stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @GetMapping("/my")
    public List<Map<String, Object>> getMyIssues(@RequestAttribute Long userId) {
        return issueRepository.findByReportedById(userId).stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @GetMapping("/assigned")
    public List<Map<String, Object>> getAssignedIssues(@RequestAttribute Long userId) {
        return issueRepository.findByAssignedToId(userId).stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @GetMapping("/{id}")
    public Map<String, Object> getIssueById(@PathVariable Long id) {
        Issue issue = issueRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Issue not found: " + id));
        return toResponse(issue);
    }

    @PostMapping
    public Map<String, Object> createIssue(@RequestAttribute Long userId, @RequestBody Map<String, Object> request) {
        Issue issue = new Issue();
        issue.setTitle((String) request.get("title"));
        issue.setDescription((String) request.get("description"));
        issue.setReportedById(userId);
        issue.setUnitNumber((String) request.get("unitNumber"));
        
        if (request.get("priority") != null) {
            issue.setPriority(IssuePriority.valueOf(((String) request.get("priority")).toUpperCase()));
        }

        Issue saved = issueRepository.save(issue);
        return toResponse(saved);
    }

    @PutMapping("/{id}")
    public Map<String, Object> updateIssue(@PathVariable Long id, @RequestBody Map<String, Object> request) {
        Issue issue = issueRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Issue not found: " + id));

        if (request.get("title") != null) issue.setTitle((String) request.get("title"));
        if (request.get("description") != null) issue.setDescription((String) request.get("description"));
        if (request.get("priority") != null) issue.setPriority(IssuePriority.valueOf(((String) request.get("priority")).toUpperCase()));
        if (request.get("status") != null) {
            issue.setStatus(IssueStatus.valueOf(((String) request.get("status")).toUpperCase()));
            if (issue.getStatus() == IssueStatus.RESOLVED || issue.getStatus() == IssueStatus.CLOSED) {
                issue.setResolvedAt(LocalDateTime.now());
            }
        }
        if (request.get("unitNumber") != null) issue.setUnitNumber((String) request.get("unitNumber"));

        Issue saved = issueRepository.save(issue);
        return toResponse(saved);
    }

    @PutMapping("/{id}/assign")
    public Map<String, Object> assignIssue(@PathVariable Long id, @RequestBody Map<String, Object> request) {
        Issue issue = issueRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Issue not found: " + id));

        Long technicianId = Long.valueOf(request.get("technicianId").toString());
        if (!userRepository.existsById(technicianId)) {
            throw new RuntimeException("Technician not found: " + technicianId);
        }

        issue.setAssignedToId(technicianId);
        issue.setStatus(IssueStatus.IN_PROGRESS);

        Issue saved = issueRepository.save(issue);
        return toResponse(saved);
    }

    @PutMapping("/{id}/status")
    public Map<String, Object> updateStatus(@PathVariable Long id, @RequestBody Map<String, Object> request) {
        Issue issue = issueRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Issue not found: " + id));

        String statusStr = (String) request.get("status");
        if (statusStr != null) {
            issue.setStatus(IssueStatus.valueOf(statusStr.toUpperCase()));
            if (issue.getStatus() == IssueStatus.RESOLVED || issue.getStatus() == IssueStatus.CLOSED) {
                issue.setResolvedAt(LocalDateTime.now());
            }
        }

        Issue saved = issueRepository.save(issue);
        return toResponse(saved);
    }

    @DeleteMapping("/{id}")
    public void deleteIssue(@PathVariable Long id) {
        issueRepository.deleteById(id);
    }

    private Map<String, Object> toResponse(Issue issue) {
        Map<String, Object> response = new java.util.HashMap<>();
        response.put("id", issue.getId());
        response.put("title", issue.getTitle());
        response.put("description", issue.getDescription());
        response.put("status", issue.getStatus().name());
        response.put("priority", issue.getPriority().name());
        response.put("reportedById", issue.getReportedById());
        response.put("reportedByName", userRepository.findById(issue.getReportedById())
                .map(u -> u.getFirstName() + " " + u.getLastName()).orElse("Unknown"));
        response.put("assignedToId", issue.getAssignedToId());
        response.put("assignedToName", issue.getAssignedToId() != null ?
                userRepository.findById(issue.getAssignedToId())
                        .map(u -> u.getFirstName() + " " + u.getLastName()).orElse("Unknown") : null);
        response.put("unitNumber", issue.getUnitNumber());
        response.put("createdAt", issue.getCreatedAt());
        response.put("updatedAt", issue.getUpdatedAt());
        response.put("resolvedAt", issue.getResolvedAt());
        return response;
    }
}
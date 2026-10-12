package com.smartbuilding.repository;

import com.smartbuilding.model.Issue;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface IssueRepository extends JpaRepository<Issue, Long> {

    List<Issue> findByReportedById(Long userId);

    List<Issue> findByAssignedToId(Long userId);

    List<Issue> findByStatus(com.smartbuilding.model.IssueStatus status);

    @Query("SELECT i FROM Issue i WHERE i.reportedById = :userId OR i.assignedToId = :userId")
    List<Issue> findByUserInvolved(@Param("userId") Long userId);
}
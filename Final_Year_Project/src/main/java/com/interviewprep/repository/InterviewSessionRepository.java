package com.interviewprep.repository;

import com.interviewprep.entity.InterviewSession;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface InterviewSessionRepository extends JpaRepository<InterviewSession, Long> {

    List<InterviewSession> findByUserIdOrderByStartedAtDesc(Long userId, Pageable pageable);

    @Query("SELECT s FROM InterviewSession s WHERE s.user.id = :userId AND s.roleSnapshot = :role ORDER BY s.startedAt DESC")
    List<InterviewSession> findByUserIdAndRoleOrderByStartedAtDesc(Long userId, String role, Pageable pageable);

    long countByUserId(Long userId);

    long countByUserIdAndRoleSnapshot(Long userId, String role);
}

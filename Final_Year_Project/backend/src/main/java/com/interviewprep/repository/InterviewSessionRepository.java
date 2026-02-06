package com.interviewprep.repository;

import com.interviewprep.entity.InterviewSession;
import com.interviewprep.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface InterviewSessionRepository extends JpaRepository<InterviewSession, Long> {
    List<InterviewSession> findByUserOrderByStartTimeDesc(User user);
    List<InterviewSession> findByUserIdOrderByStartTimeDesc(Long userId);
}

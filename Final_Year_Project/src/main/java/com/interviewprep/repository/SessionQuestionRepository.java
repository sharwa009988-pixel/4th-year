package com.interviewprep.repository;

import com.interviewprep.entity.SessionQuestion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface SessionQuestionRepository extends JpaRepository<SessionQuestion, Long> {

    List<SessionQuestion> findBySessionIdOrderByQuestionOrder(Long sessionId);

    @Query("SELECT sq.topic, AVG(sq.score) FROM SessionQuestion sq WHERE sq.session.user.id = :userId AND sq.topic IS NOT NULL GROUP BY sq.topic")
    List<Object[]> findTopicWiseAverageScoreByUserId(Long userId);
}

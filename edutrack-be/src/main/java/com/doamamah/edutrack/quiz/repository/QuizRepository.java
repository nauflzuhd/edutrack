package com.doamamah.edutrack.quiz.repository;

import com.doamamah.edutrack.quiz.model.Quiz;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository untuk akses data Quiz dari database.
 */
@Repository
public interface QuizRepository extends JpaRepository<Quiz, Long> {
    List<Quiz> findByTeacherId(Long teacherId);
    List<Quiz> findByTeacherIdIn(List<Long> teacherIds);
}

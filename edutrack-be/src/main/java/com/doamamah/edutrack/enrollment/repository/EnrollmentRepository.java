package com.doamamah.edutrack.enrollment.repository;

import com.doamamah.edutrack.enrollment.model.Enrollment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository untuk akses data Enrollment dari database.
 */
@Repository
public interface EnrollmentRepository extends JpaRepository<Enrollment, Long> {

    List<Enrollment> findByStudentId(Long studentId);

    List<Enrollment> findByTeacherId(Long teacherId);

    Optional<Enrollment> findByStudentIdAndTeacherId(Long studentId, Long teacherId);

    void deleteByStudentIdAndTeacherId(Long studentId, Long teacherId);

    long countByTeacherId(Long teacherId);
}

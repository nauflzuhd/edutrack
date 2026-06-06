package com.doamamah.edutrack.material.repository;

import com.doamamah.edutrack.material.model.CourseMaterial;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository untuk akses data CourseMaterial dari database.
 */
@Repository
public interface CourseMaterialRepository extends JpaRepository<CourseMaterial, Long> {
    List<CourseMaterial> findByTeacherId(Long teacherId);
    List<CourseMaterial> findByTeacherIdIn(List<Long> teacherIds);
}

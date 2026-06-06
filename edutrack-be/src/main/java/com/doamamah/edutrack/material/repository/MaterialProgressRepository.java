package com.doamamah.edutrack.material.repository;

import com.doamamah.edutrack.material.model.MaterialProgress;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MaterialProgressRepository extends JpaRepository<MaterialProgress, Long> {
    
    List<MaterialProgress> findByStudentId(Long studentId);
    
    Optional<MaterialProgress> findByStudentIdAndMaterialId(Long studentId, Long materialId);
    
    void deleteByMaterialId(Long materialId);
}

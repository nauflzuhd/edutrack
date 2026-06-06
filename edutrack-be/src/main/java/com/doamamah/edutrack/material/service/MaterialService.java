package com.doamamah.edutrack.material.service;

import com.doamamah.edutrack.material.model.CourseMaterial;
import com.doamamah.edutrack.material.repository.CourseMaterialRepository;
import com.doamamah.edutrack.material.model.MaterialProgress;
import com.doamamah.edutrack.material.repository.MaterialProgressRepository;
import com.doamamah.edutrack.auth.repository.UserRepository;
import com.doamamah.edutrack.auth.model.Student;
import com.doamamah.edutrack.auth.model.User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;
import java.util.Optional;
import com.doamamah.edutrack.auth.model.Teacher;

/**
 * Service layer untuk operasi CRUD materi pembelajaran.
 */
@Service
public class MaterialService {

    private final CourseMaterialRepository materialRepository;
    private final MaterialProgressRepository progressRepository;
    private final UserRepository userRepository;

    public MaterialService(CourseMaterialRepository materialRepository, 
                           MaterialProgressRepository progressRepository,
                           UserRepository userRepository) {
        this.materialRepository = materialRepository;
        this.progressRepository = progressRepository;
        this.userRepository = userRepository;
    }

    /**
     * Mengambil semua materi dari database.
     */
    public List<CourseMaterial> getAllMaterials() {
        return materialRepository.findAll();
    }

    /**
     * Mengambil materi berdasarkan ID.
     */
    public Optional<CourseMaterial> getMaterialById(Long id) {
        return materialRepository.findById(id);
    }

    /**
     * Menambahkan materi baru ke database.
     */
    public CourseMaterial addMaterial(CourseMaterial material) {
        return materialRepository.save(material);
    }

    /**
     * Menambahkan materi baru dengan ownership pengajar.
     */
    public CourseMaterial addMaterial(CourseMaterial material, Long teacherId) {
        if (teacherId != null) {
            Teacher teacher = (Teacher) userRepository.findById(teacherId)
                    .orElseThrow(() -> new RuntimeException("Pengajar tidak ditemukan."));
            material.setTeacher(teacher);
        }
        return materialRepository.save(material);
    }

    /**
     * Mengambil materi berdasarkan daftar teacher IDs (untuk siswa).
     */
    public List<CourseMaterial> getMaterialsByTeacherIds(List<Long> teacherIds) {
        if (teacherIds == null || teacherIds.isEmpty()) return List.of();
        return materialRepository.findByTeacherIdIn(teacherIds);
    }

    /**
     * Mengambil materi berdasarkan teacher ID (untuk pengajar).
     */
    public List<CourseMaterial> getMaterialsByTeacher(Long teacherId) {
        return materialRepository.findByTeacherId(teacherId);
    }

    /**
     * Memperbarui materi yang sudah ada di database.
     *
     * @param id ID materi yang akan diupdate
     * @param updatedData data materi yang baru
     * @return CourseMaterial yang sudah diupdate
     * @throws RuntimeException jika materi tidak ditemukan
     */
    public CourseMaterial updateMaterial(Long id, CourseMaterial updatedData) {
        CourseMaterial existing = materialRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Materi dengan ID " + id + " tidak ditemukan."));

        existing.setTitle(updatedData.getTitle());
        existing.setDescription(updatedData.getDescription());
        existing.setType(updatedData.getType());
        existing.setVideoUrl(updatedData.getVideoUrl());
        existing.setDurationMinutes(updatedData.getDurationMinutes());
        existing.setTextContent(updatedData.getTextContent());

        return materialRepository.save(existing);
    }

    @Transactional
    public void deleteMaterial(Long id) {
        if (!materialRepository.existsById(id)) {
            throw new RuntimeException("Materi dengan ID " + id + " tidak ditemukan.");
        }
        // Hapus progress terlebih dahulu untuk mencegah foreign key constraint error
        progressRepository.deleteByMaterialId(id);
        materialRepository.deleteById(id);
    }

    /**
     * Menandai materi telah dibaca/ditonton oleh siswa.
     */
    public MaterialProgress markAsViewed(Long materialId, Long studentId) {
        if (progressRepository.findByStudentIdAndMaterialId(studentId, materialId).isPresent()) {
            return progressRepository.findByStudentIdAndMaterialId(studentId, materialId).get();
        }

        CourseMaterial material = materialRepository.findById(materialId)
                .orElseThrow(() -> new RuntimeException("Materi tidak ditemukan"));
        
        User user = userRepository.findById(studentId)
                .orElseThrow(() -> new RuntimeException("User tidak ditemukan"));

        if (!(user instanceof Student)) {
            throw new RuntimeException("Hanya siswa yang dapat menandai progress materi");
        }

        MaterialProgress progress = new MaterialProgress((Student) user, material);
        return progressRepository.save(progress);
    }

    /**
     * Mengambil daftar ID materi yang sudah diakses siswa.
     */
    public List<Long> getViewedMaterials(Long studentId) {
        return progressRepository.findByStudentId(studentId)
                .stream()
                .map(p -> p.getMaterial().getId())
                .collect(Collectors.toList());
    }
}

package com.doamamah.edutrack.material.controller;

import com.doamamah.edutrack.material.model.CourseMaterial;
import com.doamamah.edutrack.material.service.MaterialService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * REST Controller untuk operasi CRUD materi pembelajaran.
 * Menyediakan endpoint /api/materials.
 */
@RestController
@RequestMapping("/api/materials")
public class MaterialController {

    private final MaterialService materialService;

    public MaterialController(MaterialService materialService) {
        this.materialService = materialService;
    }

    /**
     * GET /api/materials
     * Mengambil semua materi, atau filter berdasarkan teacherIds.
     * Query param optional: ?teacherIds=1,2,3
     */
    @GetMapping
    public ResponseEntity<List<Map<String, Object>>> getAllMaterials(
            @RequestParam(required = false) List<Long> teacherIds) {
        List<CourseMaterial> materials;
        if (teacherIds != null && !teacherIds.isEmpty()) {
            materials = materialService.getMaterialsByTeacherIds(teacherIds);
        } else {
            materials = materialService.getAllMaterials();
        }
        return ResponseEntity.ok(materials.stream().map(this::mapMaterial).collect(Collectors.toList()));
    }

    /**
     * GET /api/materials/teacher/{teacherId}
     * Mengambil materi berdasarkan pengajar (untuk halaman guru).
     */
    @GetMapping("/teacher/{teacherId}")
    public ResponseEntity<List<Map<String, Object>>> getMaterialsByTeacher(@PathVariable Long teacherId) {
        List<CourseMaterial> materials = materialService.getMaterialsByTeacher(teacherId);
        return ResponseEntity.ok(materials.stream().map(this::mapMaterial).collect(Collectors.toList()));
    }

    /**
     * GET /api/materials/{id}
     * Mengambil materi berdasarkan ID.
     */
    @GetMapping("/{id}")
    public ResponseEntity<?> getMaterialById(@PathVariable Long id) {
        return materialService.getMaterialById(id)
                .map(material -> ResponseEntity.ok((Object) mapMaterial(material)))
                .orElse(ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Map.of("error", "Materi dengan ID " + id + " tidak ditemukan.")));
    }

    /**
     * POST /api/materials
     * Menambahkan materi baru.
     * Body: { "title": "...", "description": "...", "type": "VIDEO"|"TEXT", "teacherId": 1, ... }
     */
    @PostMapping
    public ResponseEntity<?> addMaterial(@RequestBody Map<String, Object> body) {
        CourseMaterial material = new CourseMaterial();
        material.setTitle((String) body.get("title"));
        material.setDescription((String) body.get("description"));
        material.setType((String) body.get("type"));

        if (body.containsKey("videoUrl")) material.setVideoUrl((String) body.get("videoUrl"));
        if (body.containsKey("durationMinutes")) {
            material.setDurationMinutes(((Number) body.get("durationMinutes")).intValue());
        }
        if (body.containsKey("textContent")) material.setTextContent((String) body.get("textContent"));

        Long teacherId = body.containsKey("teacherId") && body.get("teacherId") != null
                ? ((Number) body.get("teacherId")).longValue() : null;

        CourseMaterial saved = materialService.addMaterial(material, teacherId);
        return ResponseEntity.status(HttpStatus.CREATED).body(mapMaterial(saved));
    }

    /**
     * PUT /api/materials/{id}
     * Memperbarui materi yang sudah ada.
     */
    @PutMapping("/{id}")
    public ResponseEntity<?> updateMaterial(@PathVariable Long id, @RequestBody CourseMaterial material) {
        try {
            CourseMaterial updated = materialService.updateMaterial(id, material);
            return ResponseEntity.ok(mapMaterial(updated));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * DELETE /api/materials/{id}
     * Menghapus materi berdasarkan ID.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteMaterial(@PathVariable Long id) {
        try {
            materialService.deleteMaterial(id);
            return ResponseEntity.ok(Map.of("message", "Materi berhasil dihapus."));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * POST /api/materials/{id}/view?studentId=...
     * Menandai materi telah dibaca/ditonton.
     */
    @PostMapping("/{id}/view")
    public ResponseEntity<?> markAsViewed(@PathVariable Long id, @RequestParam Long studentId) {
        try {
            materialService.markAsViewed(id, studentId);
            return ResponseEntity.ok(Map.of("message", "Materi ditandai sudah dibaca"));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * GET /api/materials/progress/{studentId}
     * Mengambil daftar ID materi yang sudah dibaca oleh siswa.
     */
    @GetMapping("/progress/{studentId}")
    public ResponseEntity<List<Long>> getViewedMaterials(@PathVariable Long studentId) {
        return ResponseEntity.ok(materialService.getViewedMaterials(studentId));
    }

    /**
     * Helper: map CourseMaterial entity ke response map (termasuk teacherName).
     */
    private Map<String, Object> mapMaterial(CourseMaterial m) {
        Map<String, Object> map = new HashMap<>();
        map.put("id", m.getId());
        map.put("title", m.getTitle());
        map.put("description", m.getDescription());
        map.put("type", m.getType());
        map.put("videoUrl", m.getVideoUrl());
        map.put("durationMinutes", m.getDurationMinutes());
        map.put("textContent", m.getTextContent());

        if (m.getTeacher() != null) {
            map.put("teacherId", m.getTeacher().getId());
            map.put("teacherName", m.getTeacher().getFullName());
        }
        return map;
    }
}

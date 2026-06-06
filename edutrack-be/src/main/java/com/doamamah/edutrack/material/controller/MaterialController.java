package com.doamamah.edutrack.material.controller;

import com.doamamah.edutrack.material.model.CourseMaterial;
import com.doamamah.edutrack.material.service.MaterialService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

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
     * Mengambil semua materi dari database.
     */
    @GetMapping
    public ResponseEntity<List<CourseMaterial>> getAllMaterials() {
        List<CourseMaterial> materials = materialService.getAllMaterials();
        return ResponseEntity.ok(materials);
    }

    /**
     * GET /api/materials/{id}
     * Mengambil materi berdasarkan ID.
     */
    @GetMapping("/{id}")
    public ResponseEntity<?> getMaterialById(@PathVariable Long id) {
        return materialService.getMaterialById(id)
                .map(material -> ResponseEntity.ok((Object) material))
                .orElse(ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Map.of("error", "Materi dengan ID " + id + " tidak ditemukan.")));
    }

    /**
     * POST /api/materials
     * Menambahkan materi baru.
     * Body: { "title": "...", "description": "...", "type": "VIDEO"|"TEXT", ... }
     */
    @PostMapping
    public ResponseEntity<CourseMaterial> addMaterial(@RequestBody CourseMaterial material) {
        CourseMaterial saved = materialService.addMaterial(material);
        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }

    /**
     * PUT /api/materials/{id}
     * Memperbarui materi yang sudah ada.
     */
    @PutMapping("/{id}")
    public ResponseEntity<?> updateMaterial(@PathVariable Long id, @RequestBody CourseMaterial material) {
        try {
            CourseMaterial updated = materialService.updateMaterial(id, material);
            return ResponseEntity.ok(updated);
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
}

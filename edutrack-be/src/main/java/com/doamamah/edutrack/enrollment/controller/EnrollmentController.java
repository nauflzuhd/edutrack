package com.doamamah.edutrack.enrollment.controller;

import com.doamamah.edutrack.enrollment.model.Enrollment;
import com.doamamah.edutrack.auth.model.Student;
import com.doamamah.edutrack.auth.model.Teacher;
import com.doamamah.edutrack.enrollment.service.EnrollmentService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * REST Controller untuk endpoint enrollment (siswa mendaftar ke kelas pengajar).
 */
@RestController
@RequestMapping("/api/enrollments")
public class EnrollmentController {

    private final EnrollmentService enrollmentService;

    public EnrollmentController(EnrollmentService enrollmentService) {
        this.enrollmentService = enrollmentService;
    }

    /**
     * POST /api/enrollments
     * Body: { "studentId": 1, "teacherId": 2 }
     */
    @PostMapping
    public ResponseEntity<?> enroll(@RequestBody Map<String, Long> body) {
        try {
            Long studentId = body.get("studentId");
            Long teacherId = body.get("teacherId");
            Enrollment enrollment = enrollmentService.enrollStudent(studentId, teacherId);
            return ResponseEntity.status(HttpStatus.CREATED).body(Map.of(
                "message", "Berhasil mendaftar ke kelas pengajar.",
                "enrollmentId", enrollment.getId()
            ));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * DELETE /api/enrollments?studentId=X&teacherId=Y
     */
    @DeleteMapping
    public ResponseEntity<?> unenroll(@RequestParam Long studentId, @RequestParam Long teacherId) {
        try {
            enrollmentService.unenrollStudent(studentId, teacherId);
            return ResponseEntity.ok(Map.of("message", "Berhasil keluar dari kelas pengajar."));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * GET /api/enrollments/student/{studentId}
     * Mengembalikan daftar pengajar yang diikuti siswa.
     */
    @GetMapping("/student/{studentId}")
    public ResponseEntity<?> getEnrolledTeachers(@PathVariable Long studentId) {
        List<Teacher> teachers = enrollmentService.getEnrolledTeachers(studentId);
        List<Map<String, Object>> result = teachers.stream().map(t -> {
            Map<String, Object> map = new HashMap<>();
            map.put("id", t.getId());
            map.put("fullName", t.getFullName());
            map.put("username", t.getUsername());
            map.put("email", t.getEmail());
            map.put("teacherId", t.getTeacherId());
            map.put("specialization", t.getSpecialization());
            map.put("bio", t.getBio());
            map.put("studentCount", enrollmentService.countStudentsByTeacher(t.getId()));
            return map;
        }).toList();
        return ResponseEntity.ok(result);
    }

    /**
     * GET /api/enrollments/student/{studentId}/ids
     * Mengembalikan daftar ID pengajar yang diikuti siswa.
     */
    @GetMapping("/student/{studentId}/ids")
    public ResponseEntity<List<Long>> getEnrolledTeacherIds(@PathVariable Long studentId) {
        return ResponseEntity.ok(enrollmentService.getEnrolledTeacherIds(studentId));
    }

    /**
     * GET /api/enrollments/teacher/{teacherId}
     * Mengembalikan daftar siswa di kelas pengajar.
     */
    @GetMapping("/teacher/{teacherId}")
    public ResponseEntity<?> getEnrolledStudents(@PathVariable Long teacherId) {
        List<Student> students = enrollmentService.getEnrolledStudents(teacherId);
        List<Map<String, Object>> result = students.stream().map(s -> {
            Map<String, Object> map = new HashMap<>();
            map.put("id", s.getId());
            map.put("fullName", s.getFullName());
            map.put("username", s.getUsername());
            map.put("email", s.getEmail());
            map.put("studentId", s.getStudentId());
            return map;
        }).toList();
        return ResponseEntity.ok(result);
    }

    /**
     * GET /api/enrollments/teacher/{teacherId}/count
     */
    @GetMapping("/teacher/{teacherId}/count")
    public ResponseEntity<Map<String, Long>> countStudents(@PathVariable Long teacherId) {
        return ResponseEntity.ok(Map.of("count", enrollmentService.countStudentsByTeacher(teacherId)));
    }
}

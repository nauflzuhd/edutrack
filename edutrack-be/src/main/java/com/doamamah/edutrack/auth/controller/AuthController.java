package com.doamamah.edutrack.auth.controller;

import com.doamamah.edutrack.auth.model.Student;
import com.doamamah.edutrack.auth.model.Teacher;
import com.doamamah.edutrack.auth.model.User;
import com.doamamah.edutrack.auth.service.AuthService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * REST Controller untuk endpoint autentikasi.
 * Menyediakan /api/auth/login dan /api/auth/logout.
 */
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    /**
     * POST /api/auth/login
     * Body: { "username": "...", "password": "..." }
     * Response 200: User JSON sesuai format yang diharapkan frontend.
     * Response 401: Jika kredensial salah.
     */
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Map<String, String> credentials) {
        String username = credentials.get("username");
        String password = credentials.get("password");

        if (username == null || username.isBlank() || password == null || password.isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Username dan password wajib diisi."));
        }

        try {
            User user = authService.login(username, password);

            // Bangun response JSON sesuai format yang diharapkan frontend
            Map<String, Object> response = new HashMap<>();
            response.put("id", user.getId());
            response.put("username", user.getUsername());
            response.put("fullName", user.getFullName());
            response.put("email", user.getEmail());
            response.put("role", user.getRole());
            response.put("bio", user.getBio());

            // Tambahkan field spesifik berdasarkan tipe user
            if (user instanceof Teacher teacher) {
                response.put("teacherId", teacher.getTeacherId());
                response.put("specialization", teacher.getSpecialization());
                response.put("totalCourses", teacher.getTotalCourses());
            } else if (user instanceof Student student) {
                response.put("studentId", student.getStudentId());
                response.put("enrolledCourses", student.getEnrolledCourses());
            }

            return ResponseEntity.ok(response);

        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * POST /api/auth/register
     * Body: { "username": "...", "password": "...", "fullName": "...", "email": "...", "role": "..." }
     * Response 201: Jika berhasil mendaftar.
     * Response 400: Jika input tidak lengkap.
     * Response 409: Jika username sudah ada.
     */
    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody Map<String, String> request) {
        String username = request.get("username");
        String password = request.get("password");
        String fullName = request.get("fullName");
        
        if (username == null || username.isBlank() || password == null || password.isBlank() || fullName == null || fullName.isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Username, password, dan nama lengkap wajib diisi."));
        }
        
        try {
            User newUser = authService.register(request);
            return ResponseEntity.status(HttpStatus.CREATED).body(Map.of(
                "message", "Registrasi berhasil",
                "username", newUser.getUsername(),
                "role", newUser.getRole()
            ));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * POST /api/auth/logout
     * Endpoint sederhana — mengembalikan 200 OK.
     */
    @PostMapping("/logout")
    public ResponseEntity<?> logout() {
        return ResponseEntity.ok(Map.of("message", "Logout berhasil."));
    }

    /**
     * GET /api/auth/profile/{id}
     * Mengambil profil user berdasarkan ID.
     */
    @GetMapping("/profile/{id}")
    public ResponseEntity<?> getProfile(@PathVariable Long id) {
        try {
            com.doamamah.edutrack.auth.model.User user = authService.getUserById(id);
            Map<String, Object> response = new HashMap<>();
            response.put("id", user.getId());
            response.put("username", user.getUsername());
            response.put("fullName", user.getFullName());
            response.put("email", user.getEmail());
            response.put("role", user.getRole());
            response.put("bio", user.getBio());

            if (user instanceof Teacher teacher) {
                response.put("teacherId", teacher.getTeacherId());
                response.put("specialization", teacher.getSpecialization());
            } else if (user instanceof Student student) {
                response.put("studentId", student.getStudentId());
            }

            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * PUT /api/auth/profile/{id}
     * Update profil user (username, bio, specialization).
     */
    @PutMapping("/profile/{id}")
    public ResponseEntity<?> updateProfile(@PathVariable Long id, @RequestBody Map<String, String> data) {
        try {
            com.doamamah.edutrack.auth.model.User user = authService.updateProfile(id, data);
            Map<String, Object> response = new HashMap<>();
            response.put("id", user.getId());
            response.put("username", user.getUsername());
            response.put("fullName", user.getFullName());
            response.put("email", user.getEmail());
            response.put("role", user.getRole());
            response.put("bio", user.getBio());

            if (user instanceof Teacher teacher) {
                response.put("specialization", teacher.getSpecialization());
            }

            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * GET /api/users/teachers
     * Mendapatkan daftar semua pengajar.
     */
    @GetMapping("/teachers")
    public ResponseEntity<?> getAllTeachers() {
        var teachers = authService.getAllTeachers();
        var result = teachers.stream().map(u -> {
            Map<String, Object> map = new HashMap<>();
            map.put("id", u.getId());
            map.put("fullName", u.getFullName());
            map.put("username", u.getUsername());
            map.put("email", u.getEmail());
            map.put("bio", u.getBio());
            if (u instanceof Teacher t) {
                map.put("teacherId", t.getTeacherId());
                map.put("specialization", t.getSpecialization());
            }
            return map;
        }).toList();
        return ResponseEntity.ok(result);
    }
}

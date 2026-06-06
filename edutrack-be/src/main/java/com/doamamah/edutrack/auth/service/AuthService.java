package com.doamamah.edutrack.auth.service;

import com.doamamah.edutrack.auth.model.Student;
import com.doamamah.edutrack.auth.model.Teacher;
import com.doamamah.edutrack.auth.model.User;
import com.doamamah.edutrack.auth.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Optional;

import java.util.Optional;

/**
 * Service layer untuk logika autentikasi pengguna.
 * Mencari user di database H2 dan memvalidasi password.
 */
@Service
public class AuthService {

    private final UserRepository userRepository;

    public AuthService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    /**
     * Memproses login berdasarkan username dan password.
     *
     * @param username nama pengguna
     * @param password kata sandi (plain text)
     * @return User jika kredensial valid
     * @throws RuntimeException jika username tidak ditemukan atau password salah
     */
    public User login(String username, String password) {
        Optional<User> optionalUser = userRepository.findByUsername(username);

        if (optionalUser.isEmpty()) {
            throw new RuntimeException("Username tidak ditemukan.");
        }

        User user = optionalUser.get();

        // Perbandingan password plain text (sesuai kebutuhan tugas)
        if (!user.getPassword().equals(password)) {
            throw new RuntimeException("Password salah.");
        }

        return user;
    }

    /**
     * Mendaftarkan pengguna baru (Siswa atau Pengajar).
     *
     * @param userData data dari request JSON
     * @return User yang berhasil didaftarkan
     * @throws RuntimeException jika username sudah digunakan
     */
    public User register(Map<String, String> userData) {
        String username = userData.get("username");
        String password = userData.get("password");
        String fullName = userData.get("fullName");
        String email = userData.get("email");
        String role = userData.get("role");

        if (userRepository.findByUsername(username).isPresent()) {
            throw new RuntimeException("Username '" + username + "' sudah digunakan.");
        }

        User newUser;
        if ("Pengajar".equalsIgnoreCase(role) || "TEACHER".equalsIgnoreCase(role)) {
            String teacherId = "TCH" + System.currentTimeMillis();
            newUser = new Teacher(username, password, fullName, email, teacherId, "Umum", 0);
        } else {
            String studentId = "STD" + System.currentTimeMillis();
            newUser = new Student(username, password, fullName, email, studentId, 0);
        }

        return userRepository.save(newUser);
    }

    /**
     * Mengambil user berdasarkan ID.
     */
    public User getUserById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User tidak ditemukan."));
    }

    /**
     * Memperbarui profil pengguna (username, bio, spesialisasi).
     */
    public User updateProfile(Long id, Map<String, String> data) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User tidak ditemukan."));

        if (data.containsKey("fullName") && data.get("fullName") != null && !data.get("fullName").isBlank()) {
            user.setFullName(data.get("fullName"));
        }

        if (data.containsKey("bio")) {
            user.setBio(data.get("bio"));
        }

        if (user instanceof Teacher teacher && data.containsKey("specialization")) {
            teacher.setSpecialization(data.get("specialization"));
        }

        return userRepository.save(user);
    }

    /**
     * Mengambil daftar semua pengajar.
     */
    public java.util.List<User> getAllTeachers() {
        return userRepository.findAllTeachers();
    }
}

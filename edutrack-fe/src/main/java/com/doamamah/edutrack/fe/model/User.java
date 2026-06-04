package com.doamamah.edutrack.fe.model;

/**
 * =====================================================================
 * PILAR OOP: INHERITANCE (Pewarisan)
 * User adalah kelas INDUK (superclass) yang menjadi fondasi bagi
 * kelas turunan Student dan Teacher.
 *
 * PILAR OOP: ENCAPSULATION (Enkapsulasi)
 * Semua field dideklarasikan private. Akses dan modifikasi data
 * hanya boleh melalui metode getter dan setter yang terkontrol.
 * Ini melindungi integritas data objek dari luar kelas.
 * =====================================================================
 */
public abstract class User {

    // PILAR OOP: ENCAPSULATION - field private, hanya diakses via getter/setter
    private Long id;
    private String username;
    private String password; // disimpan tapi tidak pernah diekspos via getter
    private String fullName;
    private String email;
    private String role;     // "STUDENT" atau "TEACHER"

    // Constructor kosong
    public User() {}

    // Constructor penuh
    public User(Long id, String username, String fullName, String email, String role) {
        this.id = id;
        this.username = username;
        this.fullName = fullName;
        this.email = email;
        this.role = role;
    }

    // --- GETTERS ---

    public Long getId() {
        return id;
    }

    public String getUsername() {
        return username;
    }

    public String getFullName() {
        return fullName;
    }

    public String getEmail() {
        return email;
    }

    public String getRole() {
        return role;
    }

    // --- SETTERS (dengan validasi ringan) ---

    public void setId(Long id) {
        // PILAR OOP: ENCAPSULATION - validasi: id tidak boleh negatif
        if (id != null && id < 0) {
            throw new IllegalArgumentException("ID tidak boleh negatif.");
        }
        this.id = id;
    }

    public void setUsername(String username) {
        // PILAR OOP: ENCAPSULATION - validasi: username tidak boleh kosong
        if (username == null || username.isBlank()) {
            throw new IllegalArgumentException("Username tidak boleh kosong.");
        }
        this.username = username;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setRole(String role) {
        this.role = role;
    }

    /**
     * Metode abstrak untuk mendapatkan tampilan nama peran yang ramah pengguna.
     * Setiap subclass WAJIB mengimplementasikan ini.
     */
    public abstract String getDisplayRole();

    @Override
    public String toString() {
        return "User{id=" + id + ", username='" + username + "', role='" + role + "'}";
    }
}

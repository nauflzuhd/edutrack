package com.doamamah.edutrack.auth.model;

import jakarta.persistence.*;

/**
 * Entity JPA untuk tabel 'users'.
 * Menggunakan strategi SINGLE_TABLE inheritance —
 * kolom 'role' sebagai discriminator untuk membedakan Student dan Teacher.
 */
@Entity
@Table(name = "users")
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "role", discriminatorType = DiscriminatorType.STRING)
public abstract class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String username;

    @Column(nullable = false)
    private String password;

    private String fullName;
    private String email;

    @Column(columnDefinition = "CLOB")
    private String bio;

    // Constructor kosong (diperlukan oleh JPA)
    public User() {}

    public User(String username, String password, String fullName, String email) {
        this.username = username;
        this.password = password;
        this.fullName = fullName;
        this.email = email;
    }

    // --- GETTERS ---

    public Long getId() { return id; }
    public String getUsername() { return username; }
    public String getPassword() { return password; }
    public String getFullName() { return fullName; }
    public String getEmail() { return email; }
    public String getBio() { return bio; }

    /**
     * Mengembalikan nilai kolom discriminator 'role'.
     * Diimplementasikan oleh masing-masing subclass.
     */
    @Transient
    public abstract String getRole();

    // --- SETTERS ---

    public void setId(Long id) { this.id = id; }
    public void setUsername(String username) { this.username = username; }
    public void setPassword(String password) { this.password = password; }
    public void setFullName(String fullName) { this.fullName = fullName; }
    public void setEmail(String email) { this.email = email; }
    public void setBio(String bio) { this.bio = bio; }
}

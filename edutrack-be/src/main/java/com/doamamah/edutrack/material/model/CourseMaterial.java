package com.doamamah.edutrack.material.model;

import com.doamamah.edutrack.auth.model.Teacher;
import jakarta.persistence.*;

/**
 * Entity JPA untuk tabel 'course_materials'.
 * Menyimpan materi pembelajaran bertipe VIDEO atau TEXT dalam satu tabel.
 */
@Entity
@Table(name = "course_materials")
public class CourseMaterial {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    private String description;

    @Column(nullable = false)
    private String type; // "VIDEO" atau "TEXT"

    // Khusus Video
    private String videoUrl;
    private int durationMinutes;

    // Khusus Teks
    @Column(columnDefinition = "CLOB")
    private String textContent;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "teacher_id")
    private Teacher teacher;

    // Constructor kosong (diperlukan oleh JPA)
    public CourseMaterial() {}

    public CourseMaterial(String title, String description, String type,
                          String videoUrl, int durationMinutes, String textContent) {
        this.title = title;
        this.description = description;
        this.type = type;
        this.videoUrl = videoUrl;
        this.durationMinutes = durationMinutes;
        this.textContent = textContent;
    }

    // --- GETTERS ---

    public Long getId() { return id; }
    public String getTitle() { return title; }
    public String getDescription() { return description; }
    public String getType() { return type; }
    public String getVideoUrl() { return videoUrl; }
    public int getDurationMinutes() { return durationMinutes; }
    public String getTextContent() { return textContent; }
    public Teacher getTeacher() { return teacher; }

    // --- SETTERS ---

    public void setId(Long id) { this.id = id; }
    public void setTitle(String title) { this.title = title; }
    public void setDescription(String description) { this.description = description; }
    public void setType(String type) { this.type = type; }
    public void setVideoUrl(String videoUrl) { this.videoUrl = videoUrl; }
    public void setDurationMinutes(int durationMinutes) { this.durationMinutes = durationMinutes; }
    public void setTextContent(String textContent) { this.textContent = textContent; }
    public void setTeacher(Teacher teacher) { this.teacher = teacher; }
}

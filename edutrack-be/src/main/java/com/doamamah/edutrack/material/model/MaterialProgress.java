package com.doamamah.edutrack.material.model;

import com.doamamah.edutrack.auth.model.Student;
import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "material_progress", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"student_id", "material_id"})
})
public class MaterialProgress {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "student_id", nullable = false)
    private Student student;

    @ManyToOne
    @JoinColumn(name = "material_id", nullable = false)
    private CourseMaterial material;

    @Column(name = "viewed_at", nullable = false)
    private LocalDateTime viewedAt;

    public MaterialProgress() {
    }

    public MaterialProgress(Student student, CourseMaterial material) {
        this.student = student;
        this.material = material;
        this.viewedAt = LocalDateTime.now();
    }

    public Long getId() {
        return id;
    }

    public Student getStudent() {
        return student;
    }

    public CourseMaterial getMaterial() {
        return material;
    }

    public LocalDateTime getViewedAt() {
        return viewedAt;
    }
}

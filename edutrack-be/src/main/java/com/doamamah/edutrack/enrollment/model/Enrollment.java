package com.doamamah.edutrack.enrollment.model;

import com.doamamah.edutrack.auth.model.Student;
import com.doamamah.edutrack.auth.model.Teacher;
import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * Entity JPA untuk tabel 'enrollments'.
 * Menyimpan relasi many-to-many antara Student dan Teacher.
 * Siswa dapat mendaftar ke beberapa pengajar sekaligus.
 */
@Entity
@Table(name = "enrollments", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"student_id", "teacher_id"})
})
public class Enrollment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_id", nullable = false)
    private Student student;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "teacher_id", nullable = false)
    private Teacher teacher;

    @Column(name = "enrolled_at", nullable = false)
    private LocalDateTime enrolledAt;

    public Enrollment() {}

    public Enrollment(Student student, Teacher teacher) {
        this.student = student;
        this.teacher = teacher;
        this.enrolledAt = LocalDateTime.now();
    }

    // --- GETTERS ---
    public Long getId() { return id; }
    public Student getStudent() { return student; }
    public Teacher getTeacher() { return teacher; }
    public LocalDateTime getEnrolledAt() { return enrolledAt; }

    // --- SETTERS ---
    public void setId(Long id) { this.id = id; }
    public void setStudent(Student student) { this.student = student; }
    public void setTeacher(Teacher teacher) { this.teacher = teacher; }
    public void setEnrolledAt(LocalDateTime enrolledAt) { this.enrolledAt = enrolledAt; }
}

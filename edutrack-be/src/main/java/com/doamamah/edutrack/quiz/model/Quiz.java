package com.doamamah.edutrack.quiz.model;

import com.doamamah.edutrack.auth.model.Teacher;
import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Entity JPA untuk tabel 'quizzes'.
 * Menyimpan data kuis yang dibuat oleh guru.
 */
@Entity
@Table(name = "quizzes")
public class Quiz {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    private String description;

    @Column(nullable = false)
    private String difficulty; // "Mudah", "Sedang", "Sulit"

    /**
     * Relasi One-to-Many: satu Quiz memiliki banyak QuizQuestion.
     * CascadeType.ALL agar saat Quiz disimpan/dihapus, semua pertanyaannya ikut.
     * orphanRemoval agar pertanyaan yang dihapus dari list juga dihapus dari DB.
     */
    @OneToMany(mappedBy = "quiz", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    private List<QuizQuestion> questions = new ArrayList<>();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "teacher_id")
    private Teacher teacher;

    public Quiz() {}

    public Quiz(String title, String description, String difficulty) {
        this.title = title;
        this.description = description;
        this.difficulty = difficulty;
    }

    // --- GETTERS ---
    public Long getId() { return id; }
    public String getTitle() { return title; }
    public String getDescription() { return description; }
    public String getDifficulty() { return difficulty; }
    public List<QuizQuestion> getQuestions() { return questions; }
    public Teacher getTeacher() { return teacher; }

    // --- SETTERS ---
    public void setId(Long id) { this.id = id; }
    public void setTitle(String title) { this.title = title; }
    public void setDescription(String description) { this.description = description; }
    public void setDifficulty(String difficulty) { this.difficulty = difficulty; }
    public void setQuestions(List<QuizQuestion> questions) { this.questions = questions; }
    public void setTeacher(Teacher teacher) { this.teacher = teacher; }

    /**
     * Helper method untuk menambahkan pertanyaan ke kuis.
     * Menjaga konsistensi relasi bidirectional.
     */
    public void addQuestion(QuizQuestion question) {
        questions.add(question);
        question.setQuiz(this);
    }
}

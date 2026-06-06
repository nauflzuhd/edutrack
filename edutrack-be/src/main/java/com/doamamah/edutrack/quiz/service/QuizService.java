package com.doamamah.edutrack.quiz.service;

import com.doamamah.edutrack.quiz.model.Quiz;
import com.doamamah.edutrack.quiz.model.QuizQuestion;
import com.doamamah.edutrack.quiz.repository.QuizRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import com.doamamah.edutrack.auth.model.Teacher;

/**
 * Service layer untuk operasi CRUD kuis.
 */
@Service
public class QuizService {

    private final QuizRepository quizRepository;
    private final com.doamamah.edutrack.quiz.repository.QuizAttemptRepository attemptRepository;
    private final com.doamamah.edutrack.auth.repository.UserRepository userRepository;

    public QuizService(QuizRepository quizRepository, 
                       com.doamamah.edutrack.quiz.repository.QuizAttemptRepository attemptRepository,
                       com.doamamah.edutrack.auth.repository.UserRepository userRepository) {
        this.quizRepository = quizRepository;
        this.attemptRepository = attemptRepository;
        this.userRepository = userRepository;
    }

    /**
     * Mengambil semua kuis dari database.
     */
    public List<Quiz> getAllQuizzes() {
        return quizRepository.findAll();
    }

    /**
     * Mengambil kuis berdasarkan ID.
     */
    public Optional<Quiz> getQuizById(Long id) {
        return quizRepository.findById(id);
    }

    /**
     * Membuat kuis baru beserta pertanyaannya.
     */
    public Quiz createQuiz(Quiz quiz) {
        // Pastikan relasi bidirectional terjaga
        if (quiz.getQuestions() != null) {
            for (QuizQuestion q : quiz.getQuestions()) {
                q.setQuiz(quiz);
            }
        }
        return quizRepository.save(quiz);
    }

    /**
     * Membuat kuis baru dengan ownership pengajar.
     */
    public Quiz createQuiz(Quiz quiz, Long teacherId) {
        if (quiz.getQuestions() != null) {
            for (QuizQuestion q : quiz.getQuestions()) {
                q.setQuiz(quiz);
            }
        }
        if (teacherId != null) {
            Teacher teacher = (Teacher) userRepository.findById(teacherId)
                    .orElseThrow(() -> new RuntimeException("Pengajar tidak ditemukan."));
            quiz.setTeacher(teacher);
        }
        return quizRepository.save(quiz);
    }

    /**
     * Mengambil kuis berdasarkan daftar teacher IDs (untuk siswa).
     */
    public List<Quiz> getQuizzesByTeacherIds(List<Long> teacherIds) {
        if (teacherIds == null || teacherIds.isEmpty()) return List.of();
        return quizRepository.findByTeacherIdIn(teacherIds);
    }

    /**
     * Mengambil kuis berdasarkan teacher ID (untuk pengajar).
     */
    public List<Quiz> getQuizzesByTeacher(Long teacherId) {
        return quizRepository.findByTeacherId(teacherId);
    }

    /**
     * Memperbarui kuis yang sudah ada.
     */
    public Quiz updateQuiz(Long id, Quiz updatedData) {
        Quiz existing = quizRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Kuis dengan ID " + id + " tidak ditemukan."));

        existing.setTitle(updatedData.getTitle());
        existing.setDescription(updatedData.getDescription());
        existing.setDifficulty(updatedData.getDifficulty());

        // Hapus pertanyaan lama dan tambahkan yang baru
        existing.getQuestions().clear();
        if (updatedData.getQuestions() != null) {
            for (QuizQuestion q : updatedData.getQuestions()) {
                q.setQuiz(existing);
                existing.getQuestions().add(q);
            }
        }

        return quizRepository.save(existing);
    }

    /**
     * Menghapus kuis berdasarkan ID.
     */
    public void deleteQuiz(Long id) {
        if (!quizRepository.existsById(id)) {
            throw new RuntimeException("Kuis dengan ID " + id + " tidak ditemukan.");
        }
        
        // Hapus semua riwayat pengerjaan yang merujuk ke kuis ini (menghindari Foreign Key violation)
        List<com.doamamah.edutrack.quiz.model.QuizAttempt> attempts = attemptRepository.findByQuizIdOrderByAttemptDateDesc(id);
        if (!attempts.isEmpty()) {
            attemptRepository.deleteAll(attempts);
        }

        quizRepository.deleteById(id);
    }

    /**
     * Menyimpan skor kuis siswa.
     */
    public com.doamamah.edutrack.quiz.model.QuizAttempt submitAttempt(Long quizId, Long studentId, int score) {
        com.doamamah.edutrack.quiz.model.Quiz quiz = quizRepository.findById(quizId)
                .orElseThrow(() -> new RuntimeException("Kuis tidak ditemukan"));
        com.doamamah.edutrack.auth.model.User student = userRepository.findById(studentId)
                .orElseThrow(() -> new RuntimeException("Siswa tidak ditemukan"));

        List<com.doamamah.edutrack.quiz.model.QuizAttempt> existingAttempts = attemptRepository.findByStudentId(studentId);
        for (com.doamamah.edutrack.quiz.model.QuizAttempt attempt : existingAttempts) {
            if (attempt.getQuiz().getId().equals(quizId)) {
                // Perbarui nilai jika kuis yang sama dikerjakan ulang
                attempt.setScore(score);
                attempt.setAttemptDate(java.time.LocalDateTime.now());
                return attemptRepository.save(attempt);
            }
        }

        com.doamamah.edutrack.quiz.model.QuizAttempt attempt = new com.doamamah.edutrack.quiz.model.QuizAttempt(quiz, student, score, java.time.LocalDateTime.now());
        return attemptRepository.save(attempt);
    }

    /**
     * Mengambil riwayat skor kuis berdasarkan kuis (untuk guru).
     */
    public List<com.doamamah.edutrack.quiz.model.QuizAttempt> getAttemptsByQuiz(Long quizId) {
        return attemptRepository.findByQuizIdOrderByAttemptDateDesc(quizId);
    }

    /**
     * Mengambil semua riwayat skor (untuk guru).
     */
    public List<com.doamamah.edutrack.quiz.model.QuizAttempt> getAllAttempts() {
        return attemptRepository.findAllByOrderByAttemptDateDesc();
    }

    /**
     * Mengambil riwayat skor kuis berdasarkan ID siswa.
     */
    public List<com.doamamah.edutrack.quiz.model.QuizAttempt> getAttemptsByStudent(Long studentId) {
        return attemptRepository.findByStudentId(studentId);
    }
}

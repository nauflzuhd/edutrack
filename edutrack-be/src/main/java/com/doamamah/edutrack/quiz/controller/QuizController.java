package com.doamamah.edutrack.quiz.controller;

import com.doamamah.edutrack.quiz.model.Quiz;
import com.doamamah.edutrack.quiz.model.QuizQuestion;
import com.doamamah.edutrack.quiz.service.QuizService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * REST Controller untuk operasi CRUD kuis.
 * Menyediakan endpoint /api/quizzes.
 */
@RestController
@RequestMapping("/api/quizzes")
public class QuizController {

    private final QuizService quizService;

    public QuizController(QuizService quizService) {
        this.quizService = quizService;
    }

    /**
     * GET /api/quizzes
     * Mengambil semua kuis, atau filter berdasarkan teacherIds.
     */
    @GetMapping
    public ResponseEntity<List<Map<String, Object>>> getAllQuizzes(
            @RequestParam(required = false) List<Long> teacherIds) {
        List<Quiz> quizzes;
        if (teacherIds != null && !teacherIds.isEmpty()) {
            quizzes = quizService.getQuizzesByTeacherIds(teacherIds);
        } else {
            quizzes = quizService.getAllQuizzes();
        }
        return ResponseEntity.ok(quizzes.stream().map(this::mapQuiz).collect(Collectors.toList()));
    }

    /**
     * GET /api/quizzes/teacher/{teacherId}
     * Mengambil kuis berdasarkan pengajar.
     */
    @GetMapping("/teacher/{teacherId}")
    public ResponseEntity<List<Map<String, Object>>> getQuizzesByTeacher(@PathVariable Long teacherId) {
        List<Quiz> quizzes = quizService.getQuizzesByTeacher(teacherId);
        return ResponseEntity.ok(quizzes.stream().map(this::mapQuiz).collect(Collectors.toList()));
    }

    /**
     * GET /api/quizzes/{id}
     * Mengambil kuis berdasarkan ID beserta pertanyaannya.
     */
    @GetMapping("/{id}")
    public ResponseEntity<?> getQuizById(@PathVariable Long id) {
        return quizService.getQuizById(id)
                .map(quiz -> ResponseEntity.ok((Object) mapQuiz(quiz)))
                .orElse(ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Map.of("error", "Kuis dengan ID " + id + " tidak ditemukan.")));
    }

    /**
     * POST /api/quizzes
     * Membuat kuis baru beserta pertanyaannya.
     * Body includes optional "teacherId" field.
     */
    @PostMapping
    public ResponseEntity<?> createQuiz(@RequestBody Map<String, Object> body) {
        try {
            Quiz quiz = new Quiz();
            quiz.setTitle((String) body.get("title"));
            quiz.setDescription((String) body.get("description"));
            quiz.setDifficulty((String) body.get("difficulty"));

            // Parse questions
            if (body.containsKey("questions") && body.get("questions") instanceof List<?> questionsList) {
                for (Object qObj : questionsList) {
                    if (qObj instanceof Map<?, ?> qMap) {
                        QuizQuestion question = new QuizQuestion();
                        question.setQuestionText((String) qMap.get("questionText"));
                        question.setOptionA((String) qMap.get("optionA"));
                        question.setOptionB((String) qMap.get("optionB"));
                        question.setOptionC((String) qMap.get("optionC"));
                        question.setOptionD((String) qMap.get("optionD"));
                        question.setCorrectOptionIndex(((Number) qMap.get("correctOptionIndex")).intValue());
                        quiz.addQuestion(question);
                    }
                }
            }

            Long teacherId = body.containsKey("teacherId") && body.get("teacherId") != null
                    ? ((Number) body.get("teacherId")).longValue() : null;

            Quiz saved = quizService.createQuiz(quiz, teacherId);
            return ResponseEntity.status(HttpStatus.CREATED).body(mapQuiz(saved));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * PUT /api/quizzes/{id}
     * Memperbarui kuis yang sudah ada.
     */
    @PutMapping("/{id}")
    public ResponseEntity<?> updateQuiz(@PathVariable Long id, @RequestBody Quiz quiz) {
        try {
            Quiz updated = quizService.updateQuiz(id, quiz);
            return ResponseEntity.ok(mapQuiz(updated));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * DELETE /api/quizzes/{id}
     * Menghapus kuis berdasarkan ID.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteQuiz(@PathVariable Long id) {
        try {
            quizService.deleteQuiz(id);
            return ResponseEntity.ok(Map.of("message", "Kuis berhasil dihapus."));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * POST /api/quizzes/{id}/submit
     * Menyimpan hasil pengerjaan kuis siswa.
     */
    @PostMapping("/{id}/submit")
    public ResponseEntity<?> submitQuizAttempt(@PathVariable Long id, @RequestBody Map<String, Object> payload) {
        try {
            Long studentId = Long.valueOf(payload.get("studentId").toString());
            int score = Integer.parseInt(payload.get("score").toString());
            com.doamamah.edutrack.quiz.model.QuizAttempt attempt = quizService.submitAttempt(id, studentId, score);
            return ResponseEntity.ok(attempt);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * GET /api/quizzes/attempts
     */
    @GetMapping("/attempts")
    public ResponseEntity<?> getAllAttempts() {
        return ResponseEntity.ok(mapAttempts(quizService.getAllAttempts()));
    }

    /**
     * GET /api/quizzes/{id}/attempts
     */
    @GetMapping("/{id}/attempts")
    public ResponseEntity<?> getAttemptsByQuiz(@PathVariable Long id) {
        return ResponseEntity.ok(mapAttempts(quizService.getAttemptsByQuiz(id)));
    }

    /**
     * GET /api/quizzes/student/{studentId}/attempts
     */
    @GetMapping("/student/{studentId}/attempts")
    public ResponseEntity<?> getAttemptsByStudent(@PathVariable Long studentId) {
        return ResponseEntity.ok(mapAttempts(quizService.getAttemptsByStudent(studentId)));
    }

    private List<Map<String, Object>> mapAttempts(List<com.doamamah.edutrack.quiz.model.QuizAttempt> attempts) {
        return attempts.stream().map(attempt -> Map.<String, Object>of(
            "id", attempt.getId(),
            "score", attempt.getScore(),
            "attemptDate", attempt.getAttemptDate().toString(),
            "quiz", Map.of("id", attempt.getQuiz().getId(), "title", attempt.getQuiz().getTitle()),
            "student", Map.of("id", attempt.getStudent().getId(), "fullName", attempt.getStudent().getFullName())
        )).toList();
    }

    /**
     * Helper: map Quiz entity ke response map (termasuk teacherName).
     */
    private Map<String, Object> mapQuiz(Quiz q) {
        Map<String, Object> map = new HashMap<>();
        map.put("id", q.getId());
        map.put("title", q.getTitle());
        map.put("description", q.getDescription());
        map.put("difficulty", q.getDifficulty());

        // Map questions
        List<Map<String, Object>> questions = new ArrayList<>();
        if (q.getQuestions() != null) {
            for (QuizQuestion qq : q.getQuestions()) {
                Map<String, Object> qm = new HashMap<>();
                qm.put("id", qq.getId());
                qm.put("questionText", qq.getQuestionText());
                qm.put("optionA", qq.getOptionA());
                qm.put("optionB", qq.getOptionB());
                qm.put("optionC", qq.getOptionC());
                qm.put("optionD", qq.getOptionD());
                qm.put("correctOptionIndex", qq.getCorrectOptionIndex());
                questions.add(qm);
            }
        }
        map.put("questions", questions);

        if (q.getTeacher() != null) {
            map.put("teacherId", q.getTeacher().getId());
            map.put("teacherName", q.getTeacher().getFullName());
        }
        return map;
    }
}

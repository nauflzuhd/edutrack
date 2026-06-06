package com.doamamah.edutrack.fe.quiz;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

/**
 * Service layer untuk operasi CRUD kuis ke backend REST API.
 */
public class QuizService {

    private static final String BASE_URL = "http://localhost:8080";
    private final HttpClient httpClient;
    private final Gson gson;

    public QuizService() {
        this.httpClient = HttpClient.newBuilder()
                .version(HttpClient.Version.HTTP_1_1)
                .connectTimeout(Duration.ofSeconds(10))
                .build();
        this.gson = new Gson();
    }

    /**
     * Data class untuk kuis (digunakan oleh frontend).
     */
    public static class QuizData {
        private final long id;
        private final String title;
        private final String description;
        private final String difficulty;
        private final List<QuestionData> questions;

        public QuizData(long id, String title, String description, String difficulty, List<QuestionData> questions) {
            this.id = id;
            this.title = title;
            this.description = description;
            this.difficulty = difficulty;
            this.questions = questions;
        }

        public long getId() { return id; }
        public String getTitle() { return title; }
        public String getDescription() { return description; }
        public String getDifficulty() { return difficulty; }
        public List<QuestionData> getQuestions() { return questions; }

        public String getColor() {
            if ("Sedang".equals(difficulty)) return "#D97706";
            if ("Sulit".equals(difficulty)) return "#DC2626";
            return "#059669";
        }
    }

    /**
     * Data class untuk pertanyaan kuis.
     */
    public static class QuestionData {
        private final long id;
        private final String questionText;
        private final String optionA;
        private final String optionB;
        private final String optionC;
        private final String optionD;
        private final int correctOptionIndex;

        public QuestionData(long id, String questionText, String optionA, String optionB,
                            String optionC, String optionD, int correctOptionIndex) {
            this.id = id;
            this.questionText = questionText;
            this.optionA = optionA;
            this.optionB = optionB;
            this.optionC = optionC;
            this.optionD = optionD;
            this.correctOptionIndex = correctOptionIndex;
        }

        public long getId() { return id; }
        public String getQuestionText() { return questionText; }
        public String getOptionA() { return optionA; }
        public String getOptionB() { return optionB; }
        public String getOptionC() { return optionC; }
        public String getOptionD() { return optionD; }
        public int getCorrectOptionIndex() { return correctOptionIndex; }

        public String[] getOptionsArray() {
            return new String[]{optionA, optionB, optionC, optionD};
        }
    }

    /**
     * Mengambil semua kuis dari backend.
     */
    public List<QuizData> getAllQuizzes() {
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(BASE_URL + "/api/quizzes"))
                    .header("Accept", "application/json")
                    .GET()
                    .timeout(Duration.ofSeconds(5))
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200) {
                return parseQuizzesFromResponse(response.body());
            }
        } catch (Exception e) {
            System.err.println("Gagal mengambil kuis dari backend: " + e.getMessage());
        }
        return new ArrayList<>();
    }

    /**
     * Membuat kuis baru di backend.
     */
    public QuizData createQuiz(String title, String description, String difficulty, List<QuestionData> questions) {
        try {
            JsonObject json = buildQuizJson(title, description, difficulty, questions);

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(BASE_URL + "/api/quizzes"))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(gson.toJson(json)))
                    .timeout(Duration.ofSeconds(5))
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() == 201) {
                return parseSingleQuiz(gson.fromJson(response.body(), JsonObject.class));
            }
        } catch (Exception e) {
            System.err.println("Gagal membuat kuis di backend: " + e.getMessage());
        }
        return null;
    }

    /**
     * Memperbarui kuis di backend.
     */
    public QuizData updateQuiz(long id, String title, String description, String difficulty, List<QuestionData> questions) {
        try {
            JsonObject json = buildQuizJson(title, description, difficulty, questions);

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(BASE_URL + "/api/quizzes/" + id))
                    .header("Content-Type", "application/json")
                    .PUT(HttpRequest.BodyPublishers.ofString(gson.toJson(json)))
                    .timeout(Duration.ofSeconds(5))
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() == 200) {
                return parseSingleQuiz(gson.fromJson(response.body(), JsonObject.class));
            }
        } catch (Exception e) {
            System.err.println("Gagal memperbarui kuis di backend: " + e.getMessage());
        }
        return null;
    }

    /**
     * Menghapus kuis dari backend.
     */
    public boolean deleteQuiz(long id) {
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(BASE_URL + "/api/quizzes/" + id))
                    .DELETE()
                    .timeout(Duration.ofSeconds(5))
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            return response.statusCode() == 200;
        } catch (Exception e) {
            System.err.println("Gagal menghapus kuis di backend: " + e.getMessage());
        }
        return false;
    }

    // ========== Helper Methods ==========

    private JsonObject buildQuizJson(String title, String description, String difficulty, List<QuestionData> questions) {
        JsonObject json = new JsonObject();
        json.addProperty("title", title);
        json.addProperty("description", description);
        json.addProperty("difficulty", difficulty);

        JsonArray questionsArray = new JsonArray();
        for (QuestionData q : questions) {
            JsonObject qJson = new JsonObject();
            qJson.addProperty("questionText", q.getQuestionText());
            qJson.addProperty("optionA", q.getOptionA());
            qJson.addProperty("optionB", q.getOptionB());
            qJson.addProperty("optionC", q.getOptionC());
            qJson.addProperty("optionD", q.getOptionD());
            qJson.addProperty("correctOptionIndex", q.getCorrectOptionIndex());
            questionsArray.add(qJson);
        }
        json.add("questions", questionsArray);
        return json;
    }

    private List<QuizData> parseQuizzesFromResponse(String body) {
        List<QuizData> quizzes = new ArrayList<>();
        try {
            JsonArray array = gson.fromJson(body, JsonArray.class);
            for (int i = 0; i < array.size(); i++) {
                JsonObject obj = array.get(i).getAsJsonObject();
                QuizData quiz = parseSingleQuiz(obj);
                if (quiz != null) quizzes.add(quiz);
            }
        } catch (Exception e) {
            System.err.println("Gagal parse quiz response: " + e.getMessage());
        }
        return quizzes;
    }

    private QuizData parseSingleQuiz(JsonObject obj) {
        try {
            long id = obj.get("id").getAsLong();
            String title = obj.has("title") ? obj.get("title").getAsString() : "";
            String description = obj.has("description") ? obj.get("description").getAsString() : "";
            String difficulty = obj.has("difficulty") ? obj.get("difficulty").getAsString() : "Mudah";

            List<QuestionData> questions = new ArrayList<>();
            if (obj.has("questions") && obj.get("questions").isJsonArray()) {
                JsonArray qArray = obj.getAsJsonArray("questions");
                for (int i = 0; i < qArray.size(); i++) {
                    JsonObject qObj = qArray.get(i).getAsJsonObject();
                    questions.add(new QuestionData(
                        qObj.has("id") ? qObj.get("id").getAsLong() : 0,
                        qObj.has("questionText") ? qObj.get("questionText").getAsString() : "",
                        qObj.has("optionA") ? qObj.get("optionA").getAsString() : "",
                        qObj.has("optionB") ? qObj.get("optionB").getAsString() : "",
                        qObj.has("optionC") ? qObj.get("optionC").getAsString() : "",
                        qObj.has("optionD") ? qObj.get("optionD").getAsString() : "",
                        qObj.has("correctOptionIndex") ? qObj.get("correctOptionIndex").getAsInt() : 0
                    ));
                }
            }
            return new QuizData(id, title, description, difficulty, questions);
        } catch (Exception e) {
            System.err.println("Gagal parse single quiz: " + e.getMessage());
            return null;
        }
    }
    /**
     * Data class untuk riwayat nilai kuis.
     */
    public static class QuizAttemptData {
        private final String studentName;
        private final String quizTitle;
        private final int score;
        private final String attemptDate;
        private final long quizId;

        public QuizAttemptData(String studentName, String quizTitle, int score, String attemptDate, long quizId) {
            this.studentName = studentName;
            this.quizTitle = quizTitle;
            this.score = score;
            this.attemptDate = attemptDate;
            this.quizId = quizId;
        }

        public String getStudentName() { return studentName; }
        public String getQuizTitle() { return quizTitle; }
        public int getScore() { return score; }
        public String getAttemptDate() { return attemptDate; }
        public long getQuizId() { return quizId; }
    }

    /**
     * Menyimpan hasil kuis siswa ke backend.
     */
    public boolean submitQuizScore(long quizId, long studentId, int score) {
        try {
            JsonObject json = new JsonObject();
            json.addProperty("studentId", studentId);
            json.addProperty("score", score);

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(BASE_URL + "/api/quizzes/" + quizId + "/submit"))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(gson.toJson(json)))
                    .timeout(Duration.ofSeconds(5))
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            return response.statusCode() == 200;
        } catch (Exception e) {
            System.err.println("Gagal menyimpan nilai kuis: " + e.getMessage());
            return false;
        }
    }

    /**
     * Mengambil riwayat nilai kuis siswa (untuk pengajar).
     */
    public List<QuizAttemptData> getAllAttempts() {
        List<QuizAttemptData> attempts = new ArrayList<>();
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(BASE_URL + "/api/quizzes/attempts"))
                    .header("Accept", "application/json")
                    .GET()
                    .timeout(Duration.ofSeconds(5))
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200) {
                JsonArray array = gson.fromJson(response.body(), JsonArray.class);
                for (int i = 0; i < array.size(); i++) {
                    JsonObject obj = array.get(i).getAsJsonObject();
                    String studentName = obj.getAsJsonObject("student").get("fullName").getAsString();
                    String quizTitle = obj.getAsJsonObject("quiz").get("title").getAsString();
                    long quizId = obj.getAsJsonObject("quiz").get("id").getAsLong();
                    int score = obj.get("score").getAsInt();
                    
                    String rawDate = obj.get("attemptDate").getAsString();
                    String formattedDate = rawDate.contains("T") ? rawDate.substring(0, 10) : rawDate;
                    
                    attempts.add(new QuizAttemptData(studentName, quizTitle, score, formattedDate, quizId));
                }
            }
        } catch (Exception e) {
            System.err.println("Gagal mengambil riwayat kuis: " + e.getMessage());
        }
        return attempts;
    }

    /**
     * Mengambil riwayat kuis untuk siswa tertentu.
     */
    public List<QuizAttemptData> getStudentAttempts(long studentId) {
        List<QuizAttemptData> attempts = new ArrayList<>();
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(BASE_URL + "/api/quizzes/student/" + studentId + "/attempts"))
                    .header("Accept", "application/json")
                    .GET()
                    .timeout(Duration.ofSeconds(5))
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200) {
                JsonArray array = gson.fromJson(response.body(), JsonArray.class);
                for (int i = 0; i < array.size(); i++) {
                    JsonObject obj = array.get(i).getAsJsonObject();
                    String studentName = obj.getAsJsonObject("student").get("fullName").getAsString();
                    String quizTitle = obj.getAsJsonObject("quiz").get("title").getAsString();
                    long quizId = obj.getAsJsonObject("quiz").get("id").getAsLong();
                    int score = obj.get("score").getAsInt();
                    
                    String rawDate = obj.get("attemptDate").getAsString();
                    String formattedDate = rawDate.contains("T") ? rawDate.substring(0, 10) : rawDate;
                    
                    attempts.add(new QuizAttemptData(studentName, quizTitle, score, formattedDate, quizId));
                }
            }
        } catch (Exception e) {
            System.err.println("Gagal mengambil riwayat kuis siswa: " + e.getMessage());
        }
        return attempts;
    }
}

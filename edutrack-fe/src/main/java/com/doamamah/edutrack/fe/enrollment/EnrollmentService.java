package com.doamamah.edutrack.fe.enrollment;

import com.doamamah.edutrack.fe.user.Teacher;
import com.doamamah.edutrack.fe.user.Student;
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
 * Service layer frontend untuk enrollment (siswa mendaftar ke kelas pengajar).
 */
public class EnrollmentService {

    private static final String BASE_URL = "http://localhost:8080";
    private final HttpClient httpClient;
    private final Gson gson;

    public EnrollmentService() {
        this.httpClient = HttpClient.newBuilder()
                .version(HttpClient.Version.HTTP_1_1)
                .connectTimeout(Duration.ofSeconds(10))
                .build();
        this.gson = new Gson();
    }

    /**
     * Mendaftarkan siswa ke kelas pengajar.
     */
    public boolean enrollToTeacher(Long studentId, Long teacherId) {
        try {
            JsonObject body = new JsonObject();
            body.addProperty("studentId", studentId);
            body.addProperty("teacherId", teacherId);

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(BASE_URL + "/api/enrollments"))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(gson.toJson(body)))
                    .timeout(Duration.ofSeconds(5))
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            return response.statusCode() == 201;
        } catch (Exception e) {
            System.err.println("Gagal mendaftar ke pengajar: " + e.getMessage());
            return false;
        }
    }

    /**
     * Mengeluarkan siswa dari kelas pengajar.
     */
    public boolean unenrollFromTeacher(Long studentId, Long teacherId) {
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(BASE_URL + "/api/enrollments?studentId=" + studentId + "&teacherId=" + teacherId))
                    .DELETE()
                    .timeout(Duration.ofSeconds(5))
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            return response.statusCode() == 200;
        } catch (Exception e) {
            System.err.println("Gagal keluar dari kelas: " + e.getMessage());
            return false;
        }
    }

    /**
     * Mengambil daftar ID pengajar yang diikuti siswa.
     */
    public List<Long> getEnrolledTeacherIds(Long studentId) {
        List<Long> ids = new ArrayList<>();
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(BASE_URL + "/api/enrollments/student/" + studentId + "/ids"))
                    .GET()
                    .timeout(Duration.ofSeconds(5))
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200) {
                JsonArray array = gson.fromJson(response.body(), JsonArray.class);
                for (int i = 0; i < array.size(); i++) {
                    ids.add(array.get(i).getAsLong());
                }
            }
        } catch (Exception e) {
            System.err.println("Gagal mengambil enrolled teacher IDs: " + e.getMessage());
        }
        return ids;
    }

    /**
     * Mengambil daftar semua pengajar.
     */
    public List<Teacher> getAllTeachers() {
        List<Teacher> teachers = new ArrayList<>();
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(BASE_URL + "/api/auth/teachers"))
                    .header("Accept", "application/json")
                    .GET()
                    .timeout(Duration.ofSeconds(10))
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200) {
                JsonArray array = gson.fromJson(response.body(), JsonArray.class);
                for (int i = 0; i < array.size(); i++) {
                    JsonObject obj = array.get(i).getAsJsonObject();
                    Long id = obj.has("id") ? obj.get("id").getAsLong() : null;
                    String username = obj.has("username") ? obj.get("username").getAsString() : "";
                    String fullName = obj.has("fullName") ? obj.get("fullName").getAsString() : "";
                    String email = obj.has("email") ? obj.get("email").getAsString() : "";
                    String teacherId = obj.has("teacherId") ? obj.get("teacherId").getAsString() : "";
                    String specialization = obj.has("specialization") && !obj.get("specialization").isJsonNull()
                            ? obj.get("specialization").getAsString() : "Umum";
                    String bio = obj.has("bio") && !obj.get("bio").isJsonNull()
                            ? obj.get("bio").getAsString() : "";

                    Teacher t = new Teacher(id, username, fullName, email, teacherId, specialization);
                    t.setBio(bio);
                    teachers.add(t);
                }
            }
        } catch (Exception e) {
            System.err.println("Gagal mengambil daftar pengajar: " + e.getMessage());
        }
        return teachers;
    }

    /**
     * Mengambil jumlah siswa di kelas pengajar.
     */
    public long getStudentCount(Long teacherId) {
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(BASE_URL + "/api/enrollments/teacher/" + teacherId + "/count"))
                    .GET()
                    .timeout(Duration.ofSeconds(5))
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() == 200) {
                JsonObject obj = gson.fromJson(response.body(), JsonObject.class);
                return obj.get("count").getAsLong();
            }
        } catch (Exception e) {
            System.err.println("Gagal mengambil jumlah siswa: " + e.getMessage());
        }
        return 0;
    }

    /**
     * Mengambil daftar siswa yang terdaftar di kelas pengajar.
     */
    public List<Student> getEnrolledStudents(Long teacherId) {
        List<Student> students = new ArrayList<>();
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(BASE_URL + "/api/enrollments/teacher/" + teacherId))
                    .header("Accept", "application/json")
                    .GET()
                    .timeout(Duration.ofSeconds(10))
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200) {
                JsonArray array = gson.fromJson(response.body(), JsonArray.class);
                for (int i = 0; i < array.size(); i++) {
                    JsonObject obj = array.get(i).getAsJsonObject();
                    Long id = obj.has("id") ? obj.get("id").getAsLong() : null;
                    String username = obj.has("username") ? obj.get("username").getAsString() : "";
                    String fullName = obj.has("fullName") ? obj.get("fullName").getAsString() : "";
                    String email = obj.has("email") ? obj.get("email").getAsString() : "";
                    String studentIdObj = obj.has("studentId") ? obj.get("studentId").getAsString() : "";

                    Student s = new Student(id, username, fullName, email, studentIdObj);
                    students.add(s);
                }
            }
        } catch (Exception e) {
            System.err.println("Gagal mengambil daftar siswa terdaftar: " + e.getMessage());
        }
        return students;
    }

    /**
     * Update profil user (fullName, bio, specialization).
     */
    public boolean updateProfile(Long userId, String fullName, String bio, String specialization) {
        try {
            JsonObject body = new JsonObject();
            if (fullName != null) body.addProperty("fullName", fullName);
            if (bio != null) body.addProperty("bio", bio);
            if (specialization != null) body.addProperty("specialization", specialization);

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(BASE_URL + "/api/auth/profile/" + userId))
                    .header("Content-Type", "application/json")
                    .PUT(HttpRequest.BodyPublishers.ofString(gson.toJson(body)))
                    .timeout(Duration.ofSeconds(5))
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            return response.statusCode() == 200;
        } catch (Exception e) {
            System.err.println("Gagal update profil: " + e.getMessage());
            return false;
        }
    }
}

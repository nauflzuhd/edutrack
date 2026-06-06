package com.doamamah.edutrack.fe.user;

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

public class UserService {

    private static final String BASE_URL = "http://localhost:8080";
    private final HttpClient httpClient;
    private final Gson gson;

    public UserService() {
        this.httpClient = HttpClient.newBuilder()
                .version(HttpClient.Version.HTTP_1_1)
                .connectTimeout(Duration.ofSeconds(10))
                .build();
        this.gson = new Gson();
    }

    public List<Student> getAllStudents() {
        List<Student> students = new ArrayList<>();
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(BASE_URL + "/api/users/students"))
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
                    String studentId = obj.has("studentId") ? obj.get("studentId").getAsString() : "";
                    
                    Student s = new Student(id, username, fullName, email, studentId);
                    if (obj.has("enrolledCourses") && !obj.get("enrolledCourses").isJsonNull()) {
                        s.setEnrolledCourses(obj.get("enrolledCourses").getAsInt());
                    }
                    students.add(s);
                }
            } else {
                System.err.println("Gagal memuat siswa: " + response.statusCode());
            }
        } catch (Exception e) {
            System.err.println("Backend tidak tersedia: " + e.getMessage());
        }
        return students;
    }
}

package com.doamamah.edutrack.fe.dashboard;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

/**
 * Service layer untuk mengambil statistik dashboard dari backend REST API.
 */
public class DashboardService {

    private static final String BASE_URL = "http://localhost:8080";
    private final HttpClient httpClient;
    private final Gson gson;

    public DashboardService() {
        this.httpClient = HttpClient.newBuilder()
                .version(HttpClient.Version.HTTP_1_1)
                .connectTimeout(Duration.ofSeconds(10))
                .build();
        this.gson = new Gson();
    }

    public Map<String, Double> getDashboardStats() {
        Map<String, Double> stats = new HashMap<>();
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(BASE_URL + "/api/dashboard/stats"))
                    .header("Accept", "application/json")
                    .GET()
                    .timeout(Duration.ofSeconds(5))
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200) {
                JsonObject obj = gson.fromJson(response.body(), JsonObject.class);
                
                stats.put("totalMaterials", obj.has("totalMaterials") ? obj.get("totalMaterials").getAsDouble() : 0.0);
                stats.put("totalQuizzes", obj.has("totalQuizzes") ? obj.get("totalQuizzes").getAsDouble() : 0.0);
                stats.put("totalStudents", obj.has("totalStudents") ? obj.get("totalStudents").getAsDouble() : 0.0);
                stats.put("totalTeachers", obj.has("totalTeachers") ? obj.get("totalTeachers").getAsDouble() : 0.0);
                stats.put("participationRate", obj.has("participationRate") ? obj.get("participationRate").getAsDouble() : 0.0);
                stats.put("averageQuizScore", obj.has("averageQuizScore") ? obj.get("averageQuizScore").getAsDouble() : 0.0);
                stats.put("totalQuizAttempts", obj.has("totalQuizAttempts") ? obj.get("totalQuizAttempts").getAsDouble() : 0.0);
            }
        } catch (Exception e) {
            System.err.println("Gagal mengambil statistik dashboard: " + e.getMessage());
        }
        
        // Fallback default if empty
        if (stats.isEmpty()) {
            stats.put("totalMaterials", 0.0);
            stats.put("totalQuizzes", 0.0);
            stats.put("totalStudents", 0.0);
            stats.put("totalTeachers", 0.0);
            stats.put("participationRate", 0.0);
            stats.put("averageQuizScore", 0.0);
            stats.put("totalQuizAttempts", 0.0);
        }
        
        return stats;
    }
}

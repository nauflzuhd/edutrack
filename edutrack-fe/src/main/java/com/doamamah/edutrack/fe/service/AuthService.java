package com.doamamah.edutrack.fe.service;

import com.doamamah.edutrack.fe.model.Student;
import com.doamamah.edutrack.fe.model.Teacher;
import com.doamamah.edutrack.fe.model.User;
import com.google.gson.Gson;
import com.google.gson.JsonObject;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

/**
 * AuthService - Service layer untuk autentikasi pengguna.
 * Menggunakan java.net.http.HttpClient (Java 11+) untuk mengirim
 * HTTP request ke REST API backend yang berjalan di localhost:8080.
 */
public class AuthService {

    private static final String BASE_URL = "http://localhost:8080";
    private static final String LOGIN_ENDPOINT = "/api/auth/login";

    // HttpClient yang dapat digunakan kembali (thread-safe)
    private final HttpClient httpClient;
    private final Gson gson;

    public AuthService() {
        this.httpClient = HttpClient.newBuilder()
                .version(HttpClient.Version.HTTP_1_1)
                .connectTimeout(Duration.ofSeconds(10))
                .build();
        this.gson = new Gson();
    }

    /**
     * Mengirim POST request ke /api/auth/login dengan kredensial pengguna.
     * Jika berhasil, mengembalikan objek User (Student atau Teacher).
     * Jika gagal, melempar Exception dengan pesan error.
     *
     * @param username nama pengguna
     * @param password kata sandi
     * @return User objek (Student atau Teacher) hasil autentikasi
     * @throws Exception jika login gagal atau terjadi kesalahan jaringan
     */
    public User login(String username, String password) throws Exception {
        // Buat body JSON: {"username": "...", "password": "..."}
        JsonObject requestBody = new JsonObject();
        requestBody.addProperty("username", username);
        requestBody.addProperty("password", password);

        String requestJson = gson.toJson(requestBody);

        // Bangun HTTP POST request
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + LOGIN_ENDPOINT))
                .header("Content-Type", "application/json")
                .header("Accept", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(requestJson))
                .timeout(Duration.ofSeconds(15))
                .build();

        // Kirim request dan dapatkan response
        HttpResponse<String> response = httpClient.send(request,
                HttpResponse.BodyHandlers.ofString());

        int statusCode = response.statusCode();

        if (statusCode == 200) {
            // Parse JSON response menjadi objek User
            return parseUserFromResponse(response.body());
        } else if (statusCode == 401) {
            throw new Exception("Username atau password salah. Silakan coba lagi.");
        } else if (statusCode == 403) {
            throw new Exception("Akun Anda tidak memiliki akses. Hubungi administrator.");
        } else if (statusCode == 404) {
            throw new Exception("Endpoint login tidak ditemukan. Pastikan backend berjalan.");
        } else {
            throw new Exception("Login gagal. Server merespons dengan kode: " + statusCode);
        }
    }

    /**
     * Parse JSON response dari server menjadi objek User yang sesuai
     * (Student atau Teacher) berdasarkan field "role" dalam respons.
     *
     * Format JSON yang diharapkan dari backend:
     * {
     *   "id": 1,
     *   "username": "john_doe",
     *   "fullName": "John Doe",
     *   "email": "john@example.com",
     *   "role": "STUDENT",  // atau "TEACHER"
     *   "token": "jwt-token-disini"
     * }
     */
    private User parseUserFromResponse(String responseBody) throws Exception {
        try {
            JsonObject jsonResponse = gson.fromJson(responseBody, JsonObject.class);

            // Ekstrak field-field umum
            Long id = jsonResponse.has("id") ? jsonResponse.get("id").getAsLong() : null;
            String uname = jsonResponse.has("username") ? jsonResponse.get("username").getAsString() : "";
            String fullName = jsonResponse.has("fullName") ? jsonResponse.get("fullName").getAsString() : uname;
            String email = jsonResponse.has("email") ? jsonResponse.get("email").getAsString() : "";
            String role = jsonResponse.has("role") ? jsonResponse.get("role").getAsString() : "STUDENT";

            // Buat objek User sesuai perannya
            // PILAR OOP: POLYMORPHISM - membuat Student atau Teacher secara polimorfis
            User user;
            if ("TEACHER".equalsIgnoreCase(role)) {
                String specialization = jsonResponse.has("specialization")
                        ? jsonResponse.get("specialization").getAsString() : "Umum";
                user = new Teacher(id, uname, fullName, email, uname, specialization);
            } else {
                // Default: STUDENT
                user = new Student(id, uname, fullName, email, uname);
            }

            return user;

        } catch (Exception e) {
            throw new Exception("Gagal memproses respons dari server: " + e.getMessage());
        }
    }

    /**
     * Mengirim POST request ke /api/auth/logout.
     */
    public void logout() {
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(BASE_URL + "/api/auth/logout"))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.noBody())
                    .timeout(Duration.ofSeconds(10))
                    .build();
            httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        } catch (Exception e) {
            // Logout gagal tidak kritis, tetap lanjutkan
            System.err.println("Logout request gagal: " + e.getMessage());
        }
    }
}

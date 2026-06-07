package com.doamamah.edutrack.fe.material;

import com.doamamah.edutrack.fe.material.CourseMaterial;
import com.doamamah.edutrack.fe.material.TextMaterial;
import com.doamamah.edutrack.fe.material.VideoMaterial;
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
 * MaterialService - Service layer untuk operasi CRUD materi pembelajaran.
 * Berkomunikasi dengan REST API backend melalui java.net.http.HttpClient.
 * Menggunakan in-memory caching untuk mendukung mode offline/demo secara interaktif.
 */
public class MaterialService {

    private static final String BASE_URL = "http://localhost:8080";
    private final HttpClient httpClient;
    private final Gson gson;

    // Cache in-memory static agar data materi yang ditambahkan terus bertahan selama sesi aplikasi berjalan
    private static List<CourseMaterial> cachedMaterials;

    public MaterialService() {
        this.httpClient = HttpClient.newBuilder()
                .version(HttpClient.Version.HTTP_1_1)
                .connectTimeout(Duration.ofSeconds(10))
                .build();
        this.gson = new Gson();
    }

    /**
     * Mengambil semua materi. Jika cache masih kosong, ambil dari backend/dummy.
     *
     * @return List berisi objek CourseMaterial (VideoMaterial atau TextMaterial)
     */
    public List<CourseMaterial> getAllMaterials(List<Long> teacherIds) {
        // Jika minta data terfilter, selalu fetch ulang
        if (teacherIds != null && !teacherIds.isEmpty()) {
            return fetchMaterialsFromBackend(teacherIds);
        }
        
        if (cachedMaterials == null) {
            cachedMaterials = fetchMaterialsFromBackend(null);
        }
        return cachedMaterials;
    }

    public List<CourseMaterial> getAllMaterials() {
        return getAllMaterials(null);
    }

    /**
     * Mengambil semua materi dari backend via GET /api/materials.
     * Jika backend tidak tersedia, mengembalikan data dummy untuk demo.
     */
    private List<CourseMaterial> fetchMaterialsFromBackend(List<Long> teacherIds) {
        try {
            String url = BASE_URL + "/api/materials";
            if (teacherIds != null && !teacherIds.isEmpty()) {
                String idsParam = teacherIds.stream().map(String::valueOf).collect(java.util.stream.Collectors.joining(","));
                url += "?teacherIds=" + idsParam;
            }

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .header("Accept", "application/json")
                    .GET()
                    .timeout(Duration.ofSeconds(5))
                    .build();

            HttpResponse<String> response = httpClient.send(request,
                    HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200) {
                return parseMaterialsFromResponse(response.body());
            }
        } catch (Exception e) {
            System.err.println("Backend tidak tersedia, menggunakan data demo: " + e.getMessage());
        }
        // Jika gagal, kembalikan list kosong tanpa dummy data
        return new ArrayList<>();
    }

    /**
     * Menambahkan materi baru ke cache lokal, lalu mencoba mengirimkannya ke backend.
     *
     * @param material Materi baru yang akan ditambahkan
     * @param fileToUpload File attachment (opsional)
     */
    public void addMaterial(CourseMaterial material, java.io.File fileToUpload) {
        if (cachedMaterials == null) {
            getAllMaterials();
        }

        // Generate ID baru secara lokal
        if (material.getId() == null) {
            long maxId = cachedMaterials.stream()
                    .mapToLong(m -> m.getId() != null ? m.getId() : 0)
                    .max()
                    .orElse(0);
            material.setId(maxId + 1);
        }

        // Tambah ke cache lokal agar langsung muncul di UI
        cachedMaterials.add(material);

        // Kirim POST request ke backend secara asynchronous agar tidak memblokir UI thread
        new Thread(() -> {
            try {
                if (fileToUpload != null) {
                    String url = uploadFile(fileToUpload);
                    if (url != null) {
                        material.setAttachmentUrl(url);
                        material.setAttachmentFileName(fileToUpload.getName());
                    }
                }

                JsonObject json = new JsonObject();
                json.addProperty("title", material.getTitle());
                json.addProperty("description", material.getDescription());
                json.addProperty("type", material.getMaterialType());

                if (material instanceof VideoMaterial) {
                    VideoMaterial vm = (VideoMaterial) material;
                    json.addProperty("videoUrl", vm.getVideoUrl());
                    json.addProperty("durationMinutes", vm.getDurationMinutes());
                } else if (material instanceof TextMaterial) {
                    TextMaterial tm = (TextMaterial) material;
                    json.addProperty("textContent", tm.getTextContent());
                }

                if (material.getTeacherId() != null) {
                    json.addProperty("teacherId", material.getTeacherId());
                }
                
                if (material.getAttachmentFileName() != null) {
                    json.addProperty("attachmentFileName", material.getAttachmentFileName());
                    json.addProperty("attachmentUrl", material.getAttachmentUrl());
                }

                HttpRequest postRequest = HttpRequest.newBuilder()
                        .uri(URI.create(BASE_URL + "/api/materials"))
                        .header("Content-Type", "application/json")
                        .POST(HttpRequest.BodyPublishers.ofString(gson.toJson(json)))
                        .timeout(Duration.ofSeconds(5))
                        .build();

                httpClient.send(postRequest, HttpResponse.BodyHandlers.discarding());
                System.out.println("Berhasil menyimpan materi baru ke backend.");
            } catch (Exception e) {
                System.err.println("Backend tidak merespon, materi disimpan dalam memori lokal saja: " + e.getMessage());
            }
        }).start();
    }

    /**
     * Parse JSON array dari backend menjadi List CourseMaterial.
     * PILAR OOP: POLYMORPHISM - membuat VideoMaterial atau TextMaterial
     * tergantung field "type" dalam JSON.
     */
    private List<CourseMaterial> parseMaterialsFromResponse(String body) {
        List<CourseMaterial> materials = new ArrayList<>();
        try {
            JsonArray array = gson.fromJson(body, JsonArray.class);
            for (int i = 0; i < array.size(); i++) {
                JsonObject obj = array.get(i).getAsJsonObject();
                CourseMaterial material = parseSingleMaterial(obj);
                if (material != null) materials.add(material);
            }
        } catch (Exception e) {
            System.err.println("Gagal parse material response: " + e.getMessage());
        }
        return materials;
    }

    private CourseMaterial parseSingleMaterial(JsonObject obj) {
        Long id = obj.has("id") ? obj.get("id").getAsLong() : null;
        String title = obj.has("title") ? obj.get("title").getAsString() : "Tanpa Judul";
        String desc = obj.has("description") && !obj.get("description").isJsonNull() ? obj.get("description").getAsString() : "";
        String type = obj.has("type") ? obj.get("type").getAsString() : "TEXT";
        String teacherName = obj.has("teacherName") && !obj.get("teacherName").isJsonNull()
                ? obj.get("teacherName").getAsString() : null;
        Long teacherId = obj.has("teacherId") && !obj.get("teacherId").isJsonNull()
                ? obj.get("teacherId").getAsLong() : null;
        
        String attachmentFileName = obj.has("attachmentFileName") && !obj.get("attachmentFileName").isJsonNull()
                ? obj.get("attachmentFileName").getAsString() : null;
        String attachmentUrl = obj.has("attachmentUrl") && !obj.get("attachmentUrl").isJsonNull()
                ? obj.get("attachmentUrl").getAsString() : null;

        CourseMaterial material;
        // PILAR OOP: POLYMORPHISM - instansiasi kelas yang tepat berdasarkan tipe
        if ("VIDEO".equalsIgnoreCase(type)) {
            String url = obj.has("videoUrl") && !obj.get("videoUrl").isJsonNull() ? obj.get("videoUrl").getAsString() : "";
            int duration = obj.has("durationMinutes") ? obj.get("durationMinutes").getAsInt() : 0;
            material = new VideoMaterial(id, title, desc, url, duration);
        } else {
            String content = obj.has("textContent") && !obj.get("textContent").isJsonNull() ? obj.get("textContent").getAsString() : "";
            material = new TextMaterial(id, title, desc, content);
        }
        material.setTeacherName(teacherName);
        material.setTeacherId(teacherId);
        material.setAttachmentFileName(attachmentFileName);
        material.setAttachmentUrl(attachmentUrl);
        return material;
    }



    /**
     * Memperbarui materi yang sudah ada dalam cache lokal dan mengirimkan request ke backend.
     *
     * @param material Materi yang telah diedit
     * @param fileToUpload File attachment baru (opsional)
     */
    public void updateMaterial(CourseMaterial material, java.io.File fileToUpload) {
        if (cachedMaterials == null) {
            getAllMaterials();
        }
        for (int i = 0; i < cachedMaterials.size(); i++) {
            if (cachedMaterials.get(i).getId() != null && cachedMaterials.get(i).getId().equals(material.getId())) {
                cachedMaterials.set(i, material);
                break;
            }
        }

        // Kirim PUT request ke backend secara asynchronous
        new Thread(() -> {
            try {
                if (fileToUpload != null) {
                    String url = uploadFile(fileToUpload);
                    if (url != null) {
                        material.setAttachmentUrl(url);
                        material.setAttachmentFileName(fileToUpload.getName());
                    }
                }

                JsonObject json = new JsonObject();
                json.addProperty("title", material.getTitle());
                json.addProperty("description", material.getDescription());
                json.addProperty("type", material.getMaterialType());

                if (material instanceof VideoMaterial) {
                    VideoMaterial vm = (VideoMaterial) material;
                    json.addProperty("videoUrl", vm.getVideoUrl());
                    json.addProperty("durationMinutes", vm.getDurationMinutes());
                } else if (material instanceof TextMaterial) {
                    TextMaterial tm = (TextMaterial) material;
                    json.addProperty("textContent", tm.getTextContent());
                }
                
                if (material.getAttachmentFileName() != null) {
                    json.addProperty("attachmentFileName", material.getAttachmentFileName());
                    json.addProperty("attachmentUrl", material.getAttachmentUrl());
                }

                HttpRequest putRequest = HttpRequest.newBuilder()
                        .uri(URI.create(BASE_URL + "/api/materials/" + material.getId()))
                        .header("Content-Type", "application/json")
                        .PUT(HttpRequest.BodyPublishers.ofString(gson.toJson(json)))
                        .timeout(Duration.ofSeconds(5))
                        .build();

                httpClient.send(putRequest, HttpResponse.BodyHandlers.discarding());
                System.out.println("Berhasil memperbarui materi di backend.");
            } catch (Exception e) {
                System.err.println("Backend tidak merespon, pembaruan materi disimpan dalam memori lokal saja: " + e.getMessage());
            }
        }).start();
    }

    /**
     * Menghapus materi dari cache lokal dan mengirimkan request hapus ke backend.
     *
     * @param id ID materi yang akan dihapus
     */
    public void deleteMaterial(Long id) {
        if (cachedMaterials == null) {
            getAllMaterials();
        }
        cachedMaterials.removeIf(m -> m.getId() != null && m.getId().equals(id));

        // Kirim DELETE request ke backend secara asynchronous
        new Thread(() -> {
            try {
                HttpRequest deleteRequest = HttpRequest.newBuilder()
                        .uri(URI.create(BASE_URL + "/api/materials/" + id))
                        .DELETE()
                        .timeout(Duration.ofSeconds(5))
                        .build();

                httpClient.send(deleteRequest, HttpResponse.BodyHandlers.discarding());
                System.out.println("Berhasil menghapus materi di backend.");
            } catch (Exception e) {
                System.err.println("Backend tidak merespon, penghapusan materi disimpan dalam memori lokal saja: " + e.getMessage());
            }
        }).start();
    }

    /**
     * Menandai materi telah dibaca/ditonton oleh siswa.
     */
    public void markAsViewed(Long materialId, Long studentId) {
        new Thread(() -> {
            try {
                HttpRequest request = HttpRequest.newBuilder()
                        .uri(URI.create(BASE_URL + "/api/materials/" + materialId + "/view?studentId=" + studentId))
                        .POST(HttpRequest.BodyPublishers.noBody())
                        .timeout(Duration.ofSeconds(5))
                        .build();
                httpClient.send(request, HttpResponse.BodyHandlers.discarding());
                System.out.println("Berhasil menandai materi " + materialId + " telah dibaca.");
            } catch (Exception e) {
                System.err.println("Gagal menandai materi dibaca ke backend: " + e.getMessage());
            }
        }).start();
    }

    /**
     * Mengambil daftar ID materi yang sudah diakses oleh siswa.
     */
    public List<Long> getViewedMaterials(Long studentId) {
        List<Long> viewedIds = new ArrayList<>();
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(BASE_URL + "/api/materials/progress/" + studentId))
                    .GET()
                    .timeout(Duration.ofSeconds(5))
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200) {
                JsonArray array = gson.fromJson(response.body(), JsonArray.class);
                for (int i = 0; i < array.size(); i++) {
                    viewedIds.add(array.get(i).getAsLong());
                }
            }
        } catch (Exception e) {
            System.err.println("Gagal mengambil progress materi: " + e.getMessage());
        }
        return viewedIds;
    }

    /**
     * Mengunggah file attachment ke backend menggunakan multipart/form-data.
     * Mengembalikan URL file yang diunggah.
     */
    public String uploadFile(java.io.File file) {
        try {
            String boundary = "---EdutrackBoundary" + System.currentTimeMillis();

            java.util.List<byte[]> byteArrays = new java.util.ArrayList<>();
            String header = "--" + boundary + "\r\n" +
                    "Content-Disposition: form-data; name=\"file\"; filename=\"" + file.getName() + "\"\r\n" +
                    "Content-Type: application/octet-stream\r\n\r\n";
            byteArrays.add(header.getBytes(java.nio.charset.StandardCharsets.UTF_8));
            byteArrays.add(java.nio.file.Files.readAllBytes(file.toPath()));
            byteArrays.add(("\r\n--" + boundary + "--\r\n").getBytes(java.nio.charset.StandardCharsets.UTF_8));

            int totalLen = 0;
            for (byte[] b : byteArrays) totalLen += b.length;
            byte[] body = new byte[totalLen];
            int currentPos = 0;
            for (byte[] b : byteArrays) {
                System.arraycopy(b, 0, body, currentPos, b.length);
                currentPos += b.length;
            }

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(BASE_URL + "/api/materials/upload"))
                    .header("Content-Type", "multipart/form-data; boundary=" + boundary)
                    .POST(HttpRequest.BodyPublishers.ofByteArray(body))
                    .timeout(Duration.ofSeconds(60))
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() == 200) {
                JsonObject jsonResponse = gson.fromJson(response.body(), JsonObject.class);
                return jsonResponse.get("fileUrl").getAsString();
            } else {
                System.err.println("Gagal upload file, status: " + response.statusCode());
            }
        } catch (Exception e) {
            System.err.println("Error saat upload file: " + e.getMessage());
        }
        return null;
    }
}

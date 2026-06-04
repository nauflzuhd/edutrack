package com.doamamah.edutrack.fe.service;

import com.doamamah.edutrack.fe.model.CourseMaterial;
import com.doamamah.edutrack.fe.model.TextMaterial;
import com.doamamah.edutrack.fe.model.VideoMaterial;
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
    public List<CourseMaterial> getAllMaterials() {
        if (cachedMaterials == null) {
            cachedMaterials = fetchMaterialsFromBackend();
        }
        return cachedMaterials;
    }

    /**
     * Mengambil semua materi dari backend via GET /api/materials.
     * Jika backend tidak tersedia, mengembalikan data dummy untuk demo.
     */
    private List<CourseMaterial> fetchMaterialsFromBackend() {
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(BASE_URL + "/api/materials"))
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
        // Fallback: data dummy untuk keperluan demo/testing
        return getDummyMaterials();
    }

    /**
     * Menambahkan materi baru ke cache lokal, lalu mencoba mengirimkannya ke backend.
     *
     * @param material Materi baru yang akan ditambahkan
     */
    public void addMaterial(CourseMaterial material) {
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
        String desc = obj.has("description") ? obj.get("description").getAsString() : "";
        String type = obj.has("type") ? obj.get("type").getAsString() : "TEXT";

        // PILAR OOP: POLYMORPHISM - instansiasi kelas yang tepat berdasarkan tipe
        if ("VIDEO".equalsIgnoreCase(type)) {
            String url = obj.has("videoUrl") ? obj.get("videoUrl").getAsString() : "";
            int duration = obj.has("durationMinutes") ? obj.get("durationMinutes").getAsInt() : 0;
            return new VideoMaterial(id, title, desc, url, duration);
        } else {
            String content = obj.has("textContent") ? obj.get("textContent").getAsString() : "";
            return new TextMaterial(id, title, desc, content);
        }
    }

    /**
     * Data dummy untuk demo ketika backend tidak tersedia.
     */
    public List<CourseMaterial> getDummyMaterials() {
        List<CourseMaterial> dummies = new ArrayList<>();

        dummies.add(new VideoMaterial(
            1L,
            "Pengantar Pemrograman Berorientasi Objek",
            "Pengantar konsep-konsep dasar OOP: kelas, objek, enkapsulasi, dan lebih banyak lagi.",
            "https://www.youtube.com/watch?v=grEKMHGYyns",
            45
        ));

        dummies.add(new TextMaterial(
            2L,
            "Konsep Inheritance dalam Java",
            "Penjelasan lengkap tentang pewarisan (inheritance) di Java dengan contoh nyata.",
            "Inheritance (Pewarisan) adalah salah satu pilar utama OOP.\n\n"
            + "Dalam Java, kelas dapat mewarisi properti dan metode dari kelas lain menggunakan "
            + "kata kunci 'extends'.\n\n"
            + "Contoh:\n"
            + "public class Hewan {\n"
            + "    private String nama;\n"
            + "    public void bersuara() { System.out.println(\"...\"); }\n"
            + "}\n\n"
            + "public class Anjing extends Hewan {\n"
            + "    @Override\n"
            + "    public void bersuara() { System.out.println(\"Guk!\"); }\n"
            + "}\n\n"
            + "Dengan inheritance, kelas Anjing mewarisi semua anggota dari Hewan dan "
            + "dapat meng-override perilaku yang perlu diubah.\n\n"
            + "Keuntungan Inheritance:\n"
            + "1. Reusability - kode dapat digunakan kembali\n"
            + "2. Extendability - mudah dikembangkan\n"
            + "3. Polymorphism - mendukung polimorfisme\n"
        ));

        dummies.add(new VideoMaterial(
            3L,
            "Polymorphism dan Dynamic Dispatch",
            "Memahami polimorfisme di Java melalui method overriding dan dynamic dispatch.",
            "https://www.youtube.com/watch?v=U8yjyEs40gI",
            60
        ));

        dummies.add(new TextMaterial(
            4L,
            "Abstraksi dengan Abstract Class & Interface",
            "Pelajari cara menggunakan abstract class dan interface untuk menerapkan abstraksi.",
            "Abstraksi adalah konsep menyembunyikan detail implementasi dan hanya menampilkan "
            + "fungsionalitas yang relevan kepada pengguna.\n\n"
            + "Di Java, abstraksi dapat dicapai dengan:\n"
            + "1. Abstract Class\n"
            + "2. Interface\n\n"
            + "Abstract Class:\n"
            + "- Tidak dapat diinstansiasi langsung\n"
            + "- Dapat memiliki metode abstrak dan non-abstrak\n"
            + "- Gunakan kata kunci 'abstract'\n\n"
            + "Interface:\n"
            + "- Semua metode secara default adalah abstrak (sebelum Java 8)\n"
            + "- Dapat memiliki default methods (Java 8+)\n"
            + "- Satu kelas dapat mengimplementasikan banyak interface\n"
        ));

        return dummies;
    }
}

package com.doamamah.edutrack.fe.model;

import javafx.scene.Node;

/**
 * =====================================================================
 * PILAR OOP: ABSTRACTION (Abstraksi)
 * CourseMaterial adalah ABSTRACT CLASS yang mendefinisikan kontrak
 * (blueprint) umum untuk semua jenis materi pembelajaran.
 *
 * Metode getUIComponent() dideklarasikan abstract karena setiap
 * jenis materi harus menentukan sendiri bagaimana cara menampilkan
 * dirinya sebagai komponen UI JavaFX.
 *
 * Dengan abstraksi, kita menyembunyikan detail implementasi UI
 * dan hanya mengekspos antarmuka yang relevan kepada pengguna kelas ini.
 * =====================================================================
 */
public abstract class CourseMaterial {

    // PILAR OOP: ENCAPSULATION - field private, akses via getter/setter
    private Long id;
    private String title;
    private String description;
    private String materialType; // "VIDEO" atau "TEXT"

    public CourseMaterial() {}

    public CourseMaterial(Long id, String title, String description, String materialType) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.materialType = materialType;
    }

    // --- GETTERS & SETTERS ---

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) {
        // PILAR OOP: ENCAPSULATION - validasi title tidak kosong
        if (title == null || title.isBlank()) {
            throw new IllegalArgumentException("Judul materi tidak boleh kosong.");
        }
        this.title = title;
    }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getMaterialType() { return materialType; }
    public void setMaterialType(String materialType) { this.materialType = materialType; }

    /**
     * PILAR OOP: ABSTRACTION
     * Metode ABSTRAK yang WAJIB diimplementasikan oleh setiap subclass.
     * Setiap jenis materi bertanggung jawab untuk merender UI-nya sendiri
     * sebagai JavaFX Node. Controller tidak perlu tahu detail implementasinya.
     *
     * @return Node JavaFX yang merepresentasikan tampilan materi ini
     */
    public abstract Node getUIComponent();

    @Override
    public String toString() {
        return "CourseMaterial{id=" + id + ", title='" + title + "', type='" + materialType + "'}";
    }
}

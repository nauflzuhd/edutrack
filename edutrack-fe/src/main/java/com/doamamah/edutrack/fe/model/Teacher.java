package com.doamamah.edutrack.fe.model;

/**
 * =====================================================================
 * PILAR OOP: INHERITANCE (Pewarisan)
 * Teacher adalah kelas TURUNAN (subclass) dari User.
 * Teacher mewarisi semua properti User, dan menambahkan
 * atribut spesifik untuk Pengajar seperti bidang spesialisasi.
 *
 * PILAR OOP: ENCAPSULATION
 * Field tambahan khusus Teacher dideklarasikan private dengan validasi.
 * =====================================================================
 */
public class Teacher extends User {

    // PILAR OOP: ENCAPSULATION - field private khusus Teacher
    private String teacherId;       // Kode/ID Pengajar
    private String specialization;  // Bidang keahlian/spesialisasi
    private int totalCourses;       // Total kursus yang diajarkan

    public Teacher() {
        super();
        // PILAR OOP: INHERITANCE - memanggil konstruktor superclass
        setRole("TEACHER");
    }

    public Teacher(Long id, String username, String fullName, String email,
                   String teacherId, String specialization) {
        // PILAR OOP: INHERITANCE - memanggil konstruktor superclass
        super(id, username, fullName, email, "TEACHER");
        this.teacherId = teacherId;
        this.specialization = specialization;
        this.totalCourses = 0;
    }

    // --- GETTERS & SETTERS khusus Teacher ---

    public String getTeacherId() {
        return teacherId;
    }

    public void setTeacherId(String teacherId) {
        this.teacherId = teacherId;
    }

    public String getSpecialization() {
        return specialization;
    }

    public void setSpecialization(String specialization) {
        // PILAR OOP: ENCAPSULATION - validasi: spesialisasi tidak boleh kosong
        if (specialization == null || specialization.isBlank()) {
            this.specialization = "Umum";
        } else {
            this.specialization = specialization;
        }
    }

    public int getTotalCourses() {
        return totalCourses;
    }

    public void setTotalCourses(int totalCourses) {
        // PILAR OOP: ENCAPSULATION - validasi: total kursus tidak negatif
        if (totalCourses < 0) {
            throw new IllegalArgumentException("Total kursus tidak boleh negatif.");
        }
        this.totalCourses = totalCourses;
    }

    /**
     * PILAR OOP: INHERITANCE - Override metode abstrak dari superclass User.
     */
    @Override
    public String getDisplayRole() {
        return "Pengajar";
    }

    @Override
    public String toString() {
        return "Teacher{teacherId='" + teacherId + "', specialization='" + specialization
                + "', " + super.toString() + "}";
    }
}

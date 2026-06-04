package com.doamamah.edutrack.fe.model;

/**
 * =====================================================================
 * PILAR OOP: INHERITANCE (Pewarisan)
 * Student adalah kelas TURUNAN (subclass) dari User.
 * Student mewarisi semua properti dan metode User, serta
 * menambahkan atribut dan perilaku yang spesifik untuk Siswa.
 *
 * PILAR OOP: ENCAPSULATION
 * Field tambahan khusus Student juga dideklarasikan private.
 * =====================================================================
 */
public class Student extends User {

    // PILAR OOP: ENCAPSULATION - field private khusus Student
    private String studentId;   // Nomor Induk Mahasiswa/Siswa
    private int enrolledCourses; // Jumlah kursus yang diikuti

    public Student() {
        super();
        // PILAR OOP: INHERITANCE - memanggil konstruktor superclass
        setRole("STUDENT");
    }

    public Student(Long id, String username, String fullName, String email, String studentId) {
        // PILAR OOP: INHERITANCE - memanggil konstruktor superclass dengan super()
        super(id, username, fullName, email, "STUDENT");
        this.studentId = studentId;
        this.enrolledCourses = 0;
    }

    // --- GETTERS & SETTERS khusus Student ---

    public String getStudentId() {
        return studentId;
    }

    public void setStudentId(String studentId) {
        // PILAR OOP: ENCAPSULATION - validasi format ID
        if (studentId != null && studentId.length() > 20) {
            throw new IllegalArgumentException("Student ID terlalu panjang (maks 20 karakter).");
        }
        this.studentId = studentId;
    }

    public int getEnrolledCourses() {
        return enrolledCourses;
    }

    public void setEnrolledCourses(int enrolledCourses) {
        // PILAR OOP: ENCAPSULATION - validasi: jumlah kursus tidak negatif
        if (enrolledCourses < 0) {
            throw new IllegalArgumentException("Jumlah kursus tidak boleh negatif.");
        }
        this.enrolledCourses = enrolledCourses;
    }

    /**
     * PILAR OOP: INHERITANCE - Override metode abstrak dari superclass User.
     */
    @Override
    public String getDisplayRole() {
        return "Siswa";
    }

    @Override
    public String toString() {
        return "Student{studentId='" + studentId + "', " + super.toString() + "}";
    }
}

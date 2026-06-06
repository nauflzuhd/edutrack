# EduTrack (Platform Edukasi Interaktif)

EduTrack adalah sebuah aplikasi platform edukasi (E-Learning) yang dibangun dengan arsitektur modern yang memisahkan antara *Frontend* dan *Backend*. Aplikasi ini dirancang untuk memudahkan interaksi antara pengajar dan siswa dalam proses belajar mengajar. Pengajar dapat mengelola materi dan kuis, sedangkan siswa dapat mengikuti kelas, membaca materi, serta mengerjakan kuis untuk memantau perkembangan belajar mereka.

Teknologi yang digunakan dalam proyek ini:
1. **edutrack-be**: Backend menggunakan Spring Boot REST API
2. **edutrack-fe**: Frontend menggunakan aplikasi GUI berbasis JavaFX

## Fitur-Fitur Utama

### Untuk Pengajar (Teacher)
- **Manajemen Materi**: Membuat, mengedit, dan menghapus materi pembelajaran (berupa modul teks atau video).
- **Manajemen Kuis**: Menyusun kuis interaktif dengan berbagai tingkat kesulitan untuk menguji pemahaman siswa.
- **Daftar Siswa**: Memantau daftar siswa yang tergabung di dalam kelasnya beserta informasi detail mereka.
- **Statistik & Laporan**: Memantau aktivitas belajar siswa dan melihat statistik skor pengerjaan kuis siswa di kelasnya.

### Untuk Siswa (Student)
- **Pemilihan Pengajar**: Memilih dan bergabung ke dalam kelas pengajar tertentu sesuai dengan minat atau kebutuhan belajar.
- **Akses Materi**: Membaca atau menonton materi edukasi yang telah disediakan oleh pengajar di kelas yang diikuti.
- **Mengerjakan Kuis**: Mengikuti kuis interaktif dan langsung melihat skor serta umpan balik pengerjaannya.
- **Dashboard Progress**: Melacak progres pembelajaran dan riwayat skor kuis melalui _dashboard_ interaktif.

## Prasyarat (Dependencies)
Sebelum menjalankan proyek, pastikan Anda sudah menginstal _environment_ berikut:
- **Java JDK 21**
- **Apache Maven** (Untuk *build* dan menjalankan Frontend)
- **Git**

## Cara Menjalankan Aplikasi

### 1. Clone Repository
```bash
git clone https://github.com/iyeppp/edutrack.git
cd edutrack
```

### 2. Menjalankan Backend (Spring Boot)
Buka terminal dan masuk ke folder `edutrack-be`, kemudian jalankan perintah *Maven Wrapper*:
```bash
cd edutrack-be

# Untuk pengguna Windows
.\mvnw.cmd spring-boot:run

# Untuk pengguna Mac/Linux
./mvnw spring-boot:run
```

### 3. Menjalankan Frontend (JavaFX)
Buka terminal *baru* (agar terminal backend tetap berjalan), masuk ke folder `edutrack-fe`, dan jalankan perintah:
```bash
cd edutrack-fe
mvn clean javafx:run
```
Jika berhasil, jendela aplikasi GUI EduTrack akan muncul dan Anda bisa langsung melakukan *Login* atau *Register* untuk mulai menggunakan aplikasi!

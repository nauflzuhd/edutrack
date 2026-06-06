-- 1. Insert Users (Teachers & Students)
-- Password default adalah nama + '123' untuk kemudahan testing
INSERT INTO users (id, username, password, full_name, email, role, bio, teacher_id, specialization, total_courses, student_id, enrolled_courses)
VALUES 
(1, 'guru', '123', 'Budi Santoso', 'budi.santoso@edutrack.com', 'TEACHER', 'Pengajar Pemrograman Java dan Konsep PBO.', 'TCH-001', 'Pemrograman', 0, NULL, 0),
(2, 'pak_eko', 'eko123', 'Eko Purnomo', 'eko.purnomo@edutrack.com', 'TEACHER', 'Ahli Basis Data dan Jaringan Komputer.', 'TCH-002', 'Basis Data', 0, NULL, 0),
(3, 'siswa', '123', 'Andi Pratama', 'andi.pratama@edutrack.com', 'STUDENT', 'Siswa yang antusias belajar teknologi.', NULL, NULL, 0, 'STD-001', 0),
(4, 'siswa_baru', 'baru123', 'Siti Rahma', 'siti.rahma@edutrack.com', 'STUDENT', 'Siswa baru di platform EduTrack.', NULL, NULL, 0, 'STD-002', 0);

-- 2. Insert Enrollments
-- Andi Pratama enrol ke Budi Santoso
INSERT INTO enrollments (id, student_id, teacher_id, enrolled_at)
VALUES (1, 3, 1, CURRENT_TIMESTAMP);

-- 3. Insert Course Materials
INSERT INTO course_materials (id, title, description, type, video_url, duration_minutes, text_content, teacher_id)
VALUES 
(1, 'Pengenalan PBO', 'Konsep dasar Pemrograman Berorientasi Objek', 'VIDEO', 'https://youtu.be/dummy1', 15, NULL, 1),
(2, 'Dasar-dasar Java', 'Tipe data, variabel, dan struktur kontrol di Java', 'TEXT', NULL, 0, 'Java adalah bahasa pemrograman yang kuat dan robust...', 1),
(3, 'Pengenalan SQL', 'Dasar-dasar Query SQL', 'VIDEO', 'https://youtu.be/dummy2', 20, NULL, 2);

-- 4. Insert Quizzes
INSERT INTO quizzes (id, title, description, difficulty, teacher_id)
VALUES 
(1, 'Kuis Konsep Dasar PBO', 'Uji pemahamanmu tentang objek dan kelas.', 'Mudah', 1),
(2, 'Kuis Lanjutan Java', 'Kuis untuk menguji pemahaman inheritance dan polymorphism.', 'Sedang', 1),
(3, 'Kuis Dasar SQL', 'Kuis tentang SELECT, INSERT, UPDATE, DELETE.', 'Sedang', 2);

-- 5. Insert Quiz Questions
-- Pertanyaan untuk Kuis 1 (Kuis Konsep Dasar PBO)
INSERT INTO quiz_questions (id, quiz_id, question_text, option_a, option_b, option_c, option_d, correct_option_index)
VALUES 
(1, 1, 'Apa itu class dalam PBO?', 'Sebuah objek', 'Sebuah blueprint atau cetakan', 'Sebuah tipe data primitif', 'Sebuah fungsi', 1),
(2, 1, 'Keyword apa yang digunakan untuk membuat objek baru di Java?', 'create', 'new', 'make', 'build', 1);

-- Pertanyaan untuk Kuis 2 (Kuis Lanjutan Java)
INSERT INTO quiz_questions (id, quiz_id, question_text, option_a, option_b, option_c, option_d, correct_option_index)
VALUES 
(3, 2, 'Konsep di mana sebuah class dapat mewarisi properti dari class lain disebut?', 'Polymorphism', 'Encapsulation', 'Inheritance', 'Abstraction', 2),
(4, 2, 'Keyword apa yang digunakan untuk menerapkan inheritance di Java?', 'implements', 'inherits', 'extends', 'super', 2);

-- Pertanyaan untuk Kuis 3 (Kuis Dasar SQL)
INSERT INTO quiz_questions (id, quiz_id, question_text, option_a, option_b, option_c, option_d, correct_option_index)
VALUES 
(5, 3, 'Perintah SQL untuk mengambil data dari tabel adalah?', 'GET', 'FETCH', 'SELECT', 'PULL', 2),
(6, 3, 'Perintah SQL untuk menghapus data adalah?', 'DELETE', 'REMOVE', 'DROP', 'CLEAR', 0);

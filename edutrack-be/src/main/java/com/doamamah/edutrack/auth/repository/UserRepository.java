package com.doamamah.edutrack.auth.repository;

import com.doamamah.edutrack.auth.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repository untuk akses data User (Student/Teacher) dari database.
 */
@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    /**
     * Mencari user berdasarkan username.
     * Digunakan saat proses login.
     */
    Optional<User> findByUsername(String username);

    @Query("SELECT COUNT(u) FROM User u WHERE TYPE(u) = Student")
    long countStudents();

    @Query("SELECT COUNT(u) FROM User u WHERE TYPE(u) = Teacher")
    long countTeachers();

    @Query("SELECT u FROM User u WHERE TYPE(u) = Student")
    java.util.List<User> findAllStudents();

    @Query("SELECT u FROM User u WHERE TYPE(u) = Teacher")
    java.util.List<User> findAllTeachers();
}

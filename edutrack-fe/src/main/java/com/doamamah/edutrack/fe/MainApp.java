/*
 * =====================================================================
 * Aplikasi  : EduTrack - Platform E-Learning Desktop
 * File      : MainApp.java
 * Pembuat   : Doa Mamah 2.0
 * Deskripsi : Entry point utama aplikasi JavaFX EduTrack.
 *             Aplikasi ini bersifat Single Page Application (SPA)
 *             berbasis desktop yang menghubungkan Pengajar & Siswa.
 *             Backend REST API diasumsikan berjalan di http://localhost:8080
 * =====================================================================
 */
package com.doamamah.edutrack.fe;

import com.doamamah.edutrack.fe.util.SceneManager;
import javafx.application.Application;
import javafx.stage.Stage;

public class MainApp extends Application {

    @Override
    public void start(Stage stage) throws Exception {
        // Inisialisasi SceneManager dengan stage utama
        SceneManager.getInstance().setPrimaryStage(stage);

        // Konfigurasi stage utama
        stage.setTitle("EduTrack by Doa Mamah 2.0");
        stage.setWidth(1024);
        stage.setHeight(768);
        stage.setMinWidth(900);
        stage.setMinHeight(650);
        stage.setResizable(true);

        // Tampilkan halaman Login sebagai halaman pertama
        SceneManager.getInstance().showLogin();

        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}

package com.doamamah.edutrack.fe.util;

import com.doamamah.edutrack.fe.model.User;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;

/**
 * SceneManager - Singleton untuk navigasi antar scene (SPA pattern).
 */
public class SceneManager {

    private static final double DEFAULT_WIDTH  = 1024;
    private static final double DEFAULT_HEIGHT = 768;

    private static SceneManager instance;
    private Stage primaryStage;
    private User currentUser;

    private SceneManager() {}

    public static SceneManager getInstance() {
        if (instance == null) {
            instance = new SceneManager();
        }
        return instance;
    }

    public void setPrimaryStage(Stage stage) {
        this.primaryStage = stage;
    }

    public Stage getPrimaryStage() {
        return primaryStage;
    }

    public User getCurrentUser() {
        return currentUser;
    }

    public void setCurrentUser(User user) {
        this.currentUser = user;
    }

    public void showLogin() {
        loadScene("/com/doamamah/edutrack/fe/view/login.fxml");
    }

    public void showDashboard() {
        loadScene("/com/doamamah/edutrack/fe/view/dashboard.fxml");
    }

    /**
     * Load FXML dan pasang ke primary stage.
     *
     * FIX LAYAR PUTIH: stage.getWidth() mengembalikan Double.NaN sebelum
     * stage.show() dipanggil, sehingga Scene dibuat dengan ukuran NaN dan
     * layout rusak. Solusi: gunakan ukuran default jika nilai NaN.
     */
    private void loadScene(String fxmlPath) {
        try {
            URL fxmlUrl = getClass().getResource(fxmlPath);
            if (fxmlUrl == null) {
                System.err.println("[ERROR] FXML tidak ditemukan: " + fxmlPath);
                return;
            }

            FXMLLoader loader = new FXMLLoader(fxmlUrl);
            Parent root = loader.load();

            // Pakai ukuran aktual jika sudah ada, fallback ke default 1024x768
            double w = Double.isNaN(primaryStage.getWidth())  ? DEFAULT_WIDTH  : primaryStage.getWidth();
            double h = Double.isNaN(primaryStage.getHeight()) ? DEFAULT_HEIGHT : primaryStage.getHeight();

            Scene scene = new Scene(root, w, h);

            // Pasang stylesheet dengan null-safety
            URL cssUrl = getClass().getResource("/com/doamamah/edutrack/fe/css/style.css");
            if (cssUrl != null) {
                scene.getStylesheets().add(cssUrl.toExternalForm());
            } else {
                System.err.println("[WARN] CSS tidak ditemukan.");
            }

            primaryStage.setScene(scene);

        } catch (IOException e) {
            System.err.println("[ERROR] Gagal memuat FXML: " + fxmlPath);
            e.printStackTrace();
        }
    }
}

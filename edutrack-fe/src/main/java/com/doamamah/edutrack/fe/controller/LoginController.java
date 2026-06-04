package com.doamamah.edutrack.fe.controller;

import com.doamamah.edutrack.fe.model.Student;
import com.doamamah.edutrack.fe.model.Teacher;
import com.doamamah.edutrack.fe.model.User;
import com.doamamah.edutrack.fe.service.AuthService;
import com.doamamah.edutrack.fe.util.SceneManager;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;

import java.net.URL;
import java.util.ResourceBundle;

/**
 * LoginController - Controller untuk halaman Login, Register, dan Lupa Password.
 * Mendukung mode ONLINE (backend) dan mode DEMO (hardcoded).
 */
public class LoginController implements Initializable {

    // --- Login Card ---
    @FXML private VBox loginCard;
    @FXML private TextField usernameField;
    @FXML private PasswordField passwordField;
    @FXML private Button loginButton;
    @FXML private Label errorLabel;
    @FXML private ProgressIndicator loadingIndicator;

    // --- Register Card ---
    @FXML private VBox registerCard;
    @FXML private TextField regUsernameField;
    @FXML private TextField regFullNameField;
    @FXML private TextField regEmailField;
    @FXML private ComboBox<String> regRoleCombo;
    @FXML private PasswordField regPasswordField;
    @FXML private PasswordField regConfirmPasswordField;

    // --- Forgot Password Card ---
    @FXML private VBox forgotPasswordCard;
    @FXML private TextField forgotEmailField;

    private final AuthService authService = new AuthService();

    // Demo Kredensial
    private static final String DEMO_STUDENT_USER = "siswa";
    private static final String DEMO_STUDENT_PASS = "123";
    private static final String DEMO_TEACHER_USER = "guru";
    private static final String DEMO_TEACHER_PASS = "123";

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        passwordField.setOnAction(e -> handleLogin());
        usernameField.setOnAction(e -> passwordField.requestFocus());
        
        if (regRoleCombo != null) {
            regRoleCombo.getItems().addAll("Siswa", "Pengajar");
            regRoleCombo.setValue("Siswa");
        }
        hideError();
    }

    // =====================================================================
    //  NAVIGASI FORM
    // =====================================================================

    @FXML
    private void showRegister() {
        loginCard.setVisible(false);
        loginCard.setManaged(false);
        forgotPasswordCard.setVisible(false);
        forgotPasswordCard.setManaged(false);
        
        registerCard.setVisible(true);
        registerCard.setManaged(true);
        clearFields();
    }

    @FXML
    private void showForgotPassword() {
        loginCard.setVisible(false);
        loginCard.setManaged(false);
        registerCard.setVisible(false);
        registerCard.setManaged(false);
        
        forgotPasswordCard.setVisible(true);
        forgotPasswordCard.setManaged(true);
        clearFields();
    }

    @FXML
    private void showLogin() {
        registerCard.setVisible(false);
        registerCard.setManaged(false);
        forgotPasswordCard.setVisible(false);
        forgotPasswordCard.setManaged(false);
        
        loginCard.setVisible(true);
        loginCard.setManaged(true);
        clearFields();
    }

    private void clearFields() {
        usernameField.clear();
        passwordField.clear();
        if (regUsernameField != null) regUsernameField.clear();
        if (regFullNameField != null) regFullNameField.clear();
        if (regEmailField != null) regEmailField.clear();
        if (regPasswordField != null) regPasswordField.clear();
        if (regConfirmPasswordField != null) regConfirmPasswordField.clear();
        if (forgotEmailField != null) forgotEmailField.clear();
        hideError();
    }

    // =====================================================================
    //  AKSI FORM
    // =====================================================================

    @FXML
    private void handleLogin() {
        String username = usernameField.getText().trim();
        String password = passwordField.getText();

        if (username.isEmpty()) {
            showError("Username tidak boleh kosong.");
            usernameField.requestFocus();
            return;
        }
        if (password.isEmpty()) {
            showError("Password tidak boleh kosong.");
            passwordField.requestFocus();
            return;
        }

        User demoUser = checkDemoCredentials(username, password);
        if (demoUser != null) {
            SceneManager.getInstance().setCurrentUser(demoUser);
            SceneManager.getInstance().showDashboard();
            return;
        }

        setLoadingState(true);
        hideError();

        Thread loginThread = new Thread(() -> {
            try {
                User loggedInUser = authService.login(username, password);
                Platform.runLater(() -> {
                    SceneManager.getInstance().setCurrentUser(loggedInUser);
                    SceneManager.getInstance().showDashboard();
                });
            } catch (Exception e) {
                Platform.runLater(() -> {
                    setLoadingState(false);
                    showError(e.getMessage());
                });
            }
        });
        loginThread.setDaemon(true);
        loginThread.start();
    }

    @FXML
    private void handleRegister() {
        String username = regUsernameField.getText().trim();
        String fullName = regFullNameField.getText().trim();
        String email = regEmailField.getText().trim();
        String role = regRoleCombo.getValue();
        String password = regPasswordField.getText();
        String confirmPassword = regConfirmPasswordField.getText();

        if (username.isEmpty() || fullName.isEmpty() || email.isEmpty() || password.isEmpty()) {
            showError("Semua field wajib diisi.");
            return;
        }

        if (!password.equals(confirmPassword)) {
            showError("Konfirmasi password tidak cocok.");
            return;
        }

        showSuccessAlert(
            "Pendaftaran Berhasil",
            "Selamat bergabung di EduTrack!",
            "Akun dengan username '" + username + "' sebagai " + role + " berhasil didaftarkan.\nSilakan masuk untuk mulai belajar!"
        );
        showLogin();
    }

    @FXML
    private void handleForgotPassword() {
        String email = forgotEmailField.getText().trim();

        if (email.isEmpty()) {
            showError("Silakan masukkan alamat email Anda.");
            return;
        }

        if (!email.contains("@")) {
            showError("Format email tidak valid.");
            return;
        }

        showSuccessAlert(
            "Pemulihan Kata Sandi",
            "Tautan Pemulihan Dikirim!",
            "Kami telah mengirimkan instruksi pemulihan kata sandi ke email: " + email + "\nSilakan periksa kotak masuk atau folder spam Anda."
        );
        showLogin();
    }

    // =====================================================================
    //  HELPERS
    // =====================================================================

    private User checkDemoCredentials(String username, String password) {
        if (DEMO_STUDENT_USER.equalsIgnoreCase(username)
                && DEMO_STUDENT_PASS.equals(password)) {
            Student s = new Student(1L, "siswa", "Budi Santoso", "budi@demo.com", "STD001");
            s.setEnrolledCourses(4);
            return s;
        }
        if (DEMO_TEACHER_USER.equalsIgnoreCase(username)
                && DEMO_TEACHER_PASS.equals(password)) {
            Teacher t = new Teacher(2L, "guru", "Ibu Sari Dewi", "sari@demo.com",
                    "TCH001", "Pemrograman Java");
            t.setTotalCourses(4);
            return t;
        }
        return null;
    }

    private void showError(String message) {
        errorLabel.setText("⚠  " + message);
        errorLabel.setVisible(true);
        errorLabel.setManaged(true);
    }

    private void hideError() {
        errorLabel.setText("");
        errorLabel.setVisible(false);
        errorLabel.setManaged(false);
    }

    private void setLoadingState(boolean loading) {
        loginButton.setDisable(loading);
        usernameField.setDisable(loading);
        passwordField.setDisable(loading);
        loadingIndicator.setVisible(loading);
        loadingIndicator.setManaged(loading);
        loginButton.setText(loading ? "Memproses..." : "Masuk  →");
    }

    private void showSuccessAlert(String title, String header, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(content);
        
        try {
            ImageView alertMascot = new ImageView(
                new Image(getClass().getResourceAsStream("/com/doamamah/edutrack/fe/images/mascot_face.png"))
            );
            alertMascot.setFitWidth(48);
            alertMascot.setPreserveRatio(true);
            alertMascot.setSmooth(true);
            alert.setGraphic(alertMascot);
        } catch (Exception ex) {
            System.err.println("Gagal memuat maskot ke alert dialog: " + ex.getMessage());
        }

        DialogPane dialogPane = alert.getDialogPane();
        try {
            String cssPath = getClass().getResource("/com/doamamah/edutrack/fe/css/style.css").toExternalForm();
            dialogPane.getStylesheets().add(cssPath);
            dialogPane.getStyleClass().add("custom-alert");
        } catch (Exception ex) {
            System.err.println("Gagal memuat stylesheet ke alert dialog: " + ex.getMessage());
        }

        alert.showAndWait();
    }
}

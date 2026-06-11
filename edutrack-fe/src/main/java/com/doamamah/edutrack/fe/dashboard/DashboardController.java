package com.doamamah.edutrack.fe.dashboard;

import com.doamamah.edutrack.fe.material.CourseMaterial;
import com.doamamah.edutrack.fe.user.Teacher;
import com.doamamah.edutrack.fe.user.User;
import com.doamamah.edutrack.fe.auth.AuthService;
import com.doamamah.edutrack.fe.material.MaterialService;
import com.doamamah.edutrack.fe.core.SceneManager;
import com.doamamah.edutrack.fe.material.MaterialView;
import com.doamamah.edutrack.fe.quiz.QuizView;
import com.doamamah.edutrack.fe.user.StudentListView;
import com.doamamah.edutrack.fe.user.TeacherListView;
import com.doamamah.edutrack.fe.user.ProfileView;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;
import javafx.animation.FadeTransition;
import javafx.animation.TranslateTransition;
import javafx.util.Duration;

/**
 * DashboardController - Controller untuk halaman Dashboard Utama.
 */
public class DashboardController implements Initializable {

    @FXML private Label avatarLabel;
    @FXML private Label profileNameLabel;
    @FXML private Label profileRoleLabel;
    @FXML private Button btnDashboard;
    @FXML private Button btnMaterials;
    @FXML private Button btnQuiz;

    @FXML private Button btnStudents;
    @FXML private Button btnTeachers;
    @FXML private Button btnProfile;
    @FXML private Button btnLogout;
    @FXML private ToggleButton btnDarkMode;
    @FXML private Label contentTitleLabel;
    @FXML private Label greetingLabel;
    @FXML private VBox contentArea;
    @FXML private ScrollPane contentScrollPane;
    @FXML private StackPane rootStackPane;

    private final MaterialService materialService = new MaterialService();
    private final DashboardService dashboardService = new DashboardService();
    private final AuthService authService = new AuthService();
    private User currentUser;

    private DashboardHomeView dashboardHomeView;
    private MaterialView materialView;
    private QuizView quizView;
    private StudentListView studentListView;
    private TeacherListView teacherListView;
    private ProfileView profileView;

    public User getCurrentUser() { return currentUser; }
    public MaterialService getMaterialService() { return materialService; }
    public DashboardService getDashboardService() { return dashboardService; }
    public VBox getContentArea() { return contentArea; }
    public Label getContentTitleLabel() { return contentTitleLabel; }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        currentUser = SceneManager.getInstance().getCurrentUser();
        setupProfile();
        dashboardHomeView = new DashboardHomeView(this);
        materialView = new MaterialView(this);
        quizView = new QuizView(this);
        studentListView = new StudentListView(this);
        teacherListView = new TeacherListView(this);
        profileView = new ProfileView(this);
        showDashboardContent();
    }

    private void setupProfile() {
        if (currentUser == null) return;

        String fullName = currentUser.getFullName();
        String initial = (fullName != null && !fullName.isEmpty())
                ? String.valueOf(fullName.charAt(0)).toUpperCase() : "?";

        profileNameLabel.setText(fullName != null ? fullName : currentUser.getUsername());
        profileRoleLabel.setText(currentUser.getDisplayRole());
        avatarLabel.setText(initial);

        String firstName = fullName != null ? fullName.split(" ")[0] : "Pengguna";
        greetingLabel.setText("Halo, " + firstName + "!");

        // Jika Guru, tampilkan tombol navigasi Daftar Siswa
        if (currentUser instanceof Teacher) {
            if (btnStudents != null) {
                btnStudents.setVisible(true);
                btnStudents.setManaged(true);
            }
        } else {
            // Siswa: tampilkan tombol Pengajar
            if (btnTeachers != null) {
                btnTeachers.setVisible(true);
                btnTeachers.setManaged(true);
            }
        }
    }

    // =====================================================================
    //  ANIMATION UTILS
    // =====================================================================

    private void switchContent(Node newContent) {
        contentArea.getChildren().clear();
        contentArea.getChildren().add(newContent);
        
        // Animasi Fade In
        FadeTransition fadeIn = new FadeTransition(Duration.millis(300), newContent);
        fadeIn.setFromValue(0.0);
        fadeIn.setToValue(1.0);
        
        // Animasi Slide Up
        TranslateTransition slideUp = new TranslateTransition(Duration.millis(300), newContent);
        slideUp.setFromY(20);
        slideUp.setToY(0);
        
        fadeIn.play();
        slideUp.play();
    }

    // =====================================================================
    //  NAVIGASI
    // =====================================================================

    @FXML
    public void showDashboardContent() {
        setActiveButton(btnDashboard);
        contentTitleLabel.setText("Dashboard");
        switchContent(dashboardHomeView.buildContent());
    }

    private final com.doamamah.edutrack.fe.enrollment.EnrollmentService enrollmentService = new com.doamamah.edutrack.fe.enrollment.EnrollmentService();

    @FXML
    public void showMaterialsContent() {
        setActiveButton(btnMaterials);
        contentTitleLabel.setText("Daftar Materi");

        ProgressIndicator loading = new ProgressIndicator();
        loading.setPrefSize(40, 40);
        VBox loadingBox = new VBox(loading, new Label("Memuat materi..."));
        loadingBox.setAlignment(Pos.CENTER);
        loadingBox.setSpacing(12);
        loadingBox.setPadding(new Insets(40));
        switchContent(loadingBox);

        Thread fetchThread = new Thread(() -> {
            List<Long> teacherIds = null;
            if (currentUser instanceof Teacher teacher) {
                teacherIds = List.of(teacher.getId());
            } else if (currentUser instanceof com.doamamah.edutrack.fe.user.Student student) {
                teacherIds = enrollmentService.getEnrolledTeacherIds(student.getId());
                if (teacherIds.isEmpty()) {
                    teacherIds = List.of(-1L); // Force empty if no teachers
                }
            }
            List<CourseMaterial> materials = materialService.getAllMaterials(teacherIds);
            Platform.runLater(() -> {
                switchContent(materialView.buildListContent(materials));
            });
        });
        fetchThread.setDaemon(true);
        fetchThread.start();
    }

    @FXML
    public void showQuizContent() {
        setActiveButton(btnQuiz);
        contentTitleLabel.setText("Kuis");
        switchContent(quizView.buildQuizContent());
    }



    @FXML
    public void showStudentsContent() {
        setActiveButton(btnStudents);
        contentTitleLabel.setText("Daftar Siswa");
        switchContent(studentListView.buildContent());
    }

    @FXML
    public void showTeachersContent() {
        setActiveButton(btnTeachers);
        contentTitleLabel.setText("Pengajar");
        switchContent(teacherListView.buildContent());
    }

    @FXML
    public void showProfileContent() {
        setActiveButton(btnProfile);
        contentTitleLabel.setText("Profil Saya");
        profileView = new ProfileView(this); // refresh with latest data
        switchContent(profileView.buildContent());
    }

    @FXML
    public void handleLogout() {
        ButtonType response = showCustomAlert(
            Alert.AlertType.CONFIRMATION,
            "Konfirmasi Logout",
            "Keluar dari EduTrack?",
            "Apakah Anda yakin ingin logout?"
        );
        if (response == ButtonType.OK) {
            new Thread(() -> {
                authService.logout();
                Platform.runLater(() -> {
                    SceneManager.getInstance().setCurrentUser(null);
                    SceneManager.getInstance().showLogin();
                });
            }).start();
        }
    }

    private boolean isDarkMode = false;

    @FXML
    public void handleToggleDarkMode() {
        if (btnDarkMode == null || btnDarkMode.getScene() == null) return;
        
        isDarkMode = btnDarkMode.isSelected();
        String darkThemeCss = getClass().getResource("/com/doamamah/edutrack/fe/css/dark-theme.css").toExternalForm();
        
        if (isDarkMode) {
            btnDarkMode.setText("Tema Terang");
            if (!btnDarkMode.getScene().getStylesheets().contains(darkThemeCss)) {
                btnDarkMode.getScene().getStylesheets().add(darkThemeCss);
            }
        } else {
            btnDarkMode.setText("Tema Gelap");
            btnDarkMode.getScene().getStylesheets().remove(darkThemeCss);
        }
    }

    public VBox createInputField(String labelText, Control field) {
        VBox box = new VBox(6);
        Label lbl = new Label(labelText);
        lbl.getStyleClass().add("input-label");
        field.getStyleClass().add("input-field");
        box.getChildren().addAll(lbl, field);
        return box;
    }

    public ButtonType showCustomAlert(Alert.AlertType type, String title, String header, String content) {
        Alert alert = new Alert(type);
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
        } catch (Exception e) {
            System.err.println("Gagal memuat maskot ke alert dialog: " + e.getMessage());
        }
        DialogPane dialogPane = alert.getDialogPane();
        try {
            String cssPath = getClass().getResource("/com/doamamah/edutrack/fe/css/style.css").toExternalForm();
            dialogPane.getStylesheets().add(cssPath);
            dialogPane.getStyleClass().add("custom-alert");
        } catch (Exception e) {
            System.err.println("Gagal memuat stylesheet ke alert dialog: " + e.getMessage());
        }
        return alert.showAndWait().orElse(ButtonType.CANCEL);
    }

    public void showToast(String message, String type) {
        if (rootStackPane == null) return;
        
        Platform.runLater(() -> {
            Label toastLabel = new Label(message);
            toastLabel.setWrapText(true);
            toastLabel.setMaxWidth(300);
            
            HBox toastBox = new HBox(12);
            toastBox.setAlignment(Pos.CENTER_LEFT);
            toastBox.setPadding(new Insets(12, 16, 12, 16));
            toastBox.setMaxSize(Region.USE_PREF_SIZE, Region.USE_PREF_SIZE);
            
            String bgColor = "#10B981"; // SUCCESS
            String icon = "✅";
            if ("ERROR".equals(type)) {
                bgColor = "#EF4444";
                icon = "❌";
            } else if ("INFO".equals(type)) {
                bgColor = "#3B82F6";
                icon = "ℹ️";
            }
            
            toastBox.setStyle("-fx-background-color: " + bgColor + "; -fx-background-radius: 8px; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.2), 8, 0, 0, 4);");
            
            Label iconLabel = new Label(icon);
            iconLabel.setStyle("-fx-text-fill: white; -fx-font-size: 16px;");
            toastLabel.setStyle("-fx-text-fill: white; -fx-font-size: 14px; -fx-font-weight: bold;");
            
            toastBox.getChildren().addAll(iconLabel, toastLabel);
            
            StackPane.setAlignment(toastBox, Pos.BOTTOM_RIGHT);
            StackPane.setMargin(toastBox, new Insets(0, 24, 24, 0));
            
            toastBox.setOpacity(0);
            rootStackPane.getChildren().add(toastBox);
            
            FadeTransition fadeIn = new FadeTransition(Duration.millis(300), toastBox);
            fadeIn.setFromValue(0);
            fadeIn.setToValue(1);
            
            FadeTransition fadeOut = new FadeTransition(Duration.millis(300), toastBox);
            fadeOut.setFromValue(1);
            fadeOut.setToValue(0);
            fadeOut.setDelay(Duration.seconds(3));
            fadeOut.setOnFinished(e -> rootStackPane.getChildren().remove(toastBox));
            
            fadeIn.play();
            fadeOut.play();
        });
    }

    private void setActiveButton(Button activeBtn) {
        btnDashboard.getStyleClass().remove("nav-btn-active");
        btnMaterials.getStyleClass().remove("nav-btn-active");
        btnQuiz.getStyleClass().remove("nav-btn-active");

        if (btnStudents != null) {
            btnStudents.getStyleClass().remove("nav-btn-active");
        }
        if (btnTeachers != null) {
            btnTeachers.getStyleClass().remove("nav-btn-active");
        }
        if (btnProfile != null) {
            btnProfile.getStyleClass().remove("nav-btn-active");
        }
        if (!activeBtn.getStyleClass().contains("nav-btn-active")) {
            activeBtn.getStyleClass().add("nav-btn-active");
        }
    }
}

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
    @FXML private Label contentTitleLabel;
    @FXML private Label greetingLabel;
    @FXML private VBox contentArea;
    @FXML private ScrollPane contentScrollPane;

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
    //  NAVIGASI
    // =====================================================================

    @FXML
    public void showDashboardContent() {
        setActiveButton(btnDashboard);
        contentTitleLabel.setText("Dashboard");
        contentArea.getChildren().clear();
        contentArea.getChildren().add(dashboardHomeView.buildContent());
    }

    private final com.doamamah.edutrack.fe.enrollment.EnrollmentService enrollmentService = new com.doamamah.edutrack.fe.enrollment.EnrollmentService();

    @FXML
    public void showMaterialsContent() {
        setActiveButton(btnMaterials);
        contentTitleLabel.setText("Daftar Materi");
        contentArea.getChildren().clear();

        ProgressIndicator loading = new ProgressIndicator();
        loading.setPrefSize(40, 40);
        VBox loadingBox = new VBox(loading, new Label("Memuat materi..."));
        loadingBox.setAlignment(Pos.CENTER);
        loadingBox.setSpacing(12);
        loadingBox.setPadding(new Insets(40));
        contentArea.getChildren().add(loadingBox);

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
                contentArea.getChildren().clear();
                contentArea.getChildren().add(materialView.buildListContent(materials));
            });
        });
        fetchThread.setDaemon(true);
        fetchThread.start();
    }

    @FXML
    public void showQuizContent() {
        setActiveButton(btnQuiz);
        contentTitleLabel.setText("Kuis");
        contentArea.getChildren().clear();
        contentArea.getChildren().add(quizView.buildQuizContent());
    }



    @FXML
    public void showStudentsContent() {
        setActiveButton(btnStudents);
        contentTitleLabel.setText("Daftar Siswa");
        contentArea.getChildren().clear();
        contentArea.getChildren().add(studentListView.buildContent());
    }

    @FXML
    public void showTeachersContent() {
        setActiveButton(btnTeachers);
        contentTitleLabel.setText("Pengajar");
        contentArea.getChildren().clear();
        contentArea.getChildren().add(teacherListView.buildContent());
    }

    @FXML
    public void showProfileContent() {
        setActiveButton(btnProfile);
        contentTitleLabel.setText("Profil Saya");
        contentArea.getChildren().clear();
        profileView = new ProfileView(this); // refresh with latest data
        contentArea.getChildren().add(profileView.buildContent());
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

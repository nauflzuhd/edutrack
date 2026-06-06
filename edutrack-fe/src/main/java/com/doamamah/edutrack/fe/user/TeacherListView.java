package com.doamamah.edutrack.fe.user;
import com.doamamah.edutrack.fe.enrollment.EnrollmentService;

import com.doamamah.edutrack.fe.dashboard.DashboardController;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;

import java.util.List;

/**
 * View untuk siswa memilih dan mengelola pengajar yang diikuti.
 */
public class TeacherListView {

    private final DashboardController controller;
    private final EnrollmentService enrollmentService = new EnrollmentService();

    public TeacherListView(DashboardController controller) {
        this.controller = controller;
    }

    public Node buildContent() {
        VBox root = new VBox(16);
        root.setMaxWidth(Double.MAX_VALUE);

        // Header
        HBox header = new HBox(8);
        header.setAlignment(Pos.CENTER_LEFT);
        Label sectionLabel = new Label("Pilih Pengajar");
        sectionLabel.getStyleClass().add("section-title");
        Label subtitle = new Label("Gabung ke kelas pengajar untuk mengakses materi dan kuis mereka");
        subtitle.getStyleClass().add("card-description");
        Region sp = new Region();
        HBox.setHgrow(sp, Priority.ALWAYS);
        header.getChildren().addAll(sectionLabel, sp);
        root.getChildren().addAll(header, subtitle);

        // Loading
        ProgressIndicator loading = new ProgressIndicator();
        loading.setPrefSize(40, 40);
        VBox loadingBox = new VBox(12, loading, new Label("Memuat daftar pengajar..."));
        loadingBox.setAlignment(Pos.CENTER);
        loadingBox.setPadding(new Insets(40));
        root.getChildren().add(loadingBox);

        Long currentUserId = controller.getCurrentUser().getId();

        Thread fetchThread = new Thread(() -> {
            List<Teacher> allTeachers = enrollmentService.getAllTeachers();
            List<Long> enrolledIds = enrollmentService.getEnrolledTeacherIds(currentUserId);

            Platform.runLater(() -> {
                root.getChildren().remove(loadingBox);

                if (allTeachers.isEmpty()) {
                    Label empty = new Label("Belum ada pengajar yang terdaftar di platform ini.");
                    empty.getStyleClass().add("placeholder-text");
                    root.getChildren().add(empty);
                    return;
                }

                // Enrolled teachers section
                List<Teacher> enrolled = allTeachers.stream()
                        .filter(t -> enrolledIds.contains(t.getId())).toList();
                List<Teacher> notEnrolled = allTeachers.stream()
                        .filter(t -> !enrolledIds.contains(t.getId())).toList();

                if (!enrolled.isEmpty()) {
                    Label enrolledTitle = new Label("✅  Pengajar yang Saya Ikuti (" + enrolled.size() + ")");
                    enrolledTitle.setStyle("-fx-font-size: 15px; -fx-font-weight: bold; -fx-text-fill: #059669;");
                    root.getChildren().add(enrolledTitle);

                    FlowPane enrolledCards = new FlowPane();
                    enrolledCards.setHgap(16);
                    enrolledCards.setVgap(16);
                    enrolledCards.setMaxWidth(Double.MAX_VALUE);
                    enrolledCards.setPrefWrapLength(750);
                    for (Teacher t : enrolled) {
                        enrolledCards.getChildren().add(buildTeacherCard(t, true, currentUserId, root));
                    }
                    root.getChildren().add(enrolledCards);
                }

                if (!notEnrolled.isEmpty()) {
                    Label availableTitle = new Label("📚  Pengajar Tersedia (" + notEnrolled.size() + ")");
                    availableTitle.setStyle("-fx-font-size: 15px; -fx-font-weight: bold; -fx-text-fill: #FF7A00;");
                    VBox.setMargin(availableTitle, new Insets(12, 0, 0, 0));
                    root.getChildren().add(availableTitle);

                    FlowPane availableCards = new FlowPane();
                    availableCards.setHgap(16);
                    availableCards.setVgap(16);
                    availableCards.setMaxWidth(Double.MAX_VALUE);
                    availableCards.setPrefWrapLength(750);
                    for (Teacher t : notEnrolled) {
                        availableCards.getChildren().add(buildTeacherCard(t, false, currentUserId, root));
                    }
                    root.getChildren().add(availableCards);
                }
            });
        });
        fetchThread.setDaemon(true);
        fetchThread.start();

        return root;
    }

    private VBox buildTeacherCard(Teacher teacher, boolean isEnrolled, Long studentId, VBox rootContainer) {
        VBox card = new VBox(10);
        card.getStyleClass().add("material-card");
        card.setPadding(new Insets(18));
        card.setPrefWidth(320);
        card.setMinWidth(280);

        // Top row: avatar + name
        HBox topRow = new HBox(12);
        topRow.setAlignment(Pos.CENTER_LEFT);

        StackPane avatarPane = new StackPane();
        Circle avatarBg = new Circle(22, isEnrolled ? Color.web("#D1FAE5") : Color.web("#FFEDD5"));
        String initial = teacher.getFullName().isEmpty() ? "?" :
                String.valueOf(teacher.getFullName().charAt(0)).toUpperCase();
        Label lblInitial = new Label(initial);
        lblInitial.setStyle("-fx-font-weight: bold; -fx-font-size: 18px; -fx-text-fill: " +
                (isEnrolled ? "#059669" : "#FF7A00") + ";");
        avatarPane.getChildren().addAll(avatarBg, lblInitial);

        VBox nameBox = new VBox(2);
        Label nameLabel = new Label(teacher.getFullName().isEmpty() ? teacher.getUsername() : teacher.getFullName());
        nameLabel.getStyleClass().add("card-title");

        // Specialization badge
        Label specLabel = new Label("📖 " + teacher.getSpecialization());
        specLabel.setStyle("-fx-font-size: 11px; -fx-text-fill: #FF7A00; -fx-font-weight: bold; " +
                "-fx-background-color: #FFF7ED; -fx-padding: 2 8; -fx-background-radius: 8;");

        nameBox.getChildren().addAll(nameLabel, specLabel);
        topRow.getChildren().addAll(avatarPane, nameBox);

        Separator sep = new Separator();

        // Bio
        VBox infoBox = new VBox(6);
        if (teacher.getBio() != null && !teacher.getBio().isEmpty()) {
            Label bioLabel = new Label(teacher.getBio());
            bioLabel.setWrapText(true);
            bioLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #4B5563; -fx-font-style: italic;");
            bioLabel.setMaxHeight(60);
            infoBox.getChildren().add(bioLabel);
        }

        // Stats
        long studentCount = enrollmentService.getStudentCount(teacher.getId());
        Label statsLabel = new Label("👥 " + studentCount + " siswa terdaftar");
        statsLabel.setStyle("-fx-font-size: 11px; -fx-text-fill: #6B7280;");
        infoBox.getChildren().add(statsLabel);

        Label emailLabel = new Label("✉ " + teacher.getEmail());
        emailLabel.setStyle("-fx-font-size: 11px; -fx-text-fill: #6B7280;");
        infoBox.getChildren().add(emailLabel);

        // Action button
        Button actionBtn;
        if (isEnrolled) {
            actionBtn = new Button("Keluar dari Kelas");
            actionBtn.setStyle("-fx-background-color: #FEE2E2; -fx-text-fill: #DC2626; " +
                    "-fx-font-weight: bold; -fx-font-size: 12px; -fx-cursor: hand; " +
                    "-fx-background-radius: 8; -fx-padding: 8 16;");
            actionBtn.setOnAction(e -> {
                actionBtn.setDisable(true);
                actionBtn.setText("Memproses...");
                new Thread(() -> {
                    boolean success = enrollmentService.unenrollFromTeacher(studentId, teacher.getId());
                    Platform.runLater(() -> {
                        if (success) {
                            controller.showTeachersContent(); // refresh
                        } else {
                            actionBtn.setDisable(false);
                            actionBtn.setText("Keluar dari Kelas");
                            showAlert("Gagal keluar dari kelas pengajar.");
                        }
                    });
                }).start();
            });
        } else {
            actionBtn = new Button("Gabung Kelas");
            actionBtn.setStyle("-fx-background-color: #FF7A00; -fx-text-fill: white; " +
                    "-fx-font-weight: bold; -fx-font-size: 12px; -fx-cursor: hand; " +
                    "-fx-background-radius: 8; -fx-padding: 8 16;");
            actionBtn.setOnAction(e -> {
                actionBtn.setDisable(true);
                actionBtn.setText("Memproses...");
                new Thread(() -> {
                    boolean success = enrollmentService.enrollToTeacher(studentId, teacher.getId());
                    Platform.runLater(() -> {
                        if (success) {
                            controller.showTeachersContent(); // refresh
                        } else {
                            actionBtn.setDisable(false);
                            actionBtn.setText("Gabung Kelas");
                            showAlert("Gagal mendaftar ke kelas pengajar.");
                        }
                    });
                }).start();
            });
        }
        actionBtn.setMaxWidth(Double.MAX_VALUE);

        card.getChildren().addAll(topRow, sep, infoBox, actionBtn);
        return card;
    }

    private void showAlert(String message) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle("Peringatan");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}

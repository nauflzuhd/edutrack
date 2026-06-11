package com.doamamah.edutrack.fe.user;
import com.doamamah.edutrack.fe.enrollment.EnrollmentService;

import com.doamamah.edutrack.fe.user.Student;
import com.doamamah.edutrack.fe.user.UserService;
import com.doamamah.edutrack.fe.dashboard.DashboardController;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;

import java.util.List;

public class StudentListView {

    private final DashboardController controller;
    private final EnrollmentService enrollmentService = new EnrollmentService();

    public StudentListView(DashboardController controller) {
        this.controller = controller;
    }

    public Node buildContent() {
        VBox root = new VBox(16);
        root.setMaxWidth(Double.MAX_VALUE);

        // Header section
        HBox header = new HBox(8);
        header.setAlignment(Pos.CENTER_LEFT);
        Label sectionLabel = new Label("Daftar Siswa Terdaftar");
        sectionLabel.getStyleClass().add("section-title");
        Region sp = new Region();
        HBox.setHgrow(sp, Priority.ALWAYS);
        
        javafx.scene.control.TextField searchField = new javafx.scene.control.TextField();
        searchField.setPromptText("🔍 Cari siswa...");
        searchField.getStyleClass().add("input-field");
        searchField.setPrefWidth(220);
        
        javafx.scene.control.Button btnExport = new javafx.scene.control.Button("Export ke Excel");
        btnExport.getStyleClass().addAll("btn-secondary", "btn-small");
        btnExport.setStyle("-fx-background-color: #059669; -fx-text-fill: white;");
        btnExport.setDisable(true); // Akan di-enable setelah data dimuat
        
        header.getChildren().addAll(sectionLabel, sp, searchField, btnExport);
        root.getChildren().add(header);

        // Loading
        ProgressIndicator loading = new ProgressIndicator();
        loading.setPrefSize(40, 40);
        VBox loadingBox = new VBox(12, loading, new Label("Memuat data siswa..."));
        loadingBox.setAlignment(Pos.CENTER);
        loadingBox.setPadding(new Insets(40));
        root.getChildren().add(loadingBox);

        Thread fetchThread = new Thread(() -> {
            List<Student> fetchedStudents = new java.util.ArrayList<>();
            if (controller.getCurrentUser() instanceof com.doamamah.edutrack.fe.user.Teacher teacher) {
                fetchedStudents = enrollmentService.getEnrolledStudents(teacher.getId());
            }
            final List<Student> students = fetchedStudents;
            Platform.runLater(() -> {
                root.getChildren().remove(loadingBox);
                
                if (students.isEmpty()) {
                    Label empty = new Label("Belum ada siswa yang mendaftar.");
                    empty.getStyleClass().add("placeholder-text");
                    root.getChildren().add(empty);
                } else {
                    FlowPane cardsContainer = new FlowPane();
                    cardsContainer.setHgap(16);
                    cardsContainer.setVgap(16);
                    cardsContainer.setMaxWidth(Double.MAX_VALUE);
                    cardsContainer.setPrefWrapLength(750);

                    Label countLabel = new Label();
                    countLabel.getStyleClass().add("material-count");

                    Runnable updateStudentList = () -> {
                        cardsContainer.getChildren().clear();
                        String query = searchField.getText().toLowerCase();
                        java.util.List<Student> filtered = students.stream()
                            .filter(s -> (s.getFullName() != null && s.getFullName().toLowerCase().contains(query)) ||
                                         (s.getUsername() != null && s.getUsername().toLowerCase().contains(query)) ||
                                         (s.getEmail() != null && s.getEmail().toLowerCase().contains(query)))
                            .toList();

                        countLabel.setText("Total " + filtered.size() + " siswa");

                        if (filtered.isEmpty()) {
                            Label emptyLbl = new Label("Tidak ada siswa ditemukan.");
                            emptyLbl.setStyle("-fx-text-fill: -fx-secondary-text; -fx-font-style: italic;");
                            cardsContainer.getChildren().add(emptyLbl);
                        } else {
                            for (Student s : filtered) {
                                cardsContainer.getChildren().add(buildStudentCard(s));
                            }
                        }
                    };

                    searchField.textProperty().addListener((obs, oldVal, newVal) -> updateStudentList.run());
                    updateStudentList.run();

                    root.getChildren().add(cardsContainer);
                    
                    header.getChildren().add(countLabel);
                    
                    // Setup Export Action
                    btnExport.setDisable(false);
                    btnExport.setOnAction(e -> exportToCsv(students, btnExport.getScene().getWindow()));
                }
            });
        });
        fetchThread.setDaemon(true);
        fetchThread.start();

        return root;
    }

    private VBox buildStudentCard(Student student) {
        VBox card = new VBox(10);
        card.getStyleClass().add("material-card"); // reuse nice styling
        card.setPadding(new Insets(18));
        card.setPrefWidth(300);
        card.setMinWidth(280);

        HBox topRow = new HBox(12);
        topRow.setAlignment(Pos.CENTER_LEFT);

        // Avatar
        StackPane avatarPane = new StackPane();
        Circle avatarBg = new Circle(20, Color.web("#FFEDD5")); // light orange
        String initial = student.getFullName().isEmpty() ? "?" : String.valueOf(student.getFullName().charAt(0)).toUpperCase();
        Label lblInitial = new Label(initial);
        lblInitial.setStyle("-fx-font-weight: bold; -fx-text-fill: #FF7A00; -fx-font-size: 16px;");
        avatarPane.getChildren().addAll(avatarBg, lblInitial);

        VBox nameBox = new VBox(2);
        Label nameLabel = new Label(student.getFullName().isEmpty() ? student.getUsername() : student.getFullName());
        nameLabel.getStyleClass().add("card-title");
        
        Label usernameLabel = new Label("@" + student.getUsername());
        usernameLabel.getStyleClass().add("card-description");
        
        nameBox.getChildren().addAll(nameLabel, usernameLabel);
        topRow.getChildren().addAll(avatarPane, nameBox);

        javafx.scene.control.Separator sep = new javafx.scene.control.Separator();

        // Info
        GridPane infoGrid = new GridPane();
        infoGrid.setVgap(8);
        infoGrid.setHgap(12);
        
        Label lblEmail = new Label("Email:");
        lblEmail.setStyle("-fx-text-fill: -fx-secondary-text; -fx-font-size: 12px;");
        Label valEmail = new Label(student.getEmail());
        valEmail.setStyle("-fx-text-fill: -fx-primary-text; -fx-font-size: 12px;");
        
        Label lblId = new Label("ID Siswa:");
        lblId.setStyle("-fx-text-fill: -fx-secondary-text; -fx-font-size: 12px;");
        Label valId = new Label(student.getStudentId() != null && !student.getStudentId().isEmpty() ? student.getStudentId() : "-");
        valId.setStyle("-fx-text-fill: -fx-primary-text; -fx-font-size: 12px;");
        
        infoGrid.add(lblEmail, 0, 0);
        infoGrid.add(valEmail, 1, 0);
        infoGrid.add(lblId, 0, 1);
        infoGrid.add(valId, 1, 1);

        card.getChildren().addAll(topRow, sep, infoGrid);
        return card;
    }

    private void exportToCsv(List<Student> students, javafx.stage.Window ownerWindow) {
        javafx.stage.FileChooser fileChooser = new javafx.stage.FileChooser();
        fileChooser.setTitle("Simpan Data Siswa");
        fileChooser.getExtensionFilters().add(new javafx.stage.FileChooser.ExtensionFilter("CSV Files", "*.csv"));
        fileChooser.setInitialFileName("data_siswa.csv");
        
        java.io.File file = fileChooser.showSaveDialog(ownerWindow);
        if (file != null) {
            try (java.io.PrintWriter writer = new java.io.PrintWriter(new java.io.FileWriter(file))) {
                // Tulis Header CSV
                writer.println("ID Siswa,Nama Lengkap,Username,Email,Status");
                
                // Tulis Data
                for (Student s : students) {
                    String id = s.getStudentId() != null ? s.getStudentId() : "-";
                    String name = s.getFullName() != null && !s.getFullName().isEmpty() ? s.getFullName() : s.getUsername();
                    String username = s.getUsername() != null ? s.getUsername() : "";
                    String email = s.getEmail() != null ? s.getEmail() : "";
                    String status = "Aktif"; // default sementara
                    
                    // Gunakan double quotes untuk mencegah masalah jika ada koma di data
                    writer.printf("\"%s\",\"%s\",\"%s\",\"%s\",\"%s\"%n", id, name, username, email, status);
                }
                
                controller.showToast("Data siswa berhasil diekspor ke: " + file.getName(), "SUCCESS");
                
            } catch (java.io.IOException ex) {
                System.err.println("Gagal mengekspor data: " + ex.getMessage());
                controller.showToast("Gagal menulis file: " + ex.getMessage(), "ERROR");
            }
        }
    }
}

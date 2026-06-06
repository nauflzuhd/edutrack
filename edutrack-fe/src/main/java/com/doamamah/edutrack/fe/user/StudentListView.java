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
        header.getChildren().addAll(sectionLabel, sp);
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

                    for (Student s : students) {
                        cardsContainer.getChildren().add(buildStudentCard(s));
                    }
                    root.getChildren().add(cardsContainer);
                    
                    Label countLabel = new Label("Total " + students.size() + " siswa");
                    countLabel.getStyleClass().add("material-count");
                    header.getChildren().add(countLabel);
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
        lblEmail.setStyle("-fx-text-fill: #6B7280; -fx-font-size: 12px;");
        Label valEmail = new Label(student.getEmail());
        valEmail.setStyle("-fx-text-fill: #374151; -fx-font-size: 12px;");
        
        Label lblId = new Label("ID Siswa:");
        lblId.setStyle("-fx-text-fill: #6B7280; -fx-font-size: 12px;");
        Label valId = new Label(student.getStudentId() != null && !student.getStudentId().isEmpty() ? student.getStudentId() : "-");
        valId.setStyle("-fx-text-fill: #374151; -fx-font-size: 12px;");
        
        infoGrid.add(lblEmail, 0, 0);
        infoGrid.add(valEmail, 1, 0);
        infoGrid.add(lblId, 0, 1);
        infoGrid.add(valId, 1, 1);

        card.getChildren().addAll(topRow, sep, infoGrid);
        return card;
    }
}

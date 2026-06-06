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

/**
 * View untuk menampilkan dan mengedit profil pengguna.
 * Siswa: edit fullName, bio
 * Pengajar: edit fullName, bio, spesialisasi
 */
public class ProfileView {

    private final DashboardController controller;
    private final EnrollmentService enrollmentService = new EnrollmentService();

    public ProfileView(DashboardController controller) {
        this.controller = controller;
    }

    public Node buildContent() {
        User currentUser = controller.getCurrentUser();

        VBox root = new VBox(20);
        root.setMaxWidth(Double.MAX_VALUE);

        // Header
        Label sectionLabel = new Label("Profil Saya");
        sectionLabel.getStyleClass().add("section-title");
        root.getChildren().add(sectionLabel);

        // Main layout: profile card + edit form side by side
        HBox mainLayout = new HBox(24);
        mainLayout.setMaxWidth(Double.MAX_VALUE);

        // Left: Profile Card
        VBox profileCard = new VBox(16);
        profileCard.getStyleClass().add("material-card");
        profileCard.setPadding(new Insets(28));
        profileCard.setPrefWidth(340);
        profileCard.setMinWidth(300);
        profileCard.setAlignment(Pos.CENTER);

        // Avatar
        StackPane avatarPane = new StackPane();
        Circle avatarBg = new Circle(40, currentUser.getRole().equals("TEACHER")
                ? Color.web("#DBEAFE") : Color.web("#FFEDD5"));
        String initial = (currentUser.getFullName() != null && !currentUser.getFullName().isEmpty())
                ? String.valueOf(currentUser.getFullName().charAt(0)).toUpperCase() : "?";
        Label avatarLabel = new Label(initial);
        avatarLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 32px; -fx-text-fill: " +
                (currentUser.getRole().equals("TEACHER") ? "#2563EB" : "#FF7A00") + ";");
        avatarPane.getChildren().addAll(avatarBg, avatarLabel);

        // Name
        Label nameLabel = new Label(currentUser.getFullName() != null ? currentUser.getFullName() : currentUser.getUsername());
        nameLabel.setStyle("-fx-font-size: 20px; -fx-font-weight: bold; -fx-text-fill: #1F2937;");

        // Role badge
        Label roleLabel = new Label(currentUser instanceof Teacher ? "👨‍🏫 Pengajar" : "🎓 Siswa");
        roleLabel.setStyle("-fx-font-size: 13px; -fx-font-weight: bold; -fx-text-fill: white; " +
                "-fx-background-color: " + (currentUser.getRole().equals("TEACHER") ? "#2563EB" : "#FF7A00") +
                "; -fx-padding: 4 14; -fx-background-radius: 12;");

        Separator sep1 = new Separator();

        // Info grid
        GridPane infoGrid = new GridPane();
        infoGrid.setHgap(12);
        infoGrid.setVgap(10);

        addInfoRow(infoGrid, 0, "Username", "@" + currentUser.getUsername());
        addInfoRow(infoGrid, 1, "Email", currentUser.getEmail() != null ? currentUser.getEmail() : "-");

        if (currentUser instanceof Teacher teacher) {
            addInfoRow(infoGrid, 2, "Spesialisasi", teacher.getSpecialization() != null ? teacher.getSpecialization() : "Umum");
            addInfoRow(infoGrid, 3, "ID Pengajar", teacher.getTeacherId() != null ? teacher.getTeacherId() : "-");
        } else if (currentUser instanceof Student student) {
            addInfoRow(infoGrid, 2, "ID Siswa", student.getStudentId() != null ? student.getStudentId() : "-");
        }

        // Bio
        VBox bioSection = new VBox(4);
        Label bioTitle = new Label("Bio");
        bioTitle.setStyle("-fx-font-size: 12px; -fx-text-fill: #6B7280; -fx-font-weight: bold;");
        Label bioLabel = new Label(currentUser.getBio() != null && !currentUser.getBio().isEmpty()
                ? currentUser.getBio() : "Belum ada bio.");
        bioLabel.setWrapText(true);
        bioLabel.setStyle("-fx-font-size: 13px; -fx-text-fill: #374151; -fx-font-style: italic;");
        bioSection.getChildren().addAll(bioTitle, bioLabel);

        profileCard.getChildren().addAll(avatarPane, nameLabel, roleLabel, sep1, infoGrid, bioSection);

        // Right: Edit Form
        VBox editCard = new VBox(16);
        editCard.getStyleClass().add("material-card");
        editCard.setPadding(new Insets(24));
        editCard.setPrefWidth(380);
        editCard.setMinWidth(320);
        HBox.setHgrow(editCard, Priority.ALWAYS);

        Label editTitle = new Label("✏️  Edit Profil");
        editTitle.setStyle("-fx-font-size: 17px; -fx-font-weight: bold; -fx-text-fill: #1F2937;");

        // Full Name field
        VBox fullNameBox = new VBox(6);
        Label fullNameLabel = new Label("Nama Lengkap");
        fullNameLabel.getStyleClass().add("input-label");
        TextField fullNameField = new TextField(currentUser.getFullName() != null ? currentUser.getFullName() : "");
        fullNameField.getStyleClass().add("input-field");
        fullNameField.setMaxWidth(Double.MAX_VALUE);
        fullNameBox.getChildren().addAll(fullNameLabel, fullNameField);

        // Bio field
        VBox bioEditBox = new VBox(6);
        Label bioEditLabel = new Label("Bio");
        bioEditLabel.getStyleClass().add("input-label");
        TextArea bioField = new TextArea(currentUser.getBio() != null ? currentUser.getBio() : "");
        bioField.setWrapText(true);
        bioField.setPrefRowCount(3);
        bioField.setMaxWidth(Double.MAX_VALUE);
        bioField.setStyle("-fx-font-size: 13px; -fx-background-radius: 8; -fx-border-radius: 8; " +
                "-fx-border-color: #D1D5DB; -fx-padding: 8;");
        bioEditBox.getChildren().addAll(bioEditLabel, bioField);

        // Specialization field (only for teachers)
        VBox specBox = new VBox(6);
        TextField specField = null;
        if (currentUser instanceof Teacher teacher) {
            Label specLabel = new Label("Spesialisasi / Mata Kuliah");
            specLabel.getStyleClass().add("input-label");
            specField = new TextField(teacher.getSpecialization() != null ? teacher.getSpecialization() : "");
            specField.getStyleClass().add("input-field");
            specField.setMaxWidth(Double.MAX_VALUE);
            specField.setPromptText("Contoh: Pemrograman Berorientasi Objek");
            specBox.getChildren().addAll(specLabel, specField);
        }

        // Error/success message
        Label messageLabel = new Label();
        messageLabel.setWrapText(true);
        messageLabel.setVisible(false);
        messageLabel.setManaged(false);

        // Save button
        Button saveBtn = new Button("💾  Simpan Perubahan");
        saveBtn.setStyle("-fx-background-color: #FF7A00; -fx-text-fill: white; -fx-font-weight: bold; " +
                "-fx-font-size: 14px; -fx-cursor: hand; -fx-background-radius: 8; -fx-padding: 10 24;");
        saveBtn.setMaxWidth(Double.MAX_VALUE);

        final TextField finalSpecField = specField;
        saveBtn.setOnAction(e -> {
            saveBtn.setDisable(true);
            saveBtn.setText("Menyimpan...");
            messageLabel.setVisible(false);
            messageLabel.setManaged(false);

            String newFullName = fullNameField.getText().trim();
            String newBio = bioField.getText().trim();
            String newSpec = finalSpecField != null ? finalSpecField.getText().trim() : null;

            new Thread(() -> {
                boolean success = enrollmentService.updateProfile(
                        currentUser.getId(), newFullName, newBio, newSpec);
                Platform.runLater(() -> {
                    saveBtn.setDisable(false);
                    saveBtn.setText("💾  Simpan Perubahan");
                    if (success) {
                        // Update local user object
                        currentUser.setFullName(newFullName);
                        currentUser.setBio(newBio);
                        if (currentUser instanceof Teacher teacher && newSpec != null) {
                            teacher.setSpecialization(newSpec);
                        }
                        messageLabel.setText("✅ Profil berhasil diperbarui!");
                        messageLabel.setStyle("-fx-text-fill: #059669; -fx-font-size: 13px; -fx-font-weight: bold;");
                        messageLabel.setVisible(true);
                        messageLabel.setManaged(true);

                        // Refresh profile card
                        controller.showProfileContent();
                    } else {
                        messageLabel.setText("❌ Gagal menyimpan profil. Periksa koneksi Anda.");
                        messageLabel.setStyle("-fx-text-fill: #DC2626; -fx-font-size: 13px; -fx-font-weight: bold;");
                        messageLabel.setVisible(true);
                        messageLabel.setManaged(true);
                    }
                });
            }).start();
        });

        editCard.getChildren().addAll(editTitle, fullNameBox, bioEditBox);
        if (currentUser instanceof Teacher) {
            editCard.getChildren().add(specBox);
        }
        editCard.getChildren().addAll(messageLabel, saveBtn);

        mainLayout.getChildren().addAll(profileCard, editCard);
        root.getChildren().add(mainLayout);

        return root;
    }

    private void addInfoRow(GridPane grid, int row, String label, String value) {
        Label lbl = new Label(label);
        lbl.setStyle("-fx-font-size: 12px; -fx-text-fill: #6B7280; -fx-font-weight: bold;");
        Label val = new Label(value);
        val.setStyle("-fx-font-size: 13px; -fx-text-fill: #374151;");
        grid.add(lbl, 0, row);
        grid.add(val, 1, row);
    }
}

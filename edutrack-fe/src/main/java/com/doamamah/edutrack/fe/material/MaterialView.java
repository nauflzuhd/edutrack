package com.doamamah.edutrack.fe.material;

import com.doamamah.edutrack.fe.material.CourseMaterial;
import com.doamamah.edutrack.fe.material.TextMaterial;
import com.doamamah.edutrack.fe.material.VideoMaterial;
import com.doamamah.edutrack.fe.user.Teacher;
import com.doamamah.edutrack.fe.user.User;
import com.doamamah.edutrack.fe.dashboard.DashboardController;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;

import java.util.List;

public class MaterialView {

    private final DashboardController controller;

    public MaterialView(DashboardController controller) {
        this.controller = controller;
    }

    public Node buildListContent(List<CourseMaterial> materials) {
        User currentUser = controller.getCurrentUser();
        if (currentUser instanceof Teacher) {
            return buildTeacherMaterialContent(materials);
        } else {
            return buildStudentMaterialContent(materials);
        }
    }

    private Node buildStudentMaterialContent(List<CourseMaterial> materials) {
        HBox mainLayout = new HBox(20);
        mainLayout.setMaxWidth(Double.MAX_VALUE);

        VBox leftColumn = new VBox(14);
        leftColumn.setMaxWidth(Double.MAX_VALUE);
        HBox.setHgrow(leftColumn, Priority.ALWAYS);

        if (materials.isEmpty()) {
            Label emptyLabel = new Label("Tidak ada materi yang tersedia.");
            emptyLabel.getStyleClass().add("placeholder-text");
            leftColumn.getChildren().add(emptyLabel);
        } else {
            // Header section
            HBox header = new HBox(12);
            header.setAlignment(Pos.CENTER_LEFT);
            Label sectionLabel = new Label("Daftar Materi Tersedia");
            sectionLabel.getStyleClass().add("section-title");
            
            TextField searchField = new TextField();
            searchField.setPromptText("🔍 Cari materi...");
            searchField.getStyleClass().add("input-field");
            searchField.setPrefWidth(220);
            
            Region sp = new Region();
            HBox.setHgrow(sp, Priority.ALWAYS);
            Label countLabel = new Label(materials.size() + " materi");
            countLabel.getStyleClass().add("material-count");
            header.getChildren().addAll(sectionLabel, sp, searchField, countLabel);
            leftColumn.getChildren().add(header);

            // FlowPane for responsive wrapping columns
            FlowPane cardsContainer = new FlowPane();
            cardsContainer.setHgap(16);
            cardsContainer.setVgap(16);
            cardsContainer.setMaxWidth(Double.MAX_VALUE);
            cardsContainer.setPrefWrapLength(750);

            Runnable updateStudentList = () -> {
                cardsContainer.getChildren().clear();
                String query = searchField.getText().toLowerCase();
                java.util.List<CourseMaterial> filtered = materials.stream()
                    .filter(m -> m.getTitle().toLowerCase().contains(query) || (m.getDescription() != null && m.getDescription().toLowerCase().contains(query)))
                    .toList();

                if (filtered.isEmpty()) {
                    Label emptyLbl = new Label("Tidak ada materi ditemukan.");
                    emptyLbl.setStyle("-fx-text-fill: -fx-secondary-text; -fx-font-style: italic;");
                    cardsContainer.getChildren().add(emptyLbl);
                } else {
                    for (CourseMaterial material : filtered) {
                        VBox card = buildMaterialCard(material);
                        card.setPrefWidth(340);
                        card.setMinWidth(300);
                        cardsContainer.getChildren().add(card);
                    }
                }
            };

            searchField.textProperty().addListener((obs, oldVal, newVal) -> updateStudentList.run());
            updateStudentList.run();

            leftColumn.getChildren().add(cardsContainer);
        }

        // Right side: Stats Panel
        VBox rightColumn = new VBox(16);
        rightColumn.setPadding(new Insets(20));
        rightColumn.getStyleClass().add("section-box");
        rightColumn.setPrefWidth(320);
        rightColumn.setMaxWidth(320);

        Label statsTitle = new Label("Ringkasan Materi");
        statsTitle.getStyleClass().add("section-title");
        statsTitle.setStyle("-fx-font-size: 15px;");

        long videoCount = materials.stream().filter(m -> "VIDEO".equals(m.getMaterialType())).count();
        long textCount = materials.stream().filter(m -> "TEXT".equals(m.getMaterialType())).count();

        VBox statBox1 = buildMiniStatRow("Total Materi", String.valueOf(materials.size()), "modul", "#FF7A00");
        VBox statBox2 = buildMiniStatRow("Materi Video", String.valueOf(videoCount), "video", "#059669");
        VBox statBox3 = buildMiniStatRow("Materi Teks", String.valueOf(textCount), "artikel", "#D97706");

        // Illustration
        VBox illusBox = new VBox(16);
        illusBox.setAlignment(Pos.CENTER);
        try {
            javafx.scene.image.ImageView matImg = new javafx.scene.image.ImageView(
                new javafx.scene.image.Image(getClass().getResourceAsStream("/com/doamamah/edutrack/fe/images/mascot_face.png"))
            );
            matImg.setFitWidth(120);
            matImg.setPreserveRatio(true);
            matImg.setSmooth(true);

            Label promoTitle = new Label("Tetap Semangat!");
            promoTitle.getStyleClass().add("section-title");
            promoTitle.setStyle("-fx-font-size: 15px;");
            Label promoText = new Label("Pelajari semua materi yang ada untuk meningkatkan pemahamanmu. Jangan lupa kerjakan kuis setelah belajar!");
            promoText.getStyleClass().add("card-description");
            promoText.setWrapText(true);
            promoText.setAlignment(Pos.CENTER);

            illusBox.getChildren().addAll(matImg, promoTitle, promoText);
        } catch (Exception e) {}

        rightColumn.getChildren().addAll(statsTitle, statBox1, statBox2, statBox3, illusBox);

        mainLayout.getChildren().addAll(leftColumn, rightColumn);
        return mainLayout;
    }

    private Node buildTeacherMaterialContent(List<CourseMaterial> materials) {
        HBox mainLayout = new HBox(20);
        mainLayout.setMaxWidth(Double.MAX_VALUE);

        VBox leftColumn = new VBox(20);
        leftColumn.setMaxWidth(Double.MAX_VALUE);
        HBox.setHgrow(leftColumn, Priority.ALWAYS);

        // Header with "Buat Materi Baru" button
        HBox headerBox = new HBox(12);
        headerBox.setAlignment(Pos.CENTER_LEFT);
        headerBox.setPadding(new Insets(0, 0, 10, 0));

        VBox titleBox = new VBox(4);
        HBox titleRow = new HBox(10);
        titleRow.setAlignment(Pos.CENTER_LEFT);
        Label matTitle = new Label("Kelola Materi Pembelajaran");
        matTitle.getStyleClass().add("section-title");
        matTitle.setStyle("-fx-font-size: 20px;");
        titleRow.getChildren().add(matTitle);
        Label matSub = new Label("Tambahkan materi baru dan pantau jumlah materi e-learning.");
        matSub.getStyleClass().add("card-description");
        titleBox.getChildren().addAll(titleRow, matSub);
        HBox.setHgrow(titleBox, Priority.ALWAYS);

        Button btnCreate = new Button();
        btnCreate.getStyleClass().add("btn-create-quiz-round");

        javafx.scene.shape.SVGPath plusIcon = new javafx.scene.shape.SVGPath();
        plusIcon.setContent("M19 13h-6v6h-2v-6H5v-2h6V5h2v6h6v2z");
        plusIcon.setStyle("-fx-fill: #FFFFFF;");

        btnCreate.setGraphic(plusIcon);
        btnCreate.setTooltip(new Tooltip("Buat Materi Baru"));

        btnCreate.setStyle(
            "-fx-background-color: #059669; " +
            "-fx-background-radius: 50%; " +
            "-fx-min-width: 40px; -fx-min-height: 40px; " +
            "-fx-max-width: 40px; -fx-max-height: 40px; " +
            "-fx-cursor: hand; " +
            "-fx-effect: dropshadow(gaussian, rgba(5, 150, 105, 0.2), 6, 0, 0, 2);"
        );

        btnCreate.setOnMouseEntered(e -> btnCreate.setStyle(
            "-fx-background-color: #047857; " +
            "-fx-background-radius: 50%; " +
            "-fx-min-width: 40px; -fx-min-height: 40px; " +
            "-fx-max-width: 40px; -fx-max-height: 40px; " +
            "-fx-cursor: hand; " +
            "-fx-effect: dropshadow(gaussian, rgba(4, 120, 87, 0.4), 8, 0, 0, 3);"
        ));

        btnCreate.setOnMouseExited(e -> btnCreate.setStyle(
            "-fx-background-color: #059669; " +
            "-fx-background-radius: 50%; " +
            "-fx-min-width: 40px; -fx-min-height: 40px; " +
            "-fx-max-width: 40px; -fx-max-height: 40px; " +
            "-fx-cursor: hand; " +
            "-fx-effect: dropshadow(gaussian, rgba(5, 150, 105, 0.2), 6, 0, 0, 2);"
        ));

        btnCreate.setOnAction(e -> {
            controller.getContentArea().getChildren().clear();
            controller.getContentArea().getChildren().add(buildForm(null));
            controller.getContentTitleLabel().setText("Tambah Materi");
        });

        headerBox.getChildren().addAll(titleBox, btnCreate);

        // Daftar Materi Tersedia
        HBox listHeader = new HBox(12);
        listHeader.setAlignment(Pos.CENTER_LEFT);
        Label matListTitle = new Label("Daftar Materi Tersedia");
        matListTitle.setStyle("-fx-font-weight: bold; -fx-text-fill: -fx-primary-text; -fx-font-size: 15px;");
        
        TextField searchField = new TextField();
        searchField.setPromptText("🔍 Cari materi...");
        searchField.getStyleClass().add("input-field");
        searchField.setPrefWidth(220);
        
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        
        listHeader.getChildren().addAll(matListTitle, spacer, searchField);

        VBox matListContainer = new VBox(10);
        matListContainer.setMaxWidth(Double.MAX_VALUE);

        Runnable updateTeacherList = () -> {
            matListContainer.getChildren().clear();
            String query = searchField.getText().toLowerCase();
            java.util.List<CourseMaterial> filtered = materials.stream()
                .filter(m -> m.getTitle().toLowerCase().contains(query) || (m.getDescription() != null && m.getDescription().toLowerCase().contains(query)))
                .toList();

            if (filtered.isEmpty()) {
                Label emptyLbl = new Label("Tidak ada materi ditemukan.");
                emptyLbl.setStyle("-fx-text-fill: -fx-secondary-text; -fx-font-style: italic;");
                matListContainer.getChildren().add(emptyLbl);
            } else {
                for (CourseMaterial m : filtered) {
                    matListContainer.getChildren().add(buildTeacherMaterialRow(m));
                }
            }
        };

        searchField.textProperty().addListener((obs, oldVal, newVal) -> updateTeacherList.run());
        updateTeacherList.run();

        leftColumn.getChildren().addAll(headerBox, listHeader, matListContainer);

        // Right side: Stats
        VBox rightColumn = new VBox(16);
        rightColumn.setPadding(new Insets(20));
        rightColumn.getStyleClass().add("section-box");
        rightColumn.setPrefWidth(320);
        rightColumn.setMaxWidth(320);

        Label statsTitle = new Label("Statistik Kelas");
        statsTitle.getStyleClass().add("section-title");
        statsTitle.setStyle("-fx-font-size: 15px;");

        com.doamamah.edutrack.fe.enrollment.EnrollmentService enrollmentService = new com.doamamah.edutrack.fe.enrollment.EnrollmentService();
        int totalStudents = enrollmentService.getEnrolledStudents(controller.getCurrentUser().getId()).size();

        long videoCount = materials.stream().filter(m -> "VIDEO".equals(m.getMaterialType())).count();
        long textCount = materials.stream().filter(m -> "TEXT".equals(m.getMaterialType())).count();

        rightColumn.getChildren().addAll(
            statsTitle,
            buildMiniStatRow("Total Materi", String.valueOf(materials.size()), "modul", "#FF7A00"),
            buildMiniStatRow("Materi Video", String.valueOf(videoCount), "video", "#059669"),
            buildMiniStatRow("Materi Teks", String.valueOf(textCount), "artikel", "#D97706"),
            buildMiniStatRow("Total Siswa Terdaftar", String.valueOf(totalStudents), "siswa", "#3B82F6")
        );

        mainLayout.getChildren().addAll(leftColumn, rightColumn);
        return mainLayout;
    }

    private HBox buildTeacherMaterialRow(CourseMaterial m) {
        HBox row = new HBox(16);
        row.setAlignment(Pos.CENTER_LEFT);
        row.getStyleClass().add("material-card");
        row.setPadding(new Insets(12, 16, 12, 16));

        boolean isVideo = "VIDEO".equals(m.getMaterialType());
        Label icon = new Label(isVideo ? "▶\uFE0F" : "\uD83D\uDCC4");
        icon.setStyle("-fx-font-size: 18px;");

        VBox matInfo = new VBox(4);
        Label titleLbl = new Label(m.getTitle());
        titleLbl.setStyle("-fx-font-weight: bold; -fx-text-fill: -fx-primary-text; -fx-font-size: 14px;");
        Label descLbl = new Label(isVideo ? "Materi Video" : "Materi Teks");
        descLbl.setStyle("-fx-text-fill: -fx-secondary-text; -fx-font-size: 12px;");
        matInfo.getChildren().addAll(titleLbl, descLbl);
        HBox.setHgrow(matInfo, Priority.ALWAYS);

        Button btnEdit = new Button("Edit");
        btnEdit.getStyleClass().addAll("btn-secondary", "btn-small");
        btnEdit.setStyle("-fx-background-color: #D97706; -fx-text-fill: white;");
        btnEdit.setOnAction(e -> {
            controller.getContentArea().getChildren().clear();
            controller.getContentArea().getChildren().add(buildForm(m));
            controller.getContentTitleLabel().setText("Edit Materi: " + m.getTitle());
        });

        Button btnDelete = new Button("Hapus");
        btnDelete.getStyleClass().addAll("btn-ghost", "btn-small");
        btnDelete.setStyle("-fx-text-fill: #DC2626; -fx-border-color: #DC2626; -fx-border-radius: 4px;");
        btnDelete.setOnAction(e -> {
            ButtonType response = controller.showCustomAlert(
                Alert.AlertType.CONFIRMATION,
                "Konfirmasi Hapus",
                "Hapus Materi?",
                "Apakah Anda yakin ingin menghapus materi '" + m.getTitle() + "'?"
            );
            if (response == ButtonType.OK) {
                controller.getMaterialService().deleteMaterial(m.getId());
                controller.showMaterialsContent();
            }
        });

        row.getChildren().addAll(icon, matInfo, btnEdit, btnDelete);
        return row;
    }

    private VBox buildMiniStatRow(String label, String value, String unit, String color) {
        VBox box = new VBox(6);
        box.setPadding(new Insets(10, 14, 10, 14));
        box.getStyleClass().add("left-stat-box");

        Label lbl = new Label(label);
        lbl.setStyle("-fx-font-size: 11px; -fx-text-fill: -fx-secondary-text; -fx-font-weight: bold;");

        HBox valBox = new HBox(4);
        valBox.setAlignment(Pos.BASELINE_LEFT);
        Label val = new Label(value);
        val.setStyle("-fx-font-size: 20px; -fx-font-weight: bold; -fx-text-fill: " + color + ";");
        Label ut = new Label(unit);
        ut.setStyle("-fx-font-size: 12px; -fx-text-fill: -fx-secondary-text;");
        valBox.getChildren().addAll(val, ut);

        box.getChildren().addAll(lbl, valBox);
        return box;
    }

    private VBox buildMaterialCard(CourseMaterial material) {
        VBox card = new VBox(10);
        card.getStyleClass().add("material-card");
        card.setPadding(new Insets(18));

        // Top row: badge + actions
        HBox topRow = new HBox(8);
        topRow.setAlignment(Pos.CENTER_LEFT);

        boolean isVideo = "VIDEO".equals(material.getMaterialType());
        Label typeBadge = new Label(isVideo ? "Video" : "Teks");
        typeBadge.getStyleClass().addAll("badge", isVideo ? "badge-video" : "badge-text");

        // Colored dot
        Circle dot = new Circle(4);
        dot.setFill(Color.web(isVideo ? "#FF7A00" : "#059669"));

        Label typeDetail = new Label(isVideo ? "Materi Video" : "Materi Teks");
        typeDetail.getStyleClass().add("card-type-detail");

        Region sp = new Region();
        HBox.setHgrow(sp, Priority.ALWAYS);

        topRow.getChildren().addAll(typeBadge, dot, typeDetail, sp);

        // Title
        Label titleLabel = new Label(material.getTitle());
        titleLabel.getStyleClass().add("card-title");

        // Description
        Label descLabel = new Label(material.getDescription());
        descLabel.getStyleClass().add("card-description");
        descLabel.setWrapText(true);

        // Teacher badge (show who created this material)
        HBox teacherRow = new HBox(6);
        teacherRow.setAlignment(Pos.CENTER_LEFT);
        if (material.getTeacherName() != null && !material.getTeacherName().isEmpty()) {
            Label teacherBadge = new Label("👨‍🏫 " + material.getTeacherName());
            teacherBadge.setStyle("-fx-font-size: 11px; -fx-text-fill: #2563EB; -fx-font-weight: bold; " +
                    "-fx-background-color: #DBEAFE; -fx-padding: 2 8; -fx-background-radius: 8;");
            teacherRow.getChildren().add(teacherBadge);
        }

        // Divider
        javafx.scene.control.Separator divider = new javafx.scene.control.Separator();

        // Button row
        HBox actions = new HBox(8);
        actions.setAlignment(Pos.CENTER_RIGHT);

        Button viewBtn = new Button("Lihat Materi");
        viewBtn.getStyleClass().addAll("btn-primary", "btn-small");
        viewBtn.setOnAction(e -> {
            Node materialUI = material.getUIComponent();
            if (materialUI instanceof Pane pane) {
                HBox backWrapper = new HBox(buildBackButton());
                backWrapper.setAlignment(Pos.CENTER_LEFT);
                backWrapper.setMaxWidth(Double.MAX_VALUE);
                backWrapper.setPadding(new Insets(0, 0, 10, 0));
                pane.getChildren().add(0, backWrapper);
            }
            controller.getContentArea().getChildren().clear();
            controller.getContentArea().getChildren().add(materialUI);
            controller.getContentTitleLabel().setText(material.getTitle());

            User currentUser = controller.getCurrentUser();
            if (currentUser instanceof com.doamamah.edutrack.fe.user.Student) {
                controller.getMaterialService().markAsViewed(material.getId(), currentUser.getId());
            }
        });

        actions.getChildren().add(viewBtn);

        User currentUser = controller.getCurrentUser();
        if (currentUser instanceof Teacher) {
            Button editBtn = new Button("Edit");
            editBtn.getStyleClass().addAll("btn-secondary", "btn-small");
            editBtn.setStyle("-fx-background-color: #D97706; -fx-text-fill: white;");
            editBtn.setOnAction(e -> {
                controller.getContentArea().getChildren().clear();
                controller.getContentArea().getChildren().add(buildForm(material));
                controller.getContentTitleLabel().setText("Edit Materi: " + material.getTitle());
            });

            Button deleteBtn = new Button("Hapus");
            deleteBtn.getStyleClass().addAll("btn-ghost", "btn-small");
            deleteBtn.setStyle("-fx-text-fill: #DC2626; -fx-border-color: #DC2626; -fx-border-radius: 4px;");
            deleteBtn.setOnAction(e -> {
                ButtonType response = controller.showCustomAlert(
                    Alert.AlertType.CONFIRMATION,
                    "Konfirmasi Hapus",
                    "Hapus Materi?",
                    "Apakah Anda yakin ingin menghapus materi '" + material.getTitle() + "'?"
                );
                if (response == ButtonType.OK) {
                    controller.getMaterialService().deleteMaterial(material.getId());
                    controller.showMaterialsContent();
                }
            });

            actions.getChildren().addAll(editBtn, deleteBtn);
        }

        card.getChildren().addAll(topRow, titleLabel, descLabel, teacherRow, divider, actions);
        return card;
    }

    private Button buildBackButton() {
        Button backBtn = new Button();
        backBtn.getStyleClass().add("btn-back-round");

        // Custom SVG back arrow icon (attractive layout)
        javafx.scene.shape.SVGPath arrow = new javafx.scene.shape.SVGPath();
        arrow.setContent("M20 11H7.83l5.59-5.59L12 4l-8 8 8 8 1.41-1.41L7.83 13H20v-2z");
        arrow.setStyle("-fx-fill: #FF7A00;");

        backBtn.setGraphic(arrow);
        backBtn.setTooltip(new Tooltip("Kembali ke Daftar Materi"));

        // Style the button as a premium circular button
        backBtn.setStyle(
            "-fx-background-color: #FFFFFF; " +
            "-fx-background-radius: 20px; " +
            "-fx-border-color: #E5E0D8; " +
            "-fx-border-radius: 20px; " +
            "-fx-border-width: 1.5px; " +
            "-fx-padding: 8px; " +
            "-fx-cursor: hand; " +
            "-fx-effect: dropshadow(gaussian, rgba(0, 0, 0, 0.05), 4, 0, 0, 1);"
        );

        // Add dynamic micro-animations/hover changes
        backBtn.setOnMouseEntered(e -> {
            backBtn.setStyle(
                "-fx-background-color: #FFF0E0; " +
                "-fx-background-radius: 20px; " +
                "-fx-border-color: #FF7A00; " +
                "-fx-border-radius: 20px; " +
                "-fx-border-width: 1.5px; " +
                "-fx-padding: 8px; " +
                "-fx-cursor: hand; " +
                "-fx-effect: dropshadow(gaussian, rgba(255, 122, 0, 0.15), 6, 0, 0, 2);"
            );
            arrow.setStyle("-fx-fill: #E66E00;");
        });
        backBtn.setOnMouseExited(e -> {
            backBtn.setStyle(
                "-fx-background-color: #FFFFFF; " +
                "-fx-background-radius: 20px; " +
                "-fx-border-color: #E5E0D8; " +
                "-fx-border-radius: 20px; " +
                "-fx-border-width: 1.5px; " +
                "-fx-padding: 8px; " +
                "-fx-cursor: hand; " +
                "-fx-effect: dropshadow(gaussian, rgba(0, 0, 0, 0.05), 4, 0, 0, 1);"
            );
            arrow.setStyle("-fx-fill: #FF7A00;");
        });

        backBtn.setOnAction(e -> controller.showMaterialsContent());
        return backBtn;
    }

    public Node buildForm(CourseMaterial materialToEdit) {
        boolean isEdit = (materialToEdit != null);
        VBox root = new VBox(20);
        root.setMaxWidth(Double.MAX_VALUE);

        // Header Box
        VBox headerBox = new VBox(6);
        headerBox.getStyleClass().add("section-box");
        headerBox.setPadding(new Insets(20));

        Label iconLabel = new Label("✨");
        iconLabel.setStyle("-fx-font-size: 32px;");
        Label titleLabel = new Label(isEdit ? "Edit Materi E-Learning" : "Buat Materi E-Learning Baru");
        titleLabel.getStyleClass().add("section-title");
        Label subLabel = new Label(isEdit ? "Ubah formulir di bawah ini untuk memperbarui materi pembelajaran." : "Lengkapi formulir di bawah ini untuk membagikan materi baru bagi siswa.");
        subLabel.getStyleClass().add("card-description");
        headerBox.getChildren().addAll(iconLabel, titleLabel, subLabel);

        // Form Card
        VBox formCard = new VBox(16);
        formCard.getStyleClass().add("material-card");
        formCard.setPadding(new Insets(24));

        // Judul Input
        TextField txtTitle = new TextField(isEdit ? materialToEdit.getTitle() : "");
        txtTitle.setPromptText("Contoh: Pengenalan Array di Java");
        VBox titleBox = controller.createInputField("Judul Materi", txtTitle);

        // Deskripsi Input
        TextArea txtDesc = new TextArea(isEdit ? materialToEdit.getDescription() : "");
        txtDesc.setPrefHeight(70);
        txtDesc.setWrapText(true);
        txtDesc.setPromptText("Tuliskan deskripsi singkat mengenai materi ini...");
        VBox descBox = controller.createInputField("Deskripsi Singkat", txtDesc);

        // Tipe Materi ComboBox
        ComboBox<String> cmbType = new ComboBox<>();
        cmbType.getItems().addAll("Video", "Teks");
        boolean isVideo = !isEdit || "VIDEO".equals(materialToEdit.getMaterialType());
        cmbType.setValue(isVideo ? "Video" : "Teks");
        cmbType.setMaxWidth(Double.MAX_VALUE);
        if (isEdit) {
            cmbType.setDisable(true);
        }
        VBox typeBox = controller.createInputField("Tipe Materi", cmbType);
        
        // File Attachment (Optional)
        VBox fileBox = new VBox(8);
        Label fileLabel = new Label("Lampiran File (Opsional)");
        fileLabel.getStyleClass().add("input-label");
        HBox fileInputBox = new HBox(10);
        fileInputBox.setAlignment(Pos.CENTER_LEFT);
        Button btnChooseFile = new Button("Pilih File");
        btnChooseFile.getStyleClass().addAll("btn-secondary", "btn-small");
        Label lblSelectedFile = new Label(isEdit && materialToEdit.getAttachmentFileName() != null ? materialToEdit.getAttachmentFileName() : "Belum ada file yang dipilih");
        lblSelectedFile.setStyle("-fx-text-fill: -fx-secondary-text; -fx-font-style: italic;");
        
        // Variable to hold the selected file
        java.io.File[] selectedFileHolder = new java.io.File[1];
        
        btnChooseFile.setOnAction(e -> {
            javafx.stage.FileChooser fileChooser = new javafx.stage.FileChooser();
            fileChooser.setTitle("Pilih File Lampiran");
            java.io.File file = fileChooser.showOpenDialog(btnChooseFile.getScene().getWindow());
            if (file != null) {
                selectedFileHolder[0] = file;
                lblSelectedFile.setText(file.getName());
                lblSelectedFile.setStyle("-fx-text-fill: -fx-primary-text;");
            }
        });
        
        fileInputBox.getChildren().addAll(btnChooseFile, lblSelectedFile);
        fileBox.getChildren().addAll(fileLabel, fileInputBox);

        // Dynamic Form Fields Container
        VBox dynamicContainer = new VBox(16);

        // Dynamic Sub-Form Video
        VBox videoForm = new VBox(16);
        String initialUrl = "";
        String initialDuration = "";
        if (isEdit && materialToEdit instanceof VideoMaterial vm) {
            initialUrl = vm.getVideoUrl();
            initialDuration = String.valueOf(vm.getDurationMinutes());
        }
        TextField txtUrl = new TextField(initialUrl);
        txtUrl.setPromptText("Contoh: https://www.youtube.com/watch?v=pTB0EiLXUC8");
        VBox urlBox = controller.createInputField("URL Video YouTube", txtUrl);

        TextField txtDuration = new TextField(initialDuration);
        txtDuration.setPromptText("Contoh: 15");
        VBox durationBox = controller.createInputField("Durasi (Menit)", txtDuration);
        videoForm.getChildren().addAll(urlBox, durationBox);

        // Dynamic Sub-Form Teks
        VBox textForm = new VBox(16);
        String initialContent = "";
        if (isEdit && materialToEdit instanceof TextMaterial tm) {
            initialContent = tm.getTextContent();
        }
        javafx.scene.web.HTMLEditor txtContent = new javafx.scene.web.HTMLEditor();
        txtContent.setHtmlText(initialContent);
        txtContent.setPrefHeight(300);
        VBox contentBox = controller.createInputField("Konten Teks Materi (Rich Text)", txtContent);
        textForm.getChildren().add(contentBox);

        // Set default dynamic form
        dynamicContainer.getChildren().add(isVideo ? videoForm : textForm);

        // Listener to change dynamic fields on ComboBox change
        if (!isEdit) {
            cmbType.valueProperty().addListener((obs, oldVal, newVal) -> {
                dynamicContainer.getChildren().clear();
                dynamicContainer.getChildren().add("Video".equals(newVal) ? videoForm : textForm);
            });
        }

        // Error message label
        Label errorMsg = new Label();
        errorMsg.getStyleClass().add("error-label");
        errorMsg.setVisible(false);
        errorMsg.setManaged(false);

        // Action Buttons Row
        HBox buttonRow = new HBox(12);
        buttonRow.setAlignment(Pos.CENTER_RIGHT);

        Button btnCancel = new Button("Batal");
        btnCancel.getStyleClass().addAll("btn-ghost", "btn-medium");
        btnCancel.setOnAction(e -> controller.showMaterialsContent());

        Button btnSave = new Button(isEdit ? "Simpan Perubahan" : "Simpan Materi");
        btnSave.getStyleClass().addAll("btn-primary", "btn-medium");
        btnSave.setStyle("-fx-background-color: #059669;"); // colorful success green
        btnSave.setOnAction(e -> {
            String title = txtTitle.getText().trim();
            String desc = txtDesc.getText().trim();
            String type = cmbType.getValue();

            if (title.isEmpty()) {
                showFormError(errorMsg, "Judul materi tidak boleh kosong!");
                return;
            }
            if (desc.isEmpty()) {
                showFormError(errorMsg, "Deskripsi materi tidak boleh kosong!");
                return;
            }

            CourseMaterial material = null;
            if ("Video".equals(type)) {
                String url = txtUrl.getText().trim();
                String durationStr = txtDuration.getText().trim();

                if (url.isEmpty()) {
                    showFormError(errorMsg, "URL Video YouTube tidak boleh kosong!");
                    return;
                }
                if (durationStr.isEmpty()) {
                    showFormError(errorMsg, "Durasi video tidak boleh kosong!");
                    return;
                }

                int duration;
                try {
                    duration = Integer.parseInt(durationStr);
                    if (duration <= 0) throw new NumberFormatException();
                } catch (NumberFormatException ex) {
                    showFormError(errorMsg, "Durasi harus berupa angka bulat positif!");
                    return;
                }

                material = new VideoMaterial(isEdit ? materialToEdit.getId() : null, title, desc, url, duration);
            } else {
                String content = txtContent.getHtmlText();
                // HTMLEditor wraps empty text in html tags, we check if it's very short or empty
                if (content == null || content.replaceAll("<[^>]*>", "").trim().isEmpty()) {
                    showFormError(errorMsg, "Konten teks materi tidak boleh kosong!");
                    return;
                }
                material = new TextMaterial(isEdit ? materialToEdit.getId() : null, title, desc, content);
            }

            // Assign teacher ID if current user is a teacher
            User currentUser = controller.getCurrentUser();
            if (currentUser instanceof Teacher teacher) {
                material.setTeacherId(teacher.getId());
            }

            // Save via service
            if (isEdit) {
                controller.getMaterialService().updateMaterial(material, selectedFileHolder[0]);
            } else {
                controller.getMaterialService().addMaterial(material, selectedFileHolder[0]);
            }

            // Success notification alert
            controller.showToast(isEdit ? "Materi Berhasil Diperbarui!" : "Materi Baru Berhasil Disimpan!", "SUCCESS");

            controller.showMaterialsContent();
        });

        buttonRow.getChildren().addAll(btnCancel, btnSave);
        formCard.getChildren().addAll(titleBox, descBox, typeBox, dynamicContainer, fileBox, errorMsg, buttonRow);
        root.getChildren().addAll(headerBox, formCard);
        return root;
    }

    private void showFormError(Label lbl, String msg) {
        lbl.setText("⚠ " + msg);
        lbl.setVisible(true);
        lbl.setManaged(true);
    }
}

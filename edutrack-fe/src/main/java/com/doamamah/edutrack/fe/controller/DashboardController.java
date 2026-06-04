package com.doamamah.edutrack.fe.controller;

import com.doamamah.edutrack.fe.model.CourseMaterial;
import com.doamamah.edutrack.fe.model.TextMaterial;
import com.doamamah.edutrack.fe.model.VideoMaterial;
import com.doamamah.edutrack.fe.model.Student;
import com.doamamah.edutrack.fe.model.Teacher;
import com.doamamah.edutrack.fe.model.User;
import com.doamamah.edutrack.fe.service.AuthService;
import com.doamamah.edutrack.fe.service.MaterialService;
import com.doamamah.edutrack.fe.util.SceneManager;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

import java.net.URL;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;
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
    @FXML private Button btnCreateMaterial;
    @FXML private Button btnLogout;
    @FXML private Label contentTitleLabel;
    @FXML private Label greetingLabel;
    @FXML private VBox contentArea;
    @FXML private ScrollPane contentScrollPane;

    private final MaterialService materialService = new MaterialService();
    private final AuthService authService = new AuthService();
    private User currentUser;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        currentUser = SceneManager.getInstance().getCurrentUser();
        setupProfile();
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

        // Jika Guru, tampilkan tombol navigasi Tambah Materi
        if (currentUser instanceof Teacher) {
            btnCreateMaterial.setVisible(true);
            btnCreateMaterial.setManaged(true);
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
        contentArea.getChildren().add(buildDashboardHomeContent());
    }

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
            List<CourseMaterial> materials = materialService.getAllMaterials();
            Platform.runLater(() -> {
                contentArea.getChildren().clear();
                contentArea.getChildren().add(buildMaterialsListContent(materials));
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
        contentArea.getChildren().add(buildQuizContent());
    }

    @FXML
    public void showCreateMaterialContent() {
        setActiveButton(btnCreateMaterial);
        contentTitleLabel.setText("Tambah Materi");
        contentArea.getChildren().clear();
        contentArea.getChildren().add(buildCreateMaterialContent());
    }

    @FXML
    public void handleLogout() {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Konfirmasi Logout");
        alert.setHeaderText("Keluar dari EduTrack?");
        alert.setContentText("Apakah Anda yakin ingin logout?");

        // Ganti icon tanda tanya default dengan maskot kita
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

        // Kustomisasi dialog konfirmasi logout agar sesuai tema orange & cream
        DialogPane dialogPane = alert.getDialogPane();
        try {
            String cssPath = getClass().getResource("/com/doamamah/edutrack/fe/css/style.css").toExternalForm();
            dialogPane.getStylesheets().add(cssPath);
            dialogPane.getStyleClass().add("custom-alert");
        } catch (Exception e) {
            System.err.println("Gagal memuat stylesheet ke alert dialog: " + e.getMessage());
        }

        alert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                new Thread(() -> {
                    authService.logout();
                    Platform.runLater(() -> {
                        SceneManager.getInstance().setCurrentUser(null);
                        SceneManager.getInstance().showLogin();
                    });
                }).start();
            }
        });
    }

    // =====================================================================
    //  BUILDER: DASHBOARD HOME — Kartu statistik, progress bar, tips
    // =====================================================================

    private Node buildDashboardHomeContent() {
        HBox mainLayout = new HBox(20);
        mainLayout.setMaxWidth(Double.MAX_VALUE);

        // --- KOLOM KIRI (Utama) ---
        VBox leftColumn = new VBox(20);
        leftColumn.setMaxWidth(Double.MAX_VALUE);
        HBox.setHgrow(leftColumn, Priority.ALWAYS);

        // Welcome Banner (HBox)
        HBox banner = new HBox(20);
        banner.getStyleClass().add("dashboard-banner");
        banner.setPadding(new Insets(12, 28, 12, 28));
        banner.setAlignment(Pos.CENTER_LEFT);

        VBox bannerText = new VBox(6);
        String greetTime = getGreetingByTime();
        String roleName = (currentUser instanceof Teacher) ? "Pengajar" : "Siswa";
        String firstName = currentUser.getFullName() != null
                ? currentUser.getFullName().split(" ")[0] : "Pengguna";

        Label welcomeLabel = new Label(greetTime + ", " + firstName + "!");
        welcomeLabel.getStyleClass().add("banner-title");

        String todayStr = LocalDate.now().format(
                DateTimeFormatter.ofPattern("EEEE, d MMMM yyyy", Locale.of("id", "ID")));
        Label dateLabel = new Label(todayStr + "  ·  " + roleName);
        dateLabel.getStyleClass().add("banner-subtitle");

        bannerText.getChildren().addAll(welcomeLabel, dateLabel);
        HBox.setHgrow(bannerText, Priority.ALWAYS);

        try {
            ImageView mascotView = new ImageView(
                new Image(getClass().getResourceAsStream("/com/doamamah/edutrack/fe/images/dashboard_mascot.png"))
            );
            mascotView.setFitHeight(115);
            mascotView.setPreserveRatio(true);
            mascotView.setSmooth(true);
            banner.getChildren().addAll(bannerText, mascotView);
        } catch (Exception e) {
            System.err.println("Gagal memuat maskot dashboard: " + e.getMessage());
            banner.getChildren().add(bannerText);
        }

        // Statistik Grid 3 kolom
        HBox statsRow = new HBox(14);
        statsRow.setMaxWidth(Double.MAX_VALUE);
        int totalMaterials = materialService.getAllMaterials().size();

        if (currentUser instanceof Student) {
            statsRow.getChildren().addAll(
                buildRichStatCard("Materi Tersedia", String.valueOf(totalMaterials), "materi", "#FF7A00", "📚", 1.0),
                buildRichStatCard("Kuis Aktif",      "2", "kuis",   "#059669", "📝", 0.5),
                buildRichStatCard("Progress Belajar","65", "%",      "#D97706", "🎯", 0.65)
            );
        } else {
            statsRow.getChildren().addAll(
                buildRichStatCard("Total Materi",  String.valueOf(totalMaterials),  "materi", "#FF7A00", "📚", 1.0),
                buildRichStatCard("Total Siswa",   "24", "siswa",  "#059669", "👥", 0.7),
                buildRichStatCard("Kuis Dibuat",   "3",  "kuis",   "#D97706", "📝", 0.6)
            );
        }

        // Progress Belajar Hari Ini
        VBox progressSection = new VBox(10);
        progressSection.getStyleClass().add("section-box");
        progressSection.setPadding(new Insets(20));

        Label progressTitle = new Label("Progress Hari Ini");
        progressTitle.getStyleClass().add("section-title");

        ProgressBar dailyBar = new ProgressBar(0.65);
        dailyBar.setMaxWidth(Double.MAX_VALUE);
        dailyBar.setPrefHeight(10);
        dailyBar.getStyleClass().add("daily-progress");

        HBox progressInfo = new HBox();
        progressInfo.setAlignment(Pos.CENTER_LEFT);
        Label pLeft = new Label("3 dari 5 materi selesai");
        pLeft.getStyleClass().add("progress-info");
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        Label pRight = new Label("65%");
        pRight.getStyleClass().add("progress-percent");
        progressInfo.getChildren().addAll(pLeft, spacer, pRight);

        progressSection.getChildren().addAll(progressTitle, dailyBar, progressInfo);
        leftColumn.getChildren().addAll(banner, statsRow, progressSection);

        // --- KOLOM KANAN (Aksi Cepat & Tips) ---
        VBox rightColumn = new VBox(20);
        rightColumn.setPrefWidth(320);
        rightColumn.setMaxWidth(320);

        // Quick Actions Card
        VBox actionsCard = new VBox(14);
        actionsCard.getStyleClass().add("section-box");
        actionsCard.setPadding(new Insets(20));

        Label actionTitle = new Label("Aksi Cepat");
        actionTitle.getStyleClass().add("section-title");

        Button goMaterials = new Button("Lihat Materi");
        goMaterials.getStyleClass().addAll("btn-primary", "btn-medium");
        goMaterials.setMaxWidth(Double.MAX_VALUE);
        goMaterials.setOnAction(e -> showMaterialsContent());

        Button goQuiz = new Button("Ikuti Kuis");
        goQuiz.getStyleClass().addAll("btn-secondary", "btn-medium");
        goQuiz.setMaxWidth(Double.MAX_VALUE);
        goQuiz.setOnAction(e -> showQuizContent());

        actionsCard.getChildren().addAll(actionTitle, goMaterials, goQuiz);

        // Tips Box
        HBox tipsBox = new HBox(12);
        tipsBox.getStyleClass().add("tips-box");
        tipsBox.setPadding(new Insets(16));
        tipsBox.setAlignment(Pos.CENTER_LEFT);

        Label bulb = new Label("💡");
        bulb.setStyle("-fx-font-size: 28px;");

        VBox tipContent = new VBox(4);
        Label tipTitle = new Label("Tips Belajar Efektif");
        tipTitle.getStyleClass().add("tip-title");
        Label tipText = new Label("Buat catatan singkat setelah selesai menonton video. Tulis 3 hal baru!");
        tipText.getStyleClass().add("tip-text");
        tipText.setWrapText(true);
        tipContent.getChildren().addAll(tipTitle, tipText);
        HBox.setHgrow(tipContent, Priority.ALWAYS);

        tipsBox.getChildren().addAll(bulb, tipContent);
        rightColumn.getChildren().addAll(actionsCard, tipsBox);

        mainLayout.getChildren().addAll(leftColumn, rightColumn);
        return mainLayout;
    }

    /**
     * Kartu statistik premium dengan ikon besar, progress bar mini, dan label detail.
     */
    private VBox buildRichStatCard(String label, String value, String unit,
                                    String accentColor, String icon, double progress) {
        VBox card = new VBox(8);
        card.getStyleClass().add("stat-card");
        card.setPadding(new Insets(20));
        card.setAlignment(Pos.TOP_LEFT);
        HBox.setHgrow(card, Priority.ALWAYS);

        // Row atas: icon + label
        HBox topRow = new HBox(8);
        topRow.setAlignment(Pos.CENTER_LEFT);

        Label iconLabel = new Label(icon);
        iconLabel.setStyle("-fx-font-size: 22px;");
        Label nameLabel = new Label(label);
        nameLabel.getStyleClass().add("stat-label");
        topRow.getChildren().addAll(iconLabel, nameLabel);

        // Angka besar
        HBox valRow = new HBox(4);
        valRow.setAlignment(Pos.BASELINE_LEFT);
        Label valueLabel = new Label(value);
        valueLabel.getStyleClass().add("stat-value");
        Label unitLabel = new Label(unit);
        unitLabel.getStyleClass().add("stat-unit");
        valRow.getChildren().addAll(valueLabel, unitLabel);

        // Mini progress bar
        ProgressBar miniBar = new ProgressBar(progress);
        miniBar.setMaxWidth(Double.MAX_VALUE);
        miniBar.setPrefHeight(6);
        miniBar.getStyleClass().add("mini-progress");
        miniBar.setStyle("-fx-accent: " + accentColor + ";");

        card.getChildren().addAll(topRow, valRow, miniBar);
        return card;
    }

    // =====================================================================
    //  BUILDER: MATERIALS LIST
    // =====================================================================

    private Node buildMaterialsListContent(List<CourseMaterial> materials) {
        VBox root = new VBox(14);
        root.setMaxWidth(Double.MAX_VALUE);

        if (materials.isEmpty()) {
            Label emptyLabel = new Label("Tidak ada materi yang tersedia.");
            emptyLabel.getStyleClass().add("placeholder-text");
            root.getChildren().add(emptyLabel);
            return root;
        }

        // Header section
        HBox header = new HBox(8);
        header.setAlignment(Pos.CENTER_LEFT);
        Label sectionLabel = new Label("Daftar Materi Tersedia");
        sectionLabel.getStyleClass().add("section-title");
        Region sp = new Region();
        HBox.setHgrow(sp, Priority.ALWAYS);
        Label countLabel = new Label(materials.size() + " materi");
        countLabel.getStyleClass().add("material-count");
        header.getChildren().addAll(sectionLabel, sp, countLabel);
        root.getChildren().add(header);

        // FlowPane for responsive wrapping columns
        FlowPane cardsContainer = new FlowPane();
        cardsContainer.setHgap(16);
        cardsContainer.setVgap(16);
        cardsContainer.setMaxWidth(Double.MAX_VALUE);
        cardsContainer.setPrefWrapLength(750);

        for (CourseMaterial material : materials) {
            VBox card = buildMaterialCard(material);
            card.setPrefWidth(340);
            card.setMinWidth(300);
            cardsContainer.getChildren().add(card);
        }

        root.getChildren().add(cardsContainer);
        return root;
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

        // Divider
        javafx.scene.control.Separator divider = new javafx.scene.control.Separator();

        // Button row
        HBox actions = new HBox(8);
        actions.setAlignment(Pos.CENTER_RIGHT);

        Button viewBtn = new Button("Lihat Materi");
        viewBtn.getStyleClass().addAll("btn-primary", "btn-small");
        viewBtn.setOnAction(e -> {
            // PILAR OOP: POLYMORPHISM
            Node materialUI = material.getUIComponent();
            if (materialUI instanceof Pane pane) {
                HBox backWrapper = new HBox(buildBackButton());
                backWrapper.setAlignment(Pos.CENTER_LEFT);
                backWrapper.setMaxWidth(Double.MAX_VALUE);
                backWrapper.setPadding(new Insets(0, 0, 10, 0));
                pane.getChildren().add(0, backWrapper);
            }
            contentArea.getChildren().clear();
            contentArea.getChildren().add(materialUI);
            contentTitleLabel.setText(material.getTitle());
        });

        actions.getChildren().add(viewBtn);
        card.getChildren().addAll(topRow, titleLabel, descLabel, divider, actions);
        return card;
    }

    private Button buildBackButton() {
        Button backBtn = new Button();
        backBtn.getStyleClass().add("btn-back-round");

        // Custom SVG back arrow icon (attractive layout)
        javafx.scene.shape.SVGPath arrow = new javafx.scene.shape.SVGPath();
        arrow.setContent("M20 11H7.83l5.59-5.59L12 4l-8 8 8 8 1.41-1.41L7.83 13H20v-2z");
        arrow.setStyle("-fx-fill: #FF7A00;"); // Match primary orange theme

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

        backBtn.setOnAction(e -> showMaterialsContent());
        return backBtn;
    }

    // =====================================================================
    //  BUILDER: TAMBAH MATERI (FORM GURU)
    // =====================================================================

    private Node buildCreateMaterialContent() {
        VBox root = new VBox(20);
        root.setMaxWidth(Double.MAX_VALUE);

        // Header Box
        VBox headerBox = new VBox(6);
        headerBox.getStyleClass().add("section-box");
        headerBox.setPadding(new Insets(20));

        Label iconLabel = new Label("✨");
        iconLabel.setStyle("-fx-font-size: 32px;");
        Label titleLabel = new Label("Buat Materi E-Learning Baru");
        titleLabel.getStyleClass().add("section-title");
        Label subLabel = new Label("Lengkapi formulir di bawah ini untuk membagikan materi baru bagi siswa.");
        subLabel.getStyleClass().add("card-description");
        headerBox.getChildren().addAll(iconLabel, titleLabel, subLabel);

        // Form Card
        VBox formCard = new VBox(16);
        formCard.getStyleClass().add("material-card");
        formCard.setPadding(new Insets(24));

        // Judul Input
        VBox titleBox = new VBox(6);
        Label lblTitle = new Label("Judul Materi");
        lblTitle.getStyleClass().add("input-label");
        TextField txtTitle = new TextField();
        txtTitle.getStyleClass().add("input-field");
        txtTitle.setPromptText("Contoh: Pengenalan Array di Java");
        titleBox.getChildren().addAll(lblTitle, txtTitle);

        // Deskripsi Input
        VBox descBox = new VBox(6);
        Label lblDesc = new Label("Deskripsi Singkat");
        lblDesc.getStyleClass().add("input-label");
        TextArea txtDesc = new TextArea();
        txtDesc.getStyleClass().add("input-field");
        txtDesc.setPrefHeight(70);
        txtDesc.setWrapText(true);
        txtDesc.setPromptText("Tuliskan deskripsi singkat mengenai materi ini...");
        descBox.getChildren().addAll(lblDesc, txtDesc);

        // Tipe Materi ComboBox
        VBox typeBox = new VBox(6);
        Label lblType = new Label("Tipe Materi");
        lblType.getStyleClass().add("input-label");
        ComboBox<String> cmbType = new ComboBox<>();
        cmbType.getItems().addAll("Video", "Teks");
        cmbType.setValue("Video");
        cmbType.getStyleClass().add("input-field");
        cmbType.setMaxWidth(Double.MAX_VALUE);
        typeBox.getChildren().addAll(lblType, cmbType);

        // Dynamic Form Fields Container
        VBox dynamicContainer = new VBox(16);

        // Dynamic Sub-Form Video
        VBox videoForm = new VBox(16);

        VBox urlBox = new VBox(6);
        Label lblUrl = new Label("URL Video YouTube");
        lblUrl.getStyleClass().add("input-label");
        TextField txtUrl = new TextField();
        txtUrl.getStyleClass().add("input-field");
        txtUrl.setPromptText("Contoh: https://www.youtube.com/watch?v=pTB0EiLXUC8");
        urlBox.getChildren().addAll(lblUrl, txtUrl);

        VBox durationBox = new VBox(6);
        Label lblDuration = new Label("Durasi (Menit)");
        lblDuration.getStyleClass().add("input-label");
        TextField txtDuration = new TextField();
        txtDuration.getStyleClass().add("input-field");
        txtDuration.setPromptText("Contoh: 15");
        durationBox.getChildren().addAll(lblDuration, txtDuration);

        videoForm.getChildren().addAll(urlBox, durationBox);

        // Dynamic Sub-Form Teks
        VBox textForm = new VBox(16);
        VBox contentBox = new VBox(6);
        Label lblContent = new Label("Konten Teks Materi");
        lblContent.getStyleClass().add("input-label");
        TextArea txtContent = new TextArea();
        txtContent.getStyleClass().add("input-field");
        txtContent.setPrefHeight(250);
        txtContent.setWrapText(true);
        txtContent.setPromptText("Tuliskan seluruh materi pembelajaran di sini...");
        contentBox.getChildren().addAll(lblContent, txtContent);
        textForm.getChildren().add(contentBox);

        // Set default dynamic form
        dynamicContainer.getChildren().add(videoForm);

        // Listener to change dynamic fields on ComboBox change
        cmbType.valueProperty().addListener((obs, oldVal, newVal) -> {
            dynamicContainer.getChildren().clear();
            if ("Video".equals(newVal)) {
                dynamicContainer.getChildren().add(videoForm);
            } else {
                dynamicContainer.getChildren().add(textForm);
            }
        });

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
        btnCancel.setOnAction(e -> showMaterialsContent());

        Button btnSave = new Button("Simpan Materi");
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

            CourseMaterial newMaterial = null;

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

                newMaterial = new VideoMaterial(null, title, desc, url, duration);
            } else {
                String content = txtContent.getText().trim();
                if (content.isEmpty()) {
                    showFormError(errorMsg, "Konten teks materi tidak boleh kosong!");
                    return;
                }
                newMaterial = new TextMaterial(null, title, desc, content);
            }

            // Save material via service
            materialService.addMaterial(newMaterial);

            // Success notification alert (styled custom matching orange/cream theme)
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Berhasil");
            alert.setHeaderText("Materi Baru Berhasil Disimpan!");
            alert.setContentText("Materi '" + title + "' kini sudah dapat diakses oleh seluruh siswa.");
            
            // Ganti icon default dengan maskot kita
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

            // Redirect back to list
            showMaterialsContent();
        });

        buttonRow.getChildren().addAll(btnCancel, btnSave);

        formCard.getChildren().addAll(titleBox, descBox, typeBox, dynamicContainer, errorMsg, buttonRow);
        root.getChildren().addAll(headerBox, formCard);
        return root;
    }

    private void showFormError(Label lbl, String msg) {
        lbl.setText("⚠ " + msg);
        lbl.setVisible(true);
        lbl.setManaged(true);
    }

    // =====================================================================
    //  BUILDER: QUIZ (placeholder yang didekorasi)
    // =====================================================================

    private Node buildQuizContent() {
        HBox mainLayout = new HBox(20);
        mainLayout.setMaxWidth(Double.MAX_VALUE);

        VBox leftColumn = new VBox(20);
        leftColumn.setMaxWidth(Double.MAX_VALUE);
        HBox.setHgrow(leftColumn, Priority.ALWAYS);

        // Header (Simple text layout, no card box)
        VBox headerBox = new VBox(4);
        headerBox.setPadding(new Insets(0, 0, 10, 0));

        HBox titleRow = new HBox(10);
        titleRow.setAlignment(Pos.CENTER_LEFT);
        Label quizIcon = new Label("📝");
        quizIcon.setStyle("-fx-font-size: 24px;");
        Label quizTitle = new Label("Kuis & Evaluasi");
        quizTitle.getStyleClass().add("section-title");
        quizTitle.setStyle("-fx-font-size: 20px;");
        titleRow.getChildren().addAll(quizIcon, quizTitle);

        Label quizSub = new Label("Uji pemahamanmu setelah mempelajari materi.");
        quizSub.getStyleClass().add("card-description");

        headerBox.getChildren().addAll(titleRow, quizSub);

        // FlowPane for responsive wrapping quiz cards
        FlowPane quizContainer = new FlowPane();
        quizContainer.setHgap(20);
        quizContainer.setVgap(20);
        quizContainer.setMaxWidth(Double.MAX_VALUE);
        quizContainer.setPrefWrapLength(750);

        // Dummy quiz cards (Made larger and fully active to highlight them)
        VBox quiz1 = buildQuizCard("Kuis: Dasar OOP",
                "3 soal pilihan ganda tentang konsep OOP dasar.", "Mudah", "#059669");
        quiz1.setPrefWidth(380);
        quiz1.setMinWidth(320);

        VBox quiz2 = buildQuizCard("Kuis: Inheritance & Polymorphism",
                "3 soal pilihan ganda tentang pewarisan dan polimorfisme di Java.", "Sedang", "#D97706");
        quiz2.setPrefWidth(380);
        quiz2.setMinWidth(320);

        quizContainer.getChildren().addAll(quiz1, quiz2);
        leftColumn.getChildren().addAll(headerBox, quizContainer);

        // Right side: Illustration Panel (Remove section-box styling so it is transparent and secondary)
        VBox rightColumn = new VBox(16);
        rightColumn.setPadding(new Insets(24));
        rightColumn.setAlignment(Pos.CENTER);
        rightColumn.setPrefWidth(320);
        rightColumn.setMaxWidth(320);

        try {
            ImageView quizImg = new ImageView(
                new Image(getClass().getResourceAsStream("/com/doamamah/edutrack/fe/images/quiz_illustration.png"))
            );
            quizImg.setFitWidth(240);
            quizImg.setPreserveRatio(true);
            quizImg.setSmooth(true);

            Label promoTitle = new Label("Siap Menghadapi Kuis?");
            promoTitle.getStyleClass().add("section-title");
            promoTitle.setStyle("-fx-font-size: 15px;");
            Label promoText = new Label("Dapatkan skor tinggi dan buka lencana baru! Kuis dirancang interaktif untuk menguji sejauh mana kamu memahami konsep Java.");
            promoText.getStyleClass().add("card-description");
            promoText.setWrapText(true);
            promoText.setAlignment(Pos.CENTER);

            rightColumn.getChildren().addAll(quizImg, promoTitle, promoText);
        } catch (Exception e) {
            System.err.println("Gagal memuat ilustrasi kuis: " + e.getMessage());
        }

        mainLayout.getChildren().addAll(leftColumn, rightColumn);
        return mainLayout;
    }

    private VBox buildQuizCard(String title, String desc, String difficulty, String color) {
        VBox card = new VBox(12);
        card.getStyleClass().add("material-card");
        card.setPadding(new Insets(20));

        HBox top = new HBox(8);
        top.setAlignment(Pos.CENTER_LEFT);
        Circle dot = new Circle(5);
        dot.setFill(Color.web(color));
        Label diffLabel = new Label(difficulty);
        diffLabel.setStyle("-fx-font-size: 11px; -fx-font-weight: bold; -fx-text-fill: " + color + ";");
        top.getChildren().addAll(dot, diffLabel);

        Label titleL = new Label(title);
        titleL.getStyleClass().add("card-title");
        titleL.setStyle("-fx-font-size: 17px;"); // Larger title to stand out

        Label descL = new Label(desc);
        descL.getStyleClass().add("card-description");
        descL.setWrapText(true);

        javafx.scene.control.Separator div = new javafx.scene.control.Separator();

        HBox actions = new HBox(8);
        actions.setAlignment(Pos.CENTER_RIGHT);

        Button startBtn = new Button("Mulai Kuis");
        startBtn.getStyleClass().addAll("btn-primary", "btn-small");
        startBtn.setDisable(false); // Enable the button to highlight it!
        startBtn.setOnAction(e -> {
            startQuizGameplay(title, difficulty);
        });

        // Show active duration metadata instead of "Segera hadir"
        Label quizMeta = new Label("3 Soal  ·  " + ("Mudah".equals(difficulty) ? "5 mnt" : "8 mnt"));
        quizMeta.getStyleClass().add("progress-info");
        quizMeta.setStyle("-fx-font-weight: bold; -fx-text-fill: #FF7A00;");

        Region spc = new Region();
        HBox.setHgrow(spc, Priority.ALWAYS);
        actions.getChildren().addAll(quizMeta, spc, startBtn);

        card.getChildren().addAll(top, titleL, descL, div, actions);
        return card;
    }

    // =====================================================================
    //  QUIZ GAMEPLAY ENGINE (INTERACTIVE SAMPLE)
    // =====================================================================

    private String activeQuizTitle;
    private List<QuizQuestion> activeQuestions;
    private int currentQuestionIndex;
    private int correctAnswersCount;
    private int selectedOptionIndex;
    private javafx.animation.Timeline quizTimer;
    private int secondsRemaining;
    private Label timerLabel;

    private static class QuizQuestion {
        String question;
        String[] options;
        int correctIndex;

        QuizQuestion(String question, String[] options, int correctIndex) {
            this.question = question;
            this.options = options;
            this.correctIndex = correctIndex;
        }
    }

    private void startQuizGameplay(String title, String difficulty) {
        activeQuizTitle = title;
        currentQuestionIndex = 0;
        correctAnswersCount = 0;
        selectedOptionIndex = -1;

        // Stop any running timer first to prevent leaks
        if (quizTimer != null) {
            quizTimer.stop();
        }

        // Set duration
        int minutes = title.contains("Dasar OOP") ? 5 : 8;
        secondsRemaining = minutes * 60;

        activeQuestions = new java.util.ArrayList<>();
        if (title.contains("Dasar OOP")) {
            activeQuestions.add(new QuizQuestion(
                "Manakah yang merupakan pilar utama dalam Object-Oriented Programming (OOP)?",
                new String[]{
                    "Inheritance, Polymorphism, Encapsulation, Abstraction",
                    "Compilation, Interpretation, Execution",
                    "Variables, Loops, Conditions",
                    "HTML, CSS, JavaScript"
                }, 0
            ));
            activeQuestions.add(new QuizQuestion(
                "Apakah fungsi utama dari Encapsulation (Pewadahan) dalam OOP?",
                new String[]{
                    "Membuat variabel global di seluruh program",
                    "Menyembunyikan detail implementasi kelas dan membatasi akses langsung ke data",
                    "Menghubungkan database eksternal dengan sistem lokal",
                    "Mempercepat waktu eksekusi program Java"
                }, 1
            ));
            activeQuestions.add(new QuizQuestion(
                "Pilar OOP yang memungkinkan objek baru mewarisi sifat dari objek induknya adalah...",
                new String[]{
                    "Polymorphism",
                    "Abstraction",
                    "Inheritance",
                    "Encapsulation"
                }, 2
            ));
        } else {
            activeQuestions.add(new QuizQuestion(
                "Kata kunci (keyword) yang digunakan di Java untuk menerapkan pewarisan kelas (inheritance) adalah...",
                new String[]{
                    "implements",
                    "extends",
                    "inherits",
                    "super"
                }, 1
            ));
            activeQuestions.add(new QuizQuestion(
                "Apa yang dimaksud dengan Polymorphism (Polimorfisme) dalam OOP?",
                new String[]{
                    "Kemampuan suatu objek memiliki banyak bentuk/implementasi metode yang berbeda",
                    "Membagi kode menjadi beberapa modul terpisah",
                    "Membuat banyak class dalam satu file",
                    "Proses mengamankan program dari serangan hacker"
                }, 0
            ));
            activeQuestions.add(new QuizQuestion(
                "Keyword 'super' di Java digunakan untuk...",
                new String[]{
                    "Membuat objek baru dari kelas induk",
                    "Mengakses konstruktor, metode, atau variabel dari parent class",
                    "Mengakhiri eksekusi program secara paksa",
                    "Mendeklarasikan konstanta global"
                }, 1
            ));
        }

        // Initialize Timeline for live countdown
        quizTimer = new javafx.animation.Timeline(
            new javafx.animation.KeyFrame(javafx.util.Duration.seconds(1), e -> {
                secondsRemaining--;
                updateTimerLabelText();
                if (secondsRemaining <= 0) {
                    quizTimer.stop();
                    handleTimeOut();
                }
            })
        );
        quizTimer.setCycleCount(javafx.animation.Timeline.INDEFINITE);
        quizTimer.play();

        renderQuizQuestion();
    }

    private void renderQuizQuestion() {
        contentArea.getChildren().clear();

        QuizQuestion q = activeQuestions.get(currentQuestionIndex);
        selectedOptionIndex = -1; // Reset selection

        VBox quizBox = new VBox(20);
        quizBox.getStyleClass().add("material-container");
        quizBox.setPadding(new Insets(28));
        quizBox.setMaxWidth(800);

        // Header Panel (Back button + progress)
        HBox topRow = new HBox(12);
        topRow.setAlignment(Pos.CENTER_LEFT);

        Button exitBtn = new Button();
        exitBtn.getStyleClass().add("btn-back-round");
        javafx.scene.shape.SVGPath exitArrow = new javafx.scene.shape.SVGPath();
        exitArrow.setContent("M20 11H7.83l5.59-5.59L12 4l-8 8 8 8 1.41-1.41L7.83 13H20v-2z");
        exitArrow.setStyle("-fx-fill: #FF7A00;");
        exitBtn.setGraphic(exitArrow);
        exitBtn.setTooltip(new Tooltip("Keluar dari Kuis"));
        exitBtn.setStyle(
            "-fx-background-color: #FFFFFF; -fx-background-radius: 20px; " +
            "-fx-border-color: #E5E0D8; -fx-border-radius: 20px; -fx-border-width: 1.5px; " +
            "-fx-padding: 8px; -fx-cursor: hand;"
        );
        exitBtn.setOnAction(e -> confirmExitQuiz());

        Label quizTitleLabel = new Label(activeQuizTitle);
        quizTitleLabel.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: #6B7280;");

        Region spacerTop = new Region();
        HBox.setHgrow(spacerTop, Priority.ALWAYS);

        // Live Timer Label
        timerLabel = new Label();
        timerLabel.setStyle(
            "-fx-font-size: 13px; -fx-font-weight: bold; -fx-text-fill: #EF4444; " +
            "-fx-background-color: #FEF2F2; -fx-background-radius: 6px; " +
            "-fx-padding: 4px 10px; -fx-border-color: #FCA5A5; " +
            "-fx-border-radius: 6px; -fx-border-width: 1px;"
        );
        updateTimerLabelText();

        Label progressLabel = new Label("Soal " + (currentQuestionIndex + 1) + " dari " + activeQuestions.size());
        progressLabel.setStyle("-fx-font-size: 13px; -fx-font-weight: bold; -fx-text-fill: #FF7A00;");

        topRow.getChildren().addAll(exitBtn, quizTitleLabel, spacerTop, timerLabel, progressLabel);

        // Progress Bar
        ProgressBar quizProgressBar = new ProgressBar((double) (currentQuestionIndex + 1) / activeQuestions.size());
        quizProgressBar.setMaxWidth(Double.MAX_VALUE);
        quizProgressBar.setPrefHeight(8);
        quizProgressBar.getStyleClass().add("daily-progress");

        // Separator
        javafx.scene.control.Separator separator = new javafx.scene.control.Separator();

        // Question VBox
        VBox questionBox = new VBox(12);
        Label qNumLabel = new Label("PERTANYAAN " + (currentQuestionIndex + 1));
        qNumLabel.setStyle("-fx-font-size: 11px; -fx-font-weight: bold; -fx-text-fill: #9CA3AF; -fx-letter-spacing: 1.5;");
        
        Label qTextLabel = new Label(q.question);
        qTextLabel.setStyle("-fx-font-size: 17px; -fx-font-weight: bold; -fx-text-fill: #1A1A1A;");
        qTextLabel.setWrapText(true);
        qTextLabel.setMaxWidth(700);

        questionBox.getChildren().addAll(qNumLabel, qTextLabel);

        // Options List (VBox)
        VBox optionsContainer = new VBox(12);
        optionsContainer.setMaxWidth(700);

        VBox[] optionCards = new VBox[q.options.length];
        Button nextBtn = new Button(currentQuestionIndex == activeQuestions.size() - 1 ? "Kirim Jawaban" : "Selanjutnya");
        nextBtn.getStyleClass().addAll("btn-primary", "btn-medium");
        nextBtn.setDisable(true); // Enabled only when selected

        char optLetter = 'A';
        for (int i = 0; i < q.options.length; i++) {
            final int idx = i;
            String optionText = q.options[i];

            HBox cardContent = new HBox(14);
            cardContent.setAlignment(Pos.CENTER_LEFT);

            // Letter badge
            Label letterBadge = new Label(String.valueOf((char)(optLetter + i)));
            letterBadge.setStyle(
                "-fx-background-color: #FAF8F3; " +
                "-fx-text-fill: #6B7280; " +
                "-fx-font-weight: bold; " +
                "-fx-font-size: 13px; " +
                "-fx-min-width: 28px; -fx-min-height: 28px; " +
                "-fx-max-width: 28px; -fx-max-height: 28px; " +
                "-fx-alignment: center; " +
                "-fx-background-radius: 50%;" +
                "-fx-border-color: #E5E0D8; -fx-border-radius: 50%; -fx-border-width: 1px;"
            );

            Label optionLabel = new Label(optionText);
            optionLabel.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: #374151;");
            optionLabel.setWrapText(true);
            HBox.setHgrow(optionLabel, Priority.ALWAYS);

            cardContent.getChildren().addAll(letterBadge, optionLabel);

            VBox card = new VBox(cardContent);
            card.setPadding(new Insets(14, 18, 14, 18));
            
            // Base Style
            String baseStyle = 
                "-fx-background-color: #FFFFFF; " +
                "-fx-background-radius: 12px; " +
                "-fx-border-color: #E5E0D8; " +
                "-fx-border-radius: 12px; " +
                "-fx-border-width: 1.5px; " +
                "-fx-cursor: hand;";
            card.setStyle(baseStyle);

            // Hover and Selection Styling
            card.setOnMouseEntered(ev -> {
                if (selectedOptionIndex != idx) {
                    card.setStyle(
                        "-fx-background-color: #FAF8F3; " +
                        "-fx-background-radius: 12px; " +
                        "-fx-border-color: #D5CFC7; " +
                        "-fx-border-radius: 12px; " +
                        "-fx-border-width: 1.5px; " +
                        "-fx-cursor: hand;"
                    );
                }
            });
            card.setOnMouseExited(ev -> {
                if (selectedOptionIndex != idx) {
                    card.setStyle(baseStyle);
                }
            });

            card.setOnMouseClicked(ev -> {
                selectedOptionIndex = idx;
                nextBtn.setDisable(false);

                // Update styles of all option cards
                for (int j = 0; j < optionCards.length; j++) {
                    if (j == idx) {
                        optionCards[j].setStyle(
                            "-fx-background-color: #FFF0E0; " +
                            "-fx-background-radius: 12px; " +
                            "-fx-border-color: #FF7A00; " +
                            "-fx-border-radius: 12px; " +
                            "-fx-border-width: 1.5px; " +
                            "-fx-cursor: hand; " +
                            "-fx-effect: dropshadow(gaussian, rgba(255, 122, 0, 0.1), 6, 0, 0, 1);"
                        );
                        // Make letter badge stand out too
                        letterBadge.setStyle(
                            "-fx-background-color: #FF7A00; " +
                            "-fx-text-fill: #FFFFFF; " +
                            "-fx-font-weight: bold; " +
                            "-fx-font-size: 13px; " +
                            "-fx-min-width: 28px; -fx-min-height: 28px; " +
                            "-fx-max-width: 28px; -fx-max-height: 28px; " +
                            "-fx-alignment: center; " +
                            "-fx-background-radius: 50%;"
                        );
                    } else {
                        optionCards[j].setStyle(baseStyle);
                        // Reset other letter badges
                        Label otherLetterBadge = (Label)((HBox)optionCards[j].getChildren().get(0)).getChildren().get(0);
                        otherLetterBadge.setStyle(
                            "-fx-background-color: #FAF8F3; " +
                            "-fx-text-fill: #6B7280; " +
                            "-fx-font-weight: bold; " +
                            "-fx-font-size: 13px; " +
                            "-fx-min-width: 28px; -fx-min-height: 28px; " +
                            "-fx-max-width: 28px; -fx-max-height: 28px; " +
                            "-fx-alignment: center; " +
                            "-fx-background-radius: 50%;" +
                            "-fx-border-color: #E5E0D8; -fx-border-radius: 50%; -fx-border-width: 1px;"
                        );
                    }
                }
            });

            optionCards[i] = card;
            optionsContainer.getChildren().add(card);
        }

        // Action row (Exit/Next)
        HBox actionRow = new HBox(12);
        actionRow.setAlignment(Pos.CENTER_RIGHT);
        
        Button cancelQuizBtn = new Button("Keluar Kuis");
        cancelQuizBtn.getStyleClass().addAll("btn-ghost", "btn-medium");
        cancelQuizBtn.setOnAction(e -> confirmExitQuiz());

        nextBtn.setOnAction(e -> handleNextQuestion());

        actionRow.getChildren().addAll(cancelQuizBtn, nextBtn);

        quizBox.getChildren().addAll(topRow, quizProgressBar, separator, questionBox, optionsContainer, actionRow);
        contentArea.getChildren().add(quizBox);
        contentTitleLabel.setText("Pengerjaan Kuis");
    }

    private void handleNextQuestion() {
        QuizQuestion q = activeQuestions.get(currentQuestionIndex);
        if (selectedOptionIndex == q.correctIndex) {
            correctAnswersCount++;
        }

        currentQuestionIndex++;
        if (currentQuestionIndex < activeQuestions.size()) {
            renderQuizQuestion();
        } else {
            if (quizTimer != null) {
                quizTimer.stop();
            }
            renderQuizResult();
        }
    }

    private void updateTimerLabelText() {
        if (timerLabel == null) return;
        int mins = secondsRemaining / 60;
        int secs = secondsRemaining % 60;
        timerLabel.setText(String.format("⏱ %02d:%02d", mins, secs));
    }

    private void handleTimeOut() {
        javafx.application.Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("Waktu Habis");
            alert.setHeaderText("Waktu Pengerjaan Kuis Telah Habis!");
            alert.setContentText("Kuis Anda akan otomatis dikirimkan berdasarkan jawaban yang sudah tersimpan.");
            
            try {
                ImageView alertMascot = new ImageView(
                    new Image(getClass().getResourceAsStream("/com/doamamah/edutrack/fe/images/mascot_face.png"))
                );
                alertMascot.setFitWidth(48);
                alertMascot.setPreserveRatio(true);
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
            
            renderQuizResult();
        });
    }

    private void confirmExitQuiz() {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Konfirmasi Keluar Kuis");
        alert.setHeaderText("Keluar dari Pengerjaan Kuis?");
        alert.setContentText("Semua jawaban Anda pada kuis ini akan hilang. Apakah Anda yakin?");
        
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

        alert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                if (quizTimer != null) {
                    quizTimer.stop();
                }
                showQuizContent();
            }
        });
    }

    private void renderQuizResult() {
        if (quizTimer != null) {
            quizTimer.stop();
        }

        contentArea.getChildren().clear();

        VBox resultBox = new VBox(24);
        resultBox.getStyleClass().add("material-container");
        resultBox.setPadding(new Insets(32));
        resultBox.setAlignment(Pos.CENTER);
        resultBox.setMaxWidth(600);

        // Mascot celebration illustration
        ImageView celebrationMascot = null;
        try {
            celebrationMascot = new ImageView(
                new Image(getClass().getResourceAsStream("/com/doamamah/edutrack/fe/images/mascot_face.png"))
            );
            celebrationMascot.setFitWidth(90);
            celebrationMascot.setPreserveRatio(true);
            celebrationMascot.setSmooth(true);
        } catch (Exception ex) {
            System.err.println("Gagal memuat gambar maskot perayaan: " + ex.getMessage());
        }

        // Calculate score mathematically using correctAnswersCount to prevent rounding errors
        int score = (int) Math.round((double) correctAnswersCount * 100.0 / activeQuestions.size());

        Label congratLabel = new Label(score >= 60 ? "Selamat! Kuis Selesai!" : "Kuis Selesai!");
        congratLabel.getStyleClass().add("banner-title");
        congratLabel.setStyle("-fx-font-size: 24px; -fx-text-fill: #FF7A00;");

        VBox scoreBox = new VBox(4);
        scoreBox.setAlignment(Pos.CENTER);
        Label scoreTitle = new Label("SKOR AKHIR KAMU");
        scoreTitle.setStyle("-fx-font-size: 11px; -fx-font-weight: bold; -fx-text-fill: #9CA3AF; -fx-letter-spacing: 1.5;");

        Label scoreLabel = new Label(score + " / 100");
        scoreLabel.setStyle("-fx-font-size: 44px; -fx-font-weight: bold; -fx-text-fill: " + (score >= 60 ? "#059669" : "#D97706") + ";");
        scoreBox.getChildren().addAll(scoreTitle, scoreLabel);

        // Progress bar visual representation of score
        ProgressBar scoreBar = new ProgressBar((double) score / 100.0);
        scoreBar.setPrefWidth(300);
        scoreBar.setPrefHeight(10);
        scoreBar.getStyleClass().add("daily-progress");
        scoreBar.setStyle("-fx-accent: " + (score >= 60 ? "#059669" : "#D97706") + ";");

        Label commentLabel = new Label();
        commentLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #6B7280; -fx-text-alignment: center;");
        commentLabel.setWrapText(true);
        commentLabel.setMaxWidth(400);

        if (score == 100) {
            commentLabel.setText("Luar biasa sempurna! Kamu telah menguasai seluruh konsep materi ini dengan sangat matang. Pertahankan prestasimu!");
        } else if (score >= 60) {
            commentLabel.setText("Kerja bagus! Pemahaman konsep Anda sudah cukup baik. Pelajari sedikit lagi untuk meraih nilai sempurna pada percobaan berikutnya!");
        } else {
            commentLabel.setText("Terus berusaha! Baca kembali materi e-learning dan coba kuis ini sekali lagi untuk memperkuat pemahaman Anda.");
        }

        // Return button
        Button backBtn = new Button("Kembali ke Halaman Kuis");
        backBtn.getStyleClass().addAll("btn-primary", "btn-large");
        backBtn.setOnAction(e -> showQuizContent());

        if (celebrationMascot != null) {
            resultBox.getChildren().addAll(celebrationMascot, congratLabel, scoreBox, scoreBar, commentLabel, backBtn);
        } else {
            resultBox.getChildren().addAll(congratLabel, scoreBox, scoreBar, commentLabel, backBtn);
        }

        contentArea.getChildren().add(resultBox);
        contentTitleLabel.setText("Evaluasi Kuis");
    }

    // =====================================================================
    //  HELPERS
    // =====================================================================

    private String getGreetingByTime() {
        int hour = java.time.LocalTime.now().getHour();
        if (hour < 11)  return "Selamat Pagi";
        if (hour < 15)  return "Selamat Siang";
        if (hour < 18)  return "Selamat Sore";
        return "Selamat Malam";
    }

    private void setActiveButton(Button activeBtn) {
        btnDashboard.getStyleClass().remove("nav-btn-active");
        btnMaterials.getStyleClass().remove("nav-btn-active");
        btnQuiz.getStyleClass().remove("nav-btn-active");
        if (btnCreateMaterial != null) {
            btnCreateMaterial.getStyleClass().remove("nav-btn-active");
        }
        if (!activeBtn.getStyleClass().contains("nav-btn-active")) {
            activeBtn.getStyleClass().add("nav-btn-active");
        }
    }
}

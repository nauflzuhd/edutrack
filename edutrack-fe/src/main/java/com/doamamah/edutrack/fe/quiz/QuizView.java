package com.doamamah.edutrack.fe.quiz;

import com.doamamah.edutrack.fe.user.Student;
import com.doamamah.edutrack.fe.user.Teacher;
import com.doamamah.edutrack.fe.user.User;
import com.doamamah.edutrack.fe.quiz.QuizService;
import com.doamamah.edutrack.fe.quiz.QuizService.QuizData;
import com.doamamah.edutrack.fe.quiz.QuizService.QuestionData;
import com.doamamah.edutrack.fe.dashboard.DashboardController;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;

import java.util.ArrayList;
import java.util.List;

public class QuizView {

    private final DashboardController controller;
    private final QuizService quizService = new QuizService();

    // Cache kuis dari backend
    private List<QuizData> cachedQuizzes;

    // =====================================================================
    //  QUIZ GAMEPLAY ENGINE STATE
    // =====================================================================

    private String activeQuizTitle;
    private long activeQuizId;
    private List<QuizQuestion> activeQuestions;
    private int currentQuestionIndex;
    private int correctAnswersCount;
    private int selectedOptionIndex;
    private int[] userAnswers;
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

    public QuizView(DashboardController controller) {
        this.controller = controller;
    }

    private final com.doamamah.edutrack.fe.enrollment.EnrollmentService enrollmentService = new com.doamamah.edutrack.fe.enrollment.EnrollmentService();

    /**
     * Memuat kuis dari backend (atau cache).
     */
    private List<QuizData> getQuizzes() {
        if (cachedQuizzes == null) {
            List<Long> teacherIds = null;
            if (controller.getCurrentUser() instanceof Teacher teacher) {
                teacherIds = List.of(teacher.getId());
            } else if (controller.getCurrentUser() instanceof com.doamamah.edutrack.fe.user.Student student) {
                teacherIds = enrollmentService.getEnrolledTeacherIds(student.getId());
                if (teacherIds.isEmpty()) {
                    teacherIds = List.of(-1L);
                }
            }
            cachedQuizzes = quizService.getAllQuizzes(teacherIds);
        }
        return cachedQuizzes;
    }

    /**
     * Invalidasi cache agar data di-refresh dari backend.
     */
    private void invalidateCache() {
        cachedQuizzes = null;
    }

    public Node buildQuizContent() {
        User currentUser = controller.getCurrentUser();
        if (currentUser instanceof Teacher) {
            return buildTeacherQuizContent();
        } else {
            return buildStudentQuizContent();
        }
    }

    // =====================================================================
    //  STUDENT VIEW
    // =====================================================================

    private Node buildStudentQuizContent() {
        HBox mainLayout = new HBox(20);
        mainLayout.setMaxWidth(Double.MAX_VALUE);

        VBox leftColumn = new VBox(20);
        leftColumn.setMaxWidth(Double.MAX_VALUE);
        HBox.setHgrow(leftColumn, Priority.ALWAYS);

        // Header
        VBox headerBox = new VBox(4);
        headerBox.setPadding(new Insets(0, 0, 10, 0));

        HBox titleRow = new HBox(10);
        titleRow.setAlignment(Pos.CENTER_LEFT);
        Label quizIcon = new Label("🎯");
        quizIcon.setStyle("-fx-font-size: 24px;");
        Label quizTitle = new Label("Kuis & Evaluasi");
        quizTitle.getStyleClass().add("section-title");
        quizTitle.setStyle("-fx-font-size: 20px;");
        
        TextField searchQuizField = new TextField();
        searchQuizField.setPromptText("🔍 Cari kuis...");
        searchQuizField.getStyleClass().add("input-field");
        searchQuizField.setPrefWidth(220);
        
        Region qSpacer = new Region();
        HBox.setHgrow(qSpacer, Priority.ALWAYS);
        
        titleRow.getChildren().addAll(quizIcon, quizTitle, qSpacer, searchQuizField);

        Label quizSub = new Label("Uji pemahamanmu setelah mempelajari materi.");
        quizSub.getStyleClass().add("card-description");

        headerBox.getChildren().addAll(titleRow, quizSub);

        // Loading indicator
        ProgressIndicator loading = new ProgressIndicator();
        loading.setPrefSize(40, 40);
        VBox loadingBox = new VBox(loading, new Label("Memuat kuis..."));
        loadingBox.setAlignment(Pos.CENTER);
        loadingBox.setSpacing(12);
        loadingBox.setPadding(new Insets(40));

        leftColumn.getChildren().addAll(headerBox, loadingBox);

        // Right side: Stats Panel
        VBox rightColumn = new VBox(16);
        rightColumn.setPadding(new Insets(20));
        rightColumn.getStyleClass().add("section-box");
        rightColumn.setPrefWidth(320);
        rightColumn.setMaxWidth(320);

        Label statsTitle = new Label("Statistik Belajarmu");
        statsTitle.getStyleClass().add("section-title");
        statsTitle.setStyle("-fx-font-size: 15px;");

        VBox statBox1 = buildMiniStatRow("Rata-rata Nilai", "-", "poin", "#FF7A00");
        VBox statBox2 = buildMiniStatRow("Kuis Dikerjakan", "-", "kuis", "#059669");
        VBox statBox3 = buildMiniStatRow("Total Percobaan", "-", "kali", "#D97706");
        
        VBox illustration = buildQuizIllustrationPanel();
        illustration.setPadding(new Insets(10, 0, 0, 0));
        illustration.setPrefWidth(Region.USE_COMPUTED_SIZE);
        illustration.setMaxWidth(Region.USE_COMPUTED_SIZE);

        rightColumn.getChildren().addAll(statsTitle, statBox1, statBox2, statBox3, illustration);

        mainLayout.getChildren().addAll(leftColumn, rightColumn);

        // Fetch dari backend di thread terpisah
        Thread fetchThread = new Thread(() -> {
            invalidateCache();
            List<QuizData> quizzes = getQuizzes();
            List<QuizService.QuizAttemptData> attempts = quizService.getStudentAttempts(controller.getCurrentUser().getId());

            Platform.runLater(() -> {
                double avgScore = 0;
                java.util.Set<Long> uniqueQuizzes = new java.util.HashSet<>();
                if (!attempts.isEmpty()) {
                    double totalScore = 0;
                    for (QuizService.QuizAttemptData att : attempts) {
                        totalScore += att.getScore();
                        uniqueQuizzes.add(att.getQuizId());
                    }
                    avgScore = totalScore / attempts.size();
                }

                rightColumn.getChildren().clear();
                rightColumn.getChildren().addAll(
                    statsTitle,
                    buildMiniStatRow("Rata-rata Nilai", attempts.isEmpty() ? "0.0" : String.format("%.1f", avgScore), "poin", "#FF7A00"),
                    buildMiniStatRow("Kuis Dikerjakan", String.valueOf(uniqueQuizzes.size()), "dari " + quizzes.size(), "#059669"),
                    buildMiniStatRow("Total Percobaan", String.valueOf(attempts.size()), "kali", "#D97706"),
                    illustration
                );

                leftColumn.getChildren().remove(loadingBox);

                FlowPane quizContainer = new FlowPane();
                quizContainer.setHgap(20);
                quizContainer.setVgap(20);
                quizContainer.setMaxWidth(Double.MAX_VALUE);
                quizContainer.setPrefWrapLength(750);

                Runnable updateStudentList = () -> {
                    quizContainer.getChildren().clear();
                    String query = searchQuizField.getText().toLowerCase();
                    List<QuizData> filtered = quizzes.stream()
                        .filter(q -> q.getTitle().toLowerCase().contains(query) || (q.getDescription() != null && q.getDescription().toLowerCase().contains(query)))
                        .toList();

                    if (filtered.isEmpty()) {
                        Label emptyLabel = new Label("Tidak ada kuis ditemukan.");
                        emptyLabel.getStyleClass().add("placeholder-text");
                        quizContainer.getChildren().add(emptyLabel);
                    } else {
                        for (QuizData q : filtered) {
                            Integer highestScore = null;
                            for (QuizService.QuizAttemptData att : attempts) {
                                if (att.getQuizId() == q.getId()) {
                                    if (highestScore == null || att.getScore() > highestScore) {
                                        highestScore = att.getScore();
                                    }
                                }
                            }
                            VBox card = buildQuizCard(q, highestScore);
                            card.setPrefWidth(380);
                            card.setMinWidth(320);
                            quizContainer.getChildren().add(card);
                        }
                    }
                };

                searchQuizField.textProperty().addListener((obs, oldVal, newVal) -> updateStudentList.run());
                updateStudentList.run();
                leftColumn.getChildren().add(quizContainer);
            });
        });
        fetchThread.setDaemon(true);
        fetchThread.start();

        return mainLayout;
    }

    // =====================================================================
    //  TEACHER VIEW
    // =====================================================================

    private Node buildTeacherQuizContent() {
        HBox mainLayout = new HBox(20);
        mainLayout.setMaxWidth(Double.MAX_VALUE);

        VBox leftColumn = new VBox(20);
        leftColumn.setMaxWidth(Double.MAX_VALUE);
        HBox.setHgrow(leftColumn, Priority.ALWAYS);

        // Header with "Buat Kuis Baru" button
        HBox headerBox = new HBox(12);
        headerBox.setAlignment(Pos.CENTER_LEFT);
        headerBox.setPadding(new Insets(0, 0, 10, 0));

        VBox titleBox = new VBox(4);
        HBox titleRow = new HBox(10);
        titleRow.setAlignment(Pos.CENTER_LEFT);
        Label quizTitle = new Label("Kelola Kuis & Evaluasi");
        quizTitle.getStyleClass().add("section-title");
        quizTitle.setStyle("-fx-font-size: 20px;");
        titleRow.getChildren().add(quizTitle);
        Label quizSub = new Label("Lihat hasil pengerjaan kuis siswa dan buat kuis baru.");
        quizSub.getStyleClass().add("card-description");
        titleBox.getChildren().addAll(titleRow, quizSub);
        HBox.setHgrow(titleBox, Priority.ALWAYS);

        Button btnCreateQuiz = buildCreateQuizButton();

        headerBox.getChildren().addAll(titleBox, btnCreateQuiz);

        // Daftar Kuis Tersedia
        HBox quizzesTitleRow = new HBox(12);
        quizzesTitleRow.setAlignment(Pos.CENTER_LEFT);
        Label quizzesTitle = new Label("Daftar Kuis Tersedia");
        quizzesTitle.setStyle("-fx-font-weight: bold; -fx-text-fill: -fx-primary-text; -fx-font-size: 15px;");
        
        TextField searchQuizField = new TextField();
        searchQuizField.setPromptText("🔍 Cari kuis...");
        searchQuizField.getStyleClass().add("input-field");
        searchQuizField.setPrefWidth(220);
        
        Region qSpacer = new Region();
        HBox.setHgrow(qSpacer, Priority.ALWAYS);
        
        quizzesTitleRow.getChildren().addAll(quizzesTitle, qSpacer, searchQuizField);

        // Loading indicator
        ProgressIndicator loading = new ProgressIndicator();
        loading.setPrefSize(30, 30);
        HBox loadingRow = new HBox(10, loading, new Label("Memuat kuis..."));
        loadingRow.setAlignment(Pos.CENTER_LEFT);

        VBox quizzesListContainer = new VBox(10);
        quizzesListContainer.setMaxWidth(Double.MAX_VALUE);
        quizzesListContainer.getChildren().add(loadingRow);

        // Student Results Container
        HBox resultsTitleRow = new HBox(12);
        resultsTitleRow.setAlignment(Pos.CENTER_LEFT);
        resultsTitleRow.setPadding(new Insets(10, 0, 0, 0));
        
        Label resultsTitle = new Label("Hasil Pengerjaan Siswa");
        resultsTitle.setStyle("-fx-font-weight: bold; -fx-text-fill: -fx-primary-text; -fx-font-size: 15px;");
        
        Region rSpacer = new Region();
        HBox.setHgrow(rSpacer, Priority.ALWAYS);
        
        Button btnExportResults = new Button("Export Nilai");
        btnExportResults.getStyleClass().addAll("btn-secondary", "btn-small");
        btnExportResults.setStyle("-fx-background-color: #059669; -fx-text-fill: white;");
        btnExportResults.setDisable(true); // di-enable setelah data dimuat
        
        resultsTitleRow.getChildren().addAll(resultsTitle, rSpacer, btnExportResults);

        VBox resultsContainer = new VBox(12);
        resultsContainer.setMaxWidth(Double.MAX_VALUE);
        Label emptyResultsLbl = new Label("Belum ada data pengerjaan kuis siswa.");
        emptyResultsLbl.setStyle("-fx-text-fill: -fx-secondary-text; -fx-font-style: italic;");
        resultsContainer.getChildren().add(emptyResultsLbl);

        leftColumn.getChildren().addAll(headerBox, quizzesTitleRow, quizzesListContainer, resultsTitleRow, resultsContainer);

        // Right side: Quiz Summary Stats
        VBox rightColumn = new VBox(16);
        rightColumn.setPadding(new Insets(20));
        rightColumn.getStyleClass().add("section-box");
        rightColumn.setPrefWidth(320);
        rightColumn.setMaxWidth(320);

        Label statsTitle = new Label("Statistik Kelas");
        statsTitle.getStyleClass().add("section-title");
        statsTitle.setStyle("-fx-font-size: 15px;");

        rightColumn.getChildren().addAll(
            statsTitle,
            buildMiniStatRow("Rata-rata Nilai", "73.4", "%", "#FF7A00"),
            buildMiniStatRow("Partisipasi Siswa", "92", "%", "#059669"),
            buildMiniStatRow("Total Percobaan", "12", "kali", "#D97706")
        );

        mainLayout.getChildren().addAll(leftColumn, rightColumn);

        // Fetch dari backend di thread terpisah
        Thread fetchThread = new Thread(() -> {
            invalidateCache();
            List<QuizData> quizzes = getQuizzes();
            List<QuizService.QuizAttemptData> attempts = quizService.getAllAttempts();
            java.util.Map<String, Double> stats = controller.getDashboardService().getDashboardStats();

            Platform.runLater(() -> {
                Runnable updateTeacherList = () -> {
                    quizzesListContainer.getChildren().clear();
                    String query = searchQuizField.getText().toLowerCase();
                    List<QuizData> filtered = quizzes.stream()
                        .filter(q -> q.getTitle().toLowerCase().contains(query) || (q.getDescription() != null && q.getDescription().toLowerCase().contains(query)))
                        .toList();

                    if (filtered.isEmpty()) {
                        Label emptyLbl = new Label("Tidak ada kuis ditemukan.");
                        emptyLbl.setStyle("-fx-text-fill: -fx-secondary-text; -fx-font-style: italic;");
                        quizzesListContainer.getChildren().add(emptyLbl);
                    } else {
                        for (QuizData q : filtered) {
                            quizzesListContainer.getChildren().add(buildTeacherQuizRow(q));
                        }
                    }
                };

                searchQuizField.textProperty().addListener((obs, oldVal, newVal) -> updateTeacherList.run());
                updateTeacherList.run();

                resultsContainer.getChildren().clear();
                if (attempts.isEmpty()) {
                    resultsContainer.getChildren().add(emptyResultsLbl);
                } else {
                    for (QuizService.QuizAttemptData attempt : attempts) {
                        resultsContainer.getChildren().add(buildStudentResultItem(
                            attempt.getStudentName(),
                            attempt.getQuizTitle(),
                            attempt.getScore(),
                            attempt.getAttemptDate()
                        ));
                    }
                    btnExportResults.setDisable(false);
                    btnExportResults.setOnAction(e -> exportQuizResultsToCsv(attempts, btnExportResults.getScene().getWindow()));
                }

                // Update Stats
                double avgScore = stats.getOrDefault("averageQuizScore", 0.0);
                double partRate = stats.getOrDefault("participationRate", 0.0);
                int totalAtt = stats.getOrDefault("totalQuizAttempts", 0.0).intValue();

                rightColumn.getChildren().clear();
                rightColumn.getChildren().addAll(
                    statsTitle,
                    buildMiniStatRow("Rata-rata Nilai", String.format("%.1f", avgScore), "poin", "#FF7A00"),
                    buildMiniStatRow("Partisipasi Siswa", String.format("%.1f", partRate), "%", "#059669"),
                    buildMiniStatRow("Total Percobaan", String.valueOf(totalAtt), "kali", "#D97706")
                );
            });
        });
        fetchThread.setDaemon(true);
        fetchThread.start();

        return mainLayout;
    }

    // =====================================================================
    //  UI BUILDER METHODS
    // =====================================================================

    private VBox buildQuizCard(QuizData quiz, Integer highestScore) {
        VBox card = new VBox(12);
        card.getStyleClass().add("material-card");
        card.setPadding(new Insets(20));

        HBox top = new HBox(8);
        top.setAlignment(Pos.CENTER_LEFT);
        Circle dot = new Circle(5);
        dot.setFill(Color.web(quiz.getColor()));
        Label diffLabel = new Label(quiz.getDifficulty());
        diffLabel.setStyle("-fx-font-size: 11px; -fx-font-weight: bold; -fx-text-fill: " + quiz.getColor() + ";");
        top.getChildren().addAll(dot, diffLabel);

        if (highestScore != null) {
            Region spacer = new Region();
            HBox.setHgrow(spacer, Priority.ALWAYS);
            
            Label scoreBadge = new Label("Nilai: " + highestScore);
            String badgeColor = highestScore >= 80 ? "#059669" : (highestScore >= 60 ? "#D97706" : "#DC2626");
            String badgeBg = highestScore >= 80 ? "#ECFDF5" : (highestScore >= 60 ? "#FFFBEA" : "#FEF2F2");
            scoreBadge.setStyle(
                "-fx-background-color: " + badgeBg + "; -fx-text-fill: " + badgeColor + "; " +
                "-fx-font-weight: bold; -fx-font-size: 11px; -fx-padding: 4px 10px; -fx-background-radius: 12px;"
            );
            top.getChildren().addAll(spacer, scoreBadge);
        }

        Label titleL = new Label(quiz.getTitle());
        titleL.getStyleClass().add("card-title");
        titleL.setStyle("-fx-font-size: 17px;");

        Label descL = new Label(quiz.getDescription());
        descL.getStyleClass().add("card-description");
        descL.setWrapText(true);

        // Teacher badge (show who created this quiz)
        HBox teacherRow = new HBox(6);
        teacherRow.setAlignment(Pos.CENTER_LEFT);
        if (quiz.getTeacherName() != null && !quiz.getTeacherName().isEmpty()) {
            Label teacherBadge = new Label("👨‍🏫 " + quiz.getTeacherName());
            teacherBadge.setStyle("-fx-font-size: 11px; -fx-text-fill: #2563EB; -fx-font-weight: bold; " +
                    "-fx-background-color: #DBEAFE; -fx-padding: 2 8; -fx-background-radius: 8;");
            teacherRow.getChildren().add(teacherBadge);
        }

        javafx.scene.control.Separator div = new javafx.scene.control.Separator();

        HBox actions = new HBox(8);
        actions.setAlignment(Pos.CENTER_RIGHT);

        int questionCount = quiz.getQuestions() != null ? quiz.getQuestions().size() : 0;
        int minutes = "Mudah".equals(quiz.getDifficulty()) ? 5 : 8;

        Button startBtn = new Button(highestScore != null ? "Perbaiki Nilai" : "Mulai Kuis");
        startBtn.getStyleClass().addAll("btn-primary", "btn-small");
        startBtn.setOnAction(e -> startQuizGameplay(quiz));

        Label quizMeta = new Label(questionCount + " Soal  ·  " + minutes + " mnt");
        quizMeta.getStyleClass().add("progress-info");
        quizMeta.setStyle("-fx-font-weight: bold; -fx-text-fill: #FF7A00;");

        Region spc = new Region();
        HBox.setHgrow(spc, Priority.ALWAYS);
        actions.getChildren().addAll(quizMeta, spc, startBtn);

        card.getChildren().addAll(top, titleL, descL, teacherRow, div, actions);
        return card;
    }

    private HBox buildTeacherQuizRow(QuizData q) {
        HBox row = new HBox(16);
        row.setAlignment(Pos.CENTER_LEFT);
        row.getStyleClass().add("material-card");
        row.setPadding(new Insets(12, 16, 12, 16));

        Label icon = new Label("📝");
        icon.setStyle("-fx-font-size: 18px;");

        VBox quizInfo = new VBox(4);
        Label titleLbl = new Label(q.getTitle());
        titleLbl.setStyle("-fx-font-weight: bold; -fx-text-fill: -fx-primary-text; -fx-font-size: 14px;");
        int qCount = q.getQuestions() != null ? q.getQuestions().size() : 0;
        Label descLbl = new Label(qCount + " soal  ·  Tingkat: " + q.getDifficulty());
        descLbl.setStyle("-fx-text-fill: -fx-secondary-text; -fx-font-size: 12px;");
        quizInfo.getChildren().addAll(titleLbl, descLbl);
        HBox.setHgrow(quizInfo, Priority.ALWAYS);

        Button btnEdit = new Button("Edit");
        btnEdit.getStyleClass().addAll("btn-secondary", "btn-small");
        btnEdit.setStyle("-fx-background-color: #D97706; -fx-text-fill: white;");
        btnEdit.setOnAction(e -> {
            controller.getContentArea().getChildren().clear();
            controller.getContentArea().getChildren().add(buildQuizForm(q));
        });

        Button btnDelete = new Button("Hapus");
        btnDelete.getStyleClass().addAll("btn-ghost", "btn-small");
        btnDelete.setStyle("-fx-text-fill: #DC2626; -fx-border-color: #DC2626; -fx-border-radius: 4px;");
        btnDelete.setOnAction(e -> {
            ButtonType response = controller.showCustomAlert(
                Alert.AlertType.CONFIRMATION,
                "Konfirmasi Hapus Kuis",
                "Hapus Kuis?",
                "Apakah Anda yakin ingin menghapus kuis '" + q.getTitle() + "'?"
            );
            if (response == ButtonType.OK) {
                new Thread(() -> {
                    quizService.deleteQuiz(q.getId());
                    Platform.runLater(() -> {
                        invalidateCache();
                        controller.showQuizContent();
                    });
                }).start();
            }
        });

        row.getChildren().addAll(icon, quizInfo, btnEdit, btnDelete);
        return row;
    }

    private Button buildCreateQuizButton() {
        Button btnCreateQuiz = new Button();
        btnCreateQuiz.getStyleClass().add("btn-create-quiz-round");

        javafx.scene.shape.SVGPath plusIcon = new javafx.scene.shape.SVGPath();
        plusIcon.setContent("M19 13h-6v6h-2v-6H5v-2h6V5h2v6h6v2z");
        plusIcon.setStyle("-fx-fill: #FFFFFF;");

        btnCreateQuiz.setGraphic(plusIcon);
        btnCreateQuiz.setTooltip(new Tooltip("Buat Kuis Baru"));

        btnCreateQuiz.setStyle(
            "-fx-background-color: #059669; " +
            "-fx-background-radius: 50%; " +
            "-fx-min-width: 40px; -fx-min-height: 40px; " +
            "-fx-max-width: 40px; -fx-max-height: 40px; " +
            "-fx-cursor: hand; " +
            "-fx-effect: dropshadow(gaussian, rgba(5, 150, 105, 0.2), 6, 0, 0, 2);"
        );

        btnCreateQuiz.setOnMouseEntered(e -> btnCreateQuiz.setStyle(
            "-fx-background-color: #047857; " +
            "-fx-background-radius: 50%; " +
            "-fx-min-width: 40px; -fx-min-height: 40px; " +
            "-fx-max-width: 40px; -fx-max-height: 40px; " +
            "-fx-cursor: hand; " +
            "-fx-effect: dropshadow(gaussian, rgba(4, 120, 87, 0.4), 8, 0, 0, 3);"
        ));

        btnCreateQuiz.setOnMouseExited(e -> btnCreateQuiz.setStyle(
            "-fx-background-color: #059669; " +
            "-fx-background-radius: 50%; " +
            "-fx-min-width: 40px; -fx-min-height: 40px; " +
            "-fx-max-width: 40px; -fx-max-height: 40px; " +
            "-fx-cursor: hand; " +
            "-fx-effect: dropshadow(gaussian, rgba(5, 150, 105, 0.2), 6, 0, 0, 2);"
        ));

        btnCreateQuiz.setOnAction(e -> {
            controller.getContentArea().getChildren().clear();
            controller.getContentArea().getChildren().add(buildQuizForm(null));
        });

        return btnCreateQuiz;
    }

    private VBox buildQuizIllustrationPanel() {
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
        return rightColumn;
    }

    private VBox buildStudentResultItem(String studentName, String quizName, int score, String date) {
        HBox row = new HBox(16);
        row.setAlignment(Pos.CENTER_LEFT);

        String initial = String.valueOf(studentName.charAt(0)).toUpperCase();
        Label avatar = new Label(initial);
        avatar.setStyle(
            "-fx-background-color: #FFF0E0; -fx-text-fill: #FF7A00; -fx-font-weight: bold; " +
            "-fx-font-size: 14px; -fx-min-width: 38px; -fx-min-height: 38px; " +
            "-fx-max-width: 38px; -fx-max-height: 38px; -fx-alignment: center; -fx-background-radius: 50%;"
        );

        VBox studentInfo = new VBox(4);
        Label nameLbl = new Label(studentName);
        nameLbl.setStyle("-fx-font-weight: bold; -fx-text-fill: -fx-primary-text; -fx-font-size: 14px;");
        Label quizLbl = new Label(quizName + "  ·  " + date);
        quizLbl.setStyle("-fx-text-fill: -fx-secondary-text; -fx-font-size: 12px;");
        studentInfo.getChildren().addAll(nameLbl, quizLbl);
        HBox.setHgrow(studentInfo, Priority.ALWAYS);

        Label scoreBadge = new Label(score + " / 100");
        String badgeColor = score >= 80 ? "#059669" : (score >= 60 ? "#D97706" : "#DC2626");
        String badgeBg = score >= 80 ? "#ECFDF5" : (score >= 60 ? "#FFFBEA" : "#FEF2F2");
        scoreBadge.setStyle(
            "-fx-background-color: " + badgeBg + "; -fx-text-fill: " + badgeColor + "; " +
            "-fx-font-weight: bold; -fx-font-size: 13px; -fx-padding: 6px 14px; -fx-background-radius: 20px;"
        );

        row.getChildren().addAll(avatar, studentInfo, scoreBadge);

        VBox card = new VBox(row);
        card.getStyleClass().add("material-card");
        card.setPadding(new Insets(14, 18, 14, 18));
        return card;
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

    private void exportQuizResultsToCsv(List<QuizService.QuizAttemptData> attempts, javafx.stage.Window ownerWindow) {
        javafx.stage.FileChooser fileChooser = new javafx.stage.FileChooser();
        fileChooser.setTitle("Simpan Data Nilai Kuis");
        fileChooser.getExtensionFilters().add(new javafx.stage.FileChooser.ExtensionFilter("CSV Files", "*.csv"));
        fileChooser.setInitialFileName("nilai_kuis.csv");
        
        java.io.File file = fileChooser.showSaveDialog(ownerWindow);
        if (file != null) {
            try (java.io.PrintWriter writer = new java.io.PrintWriter(new java.io.FileWriter(file))) {
                writer.println("Nama Siswa,Judul Kuis,Skor,Tanggal");
                for (QuizService.QuizAttemptData att : attempts) {
                    writer.printf("\"%s\",\"%s\",%d,\"%s\"%n",
                            att.getStudentName(),
                            att.getQuizTitle(),
                            att.getScore(),
                            att.getAttemptDate());
                }
                controller.showToast("Data nilai kuis berhasil diekspor ke: " + file.getName(), "SUCCESS");
            } catch (java.io.IOException ex) {
                System.err.println("Gagal mengekspor data: " + ex.getMessage());
                controller.showToast("Gagal menulis file: " + ex.getMessage(), "ERROR");
            }
        }
    }

    // =====================================================================
    //  QUIZ FORM (CREATE / EDIT) — For Teachers
    // =====================================================================

    public Node buildQuizForm(QuizData quizToEdit) {
        boolean isEdit = (quizToEdit != null);
        VBox root = new VBox(20);
        root.setMaxWidth(Double.MAX_VALUE);

        // Header Box
        VBox headerBox = new VBox(6);
        headerBox.getStyleClass().add("section-box");
        headerBox.setPadding(new Insets(20));

        Label iconLabel = new Label("✨");
        iconLabel.setStyle("-fx-font-size: 32px;");
        Label titleLabel = new Label(isEdit ? "Edit Kuis" : "Buat Kuis Baru");
        titleLabel.getStyleClass().add("section-title");
        Label subLabel = new Label(isEdit
            ? "Ubah formulir di bawah ini untuk memperbarui kuis bagi siswa."
            : "Lengkapi formulir di bawah ini untuk membuat kuis baru bagi siswa.");
        subLabel.getStyleClass().add("card-description");
        headerBox.getChildren().addAll(iconLabel, titleLabel, subLabel);

        // Form Card
        VBox formCard = new VBox(16);
        formCard.getStyleClass().add("material-card");
        formCard.setPadding(new Insets(24));

        // Judul Input
        TextField txtTitle = new TextField(isEdit ? quizToEdit.getTitle() : "");
        txtTitle.setPromptText("Contoh: Kuis: Abstract Class & Interface");
        VBox titleBox = controller.createInputField("Judul Kuis", txtTitle);

        // Deskripsi Input
        TextArea txtDesc = new TextArea(isEdit ? quizToEdit.getDescription() : "");
        txtDesc.setPrefHeight(60);
        txtDesc.setWrapText(true);
        txtDesc.setPromptText("Tuliskan deskripsi singkat kuis ini...");
        VBox descBox = controller.createInputField("Deskripsi Singkat", txtDesc);

        // Difficulty ComboBox
        ComboBox<String> cmbDiff = new ComboBox<>();
        cmbDiff.getItems().addAll("Mudah", "Sedang", "Sulit");
        cmbDiff.setValue(isEdit ? quizToEdit.getDifficulty() : "Mudah");
        cmbDiff.setMaxWidth(Double.MAX_VALUE);
        VBox diffBox = controller.createInputField("Tingkat Kesulitan", cmbDiff);

        // Question fields container
        VBox questionsBox = new VBox(12);

        HBox countSelectorRow = new HBox(12);
        countSelectorRow.setAlignment(Pos.CENTER_LEFT);

        Label lblSelectCount = new Label("Jumlah Soal:");
        lblSelectCount.getStyleClass().add("input-label");

        ComboBox<Integer> cmbQuestionCount = new ComboBox<>();
        cmbQuestionCount.getItems().addAll(1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 12, 15);
        int initialCount = isEdit ? quizToEdit.getQuestions().size() : 3;
        cmbQuestionCount.setValue(initialCount);
        cmbQuestionCount.getStyleClass().add("input-field");
        cmbQuestionCount.setPrefWidth(150);

        Button btnAddQ = new Button("+ Tambah Soal");
        btnAddQ.getStyleClass().addAll("btn-secondary", "btn-small");
        btnAddQ.setStyle("-fx-background-color: #059669; -fx-text-fill: white;");

        Button btnRemoveQ = new Button("- Hapus Soal");
        btnRemoveQ.getStyleClass().addAll("btn-ghost", "btn-small");
        btnRemoveQ.setStyle("-fx-text-fill: #DC2626; -fx-border-color: #DC2626; -fx-border-radius: 4px;");

        countSelectorRow.getChildren().addAll(lblSelectCount, cmbQuestionCount, btnAddQ, btnRemoveQ);

        VBox questionsContainer = new VBox(16);

        // Data holder untuk setiap soal: questionText, optionA-D, correctIndex
        List<QuestionFormFields> questionFieldsList = new ArrayList<>();

        Runnable updateQuestionFields = () -> {
            questionsContainer.getChildren().clear();
            for (int i = 0; i < questionFieldsList.size(); i++) {
                QuestionFormFields qf = questionFieldsList.get(i);

                VBox singleQBox = new VBox(8);
                singleQBox.setStyle(
                    "-fx-background-color: #FAF8F3; -fx-background-radius: 8px; " +
                    "-fx-border-color: #E5E0D8; -fx-border-radius: 8px; -fx-border-width: 1px; " +
                    "-fx-padding: 14px;"
                );

                Label lblNumber = new Label("Soal " + (i + 1));
                lblNumber.getStyleClass().add("input-label");
                lblNumber.setStyle("-fx-font-size: 13px; -fx-text-fill: #FF7A00; -fx-font-weight: bold;");

                qf.questionField.getStyleClass().add("input-field");
                qf.questionField.setPromptText("Tulis pertanyaan soal " + (i + 1) + "...");

                // Opsi A-D
                HBox optARow = buildOptionRow("A", qf.optionA);
                HBox optBRow = buildOptionRow("B", qf.optionB);
                HBox optCRow = buildOptionRow("C", qf.optionC);
                HBox optDRow = buildOptionRow("D", qf.optionD);

                // Jawaban Benar
                HBox correctRow = new HBox(8);
                correctRow.setAlignment(Pos.CENTER_LEFT);
                Label correctLbl = new Label("Jawaban Benar:");
                correctLbl.setStyle("-fx-font-size: 12px; -fx-font-weight: bold; -fx-text-fill: #059669;");
                qf.correctCombo.getStyleClass().add("input-field");
                qf.correctCombo.setPrefWidth(150);
                correctRow.getChildren().addAll(correctLbl, qf.correctCombo);

                singleQBox.getChildren().addAll(lblNumber, qf.questionField, optARow, optBRow, optCRow, optDRow, correctRow);
                questionsContainer.getChildren().add(singleQBox);
            }
        };

        // Populate with existing questions or initialize default
        if (isEdit && quizToEdit.getQuestions() != null) {
            for (QuestionData qd : quizToEdit.getQuestions()) {
                questionFieldsList.add(new QuestionFormFields(
                    qd.getQuestionText(),
                    qd.getOptionA(), qd.getOptionB(), qd.getOptionC(), qd.getOptionD(),
                    qd.getCorrectOptionIndex()
                ));
            }
        } else {
            for (int i = 0; i < initialCount; i++) {
                questionFieldsList.add(new QuestionFormFields("", "", "", "", "", 0));
            }
        }
        updateQuestionFields.run();

        cmbQuestionCount.valueProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal == null) return;
            int targetCount = newVal;
            while (questionFieldsList.size() < targetCount) {
                questionFieldsList.add(new QuestionFormFields("", "", "", "", "", 0));
            }
            while (questionFieldsList.size() > targetCount) {
                questionFieldsList.remove(questionFieldsList.size() - 1);
            }
            updateQuestionFields.run();
        });

        btnAddQ.setOnAction(e -> {
            int currentCount = questionFieldsList.size();
            if (currentCount < 15) {
                cmbQuestionCount.setValue(currentCount + 1);
            }
        });

        btnRemoveQ.setOnAction(e -> {
            int currentCount = questionFieldsList.size();
            if (currentCount > 1) {
                cmbQuestionCount.setValue(currentCount - 1);
            }
        });

        questionsBox.getChildren().addAll(countSelectorRow, questionsContainer);

        // Error Msg
        Label errorMsg = new Label();
        errorMsg.getStyleClass().add("error-label");
        errorMsg.setVisible(false);
        errorMsg.setManaged(false);

        // Action Buttons Row
        HBox buttonRow = new HBox(12);
        buttonRow.setAlignment(Pos.CENTER_RIGHT);

        Button btnCancel = new Button("Batal");
        btnCancel.getStyleClass().addAll("btn-ghost", "btn-medium");
        btnCancel.setOnAction(e -> controller.showQuizContent());

        Button btnSave = new Button(isEdit ? "Simpan Perubahan" : "Simpan Kuis");
        btnSave.getStyleClass().addAll("btn-primary", "btn-medium");
        btnSave.setStyle("-fx-background-color: #059669;");
        btnSave.setOnAction(e -> {
            String title = txtTitle.getText().trim();
            String desc = txtDesc.getText().trim();
            String difficulty = cmbDiff.getValue();

            if (title.isEmpty()) {
                showFormError(errorMsg, "Judul kuis tidak boleh kosong!");
                return;
            }

            List<QuestionData> questionDataList = new ArrayList<>();
            for (int i = 0; i < questionFieldsList.size(); i++) {
                QuestionFormFields qf = questionFieldsList.get(i);
                String qText = qf.questionField.getText().trim();
                String optA = qf.optionA.getText().trim();
                String optB = qf.optionB.getText().trim();
                String optC = qf.optionC.getText().trim();
                String optD = qf.optionD.getText().trim();

                if (qText.isEmpty()) {
                    showFormError(errorMsg, "Soal " + (i + 1) + " tidak boleh kosong!");
                    return;
                }
                if (optA.isEmpty() || optB.isEmpty() || optC.isEmpty() || optD.isEmpty()) {
                    showFormError(errorMsg, "Semua opsi jawaban untuk Soal " + (i + 1) + " harus diisi!");
                    return;
                }

                int correctIdx = qf.correctCombo.getSelectionModel().getSelectedIndex();
                questionDataList.add(new QuestionData(0, qText, optA, optB, optC, optD, correctIdx));
            }

            if (desc.isEmpty()) {
                desc = questionFieldsList.size() + " soal pilihan ganda tentang " + title + ".";
            }

            // Simpan ke backend via thread
            final String finalDesc = desc;
            btnSave.setDisable(true);
            btnSave.setText("Menyimpan...");

            new Thread(() -> {
                QuizData result;
                if (isEdit) {
                    result = quizService.updateQuiz(quizToEdit.getId(), title, finalDesc, difficulty, questionDataList);
                } else {
                    Long currentTeacherId = null;
                    if (controller.getCurrentUser() instanceof Teacher teacher) {
                        currentTeacherId = teacher.getId();
                    }
                    result = quizService.createQuiz(title, finalDesc, difficulty, questionDataList, currentTeacherId);
                }
                Platform.runLater(() -> {
                    btnSave.setDisable(false);
                    btnSave.setText(isEdit ? "Simpan Perubahan" : "Simpan Kuis");

                    if (result != null) {
                        controller.showToast(isEdit ? "Kuis Berhasil Diperbarui!" : "Kuis Baru Berhasil Dibuat!", "SUCCESS");
                        invalidateCache();
                        controller.showQuizContent();
                    } else {
                        showFormError(errorMsg, "Gagal menyimpan kuis ke server. Periksa koneksi backend.");
                    }
                });
            }).start();
        });

        buttonRow.getChildren().addAll(btnCancel, btnSave);
        formCard.getChildren().addAll(titleBox, descBox, diffBox, questionsBox, errorMsg, buttonRow);
        root.getChildren().addAll(headerBox, formCard);
        return root;
    }

    private HBox buildOptionRow(String letter, TextField field) {
        HBox row = new HBox(8);
        row.setAlignment(Pos.CENTER_LEFT);
        Label lbl = new Label(letter + ".");
        lbl.setStyle("-fx-font-size: 12px; -fx-font-weight: bold; -fx-text-fill: -fx-secondary-text; -fx-min-width: 20px;");
        field.getStyleClass().add("input-field");
        field.setPromptText("Opsi jawaban " + letter);
        HBox.setHgrow(field, Priority.ALWAYS);
        row.getChildren().addAll(lbl, field);
        return row;
    }

    /**
     * Helper class untuk menyimpan state form field per-soal.
     */
    private static class QuestionFormFields {
        TextField questionField;
        TextField optionA;
        TextField optionB;
        TextField optionC;
        TextField optionD;
        ComboBox<String> correctCombo;

        QuestionFormFields(String qText, String oA, String oB, String oC, String oD, int correctIdx) {
            questionField = new TextField(qText);
            optionA = new TextField(oA);
            optionB = new TextField(oB);
            optionC = new TextField(oC);
            optionD = new TextField(oD);
            correctCombo = new ComboBox<>();
            correctCombo.getItems().addAll("A", "B", "C", "D");
            correctCombo.setValue(switch (correctIdx) {
                case 1 -> "B";
                case 2 -> "C";
                case 3 -> "D";
                default -> "A";
            });
        }
    }

    // =====================================================================
    //  QUIZ GAMEPLAY ENGINE (INTERACTIVE)
    // =====================================================================

    private void startQuizGameplay(QuizData quiz) {
        activeQuizTitle = quiz.getTitle();
        activeQuizId = quiz.getId();
        currentQuestionIndex = 0;
        correctAnswersCount = 0;
        selectedOptionIndex = -1;

        if (quizTimer != null) {
            quizTimer.stop();
        }

        int minutes = "Mudah".equals(quiz.getDifficulty()) ? 5 : 8;
        secondsRemaining = minutes * 60;

        // Konversi QuestionData dari backend ke QuizQuestion internal
        activeQuestions = new ArrayList<>();
        if (quiz.getQuestions() != null) {
            for (QuestionData qd : quiz.getQuestions()) {
                activeQuestions.add(new QuizQuestion(
                    qd.getQuestionText(),
                    qd.getOptionsArray(),
                    qd.getCorrectOptionIndex()
                ));
            }
        }

        if (activeQuestions.isEmpty()) {
            controller.showCustomAlert(
                Alert.AlertType.WARNING,
                "Kuis Kosong",
                "Kuis Tidak Memiliki Soal",
                "Kuis ini belum memiliki pertanyaan. Hubungi pengajar Anda."
            );
            return;
        }

        userAnswers = new int[activeQuestions.size()];
        java.util.Arrays.fill(userAnswers, -1);

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
        controller.getContentArea().getChildren().clear();

        QuizQuestion q = activeQuestions.get(currentQuestionIndex);
        selectedOptionIndex = userAnswers[currentQuestionIndex];

        VBox quizBox = new VBox(20);
        quizBox.getStyleClass().add("material-container");
        quizBox.setPadding(new Insets(28));
        quizBox.setMaxWidth(800);

        HBox topRow = new HBox(12);
        topRow.setAlignment(Pos.CENTER_LEFT);

        Button prevBtn = new Button("Kembali");
        prevBtn.getStyleClass().addAll("btn-ghost", "btn-medium");
        prevBtn.setStyle(
            "-fx-text-fill: #FF7A00; " +
            "-fx-border-color: #FF7A00; " +
            "-fx-border-radius: 8px; " +
            "-fx-border-width: 1.5px; " +
            "-fx-font-weight: bold;"
        );
        prevBtn.setOnAction(e -> {
            if (currentQuestionIndex > 0) {
                currentQuestionIndex--;
                renderQuizQuestion();
            }
        });

        if (currentQuestionIndex == 0) {
            prevBtn.setVisible(false);
            prevBtn.setManaged(false);
        }

        Label quizTitleLabel = new Label(activeQuizTitle);
        quizTitleLabel.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: -fx-secondary-text;");

        Region spacerTop = new Region();
        HBox.setHgrow(spacerTop, Priority.ALWAYS);

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

        Button closeBtn = new Button();
        closeBtn.getStyleClass().add("btn-back-round");
        javafx.scene.shape.SVGPath closeIcon = new javafx.scene.shape.SVGPath();
        closeIcon.setContent("M19 6.41L17.59 5 12 10.59 6.41 5 5 6.41 10.59 12 5 17.59 6.41 19 12 13.41 17.59 19 19 17.59 13.41 12z");
        closeIcon.setStyle("-fx-fill: #EF4444;");
        closeBtn.setGraphic(closeIcon);
        closeBtn.setTooltip(new Tooltip("Keluar dari Kuis"));
        closeBtn.setStyle(
            "-fx-background-color: #FFFFFF; -fx-background-radius: 20px; " +
            "-fx-border-color: #E5E0D8; -fx-border-radius: 20px; -fx-border-width: 1.5px; " +
            "-fx-padding: 8px; -fx-cursor: hand;"
        );
        closeBtn.setOnMouseEntered(ev -> {
            closeBtn.setStyle(
                "-fx-background-color: #FEF2F2; -fx-background-radius: 20px; " +
                "-fx-border-color: #EF4444; -fx-border-radius: 20px; -fx-border-width: 1.5px; " +
                "-fx-padding: 8px; -fx-cursor: hand;"
            );
            closeIcon.setStyle("-fx-fill: #DC2626;");
        });
        closeBtn.setOnMouseExited(ev -> {
            closeBtn.setStyle(
                "-fx-background-color: #FFFFFF; -fx-background-radius: 20px; " +
                "-fx-border-color: #E5E0D8; -fx-border-radius: 20px; -fx-border-width: 1.5px; " +
                "-fx-padding: 8px; -fx-cursor: hand;"
            );
            closeIcon.setStyle("-fx-fill: #EF4444;");
        });
        closeBtn.setOnAction(e -> confirmExitQuiz());

        topRow.getChildren().addAll(quizTitleLabel, spacerTop, timerLabel, progressLabel, closeBtn);

        ProgressBar quizProgressBar = new ProgressBar((double) (currentQuestionIndex + 1) / activeQuestions.size());
        quizProgressBar.setMaxWidth(Double.MAX_VALUE);
        quizProgressBar.setPrefHeight(8);
        quizProgressBar.getStyleClass().add("daily-progress");

        javafx.scene.control.Separator separator = new javafx.scene.control.Separator();

        VBox questionBox = new VBox(12);
        Label qNumLabel = new Label("PERTANYAAN " + (currentQuestionIndex + 1));
        qNumLabel.setStyle("-fx-font-size: 11px; -fx-font-weight: bold; -fx-text-fill: -fx-secondary-text; -fx-letter-spacing: 1.5;");

        Label qTextLabel = new Label(q.question);
        qTextLabel.setStyle("-fx-font-size: 17px; -fx-font-weight: bold; -fx-text-fill: -fx-primary-text;");
        qTextLabel.setWrapText(true);
        qTextLabel.setMaxWidth(700);

        questionBox.getChildren().addAll(qNumLabel, qTextLabel);

        VBox optionsContainer = new VBox(12);
        optionsContainer.setMaxWidth(700);

        VBox[] optionCards = new VBox[q.options.length];
        Button nextBtn = new Button(currentQuestionIndex == activeQuestions.size() - 1 ? "Kirim Jawaban" : "Selanjutnya");
        nextBtn.getStyleClass().addAll("btn-primary", "btn-medium");
        nextBtn.setDisable(selectedOptionIndex == -1);

        char optLetter = 'A';
        for (int i = 0; i < q.options.length; i++) {
            final int idx = i;
            String optionText = q.options[i];

            HBox cardContent = new HBox(14);
            cardContent.setAlignment(Pos.CENTER_LEFT);

            Label letterBadge = new Label(String.valueOf((char)(optLetter + i)));
            boolean isSelected = (selectedOptionIndex == idx);
            letterBadge.setStyle(isSelected ?
                "-fx-background-color: #FF7A00; " +
                "-fx-text-fill: #FFFFFF; " +
                "-fx-font-weight: bold; " +
                "-fx-font-size: 13px; " +
                "-fx-min-width: 28px; -fx-min-height: 28px; " +
                "-fx-max-width: 28px; -fx-max-height: 28px; " +
                "-fx-alignment: center; " +
                "-fx-background-radius: 50%;"
                :
                "-fx-background-color: #FAF8F3; " +
                "-fx-text-fill: -fx-secondary-text; " +
                "-fx-font-weight: bold; " +
                "-fx-font-size: 13px; " +
                "-fx-min-width: 28px; -fx-min-height: 28px; " +
                "-fx-max-width: 28px; -fx-max-height: 28px; " +
                "-fx-alignment: center; " +
                "-fx-background-radius: 50%;" +
                "-fx-border-color: #E5E0D8; -fx-border-radius: 50%; -fx-border-width: 1px;"
            );

            Label optionLabel = new Label(optionText);
            optionLabel.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: -fx-primary-text;");
            optionLabel.setWrapText(true);
            HBox.setHgrow(optionLabel, Priority.ALWAYS);

            cardContent.getChildren().addAll(letterBadge, optionLabel);

            VBox card = new VBox(cardContent);
            card.setPadding(new Insets(14, 18, 14, 18));

            String baseStyle =
                "-fx-background-color: #FFFFFF; " +
                "-fx-background-radius: 12px; " +
                "-fx-border-color: #E5E0D8; " +
                "-fx-border-radius: 12px; " +
                "-fx-border-width: 1.5px; " +
                "-fx-cursor: hand;";
            String selectedStyle =
                "-fx-background-color: #FFF0E0; " +
                "-fx-background-radius: 12px; " +
                "-fx-border-color: #FF7A00; " +
                "-fx-border-radius: 12px; " +
                "-fx-border-width: 1.5px; " +
                "-fx-cursor: hand; " +
                "-fx-effect: dropshadow(gaussian, rgba(255, 122, 0, 0.1), 6, 0, 0, 1);";
            card.setStyle(isSelected ? selectedStyle : baseStyle);

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
                userAnswers[currentQuestionIndex] = idx;
                nextBtn.setDisable(false);

                for (int j = 0; j < optionCards.length; j++) {
                    if (j == idx) {
                        optionCards[j].setStyle(selectedStyle);
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
                        Label otherLetterBadge = (Label)((HBox)optionCards[j].getChildren().get(0)).getChildren().get(0);
                        otherLetterBadge.setStyle(
                            "-fx-background-color: #FAF8F3; " +
                            "-fx-text-fill: -fx-secondary-text; " +
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

        HBox actionRow = new HBox();
        actionRow.setAlignment(Pos.CENTER_LEFT);
        actionRow.setMaxWidth(Double.MAX_VALUE);

        Region spacerBottom = new Region();
        HBox.setHgrow(spacerBottom, Priority.ALWAYS);

        nextBtn.setOnAction(e -> handleNextQuestion());

        actionRow.getChildren().addAll(prevBtn, spacerBottom, nextBtn);

        quizBox.getChildren().addAll(topRow, quizProgressBar, separator, questionBox, optionsContainer, actionRow);
        controller.getContentArea().getChildren().add(quizBox);
        controller.getContentTitleLabel().setText("Pengerjaan Kuis");
    }

    private void handleNextQuestion() {
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
        Platform.runLater(() -> {
            controller.showCustomAlert(
                Alert.AlertType.WARNING,
                "Waktu Habis",
                "Waktu Pengerjaan Kuis Telah Habis!",
                "Kuis Anda akan otomatis dikirimkan berdasarkan jawaban yang sudah tersimpan."
            );
            renderQuizResult();
        });
    }

    private void confirmExitQuiz() {
        ButtonType response = controller.showCustomAlert(
            Alert.AlertType.CONFIRMATION,
            "Konfirmasi Keluar Kuis",
            "Keluar dari Pengerjaan Kuis?",
            "Semua jawaban Anda pada kuis ini akan hilang. Apakah Anda yakin?"
        );
        if (response == ButtonType.OK) {
            if (quizTimer != null) {
                quizTimer.stop();
            }
            controller.showQuizContent();
        }
    }

    private void renderQuizResult() {
        if (quizTimer != null) {
            quizTimer.stop();
        }

        controller.getContentArea().getChildren().clear();

        VBox resultBox = new VBox(24);
        resultBox.getStyleClass().add("material-container");
        resultBox.setPadding(new Insets(32));
        resultBox.setAlignment(Pos.CENTER);
        resultBox.setMaxWidth(600);

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

        correctAnswersCount = 0;
        for (int i = 0; i < activeQuestions.size(); i++) {
            if (userAnswers[i] == activeQuestions.get(i).correctIndex) {
                correctAnswersCount++;
            }
        }

        int score = (int) Math.round((double) correctAnswersCount * 100.0 / activeQuestions.size());

        // Simpan nilai ke backend
        new Thread(() -> {
            quizService.submitQuizScore(activeQuizId, controller.getCurrentUser().getId(), score);
        }).start();

        Label congratLabel = new Label(score >= 60 ? "Selamat! Kuis Selesai!" : "Kuis Selesai!");
        congratLabel.getStyleClass().add("banner-title");
        congratLabel.setStyle("-fx-font-size: 24px; -fx-text-fill: #FF7A00;");

        VBox scoreBox = new VBox(4);
        scoreBox.setAlignment(Pos.CENTER);
        Label scoreTitle = new Label("SKOR AKHIR KAMU");
        scoreTitle.setStyle("-fx-font-size: 11px; -fx-font-weight: bold; -fx-text-fill: -fx-secondary-text; -fx-letter-spacing: 1.5;");

        Label scoreLabel = new Label(score + " / 100");
        scoreLabel.setStyle("-fx-font-size: 44px; -fx-font-weight: bold; -fx-text-fill: " + (score >= 60 ? "#059669" : "#D97706") + ";");
        scoreBox.getChildren().addAll(scoreTitle, scoreLabel);

        ProgressBar scoreBar = new ProgressBar((double) score / 100.0);
        scoreBar.setPrefWidth(300);
        scoreBar.setPrefHeight(10);
        scoreBar.getStyleClass().add("daily-progress");
        scoreBar.setStyle("-fx-accent: " + (score >= 60 ? "#059669" : "#D97706") + ";");

        Label commentLabel = new Label();
        commentLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: -fx-secondary-text; -fx-text-alignment: center;");
        commentLabel.setWrapText(true);
        commentLabel.setMaxWidth(400);

        if (score == 100) {
            commentLabel.setText("Luar biasa sempurna! Kamu telah menguasai seluruh konsep materi ini dengan sangat matang. Pertahankan prestasimu!");
        } else if (score >= 60) {
            commentLabel.setText("Kerja bagus! Pemahaman konsep Anda sudah cukup baik. Pelajari sedikit lagi untuk meraih nilai sempurna pada percobaan berikutnya!");
        } else {
            commentLabel.setText("Terus berusaha! Baca kembali materi e-learning dan coba kuis ini sekali lagi untuk memperkuat pemahaman Anda.");
        }

        Button backBtn = new Button("Kembali ke Halaman Kuis");
        backBtn.getStyleClass().addAll("btn-primary", "btn-large");
        backBtn.setOnAction(e -> controller.showQuizContent());

        if (celebrationMascot != null) {
            resultBox.getChildren().addAll(celebrationMascot, congratLabel, scoreBox, scoreBar, commentLabel, backBtn);
        } else {
            resultBox.getChildren().addAll(congratLabel, scoreBox, scoreBar, commentLabel, backBtn);
        }

        controller.getContentArea().getChildren().add(resultBox);
        controller.getContentTitleLabel().setText("Evaluasi Kuis");
    }

    private void showFormError(Label lbl, String msg) {
        lbl.setText("⚠ " + msg);
        lbl.setVisible(true);
        lbl.setManaged(true);
    }
}

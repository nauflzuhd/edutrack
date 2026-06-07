package com.doamamah.edutrack.fe.dashboard;

import com.doamamah.edutrack.fe.user.Student;
import com.doamamah.edutrack.fe.user.Teacher;
import com.doamamah.edutrack.fe.user.User;
import com.doamamah.edutrack.fe.quiz.QuizService;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

public class DashboardHomeView {

    private final DashboardController controller;

    public DashboardHomeView(DashboardController controller) {
        this.controller = controller;
    }

    public Node buildContent() {
        User currentUser = controller.getCurrentUser();
        if (currentUser instanceof Teacher) {
            return buildTeacherDashboard(currentUser);
        } else {
            return buildStudentDashboard(currentUser);
        }
    }

    private Node buildStudentDashboard(User currentUser) {
        HBox mainLayout = new HBox(20);
        mainLayout.setMaxWidth(Double.MAX_VALUE);

        // --- KOLOM KIRI (Utama) ---
        VBox leftColumn = new VBox(20);
        leftColumn.setMaxWidth(Double.MAX_VALUE);
        HBox.setHgrow(leftColumn, Priority.ALWAYS);

        // Welcome Banner
        HBox banner = new HBox(20);
        banner.getStyleClass().add("dashboard-banner");
        banner.setPadding(new Insets(12, 28, 12, 28));
        banner.setAlignment(Pos.CENTER_LEFT);

        VBox bannerText = new VBox(6);
        String greetTime = getGreetingByTime();
        String firstName = currentUser.getFullName() != null
                ? currentUser.getFullName().split(" ")[0] : "Pengguna";

        Label welcomeLabel = new Label(greetTime + ", " + firstName + "!");
        welcomeLabel.getStyleClass().add("banner-title");

        String todayStr = LocalDate.now().format(
                DateTimeFormatter.ofPattern("EEEE, d MMMM yyyy", Locale.of("id", "ID")));
        Label dateLabel = new Label(todayStr + "  ·  Siswa");
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
            banner.getChildren().add(bannerText);
        }

        // Stats
        java.util.Map<String, Double> stats = controller.getDashboardService().getDashboardStats();
        
        com.doamamah.edutrack.fe.enrollment.EnrollmentService enrollmentService = new com.doamamah.edutrack.fe.enrollment.EnrollmentService();
        java.util.List<Long> teacherIds = null;
        if (currentUser instanceof com.doamamah.edutrack.fe.user.Teacher teacher) {
            teacherIds = java.util.List.of(teacher.getId());
        } else if (currentUser instanceof com.doamamah.edutrack.fe.user.Student student) {
            teacherIds = enrollmentService.getEnrolledTeacherIds(student.getId());
            if (teacherIds.isEmpty()) {
                teacherIds = java.util.List.of(-1L);
            }
        }
        
        int totalMaterials = controller.getMaterialService().getAllMaterials(teacherIds).size();
        QuizService quizService = new QuizService();
        int totalQuizzes = quizService.getAllQuizzes(teacherIds).size();
        
        int totalAttempts = 0;
        double avgScore = 0.0;
        java.util.List<QuizService.QuizAttemptData> studentAttempts = quizService.getStudentAttempts(currentUser.getId());
        totalAttempts = studentAttempts.size();
        
        java.util.Set<Long> uniqueQuizzes = new java.util.HashSet<>();
        if (totalAttempts > 0) {
            double sum = 0;
            for (QuizService.QuizAttemptData attempt : studentAttempts) {
                sum += attempt.getScore();
                uniqueQuizzes.add(attempt.getQuizId());
            }
            avgScore = sum / totalAttempts;
        }

        int kuisTersedia = totalQuizzes;

        java.util.List<Long> viewedMaterials = controller.getMaterialService().getViewedMaterials(currentUser.getId());
        int materiTersedia = totalMaterials;

        HBox statsRow = new HBox(14);
        statsRow.setMaxWidth(Double.MAX_VALUE);
        statsRow.getChildren().addAll(
            buildRichStatCard("Materi Tersedia", String.valueOf(materiTersedia), "materi", "#FF7A00", "📚", 1.0),
            buildRichStatCard("Kuis Tersedia",   String.valueOf(kuisTersedia), "kuis",   "#059669", "📝", 1.0),
            buildRichStatCard("Rata-rata Nilai", String.format("%.1f", avgScore), "poin", "#D97706", "🎯", avgScore/100.0)
        );

        // Progress Belajar Hari Ini
        VBox progressSection = new VBox(10);
        progressSection.getStyleClass().add("section-box");
        progressSection.setPadding(new Insets(20));

        Label progressTitle = new Label("Aktivitas Pembelajaran (Kuis)");
        progressTitle.getStyleClass().add("section-title");

        int uniqueAttempts = uniqueQuizzes.size();
        double maxExpectedAttempts = totalQuizzes;
        double progressRatio = maxExpectedAttempts > 0 ? (double)uniqueAttempts / maxExpectedAttempts : 0.0;
        if (progressRatio > 1.0) progressRatio = 1.0;

        ProgressBar dailyBar = new ProgressBar(progressRatio);
        dailyBar.setMaxWidth(Double.MAX_VALUE);
        dailyBar.setPrefHeight(10);
        dailyBar.getStyleClass().add("daily-progress");

        HBox progressInfo = new HBox();
        progressInfo.setAlignment(Pos.CENTER_LEFT);
        Label pLeft = new Label(uniqueAttempts + " dari " + (int)maxExpectedAttempts + " kuis diselesaikan");
        pLeft.getStyleClass().add("progress-info");
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        Label pRight = new Label(String.format("%.1f%%", progressRatio * 100));
        pRight.getStyleClass().add("progress-percent");
        progressInfo.getChildren().addAll(pLeft, spacer, pRight);

        progressSection.getChildren().addAll(progressTitle, dailyBar, progressInfo);

        // Riwayat Kuis Terakhir
        VBox historySection = new VBox(10);
        historySection.getStyleClass().add("section-box");
        historySection.setPadding(new Insets(20));

        Label historyTitle = new Label("Riwayat Kuis Terakhir");
        historyTitle.getStyleClass().add("section-title");
        historySection.getChildren().add(historyTitle);

        if (studentAttempts.isEmpty()) {
            Label emptyLbl = new Label("Belum ada kuis yang dikerjakan.");
            emptyLbl.setStyle("-fx-text-fill: -fx-secondary-text; -fx-font-style: italic;");
            historySection.getChildren().add(emptyLbl);
        } else {
            int limit = Math.min(3, studentAttempts.size());
            for (int i = 0; i < limit; i++) {
                QuizService.QuizAttemptData att = studentAttempts.get(i);
                HBox attRow = new HBox(12);
                attRow.setAlignment(Pos.CENTER_LEFT);
                attRow.setPadding(new Insets(10));
                attRow.setStyle("-fx-background-color: #F9FAFB; -fx-background-radius: 6; -fx-border-color: #E5E7EB; -fx-border-radius: 6;");
                
                Label icon = new Label("🏆");
                icon.setStyle("-fx-font-size: 20px;");
                
                VBox textInfo = new VBox(2);
                Label titleLbl = new Label(att.getQuizTitle());
                titleLbl.setStyle("-fx-font-weight: bold; -fx-text-fill: -fx-primary-text;");
                Label dateLbl = new Label(att.getAttemptDate());
                dateLbl.setStyle("-fx-font-size: 11px; -fx-text-fill: -fx-secondary-text;");
                textInfo.getChildren().addAll(titleLbl, dateLbl);
                
                Region sp2 = new Region();
                HBox.setHgrow(sp2, Priority.ALWAYS);
                
                Label scoreLbl = new Label(att.getScore() + " Poin");
                scoreLbl.setStyle("-fx-font-weight: bold; -fx-text-fill: " + (att.getScore() >= 75 ? "#059669" : "#DC2626") + ";");
                
                attRow.getChildren().addAll(icon, textInfo, sp2, scoreLbl);
                historySection.getChildren().add(attRow);
            }
        }

        leftColumn.getChildren().addAll(banner, statsRow, progressSection, historySection);

        // --- KOLOM KANAN (Aksi Cepat & Tips) ---
        VBox rightColumn = new VBox(20);
        rightColumn.setPrefWidth(250);
        rightColumn.setMaxWidth(250);

        VBox actionsCard = new VBox(14);
        actionsCard.getStyleClass().add("section-box");
        actionsCard.setPadding(new Insets(20));

        Label actionTitle = new Label("Aksi Cepat");
        actionTitle.getStyleClass().add("section-title");

        Button goMaterials = new Button("Lihat Materi");
        goMaterials.getStyleClass().addAll("btn-primary", "btn-medium");
        goMaterials.setMaxWidth(Double.MAX_VALUE);
        goMaterials.setOnAction(e -> controller.showMaterialsContent());

        Button goQuiz = new Button("Ikuti Kuis");
        goQuiz.getStyleClass().addAll("btn-secondary", "btn-medium");
        goQuiz.setMaxWidth(Double.MAX_VALUE);
        goQuiz.setOnAction(e -> controller.showQuizContent());

        actionsCard.getChildren().addAll(actionTitle, goMaterials, goQuiz);

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

    private Node buildTeacherDashboard(User currentUser) {
        HBox mainLayout = new HBox(20);
        mainLayout.setMaxWidth(Double.MAX_VALUE);

        // --- KOLOM KIRI (Utama) ---
        VBox leftColumn = new VBox(20);
        leftColumn.setMaxWidth(Double.MAX_VALUE);
        HBox.setHgrow(leftColumn, Priority.ALWAYS);

        // Welcome Banner Khusus Guru (mengikuti standar palet)
        HBox banner = new HBox(20);
        banner.getStyleClass().add("dashboard-banner");
        banner.setPadding(new Insets(12, 28, 12, 28));
        banner.setAlignment(Pos.CENTER_LEFT);

        VBox bannerText = new VBox(6);
        String greetTime = getGreetingByTime();
        String firstName = currentUser.getFullName() != null
                ? currentUser.getFullName().split(" ")[0] : "Pengajar";

        Label welcomeLabel = new Label(greetTime + ", " + firstName + "!");
        welcomeLabel.getStyleClass().add("banner-title");

        String todayStr = LocalDate.now().format(
                DateTimeFormatter.ofPattern("EEEE, d MMMM yyyy", Locale.of("id", "ID")));
        Label dateLabel = new Label(todayStr + "  ·  Pengajar");
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
            banner.getChildren().add(bannerText);
        }

        // Stats
        java.util.List<Long> teacherIds = java.util.List.of(currentUser.getId());
        int totalMaterials = controller.getMaterialService().getAllMaterials(teacherIds).size();
        QuizService qs = new QuizService();
        java.util.List<QuizService.QuizData> teacherQuizzes = qs.getAllQuizzes(teacherIds);
        int totalQuizzes = teacherQuizzes.size();
        
        com.doamamah.edutrack.fe.enrollment.EnrollmentService enrollmentService = new com.doamamah.edutrack.fe.enrollment.EnrollmentService();
        int totalStudents = enrollmentService.getEnrolledStudents(currentUser.getId()).size();

        java.util.Set<Long> teacherQuizIds = new java.util.HashSet<>();
        for (QuizService.QuizData q : teacherQuizzes) {
            teacherQuizIds.add(q.getId());
        }
        
        java.util.List<QuizService.QuizAttemptData> allAttempts = qs.getAllAttempts();
        java.util.List<QuizService.QuizAttemptData> teacherAttempts = new java.util.ArrayList<>();
        for (QuizService.QuizAttemptData att : allAttempts) {
            if (teacherQuizIds.contains(att.getQuizId())) {
                teacherAttempts.add(att);
            }
        }
        int totalAttempts = teacherAttempts.size();

        HBox statsRow = new HBox(14);
        statsRow.setMaxWidth(Double.MAX_VALUE);
        statsRow.getChildren().addAll(
            buildRichStatCard("Total Siswa",   String.valueOf(totalStudents), "siswa aktif",  "#2563EB", "👥", 1.0),
            buildRichStatCard("Total Materi",  String.valueOf(totalMaterials),  "modul", "#FF7A00", "📚", 1.0),
            buildRichStatCard("Kuis Dibuat",   String.valueOf(totalQuizzes),  "ujian",   "#059669", "📝", 1.0)
        );

        // Ringkasan Aktivitas Kelas
        VBox activitySection = new VBox(12);
        activitySection.getStyleClass().add("section-box");
        activitySection.setPadding(new Insets(24));

        Label activityTitle = new Label("Ringkasan Keterlibatan Kelas");
        activityTitle.getStyleClass().add("section-title");

        HBox activityContent = new HBox(20);
        activityContent.setAlignment(Pos.CENTER_LEFT);

        VBox metricBox = new VBox(4);
        Label metricVal = new Label(String.valueOf(totalAttempts));
        metricVal.setStyle("-fx-font-size: 36px; -fx-font-weight: bold; -fx-text-fill: #FF7A00;");
        Label metricLbl = new Label("Total Pengerjaan Kuis");
        metricLbl.setStyle("-fx-text-fill: -fx-secondary-text; -fx-font-size: 14px;");
        metricBox.getChildren().addAll(metricVal, metricLbl);

        javafx.scene.control.Separator vSep = new javafx.scene.control.Separator();
        vSep.setOrientation(javafx.geometry.Orientation.VERTICAL);

        Label activityDesc = new Label("Siswa secara aktif berinteraksi dengan kuis yang Anda sediakan. Anda bisa memantau detail pengerjaan pada daftar aktivitas terbaru di bawah ini.");
        activityDesc.setWrapText(true);
        activityDesc.setStyle("-fx-font-size: 14px; -fx-text-fill: -fx-secondary-text; -fx-line-spacing: 4px;");
        HBox.setHgrow(activityDesc, Priority.ALWAYS);

        activityContent.getChildren().addAll(metricBox, vSep, activityDesc);
        activitySection.getChildren().addAll(activityTitle, activityContent);

        // Aktivitas Kuis Terbaru Kelas
        VBox recentHistorySection = new VBox(10);
        recentHistorySection.getStyleClass().add("section-box");
        recentHistorySection.setPadding(new Insets(20));

        Label recentHistoryTitle = new Label("Aktivitas Kuis Terbaru Kelas");
        recentHistoryTitle.getStyleClass().add("section-title");
        recentHistorySection.getChildren().add(recentHistoryTitle);

        if (teacherAttempts.isEmpty()) {
            Label emptyLbl = new Label("Belum ada aktivitas kuis dari siswa.");
            emptyLbl.setStyle("-fx-text-fill: -fx-secondary-text; -fx-font-style: italic;");
            recentHistorySection.getChildren().add(emptyLbl);
        } else {
            // Sort attempts by date descending (optional, assuming backend doesn't sort or just relying on existing order)
            int limit = Math.min(4, teacherAttempts.size());
            for (int i = 0; i < limit; i++) {
                QuizService.QuizAttemptData att = teacherAttempts.get(i);
                HBox attRow = new HBox(12);
                attRow.setAlignment(Pos.CENTER_LEFT);
                attRow.setPadding(new Insets(10));
                attRow.setStyle("-fx-background-color: #F9FAFB; -fx-background-radius: 6; -fx-border-color: #E5E7EB; -fx-border-radius: 6;");
                
                Label icon = new Label("👤");
                icon.setStyle("-fx-font-size: 20px;");
                
                VBox textInfo = new VBox(2);
                Label nameLbl = new Label(att.getStudentName() + " mengerjakan " + att.getQuizTitle());
                nameLbl.setStyle("-fx-font-weight: bold; -fx-text-fill: -fx-primary-text;");
                Label dateLbl = new Label(att.getAttemptDate());
                dateLbl.setStyle("-fx-font-size: 11px; -fx-text-fill: -fx-secondary-text;");
                textInfo.getChildren().addAll(nameLbl, dateLbl);
                
                Region sp2 = new Region();
                HBox.setHgrow(sp2, Priority.ALWAYS);
                
                Label scoreLbl = new Label(att.getScore() + " Poin");
                scoreLbl.setStyle("-fx-font-weight: bold; -fx-text-fill: " + (att.getScore() >= 75 ? "#059669" : "#DC2626") + ";");
                
                attRow.getChildren().addAll(icon, textInfo, sp2, scoreLbl);
                recentHistorySection.getChildren().add(attRow);
            }
        }

        leftColumn.getChildren().addAll(banner, statsRow, activitySection, recentHistorySection);

        // --- KOLOM KANAN (Aksi Cepat) ---
        VBox rightColumn = new VBox(20);
        rightColumn.setPrefWidth(280);
        rightColumn.setMinWidth(280);
        rightColumn.setMaxWidth(280);

        VBox actionsCard = new VBox(14);
        actionsCard.getStyleClass().add("section-box");
        actionsCard.setPadding(new Insets(20));

        Label actionTitle = new Label("Manajemen Kelas");
        actionTitle.getStyleClass().add("section-title");

        Button goStudents = new Button("Daftar Siswa");
        goStudents.getStyleClass().addAll("btn-primary", "btn-medium");
        goStudents.setMaxWidth(Double.MAX_VALUE);
        goStudents.setMinWidth(0);
        goStudents.setOnAction(e -> controller.showStudentsContent());

        Button goMaterial = new Button("Kelola Materi");
        goMaterial.getStyleClass().addAll("btn-secondary", "btn-medium");
        goMaterial.setMaxWidth(Double.MAX_VALUE);
        goMaterial.setMinWidth(0);
        goMaterial.setOnAction(e -> controller.showMaterialsContent());

        Button goQuiz = new Button("Kelola Kuis");
        goQuiz.getStyleClass().addAll("btn-secondary", "btn-medium");
        goQuiz.setStyle("-fx-background-color: #FFF0E0; -fx-text-fill: #FF7A00; -fx-border-color: #FF7A00; -fx-border-width: 1px; -fx-background-radius: 8px; -fx-border-radius: 8px; -fx-font-weight: bold; -fx-cursor: hand;");
        goQuiz.setMaxWidth(Double.MAX_VALUE);
        goQuiz.setMinWidth(0);
        goQuiz.setOnAction(e -> controller.showQuizContent());

        actionsCard.getChildren().addAll(actionTitle, goStudents, goMaterial, goQuiz);

        // Tips Box
        HBox tipsBox = new HBox(10);
        tipsBox.getStyleClass().add("tips-box");
        tipsBox.setPadding(new Insets(16));
        tipsBox.setAlignment(Pos.TOP_LEFT);

        Label bulb = new Label("💡");
        bulb.setStyle("-fx-font-size: 22px;");
        bulb.setMinWidth(28);

        VBox tipContent = new VBox(4);
        Label tipTitle = new Label("Tips Mengajar");
        tipTitle.getStyleClass().add("tip-title");
        Label tipText = new Label("Tambahkan materi kuis secara berkala untuk menjaga antusiasme siswa!");
        tipText.getStyleClass().add("tip-text");
        tipText.setWrapText(true);
        tipText.setMaxWidth(Double.MAX_VALUE);
        tipContent.getChildren().addAll(tipTitle, tipText);
        HBox.setHgrow(tipContent, Priority.ALWAYS);

        tipsBox.getChildren().addAll(bulb, tipContent);

        rightColumn.getChildren().addAll(actionsCard, tipsBox);

        mainLayout.getChildren().addAll(leftColumn, rightColumn);
        return mainLayout;
    }

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

    private String getGreetingByTime() {
        int hour = java.time.LocalTime.now().getHour();
        if (hour < 11)  return "Selamat Pagi";
        if (hour < 15)  return "Selamat Siang";
        if (hour < 18)  return "Selamat Sore";
        return "Selamat Malam";
    }
}
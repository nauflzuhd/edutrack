package com.doamamah.edutrack.fe.material;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Polygon;
import javafx.scene.web.WebView;

/**
 * =====================================================================
 * PILAR OOP: INHERITANCE (Pewarisan)
 * VideoMaterial adalah subclass dari CourseMaterial.
 *
 * PILAR OOP: POLYMORPHISM (Polimorfisme)
 * VideoMaterial melakukan OVERRIDE pada metode getUIComponent() milik
 * superclass CourseMaterial. Implementasinya BERBEDA dari TextMaterial —
 * ia merender pemutar video interaktif YouTube via WebView dengan bypass User-Agent.
 * =====================================================================
 */
public class VideoMaterial extends CourseMaterial {

    // PILAR OOP: ENCAPSULATION - URL video disimpan private
    private String videoUrl;
    private int durationMinutes;

    public VideoMaterial() {
        super();
        setMaterialType("VIDEO");
    }

    public VideoMaterial(Long id, String title, String description, String videoUrl, int durationMinutes) {
        super(id, title, description, "VIDEO");
        this.videoUrl = videoUrl;
        this.durationMinutes = durationMinutes;
    }

    public String getVideoUrl() { return videoUrl; }

    public void setVideoUrl(String videoUrl) {
        // PILAR OOP: ENCAPSULATION - validasi URL tidak null
        if (videoUrl == null || videoUrl.isBlank()) {
            throw new IllegalArgumentException("URL video tidak boleh kosong.");
        }
        this.videoUrl = videoUrl;
    }

    public int getDurationMinutes() { return durationMinutes; }
    public void setDurationMinutes(int durationMinutes) { this.durationMinutes = durationMinutes; }

    /**
     * PILAR OOP: POLYMORPHISM - Override metode abstrak dari CourseMaterial.
     * Menghasilkan pemutar YouTube interaktif menggunakan WebView secara langsung.
     * Mengatur User-Agent modern agar YouTube memperbolehkan playback langsung di aplikasi.
     *
     * @return VBox berisi WebView player interaktif
     */
    @Override
    public Node getUIComponent() {
        VBox container = new VBox(16);
        container.setAlignment(Pos.CENTER);
        container.setPadding(new Insets(24));
        container.getStyleClass().add("material-container");

        // Label judul materi
        Label titleLabel = new Label(getTitle());
        titleLabel.getStyleClass().add("material-title");

        // Player Container
        VBox playerContainer = new VBox(0);
        playerContainer.setMaxWidth(650);
        playerContainer.getStyleClass().add("video-player-container");

        String embedUrl = convertToEmbedUrl(videoUrl);

        if (embedUrl != null && (embedUrl.startsWith("http://") || embedUrl.startsWith("https://"))) {
            try {
                // Tampilkan video asli menggunakan WebView
                WebView webView = new WebView();
                webView.setPrefSize(600, 340);
                
                // Konfigurasi WebEngine agar kompatibel dengan pemutar YouTube modern
                webView.getEngine().setJavaScriptEnabled(true);
                webView.getEngine().setUserAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36");
                
                // Tambahkan autoplay dan mute parameters agar langsung mulai tanpa diblokir
                String playUrl = embedUrl + "?autoplay=1&mute=1&rel=0&showinfo=0";
                webView.getEngine().load(playUrl);

                StackPane webWrapper = new StackPane(webView);
                webWrapper.getStyleClass().add("video-screen");
                playerContainer.getChildren().add(webWrapper);
            } catch (Throwable t) {
                System.err.println("Gagal memuat WebView, beralih ke Mock Player: " + t.getMessage());
                playerContainer.getChildren().addAll(buildMockPlayerScreen(), buildMockControlBar());
            }
        } else {
            playerContainer.getChildren().addAll(buildMockPlayerScreen(), buildMockControlBar());
        }

        // Badge Informasi
        Label durationLabel = new Label("🎥 Video  •  " + durationMinutes + " menit");
        durationLabel.getStyleClass().add("material-meta");

        // Deskripsi
        Label descLabel = new Label(getDescription());
        descLabel.getStyleClass().add("material-description");
        descLabel.setWrapText(true);
        descLabel.setMaxWidth(650);

        // Warning/Tip Notice Box jika terjadi pembatasan YouTube (Error 153)
        VBox noticeBox = new VBox(4);
        noticeBox.getStyleClass().add("tips-box");
        noticeBox.setPadding(new Insets(12, 16, 12, 16));
        noticeBox.setMaxWidth(650);

        Label noticeTitle = new Label("💡 Tips Pemutaran Video");
        noticeTitle.getStyleClass().add("tip-title");
        Label noticeText = new Label("Jika video menampilkan pesan pembatasan hak cipta YouTube (Error 153), silakan klik tombol oranye di bawah ini untuk membukanya secara langsung di browser bawaan komputer Anda.");
        noticeText.getStyleClass().add("tip-text");
        noticeText.setWrapText(true);
        noticeBox.getChildren().addAll(noticeTitle, noticeText);

        // Tombol Aksi Alternatif
        Button openLinkButton = new Button("Buka Video di Browser Luar");
        openLinkButton.getStyleClass().addAll("btn-primary", "btn-large");
        openLinkButton.setOnAction(e -> {
            try {
                if (videoUrl != null && !videoUrl.isBlank()) {
                    java.awt.Desktop.getDesktop().browse(new java.net.URI(videoUrl));
                }
            } catch (Exception ex) {
                System.err.println("Gagal membuka URL: " + videoUrl);
                ex.printStackTrace();
            }
        });

        HBox actionBox = new HBox(12);
        actionBox.setAlignment(Pos.CENTER);
        actionBox.getChildren().add(openLinkButton);

        if (getAttachmentUrl() != null && !getAttachmentUrl().isEmpty()) {
            Button btnDownload = new Button("Unduh Lampiran: " + getAttachmentFileName());
            btnDownload.setStyle("-fx-background-color: #3B82F6; -fx-text-fill: white; -fx-cursor: hand;");
            btnDownload.getStyleClass().addAll("btn-primary", "btn-large");
            btnDownload.setOnAction(e -> {
                try {
                    java.awt.Desktop.getDesktop().browse(new java.net.URI(getAttachmentUrl()));
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            });
            actionBox.getChildren().add(btnDownload);
        }

        container.getChildren().addAll(titleLabel, playerContainer, durationLabel, descLabel, noticeBox, actionBox);
        return container;
    }

    /**
     * Mengubah URL biasa YouTube (watch?v=...) menjadi URL embed agar bisa dirender
     * dengan rapi oleh WebView tanpa batasan header X-Frame-Options.
     */
    private String convertToEmbedUrl(String url) {
        if (url == null || url.isBlank()) return "";

        if (url.contains("youtube.com/embed/")) {
            return url;
        }

        String videoId = null;
        try {
            if (url.contains("youtube.com/watch")) {
                String[] parts = url.split("\\?");
                if (parts.length > 1) {
                    String[] params = parts[1].split("&");
                    for (String param : params) {
                        if (param.startsWith("v=")) {
                            videoId = param.substring(2);
                            break;
                        }
                    }
                }
            } else if (url.contains("youtu.be/")) {
                String[] parts = url.split("youtu.be/");
                if (parts.length > 1) {
                    String sub = parts[1];
                    int qIdx = sub.indexOf("?");
                    if (qIdx != -1) {
                        videoId = sub.substring(0, qIdx);
                    } else {
                        videoId = sub;
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("Gagal mengurai ID video YouTube: " + e.getMessage());
        }

        if (videoId != null && !videoId.isBlank()) {
            return "https://www.youtube.com/embed/" + videoId;
        }

        return url;
    }

    private StackPane buildMockPlayerScreen() {
        StackPane videoScreen = new StackPane();
        videoScreen.setPrefSize(600, 300);
        videoScreen.getStyleClass().add("video-screen");

        Polygon playTriangle = new Polygon(0.0, 0.0, 0.0, 30.0, 26.0, 15.0);
        playTriangle.setFill(javafx.scene.paint.Color.web("#FFFFFF", 0.9));

        Circle playCircle = new Circle(30);
        playCircle.setFill(javafx.scene.paint.Color.web("#FF7A00", 0.95)); // changed to orange

        StackPane playBtn = new StackPane(playCircle, playTriangle);
        playBtn.setCursor(javafx.scene.Cursor.HAND);
        videoScreen.getChildren().add(playBtn);
        return videoScreen;
    }

    private HBox buildMockControlBar() {
        HBox controlBar = new HBox(12);
        controlBar.setAlignment(Pos.CENTER_LEFT);
        controlBar.getStyleClass().add("video-control-bar");

        Label playIcon = new Label("▶");
        playIcon.getStyleClass().add("video-play-indicator");

        String maxTimeStr = String.format("%02d:00", durationMinutes);
        Label timeLabel = new Label("00:00 / " + maxTimeStr);
        timeLabel.getStyleClass().add("video-control-text");

        ProgressBar seekBar = new ProgressBar(0.0);
        seekBar.setMaxWidth(Double.MAX_VALUE);
        seekBar.getStyleClass().add("video-seek-bar");
        HBox.setHgrow(seekBar, Priority.ALWAYS);

        Label volumeLabel = new Label("🔊");
        volumeLabel.getStyleClass().add("video-control-text");

        Label fullscreenLabel = new Label("⛶");
        fullscreenLabel.getStyleClass().add("video-control-text");

        controlBar.getChildren().addAll(playIcon, timeLabel, seekBar, volumeLabel, fullscreenLabel);
        return controlBar;
    }
}

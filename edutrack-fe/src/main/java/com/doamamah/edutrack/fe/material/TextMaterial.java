package com.doamamah.edutrack.fe.material;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

/**
 * =====================================================================
 * PILAR OOP: INHERITANCE (Pewarisan)
 * TextMaterial adalah subclass dari CourseMaterial.
 *
 * PILAR OOP: POLYMORPHISM (Polimorfisme)
 * TextMaterial melakukan OVERRIDE pada metode getUIComponent() milik
 * superclass CourseMaterial. Implementasinya BERBEDA dari VideoMaterial —
 * ia merender area bacaan (document view) yang nyaman, bersih, dan elegan.
 * =====================================================================
 */
public class TextMaterial extends CourseMaterial {

    // PILAR OOP: ENCAPSULATION - konten teks disimpan private
    private String textContent;
    private int wordCount;

    public TextMaterial() {
        super();
        setMaterialType("TEXT");
    }

    public TextMaterial(Long id, String title, String description, String textContent) {
        super(id, title, description, "TEXT");
        this.textContent = textContent;
        this.wordCount = textContent != null ? textContent.split("\\s+").length : 0;
    }

    public String getTextContent() { return textContent; }

    public void setTextContent(String textContent) {
        // PILAR OOP: ENCAPSULATION - update wordCount otomatis saat konten diset
        this.textContent = textContent;
        this.wordCount = (textContent != null && !textContent.isBlank())
            ? textContent.split("\\s+").length : 0;
    }

    public int getWordCount() { return wordCount; }

    /**
     * PILAR OOP: POLYMORPHISM - Override metode abstrak dari CourseMaterial.
     * Menghasilkan UI berupa halaman bacaan digital yang terformat rapi dengan ScrollPane.
     *
     * @return VBox berisi layout bacaan berformat premium
     */
    @Override
    public Node getUIComponent() {
        VBox container = new VBox(16);
        container.setAlignment(Pos.TOP_LEFT);
        container.setPadding(new Insets(28));
        container.getStyleClass().add("text-reader-container");
        container.setMaxWidth(800);

        // Header Section
        VBox headerBox = new VBox(6);
        Label titleLabel = new Label(getTitle());
        titleLabel.getStyleClass().add("material-title");

        HBox metaRow = new HBox(8);
        metaRow.setAlignment(Pos.CENTER_LEFT);
        Label typeBadge = new Label("📄 Materi Teks");
        typeBadge.getStyleClass().add("material-meta");
        Label wordCountBadge = new Label(wordCount + " kata");
        wordCountBadge.getStyleClass().add("word-count-badge");
        metaRow.getChildren().addAll(typeBadge, wordCountBadge);

        if (getAttachmentUrl() != null && !getAttachmentUrl().isEmpty()) {
            javafx.scene.control.Button btnDownload = new javafx.scene.control.Button("Unduh Lampiran: " + getAttachmentFileName());
            btnDownload.setStyle("-fx-background-color: #3B82F6; -fx-text-fill: white; -fx-cursor: hand; -fx-font-size: 11px;");
            btnDownload.setOnAction(e -> {
                try {
                    java.awt.Desktop.getDesktop().browse(new java.net.URI(getAttachmentUrl()));
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            });
            metaRow.getChildren().add(btnDownload);
        }

        headerBox.getChildren().addAll(titleLabel, metaRow);

        // Deskripsi Singkat
        Label descLabel = new Label(getDescription());
        descLabel.getStyleClass().add("material-description");
        descLabel.setWrapText(true);

        // Separator visual
        javafx.scene.control.Separator separator = new javafx.scene.control.Separator();

        // Area bacaan yang rapi dan nyaman dibaca (WebView)
        javafx.scene.web.WebView webView = new javafx.scene.web.WebView();
        String htmlContent = textContent != null ? textContent : "(Konten tidak tersedia)";
        
        // Wrap content with basic styling to ensure it looks good and fits the width
        String styledHtml = "<html><head><style>" +
                "body { font-family: 'Segoe UI', Helvetica, Arial, sans-serif; font-size: 15px; color: #1F2937; line-height: 1.6; padding: 10px; }" +
                "img { max-width: 100%; height: auto; }" +
                "</style></head><body>" + htmlContent + "</body></html>";
                
        webView.getEngine().loadContent(styledHtml);
        webView.setPrefHeight(380);
        VBox.setVgrow(webView, Priority.ALWAYS);

        container.getChildren().addAll(headerBox, descLabel, separator, webView);
        return container;
    }
}

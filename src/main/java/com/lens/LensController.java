package com.lens;

import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.PauseTransition;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Line;
import javafx.scene.shape.SVGPath;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.awt.Desktop;
import java.io.File;
import java.util.List;

public class LensController {

    @FXML
    private TextField searchField;
    @FXML
    private Label directoryLabel;
    @FXML
    private ComboBox<String> timeCombo;
    @FXML
    private ComboBox<String> typeCombo;
    @FXML
    private ComboBox<String> pagesCombo;
    @FXML
    private HBox resultsBox;
    @FXML
    private Button searchBtn;
    @FXML
    private VBox loadingPane;
    @FXML
    private SVGPath animationPath;
    @FXML
    private Label loadingPercent;

    private SearchEngine searchEngine = new SearchEngine();
    private File selectedDirectory = null;
    private Timeline loadingAnimation;

    // SVG Paths
    private final String PATH_INFINITY = "M 100 100 C 60 20, 20 20, 20 100 C 20 180, 60 180, 100 100 C 140 20, 180 20, 180 100 C 180 180, 140 180, 100 100 Z";
    private final String PATH_CIRCLE   = "M 100 60 A 40 40 0 1 1 99.9 60";
    private final String PATH_FUNNEL   = "M 60 60 L 140 60 L 110 100 L 110 140 L 90 140 L 90 100 Z";
    private final String PATH_STACK    = "M 70 80 L 130 80 M 70 100 L 130 100 M 70 120 L 130 120";
    private final String PATH_FOLDER   = "M 60 80 L 90 80 L 100 95 L 140 95 L 140 140 L 60 140 Z";

    @FXML
    public void initialize() {
        timeCombo.getItems().addAll("Any Time", "Past Week", "Past Month", "Past Year");
        timeCombo.getSelectionModel().selectFirst();
        
        typeCombo.getItems().addAll("All Types", "PDF", "DOCX", "TXT");
        typeCombo.getSelectionModel().selectFirst();
        
        pagesCombo.getItems().addAll("First 10 Pages", "First 50 Pages", "First 100 Pages", "All Pages (Slow)");
        pagesCombo.getSelectionModel().select(1);
    }

    private void playAnimation(String message, String svgPath, double length, double seconds, boolean loop) {
        if (loadingAnimation != null) {
            loadingAnimation.stop();
        }
        
        animationPath.setContent(svgPath);
        animationPath.getStrokeDashArray().setAll(length);
        animationPath.setStrokeDashOffset(length);
        
        loadingAnimation = new Timeline(
            new KeyFrame(Duration.ZERO, new KeyValue(animationPath.strokeDashOffsetProperty(), length)),
            new KeyFrame(Duration.seconds(seconds), new KeyValue(animationPath.strokeDashOffsetProperty(), 0))
        );
        loadingAnimation.setCycleCount(loop ? Animation.INDEFINITE : 1);
        
        loadingPercent.setText(message);
        loadingPane.setVisible(true);
        loadingAnimation.playFromStart();
        
        if (!loop) {
            // Hide exactly after it completes one full loop
            PauseTransition pause = new PauseTransition(Duration.seconds(seconds + 0.2));
            pause.setOnFinished(e -> {
                if (!searchBtn.isDisabled()) {
                    loadingPane.setVisible(false);
                }
            });
            pause.play();
        }
    }

    @FXML
    public void onTimeChange() {
        if (searchBtn.isDisabled()) return;
        playAnimation("S H I F T I N G   T I M E L I N E . . .", PATH_CIRCLE, 260.0, 1.5, false);
    }

    @FXML
    public void onTypeChange() {
        if (searchBtn.isDisabled()) return;
        playAnimation("F I L T E R I N G   M A T T E R . . .", PATH_FUNNEL, 350.0, 1.5, false);
    }

    @FXML
    public void onPageChange() {
        if (searchBtn.isDisabled()) return;
        playAnimation("A D J U S T I N G   D E P T H . . .", PATH_STACK, 200.0, 1.5, false);
    }

    @FXML
    public void onSelectDirectory() {
        DirectoryChooser chooser = new DirectoryChooser();
        chooser.setTitle("Limit Search Area (Select Folder or Drive)");
        File dir = chooser.showDialog(new Stage());
        if (dir != null) {
            selectedDirectory = dir;
            directoryLabel.setText("DIR: " + dir.getAbsolutePath());
            if (!searchBtn.isDisabled()) {
                playAnimation("L O C A T I N G   A R E A . . .", PATH_FOLDER, 350.0, 1.5, false);
            }
        }
    }
    
    @FXML
    public void onSearch() {
        String query = searchField.getText();
        if (query == null || query.isEmpty()) return;
        if (selectedDirectory == null) {
            resultsBox.getChildren().clear();
            Label err = new Label("SELECT DIRECTORY FIRST");
            err.setStyle("-fx-text-fill: white; -fx-font-weight: 300; -fx-letter-spacing: 2px;");
            resultsBox.getChildren().add(err);
            return;
        }
        
        SearchEngine.TimeLimit limit = SearchEngine.TimeLimit.ANY;
        String timeSel = timeCombo.getValue();
        if (timeSel.equals("Past Week")) limit = SearchEngine.TimeLimit.PAST_WEEK;
        if (timeSel.equals("Past Month")) limit = SearchEngine.TimeLimit.PAST_MONTH;
        if (timeSel.equals("Past Year")) limit = SearchEngine.TimeLimit.PAST_YEAR;

        String typeSel = typeCombo.getValue();
        int maxPages = -1;
        String pageSel = pagesCombo.getValue();
        if (pageSel.contains("10")) maxPages = 10;
        else if (pageSel.contains("50")) maxPages = 50;
        else if (pageSel.contains("100")) maxPages = 100;

        resultsBox.getChildren().clear();
        searchBtn.setDisable(true);
        
        // Endless Infinity Loop for Searching
        playAnimation("S E A R C H I N G . . .", PATH_INFINITY, 600.0, 2.0, true);

        SearchEngine.TimeLimit finalLimit = limit;
        int finalMaxPages = maxPages;
        
        new Thread(() -> {
            List<SearchResult> results = searchEngine.search(query, selectedDirectory, finalLimit, typeSel, finalMaxPages);
            Platform.runLater(() -> {
                searchBtn.setDisable(false);
                if (loadingAnimation != null) loadingAnimation.stop();
                loadingPane.setVisible(false);
                resultsBox.getChildren().clear();
                
                if (results.isEmpty()) {
                    Label noRes = new Label("NO MATCHES IN THIS TIMELINE");
                    noRes.setStyle("-fx-text-fill: #555555; -fx-font-weight: 300; -fx-letter-spacing: 2px;");
                    resultsBox.getChildren().add(noRes);
                } else {
                    for (int i = 0; i < results.size(); i++) {
                        addResultToUI(results.get(i), i == results.size() - 1);
                    }
                }
            });
        }).start();
    }

    private void addResultToUI(SearchResult result, boolean isLast) {
        VBox nodeBox = new VBox(0);
        nodeBox.setAlignment(Pos.CENTER);
        
        HBox lineBox = new HBox(0);
        lineBox.setAlignment(Pos.CENTER_LEFT);
        
        Circle dot = new Circle(4, javafx.scene.paint.Color.WHITE);
        dot.getStyleClass().add("timeline-dot");
        
        Line lineSegment = new Line(0, 0, 300, 0);
        lineSegment.setStroke(javafx.scene.paint.Color.web("#333333"));
        lineSegment.setStrokeWidth(1);
        
        if (isLast) {
            lineBox.getChildren().addAll(dot);
        } else {
            lineBox.getChildren().addAll(dot, lineSegment);
        }

        VBox card = new VBox(15);
        card.getStyleClass().add("result-card");
        card.setAlignment(Pos.CENTER);
        card.setPrefWidth(280);
        
        Label title = new Label(result.getFile().getName().toUpperCase());
        title.getStyleClass().add("result-title");
        title.setWrapText(true);
        title.setAlignment(Pos.CENTER);
        
        Label meta = new Label(result.getRelevance() + " MATCHES");
        meta.getStyleClass().add("result-meta");
        
        HBox actions = new HBox(15);
        actions.setAlignment(Pos.CENTER);
        Button openBtn = new Button("OPEN");
        openBtn.getStyleClass().add("action-btn");
        openBtn.setOnAction(e -> {
            try { Desktop.getDesktop().open(result.getFile()); } catch (Exception ex) {}
        });
        
        Button folderBtn = new Button("FOLDER");
        folderBtn.getStyleClass().add("action-btn");
        folderBtn.setOnAction(e -> {
            try { Desktop.getDesktop().open(result.getFile().getParentFile()); } catch (Exception ex) {}
        });

        actions.getChildren().addAll(openBtn, folderBtn);
        card.getChildren().addAll(title, meta, actions);
        
        VBox.setMargin(card, new javafx.geometry.Insets(40, 0, 0, 0));
        nodeBox.getChildren().addAll(lineBox, card);
        resultsBox.getChildren().add(nodeBox);
    }
}

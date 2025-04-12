package com.desk.classmesh;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class test extends Application {
    Label mapTitle;
    Label tCountTitle;
    ComboBox<Integer> teachCount;
    GridPane mapSubs;
    private ObservableList<String> allSubjects = FXCollections.observableArrayList();
    private Set<String> assignedSubjects = new HashSet<>();

    @Override
    public void start(Stage stage) {
        VBox root = new VBox();
        root.setPadding(new Insets(20));
        root.setStyle("-fx-border-color:#6ca0dc;-fx-border-width: 0 2 2 2;-fx-border-radius:4px;");

        ScrollPane scrollPane = new ScrollPane();
        scrollPane.setContent(root);
        scrollPane.setFitToWidth(true);
        scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);

        // Label Section
        Label titleLabel = new Label("Semester Configuration");
        titleLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");
        titleLabel.setPadding(new Insets(10, 0, 5, 0));

        Label gridTitle = new Label("Semester/Class Names:");
        gridTitle.setStyle("-fx-font-size: 14px; -fx-font-weight: bold;");
        gridTitle.setPadding(new Insets(10, 0, 5, 0));

        // Input Number of Working Semesters
        ComboBox<Integer> semCount = new ComboBox<>();
        semCount.setPromptText("Number of Working Semesters/Classes");
        for (int i = 1; i <= 12; i++) {
            semCount.getItems().add(i);
        }

        // Semester grid
        GridPane subGrid = new GridPane();
        subGrid.setStyle("-fx-border-color:#6ca0dc;-fx-border-thickness:4px;-fx-border-radius:4px;");
        subGrid.setPadding(new Insets(10));
        subGrid.setHgap(10);
        subGrid.setVgap(10);

        semCount.setOnAction(actionEvent -> {
            subGrid.getChildren().clear();
            allSubjects.clear();
            assignedSubjects.clear();

            int selSemCount = semCount.getValue();
            for (int k = 1; k <= selSemCount; k++) {
                TextField semName = new TextField();
                semName.setPromptText("Semester/Class " + k);
                ComboBox<Integer> subCount = new ComboBox<>();
                for (int j = 1; j <= 8; j++) {
                    subCount.setPromptText("Subject Count");
                    subCount.getItems().add(j);
                }

                GridPane subNameGrid = new GridPane();
                subCount.setOnAction(e -> {
                    subNameGrid.getChildren().clear();
                    int numSubs = subCount.getValue();
                    for (int s = 0; s < numSubs; s++) {
                        TextField subName = new TextField();
                        subName.setPromptText("Subject " + (s + 1) + " name");
                        subNameGrid.addColumn(s, subName);
                    }
                });
                subGrid.addRow(k, semName, subCount, subNameGrid);
            }

            Button subGridNxt = new Button("Next");
            subGridNxt.setStyle("-fx-background-color: #2196F3; -fx-text-fill: white; -fx-font-size: 14px; -fx-padding: 6 16 6 16; -fx-background-radius: 6;");
            subGrid.addRow(selSemCount + 1, subGridNxt);

            subGridNxt.setOnMouseClicked(c -> {
                boolean isValid = true;
                String errorMessage = "";
                allSubjects.clear();
                assignedSubjects.clear();

                // Collect all subject names
                for (Node node : subGrid.getChildren()) {
                    if (node instanceof TextField) {
                        TextField semField = (TextField) node;
                        if (semField.getPromptText().startsWith("Semester") && semField.getText().trim().isEmpty()) {
                            isValid = false;
                            errorMessage = "Semester name cannot be empty.";
                            break;
                        }
                    } else if (node instanceof ComboBox) {
                        ComboBox<?> subCountBox = (ComboBox<?>) node;
                        if ("Subject Count".equals(subCountBox.getPromptText()) && subCountBox.getValue() == null) {
                            isValid = false;
                            errorMessage = "Please select subject count for each semester.";
                            break;
                        }
                    } else if (node instanceof GridPane) {
                        GridPane subNameGrid = (GridPane) node;
                        for (Node subNode : subNameGrid.getChildren()) {
                            if (subNode instanceof TextField) {
                                TextField subName = (TextField) subNode;
                                String name = subName.getText().trim();
                                if (name.isEmpty()) {
                                    isValid = false;
                                    errorMessage = "Subject name cannot be empty.";
                                    break;
                                }
                                allSubjects.add(name);
                            }
                        }
                    }
                    if (!isValid) break;
                }

                // Check for duplicate subjects
                Set<String> uniqueSubjects = new HashSet<>();
                for (String subject : allSubjects) {
                    if (!uniqueSubjects.add(subject)) {
                        isValid = false;
                        errorMessage = "Duplicate subject name found: " + subject;
                        break;
                    }
                }

                if (isValid) {
                    mapTitle.setVisible(true);
                    tCountTitle.setVisible(true);
                    teachCount.setVisible(true);
                    mapSubs.setVisible(true);
                } else {
                    showErrorAlert(errorMessage);
                }
            });
        });

        // Teacher count section
        tCountTitle = new Label("Number of teaching staff");
        tCountTitle.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");
        tCountTitle.setPadding(new Insets(10, 0, 5, 0));

        teachCount = new ComboBox<>();
        teachCount.setPromptText("Select Number of teachers");
        for (int t = 1; t <= 15; t++) {
            teachCount.getItems().add(t);
        }

        // Teacher-subject mapping section
        mapSubs = new GridPane();
        mapSubs.setStyle("-fx-border-color:#6ca0dc;-fx-border-thickness:4px;-fx-border-radius:4px;-fx-padding:10px;");
        mapSubs.setVgap(10);
        mapSubs.setHgap(20);
        mapTitle = new Label("Teacher - Subject Mapping");
        mapTitle.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");
        mapTitle.setPadding(new Insets(10, 0, 5, 0));

        teachCount.setOnAction(f -> {
            mapSubs.getChildren().clear();
            assignedSubjects.clear();
            int guruCount = teachCount.getValue();

            for (int v = 1; v <= guruCount; v++) {
                TextField teachName = new TextField();
                teachName.setPromptText("Teacher " + v + " name");

                ComboBox<Integer> subTeach = new ComboBox<>();
                subTeach.setPromptText("Subject Count");
                for (int l = 1; l <= 6; l++) {
                    subTeach.getItems().add(l);
                }

                GridPane tSubMapGrid = new GridPane();
                tSubMapGrid.setHgap(20);

                subTeach.setOnAction(p -> {
                    tSubMapGrid.getChildren().clear();
                    int teachSubCount = subTeach.getValue();

                    for (int m = 0; m < teachSubCount; m++) {
                        ComboBox<String> selSub = new ComboBox<>();
                        selSub.setPromptText("Select Subject");

                        // Get available subjects (all subjects minus already assigned ones)
                        List<String> availableSubjects = allSubjects.stream()
                                .filter(sub -> !assignedSubjects.contains(sub))
                                .collect(Collectors.toList());

                        selSub.setItems(FXCollections.observableArrayList(availableSubjects));

                        // When a subject is selected, add it to assignedSubjects and update other dropdowns
                        // Replace the selSub.setOnAction handler with this:
                        selSub.setOnAction(event -> {
                            String previousSelection = selSub.getUserData() != null ?
                                    (String) selSub.getUserData() : null;
                            String newSelection = selSub.getValue();

                            // Update the assigned subjects set
                            if (previousSelection != null) {
                                assignedSubjects.remove(previousSelection);
                            }
                            if (newSelection != null) {
                                assignedSubjects.add(newSelection);
                            }

                            // Store the current selection as user data
                            selSub.setUserData(newSelection);

                            updateSubjectDropdowns();
                        });

                        tSubMapGrid.addColumn(m, selSub);
                    }
                });
                mapSubs.addRow(v, teachName, subTeach, tSubMapGrid);
            }
        });

        // Tab pane setup
        TabPane myTabPane = new TabPane();
        Tab inputTab = new Tab("Input Details - Required to generate the TimeTable", scrollPane);
        inputTab.setClosable(false);
        Tab genTimeTab = new Tab("Generated - TimeTable");
        genTimeTab.setClosable(false);
        myTabPane.getTabs().addAll(inputTab, genTimeTab);

        myTabPane.getSelectionModel().selectedItemProperty().addListener((obs, oldTab, newTab) -> {
            for (Tab tab : myTabPane.getTabs()) {
                tab.setStyle("");
            }
            newTab.setStyle("-fx-border-color: #3c7fb1 ; -fx-border-width: 2 2 0 2;");
        });

        // Initial visibility
        mapTitle.setVisible(false);
        tCountTitle.setVisible(false);
        teachCount.setVisible(false);
        mapSubs.setVisible(false);

        Scene scene = new Scene(myTabPane, 1080, 720);
        root.getChildren().addAll(titleLabel, semCount, gridTitle, subGrid, tCountTitle, teachCount, mapTitle, mapSubs);

        try {
            Image icon = new Image(getClass().getResourceAsStream("/icon.png"));
            stage.getIcons().add(icon);
        } catch (Exception e) {
            System.err.println("Could not load application icon");
        }

        stage.setScene(scene);
        stage.setTitle("ClassMesh - TimeTable Management");
        stage.show();
    }

    // Replace the updateSubjectDropdowns() method with this improved version:
    private void updateSubjectDropdowns() {
        // First, collect all currently selected subjects that should be preserved
        Set<String> currentSelections = new HashSet<>();

        // Gather all currently selected values first
        for (Node node : mapSubs.getChildren()) {
            if (node instanceof GridPane) {
                GridPane grid = (GridPane) node;
                for (Node comboNode : grid.getChildren()) {
                    if (comboNode instanceof ComboBox) {
                        @SuppressWarnings("unchecked")
                        ComboBox<String> comboBox = (ComboBox<String>) comboNode;
                        if (comboBox.getPromptText().equals("Select Subject") && comboBox.getValue() != null) {
                            currentSelections.add(comboBox.getValue());
                        }
                    }
                }
            }
        }

        // Now update all dropdowns while preserving valid selections
        for (Node node : mapSubs.getChildren()) {
            if (node instanceof GridPane) {
                GridPane grid = (GridPane) node;
                for (Node comboNode : grid.getChildren()) {
                    if (comboNode instanceof ComboBox) {
                        @SuppressWarnings("unchecked")
                        ComboBox<String> comboBox = (ComboBox<String>) comboNode;
                        if (comboBox.getPromptText().equals("Select Subject")) {
                            String currentValue = comboBox.getValue();

                            // Create list of available subjects:
                            // 1. All subjects not assigned to others, OR
                            // 2. Subjects that are currently selected in this combo box
                            List<String> availableSubjects = allSubjects.stream()
                                    .filter(sub -> !assignedSubjects.contains(sub) ||
                                            (currentValue != null && sub.equals(currentValue)))
                                    .collect(Collectors.toList());

                            // Preserve the current value if it's still valid
                            if (currentValue != null && availableSubjects.contains(currentValue)) {
                                comboBox.setItems(FXCollections.observableArrayList(availableSubjects));
                                comboBox.setValue(currentValue);
                            } else {
                                // Current value is no longer valid, clear it
                                if (currentValue != null) {
                                    assignedSubjects.remove(currentValue);
                                }
                                comboBox.setItems(FXCollections.observableArrayList(availableSubjects));
                                comboBox.setValue(null);
                            }
                        }
                    }
                }
            }
        }
    }

    private void showErrorAlert(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Input Validation Error");
        alert.setHeaderText(null);
        alert.setContentText(message);

        try {
            Stage alertStage = (Stage) alert.getDialogPane().getScene().getWindow();
            alertStage.getIcons().add(new Image(getClass().getResourceAsStream("/icon.png")));
        } catch (Exception e) {
            System.err.println("Could not load alert icon");
        }

        alert.showAndWait();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
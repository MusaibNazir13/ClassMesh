package com.desk.classmesh.views;

import javafx.geometry.Insets;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;

import java.util.ArrayList;
import java.util.List;

public class TeacherMappingView {

    private final VBox root;
    private final Label mapTitle;
    private final Label tCountTitle;
    private final ComboBox<Integer> teachCount;
    private final GridPane mapSubs;
    private List<String> availableSubjects; // To store subjects passed from main app

    public TeacherMappingView() {
        root = new VBox(5); // Spacing
        availableSubjects = new ArrayList<>(); // Initialize the list

        tCountTitle = new Label("Number of teaching staff");
        tCountTitle.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");
        tCountTitle.setPadding(new Insets(10, 0, 5, 0));

        teachCount = new ComboBox<>();
        teachCount.setPromptText("Select Number of teachers");
        teachCount.setMaxWidth(Double.MAX_VALUE);
        for (int t = 1; t <= 15; t++) {
            teachCount.getItems().add(t);
        }

        mapTitle = new Label("Teacher - Subject Mapping");
        mapTitle.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");
        mapTitle.setPadding(new Insets(10, 0, 5, 0));

        mapSubs = new GridPane();
        mapSubs.setStyle("-fx-border-color:#6ca0dc;-fx-border-thickness:4px;-fx-border-radius:4px;-fx-padding:10px;");
        mapSubs.setVgap(10);
        mapSubs.setHgap(20);

        // Initial setup: Section is hidden by default in the main app
        root.getChildren().addAll(tCountTitle, teachCount, mapTitle, mapSubs);

        // Action listener for teacher count selection
        teachCount.setOnAction(f -> buildTeacherSubjectInputs());
    }

    // Method for the main app to provide the list of subjects
    public void setAvailableSubjects(List<String> subjects) {
        this.availableSubjects.clear();
        if (subjects != null) {
            this.availableSubjects.addAll(subjects);
        }
        // Rebuild inputs if teacher count is already selected, otherwise it will build when count is selected
        if (teachCount.getValue() != null) {
            buildTeacherSubjectInputs();
        }
    }

    private void buildTeacherSubjectInputs() {
        mapSubs.getChildren().clear(); // Clear previous entries

        Integer guruCount = teachCount.getValue();
        if (guruCount == null) return; // Do nothing if no count selected

        for (int v = 1; v <= guruCount; v++) {
            TextField teachName = new TextField();
            teachName.setPromptText("Teacher " + v + " name");

            ComboBox<Integer> subTeach = new ComboBox<>();
            subTeach.setPromptText("Subject Count");
            for (int l = 1; l <= 6; l++) { // Start from 1 subject? Original code had 0-5. Adjust if needed.
                subTeach.getItems().add(l);
            }

            GridPane tSubMapGrid = new GridPane();
            tSubMapGrid.setHgap(10); // Reduced gap

            // Add listener to the subject count combo for this teacher
            subTeach.setOnAction(p -> {
                buildSubjectSelectionInputs(tSubMapGrid, subTeach.getValue());
            });

            mapSubs.addRow(v - 1, teachName, subTeach, tSubMapGrid); // Use 0-based index
        }
    }

    private void buildSubjectSelectionInputs(GridPane targetGrid, Integer teachSubCount) {
        targetGrid.getChildren().clear();
        if (teachSubCount == null) return;

        for (int m = 0; m < teachSubCount; m++) {
            ComboBox<String> selSub = new ComboBox<>();
            selSub.setPromptText("Select Subject " + (m + 1));
            // Populate with available subjects
            if (availableSubjects != null && !availableSubjects.isEmpty()) {
                selSub.getItems().addAll(availableSubjects);
            } else {
                selSub.getItems().add("No subjects defined yet"); // Placeholder
                selSub.setDisable(true);
            }
            targetGrid.addColumn(m, selSub);
        }
    }

    // Method to get the root node of this view
    public VBox getView() {
        return root;
    }

    // Methods to control visibility from the main application
    public void show() {
        root.setVisible(true);
        root.setManaged(true); // Ensure it takes space
    }

    public void hide() {
        root.setVisible(false);
        root.setManaged(false); // Ensure it doesn't take space
    }
}
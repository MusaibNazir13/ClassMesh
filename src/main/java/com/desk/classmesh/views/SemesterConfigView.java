package com.desk.classmesh.views;

import com.desk.classmesh.listeners.ValidationListener;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.util.ArrayList;
import java.util.List;

public class SemesterConfigView {

    private final VBox root;
    private final ComboBox<Integer> semCount;
    private final GridPane subGrid;
    private ValidationListener validationListener; // Listener to notify main app

    public SemesterConfigView() {
        root = new VBox(5); // Spacing between elements
        root.setPadding(new Insets(0, 0, 20, 0)); // Padding at the bottom

        Label gridTitle = new Label("Semester/Class Names:");
        gridTitle.setStyle("-fx-font-size: 14px; -fx-font-weight: bold;");
        gridTitle.setPadding(new Insets(10, 0, 5, 0));

        semCount = new ComboBox<>();
        semCount.setPromptText("Number of Working Semesters/Classes");
        semCount.setMaxWidth(Double.MAX_VALUE);
        for (int i = 1; i <= 12; i++) {
            semCount.getItems().add(i);
        }

        subGrid = new GridPane();
        subGrid.setStyle("-fx-border-color:#6ca0dc;-fx-border-thickness:4px;-fx-border-radius:4px;");
        subGrid.setPadding(new Insets(10));
        subGrid.setHgap(10);
        subGrid.setVgap(10);

        semCount.setOnAction(actionEvent -> buildSemesterInputs());

        root.getChildren().addAll(semCount, gridTitle, subGrid);
    }

    // Method to set the listener
    public void setValidationListener(ValidationListener listener) {
        this.validationListener = listener;
    }

    private void buildSemesterInputs() {
        subGrid.getChildren().clear(); // Clear previous entries

        Integer selectedCount = semCount.getValue();
        if (selectedCount == null) return; // Do nothing if no value selected

        int selSemCount = selectedCount;
        for (int k = 1; k <= selSemCount; k++) {
            TextField semName = new TextField();
            semName.setPromptText("Semester/Class " + k);
            semName.setId("semName_" + k); // Add ID for easier identification if needed

            ComboBox<Integer> subCount = new ComboBox<>();
            subCount.setPromptText("Subject Count");
            subCount.setId("subCount_" + k); // Add ID
            for (int j = 1; j <= 8; j++) {
                subCount.getItems().add(j);
            }

            GridPane subNameGrid = new GridPane();
            subNameGrid.setHgap(5); // Smaller gap for subject names
            subNameGrid.setId("subNameGrid_" + k); // Add ID

            // Add listener to subCount ComboBox
            subCount.setOnAction(e -> {
                buildSubjectNameInputs(subNameGrid, subCount.getValue());
            });

            subGrid.addRow(k - 1, semName, subCount, subNameGrid); // Use 0-based index for rows
        }

        Button subGridNxt = new Button("Next");
        subGridNxt.setStyle("-fx-background-color: #2196F3; -fx-text-fill: white; -fx-font-size: 14px; -fx-padding: 6 16 6 16; -fx-background-radius: 6;");
        subGridNxt.setOnAction(e -> validateInputs());

        // Add button below the last row
        subGrid.add(subGridNxt, 0, selSemCount, 3, 1); // Span across 3 columns
        GridPane.setHalignment(subGridNxt, javafx.geometry.HPos.RIGHT); // Align right
    }

    private void buildSubjectNameInputs(GridPane targetGrid, Integer numSubs) {
        targetGrid.getChildren().clear();
        if (numSubs == null) return;

        for (int s = 0; s < numSubs; s++) {
            TextField subName = new TextField();
            subName.setPromptText("Subject " + (s + 1) + " name");
            subName.setId("subName_" + s); // Add ID
            targetGrid.addColumn(s, subName);
        }
    }

    private void validateInputs() {
        boolean isValid = true;
        String errorMessage = "";
        List<String> allSubjects = new ArrayList<>();

        for (Node node : subGrid.getChildren()) {
            if (node instanceof TextField semField && semField.getPromptText().startsWith("Semester")) {
                if (semField.getText().trim().isEmpty()) {
                    isValid = false;
                    errorMessage = "Semester/Class name cannot be empty.";
                    styleErrorNode(semField); // Highlight error
                    break;
                } else {
                    resetStyle(semField); // Reset style if valid
                }
            } else if (node instanceof ComboBox<?> box && box.getPromptText().equals("Subject Count")) {
                ComboBox<Integer> subCountBox = (ComboBox<Integer>) box;
                if (subCountBox.getValue() == null) {
                    isValid = false;
                    errorMessage = "Please select subject count for each semester/class.";
                    styleErrorNode(subCountBox);
                    break;
                } else {
                    resetStyle(subCountBox);
                }
            } else if (node instanceof GridPane subNameGrid && node.getId() != null && node.getId().startsWith("subNameGrid")) {
                if (subNameGrid.getChildren().isEmpty()) {
                    // This check might be redundant if subject count combo validation works
                    Node parentRowNode = findParentComboBox(subNameGrid); // Find the corresponding subCount combo
                    if (parentRowNode instanceof ComboBox && ((ComboBox<?>)parentRowNode).getValue() != null && ((ComboBox<Integer>)parentRowNode).getValue() > 0){
                        isValid = false;
                        errorMessage = "Subject names are required if subject count is greater than 0.";
                        // Optionally highlight the subNameGrid or its parent ComboBox
                        styleErrorNode(parentRowNode != null ? parentRowNode : subNameGrid);
                        break;
                    }
                }
                for (Node subNode : subNameGrid.getChildren()) {
                    if (subNode instanceof TextField subNameField) {
                        String name = subNameField.getText().trim();
                        if (name.isEmpty()) {
                            isValid = false;
                            errorMessage = "Subject name cannot be empty.";
                            styleErrorNode(subNameField);
                            break; // Break inner loop
                        } else {
                            resetStyle(subNameField);
                            if (!allSubjects.contains(name)) { // Avoid duplicates if needed
                                allSubjects.add(name);
                            }
                        }
                    }
                }
            }
            if (!isValid) break; // Break outer loop
        }


        if (isValid && validationListener != null) {
            validationListener.onValidationSuccess(allSubjects);
        } else if (!isValid && validationListener != null) {
            // Reset styles of non-error nodes before showing alert
            resetAllStyles();
            // Re-apply style to the specific error node identified above
            for (Node node : subGrid.getChildren()) {
                if (node.getStyle().contains("-fx-border-color: red;")) { // Find the node marked during validation
                    styleErrorNode(node); // Re-apply just to be sure
                    break; // Assuming only one error is highlighted at a time
                }
                if (node instanceof GridPane subNameGrid && node.getId() != null && node.getId().startsWith("subNameGrid")) {
                    for (Node subNode : subNameGrid.getChildren()) {
                        if (subNode.getStyle().contains("-fx-border-color: red;")) {
                            styleErrorNode(subNode);
                            break;
                        }
                    }
                }
            }
            validationListener.onValidationFailure(errorMessage);
            showErrorAlert(errorMessage);
        } else if (validationListener == null) {
            System.err.println("ValidationListener is not set in SemesterConfigView!");
        }
    }
    // Helper to find the corresponding subject count ComboBox for a given subNameGrid
    private Node findParentComboBox(GridPane subNameGrid) {
        Integer rowIndex = GridPane.getRowIndex(subNameGrid);
        if (rowIndex != null) {
            for (Node child : subGrid.getChildren()) {
                if (GridPane.getRowIndex(child) != null && GridPane.getRowIndex(child).equals(rowIndex) && child instanceof ComboBox && ((ComboBox<?>)child).getPromptText().equals("Subject Count")) {
                    return child;
                }
            }
        }
        return null; // Should not happen in normal flow
    }


    // Helper method to apply error styling
    private void styleErrorNode(Node node) {
        node.setStyle("-fx-border-color: red; -fx-border-width: 1px; -fx-border-radius: 3px;");
    }

    // Helper method to reset node style
    private void resetStyle(Node node) {
        node.setStyle(null); // Reset to default defined in CSS or programmatically
        if (node instanceof GridPane){ // Need to reset grid border specifically if needed
            node.setStyle("-fx-border-color:#6ca0dc;-fx-border-thickness:4px;-fx-border-radius:4px; -fx-padding: 10px;"); // Reset grid style if it was the grid itself
        } else if (node instanceof ComboBox || node instanceof TextField){
            node.setStyle(""); // Clear specific styles
        }

    }
    // Helper to reset all input field styles in the grid before highlighting a new error
    private void resetAllStyles() {
        for (Node node : subGrid.getChildren()) {
            if (node instanceof TextField || node instanceof ComboBox) {
                resetStyle(node);
            } else if (node instanceof GridPane subNameGrid) {
                // Do not reset the main border of subNameGrid itself unless it was the error
                for (Node subNode : subNameGrid.getChildren()) {
                    if (subNode instanceof TextField) {
                        resetStyle(subNode);
                    }
                }
            }
        }
    }


    private void showErrorAlert(String message) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle("Validation Error");
        alert.setHeaderText(null); // No header
        alert.setContentText(message);

        // Set Icon
        try {
            Stage alertStage = (Stage) alert.getDialogPane().getScene().getWindow();
            alertStage.getIcons().add(new Image(getClass().getResourceAsStream("/icon.png"))); // Ensure icon is in resources
        } catch (Exception e) {
            System.err.println("Warning: Could not load alert icon. " + e.getMessage());
        }


        // Style the content label
        DialogPane dialogPane = alert.getDialogPane();
        dialogPane.getStylesheets().add(getClass().getResource("/styles.css").toExternalForm()); // Optional: Use CSS file
        dialogPane.lookup(".content.label").setStyle(
                "-fx-font-size: 14px; " +
                        "-fx-text-fill: #d32f2f; " + // Reddish color
                        "-fx-alignment: center-left; " + // Align left for better readability
                        "-fx-wrap-text: true;"
        );
        // Ensure wrap text works correctly (sometimes needs runLater)
        Platform.runLater(() -> {
            Label contentLabel = (Label) dialogPane.lookup(".content.label");
            if (contentLabel != null) {
                contentLabel.setWrapText(true);
                contentLabel.setMaxWidth(Double.MAX_VALUE);
                // contentLabel.setAlignment(Pos.CENTER_LEFT); // Re-apply alignment if needed
            }
        });


        alert.showAndWait();
    }


    // Method to get the root node of this view
    public VBox getView() {
        return root;
    }
}
package com.desk.classmesh;

import javafx.application.Application;
import javafx.application.Platform;
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

public class ClassMesh extends Application {
    Label mapTitle;
    Label tCountTitle;
    ComboBox<Integer> teachCount;
    GridPane mapSubs;

    @Override
    public void start(Stage stage){
        VBox root = new VBox();
        root.setPadding(new Insets(20));
        root.setStyle("-fx-border-color:#6ca0dc;-fx-border-width: 0 2 2 2;-fx-border-radius:4px;");


        // Wrap root in a ScrollPane
        ScrollPane scrollPane = new ScrollPane();
        scrollPane.setContent(root);
        scrollPane.setFitToWidth(true); // Ensures it fits the window width
        scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.NEVER); // Show scrollbar when needed


        //Label Section
        Label titleLabel = new Label("Semester Configuration");
        titleLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");
        titleLabel.setPadding(new Insets(10,0,5,0));

        Label gridTitle = new Label("Semester/Class Names:");
        gridTitle.setStyle("-fx-font-size: 14px; -fx-font-weight: bold;");
        gridTitle.setPadding(new Insets(10,0,5,0));

        //Input Number of Working Semesters then put it in root VBox
        ComboBox<Integer> semCount = new ComboBox<>();
        semCount.setPromptText("Number of Working Semesters/Classes");
        for (int i = 1; i <= 12; i++) {
            semCount.getItems().add(i);
        }

        //based on selected count generate the placeholders for semester names
        GridPane subGrid = new GridPane();
        subGrid.setStyle("-fx-border-color:#6ca0dc;-fx-border-thickness:4px;-fx-border-radius:4px;");
        subGrid.setPadding(new Insets(10));
        subGrid.setHgap(10);
        subGrid.setVgap(10);

        semCount.setOnAction(actionEvent -> {
            subGrid.getChildren().clear();

            int selSemCount = semCount.getValue();
            for (int k = 1; k <= selSemCount; k++) {
                TextField semName = new TextField();
                semName.setPromptText("Semester/Class "+k);
                ComboBox<Integer> subCount = new ComboBox<>();
                for (int j = 1; j <= 8; j++) {
                    subCount.setPromptText("Subject Count");
                    subCount.getItems().add(j);
                }
                GridPane subNameGrid = new GridPane();
                subCount.setOnAction(e->{
                    subNameGrid.getChildren().clear();
                    int numSubs = subCount.getValue();
                    for (int s = 0; s < numSubs; s++) {
                        TextField subName = new TextField();
                        subName.setPromptText("Subject "+(s+1)+" name");
                        subNameGrid.addColumn(s,subName);
                    }
                });

                subGrid.addRow(k,semName,subCount,subNameGrid);
            }
            Button subGridNxt = new Button("Next");
            subGridNxt.setStyle("-fx-background-color: #2196F3; -fx-text-fill: white; -fx-font-size: 14px; -fx-padding: 6 16 6 16; -fx-background-radius: 6;-fx-");

            int rowNum=semCount.getValue();
            subGrid.addRow(rowNum+1,subGridNxt);


            subGridNxt.setOnMouseClicked(c -> {
                boolean isValid = true;
                String errorMessage = "";
                java.util.List<String> allSubjects = new java.util.ArrayList<>();

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
                    } else if (node instanceof GridPane subNameGrid) {
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

                if (isValid) {
                    mapTitle.setVisible(true);
                    tCountTitle.setVisible(true);
                    teachCount.setVisible(true);
                    mapSubs.setVisible(true);

                    // Prepopulate subject mapping options
                    teachCount.setOnAction(f -> {
                        mapSubs.getChildren().clear();
                        int guruCount = teachCount.getValue();

                        for (int v = 1; v <= guruCount ; v++) {
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
                                    selSub.getItems().addAll(allSubjects); // Load subject options

                                    tSubMapGrid.addColumn(m, selSub);
                                }
                            });

                            mapSubs.addRow(v, teachName, subTeach, tSubMapGrid);
                        }
                    });

                } else {
                    // Hide the mapping section again on validation failure
                    mapTitle.setVisible(false);
                    tCountTitle.setVisible(false);
                    teachCount.setVisible(false);
                    mapSubs.setVisible(false);

                    Alert alert = new Alert(Alert.AlertType.WARNING);
                    alert.setTitle("Validation Error");
                    alert.setHeaderText(null);
                    alert.setContentText(errorMessage);

                    Stage alertStage = (Stage) alert.getDialogPane().getScene().getWindow();
                    alertStage.getIcons().add(new Image("icon.png"));

                    DialogPane dialogPane = alert.getDialogPane();
                    Platform.runLater(() -> {
                        Label contentLabel = (Label) dialogPane.lookup(".content.label");
                        if (contentLabel != null) {
                            contentLabel.setStyle(
                                    "-fx-font-size: 14px;" +
                                            "-fx-text-fill: #d32f2f;" +
                                            "-fx-alignment: center;" +
                                            "-fx-wrap-text: true;"
                            );
                            contentLabel.setWrapText(true);
                            contentLabel.setMaxWidth(Double.MAX_VALUE);
                            contentLabel.setAlignment(Pos.CENTER);
                        }
                    });

                    alert.showAndWait();
                }
            });





        });



        //number of teachers in department
        tCountTitle = new Label("Number of teaching staff");
        tCountTitle.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");
        tCountTitle.setPadding(new Insets(10,0,5,0));
        teachCount = new ComboBox<>();
        teachCount.setPromptText("Select Number of teachers");
        for (int t = 1; t <= 15; t++) {
            teachCount.getItems().add(t);
        }

        //(interface) Input teacher names and map the subjects to teachers
        mapSubs = new GridPane();
        mapSubs.setStyle("-fx-border-color:#6ca0dc;-fx-border-thickness:4px;-fx-border-radius:4px;-fx-padding:10px;");
        mapSubs.setVgap(10);
        mapSubs.setHgap(20);
        mapTitle = new Label("Teacher - Subject Mapping");

        mapTitle.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");
        mapTitle.setPadding(new Insets(10,0,5,0));

        teachCount.setOnAction(f->{
            mapSubs.getChildren().clear();
            int guruCount = teachCount.getValue();

            for (int v = 1; v <= guruCount ; v++) {
                TextField teachName = new TextField();
                teachName.setPromptText("Teacher "+v+" name");

                ComboBox<Integer> subTeach=new ComboBox<>();
                subTeach.setPromptText("Subject Count");
                for (int l = 0; l < 6; l++) {
                    subTeach.getItems().add(l);
                }
                GridPane tSubMapGrid = new GridPane();
                tSubMapGrid.setHgap(20);
                subTeach.setOnAction(p->{
                    tSubMapGrid.getChildren().clear();
                    int teachSubCount= subTeach.getValue();
                    for (int m = 0; m < teachSubCount; m++) {
                        ComboBox<String> selSub = new ComboBox<>();
                        selSub.setPromptText("Select Subject");

                        tSubMapGrid.addColumn(m,selSub);
                    }
                });
                mapSubs.addRow(v,teachName,subTeach,tSubMapGrid);
            }


        });


        //adding scrollable to tab pane
        TabPane myTabPane = new TabPane();


        Tab inputTab = new Tab("Input Details - Required to generate the TimeTable",scrollPane);

        inputTab.setClosable(false);

        Tab genTimeTab = new Tab("Generated - TimeTable");
        genTimeTab.setClosable(false);
        myTabPane.getTabs().addAll(inputTab,genTimeTab);
        // Style selected tab with border

        myTabPane.getSelectionModel().selectedItemProperty().addListener((obs, oldTab, newTab) -> {
            for (Tab tab : myTabPane.getTabs()) {
                tab.setStyle(""); // Reset others
            }
            newTab.setStyle("-fx-border-color: #3c7fb1 ; -fx-border-width: 2 2 0 2;");
        });



        //visibility of subject mapping section before the action is triggered
        mapTitle.setVisible(false);
        tCountTitle.setVisible(false);
        teachCount.setVisible(false);
        mapSubs.setVisible(false);



        Scene scene = new Scene(myTabPane, 1080, 720); // Set scene with tab pane content
        root.getChildren().addAll(titleLabel,semCount,gridTitle,subGrid,tCountTitle,teachCount,mapTitle,mapSubs);
        Image icon = new Image("icon.png");
        stage.getIcons().add(icon);
        stage.setScene(scene);
        stage.setTitle("ClassMesh - TimeTable Management");
        stage.show();
    }


    public static void main(String[] args){
        launch(args);
    }
}
//Ver 1.5
package com.desk.classmesh; 

// JavaFX Imports
import javafx.application.Application;
import javafx.application.Platform;

//mac-address imports
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Collections;
import java.util.Enumeration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import java.util.Optional;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.util.Callback;
// iText 7 (for PDF Export)
import com.itextpdf.kernel.colors.ColorConstants;
import com.itextpdf.kernel.colors.DeviceRgb;
import com.itextpdf.kernel.font.PdfFont;
import com.itextpdf.kernel.font.PdfFontFactory;
import com.itextpdf.kernel.geom.PageSize;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.borders.Border;
import com.itextpdf.layout.borders.SolidBorder;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.layout.properties.UnitValue;
import com.itextpdf.layout.properties.VerticalAlignment;
import com.itextpdf.io.font.constants.StandardFonts;

// Also make sure you have the standard Java IO imports needed by handleExportPdf:
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
// Java Standard Library Imports
import java.io.*;
import java.time.Duration;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;

import java.util.stream.Collectors;

// Apache POI (for Excel Export)
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.usermodel.Cell; // Explicit import for Cell
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.apache.poi.ss.util.CellRangeAddress; // For merging cells


import static org.apache.poi.ss.usermodel.VerticalAlignment.CENTER;

// --- End External Library Imports ---


// ---Start of Data Model Classes ---

//SubjectObject
class Subject {
    String name;
    int weeklyHours;
    public Subject(String name, int hours) {
        this.name = name;
        this.weeklyHours = hours;
    }
    public String getName() {
        return name;
    }
    public int getWeeklyHours() {
        return weeklyHours;
    }
    @Override
    public String toString() {
        return name;
    }

    /**
     * MODIFIED: Now considers two subjects equal if their names match,
     * regardless of weekly hours. This is key to preserving teacher mappings
     * when only the hours of a subject are changed.
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Subject subject = (Subject) o;
        return Objects.equals(name, subject.name); // Only compare by name
    }

    /**
     * MODIFIED: Hash code now only depends on the name to be consistent
     * with the new equals() method.
     */
    @Override
    public int hashCode() {
        return Objects.hash(name);
    }
}
//semester object
class Semester {
    String name;
    List<Subject> subjects = new ArrayList<>();
    public Semester(String name) {
        this.name = name;
    }
    public String getName() {
        return name;
    }
    public List<Subject> getSubjects() {
        return subjects;
    }
    @Override
    public String toString() {
        return name;
    }
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Semester semester = (Semester) o;
        return Objects.equals(name, semester.name);
    }
    @Override
    public int hashCode() {
        return Objects.hash(name);
    }
}
//Teacher Object
class Teacher {
    String name;
    List<Subject> subjectsTaught = new ArrayList<>();
    public Teacher(String name) {
        this.name = name;
    }
    public String getName() {
        return name;
    }
    public List<Subject> getSubjectsTaught() {
        return subjectsTaught;
    }
    @Override
    public String toString() {
        return name;
    }
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Teacher teacher = (Teacher) o;
        return Objects.equals(name, teacher.name);
    }
    @Override
    public int hashCode() {
        return Objects.hash(name);
    }
}
//SubjectContextObject
class SubjectContext {
    private final Subject subject;
    private final Semester semester;
    public SubjectContext(Subject subject, Semester semester) {
        this.subject = subject;
        this.semester = semester;
    }
    public Subject getSubject() {
        return subject;
    }
    public Semester getSemester() {
        return semester;
    }

    @Override
    public String toString() {
        return subject.getName() + " (" + semester.getName() + ")";
    }

    /**
     * This method now relies on the updated Subject.equals() and Semester.equals()
     * methods, making it more resilient to changes.
     */
    @Override
    public boolean equals(Object o) { if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SubjectContext that = (SubjectContext) o;
        return Objects.equals(subject, that.subject) && Objects.equals(semester, that.semester);
    }
    @Override
    public int hashCode() {
        return Objects.hash(subject, semester);
    }
}
//TimeTableEntryObject
class TimetableEntry {
    String day;
    LocalTime startTime;
    LocalTime endTime;
    Semester semester;
    Subject subject;
    Teacher teacher;
    public TimetableEntry(String day, LocalTime startTime, LocalTime endTime, Semester semester, Subject subject, Teacher teacher) {
        this.day = day;
        this.startTime = startTime;
        this.endTime = endTime;
        this.semester = semester;
        this.subject = subject;
        this.teacher = teacher;
    }
    public String getDay() {
        return day;
    }
    public LocalTime getStartTime() {
        return startTime;
    }

    public Semester getSemester() {
        return semester;
    }
    public Subject getSubject() {
        return subject;
    }
    public Teacher getTeacher() {
        return teacher;
    }
}
// --- End Data Model Classes ---



public class ClassMesh extends Application {
    //About Section Constants

    // --- Data Structures for Save Simulation ---
    record TimeConfiguration(String workStart, String workEnd, String breakStart, String breakEnd, int slotDuration, int maxTeacherSlots) {
    }


// --- End Data Structures for Save Simulation ---

    // Helper record to temporarily store UI data for a subject
    record TempSubjectUIData(String subjectName, Integer hours) {}

    // Helper record to temporarily store UI data for a semester
    record TempSemesterUIData(String semesterName, Integer subjectCount, List<TempSubjectUIData> subjects) {}


    // NEW Configuration Data Structures
    record SubjectDetailConfig(String name, int hours) {}

    record SemesterConfigData(String name, List<SubjectDetailConfig> subjects) {}

    record TeacherAssignmentConfig(String subjectName, String semesterName) {}

    record TeacherConfigData(String name, List<TeacherAssignmentConfig> assignedSubjects) {}

    //...
    //record TempSemesterUIData(String semesterName, Integer subjectCount, List<TempSubjectUIData> subjects) {}

    // PASTE THE NEW RECORD HERE
    record TeacherRestoreState(int newSubjectCount, List<SubjectContext> validSubjects) {}

    // NEW Configuration Data Structures
    //record SubjectDetailConfig(String name, int hours) {}

    // Add this record definition inside your ClassMesh class, near the other records.
    //record TeacherRestoreState(int newSubjectCount, List<SubjectContext> validSubjects) {}

    // Main configuration container class
    static class AppConfiguration {
        public String schoolName;
        public String departmentName;
        public TimeConfiguration timeSettings;
        public List<SemesterConfigData> semesterConfigurations;
        public List<TeacherConfigData> teacherConfigurations;
        public Integer maxConsecutiveSlotsPerSubject;

        public AppConfiguration() {}
    }

    private static final String APP_NAME = "ClassMesh";
    private static final String APP_VERSION = "1.3.0";
    private static final String DEVELOPER_NAME = "Musaib Nazir";
    private static final String DEVELOPER_EMAIL = "grmusa9797@gmail.com";
    private static final String DEVELOPER_CONTACT = "+91-9541757976";



    // --- Constants ---
    private static final String MAIN_CONTAINER_STYLE_CLASS = "main-config-pane";
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm");
    private static final List<String> WORKING_DAYS = List.of("Monday", "Tuesday", "Wednesday", "Thursday", "Friday","Saturday");
    private static final int MIN_SUBJECT_HOURS = 1;
    private static final int DEFAULT_SUBJECT_HOURS = 3;
    private static final int MAX_SUBJECT_HOURS_SPINNER = 50;
    private static final int ABSOLUTE_MAX_SUBJECTS = 50;
    private static final String ALLOWED_MAC_ADDRESS = "74-13-EA-30-C9-5B";
    private int maxConsecutiveSlotsPerSubject = 0;

    // --- UI Elements ---
    private TextField schoolNameField;
    private TextField departmentNameField;
    private String schoolName = "";
    private String departmentName = "";
    private ComboBox<Integer> semCountComboBox; private GridPane semesterSubjectGrid; private Node semesterSectionContainer;
    private VBox timeConfigSection; private ComboBox<String> workStartTimeCombo; private ComboBox<String> workEndTimeCombo; private ComboBox<String> breakStartTimeCombo; private ComboBox<String> breakEndTimeCombo; private ComboBox<Integer> slotDurationCombo; private Label workingHoursLabel;
    private Spinner<Integer> maxTeacherSlotsSpinner;
    private Spinner<Integer> maxConsecutiveSubjectSpinner;
    private Label teacherSectionTitle; private Label teacherMapTitle; private ComboBox<Integer> teacherCountComboBox; private GridPane teacherMappingGrid; private Node teacherSectionContainer;
    private Button generateButton;
    private VBox mainContentVBox; private TabPane mainTabPane; private Tab generatedTimetableTab; private GridPane timetableDisplayGrid; private ScrollPane generatedScrollPane;
    private Button exportExcelButton;
    private Button exportPdfButton;


    // --- Data Storage ---
    private final List<Semester> semesterList = new ArrayList<>();
    private final List<Teacher> teacherList = new ArrayList<>();
    private final ObservableList<SubjectContext> allSubjectsObservableList = FXCollections.observableArrayList();
    private LocalTime workStartTime; private LocalTime workEndTime; private LocalTime breakStartTime; private LocalTime breakEndTime; private int slotDurationMinutes;
    private int maxTeacherSlotsPerDay = 2;
    private long netWorkingMinutesPerDay = 0;
    private int maxWeeklyHoursPerSemester = 0;
    private final Set<SubjectContext> globallySelectedSubjectContexts = new HashSet<>();
    private List<TimetableEntry> lastGeneratedTimetable = null;

    private boolean configLoadedFromFile = false;

    private final AtomicBoolean isAdjustingHours = new AtomicBoolean(false);
    private boolean adjustmentAlertPending = false;


    @Override
    public void start(Stage stage) {
        BorderPane mainLayout = new BorderPane();
        mainContentVBox = new VBox();
        mainContentVBox.setPadding(new Insets(10));
        mainContentVBox.setStyle("-fx-border-color:#6ca0dc;-fx-border-width: 0 2 2 2;-fx-border-radius:0 0 4 4;");
        VBox.setVgrow(mainContentVBox, Priority.ALWAYS);
        mainLayout.setTop(createMenuBar(stage));
        ScrollPane inputScrollPane = new ScrollPane(mainContentVBox);
        inputScrollPane.setFitToWidth(true);
        inputScrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        inputScrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        inputScrollPane.setStyle("-fx-background-color: transparent;");
        mainLayout.setCenter(inputScrollPane);

        if (!isOperationAllowed()) {
            showErrorAlert("Security Restriction", "This application is not authorized to run on this computer.\nPlease contact the application provider for assistance.");
            Platform.exit();
            System.exit(0);
            return;
        }

        Node schoolInfoSectionContainer = createSchoolInfoSection();
        timeConfigSection = createTimeConfigurationSection();
        semesterSectionContainer = createSemesterInputSection();
        teacherSectionContainer = createTeacherMappingSection();
        Node generateButtonSection = createGenerateButtonSection();

        mainContentVBox.getChildren().addAll(schoolInfoSectionContainer, timeConfigSection, semesterSectionContainer, teacherSectionContainer, generateButtonSection);

        mainTabPane = new TabPane();
        Tab inputTab = new Tab("Input Details", mainLayout);
        inputTab.setClosable(false);
        generatedTimetableTab = createTimetableTab();
        mainTabPane.getTabs().addAll(inputTab, generatedTimetableTab);
        setupTabPaneSelectionStyle(mainTabPane);

        hideSemesterSection();
        hideTeacherSection();
        hideGenerateButton();
        disableExportButtons();

        Scene scene = new Scene(mainTabPane, 1200, 800);
        loadStageIcon(stage);
        stage.setScene(scene);
        stage.setTitle("ClassMesh - TimeTable Management");
        stage.show();
    }

    //=========================================================================
    // UI Creation Helper Methods
    //=========================================================================
    private MenuBar createMenuBar(Stage stage) {
        MenuBar menuBar = new MenuBar();
        Menu fileMenu = new Menu("File");
        MenuItem loadItem = new MenuItem("Load Configuration");
        MenuItem saveItem = new MenuItem("Save Configuration");
        loadItem.setOnAction(e -> handleLoadConfiguration(stage));
        saveItem.setOnAction(e -> handleSaveConfiguration(stage));
        fileMenu.getItems().addAll(loadItem, saveItem);
        Menu helpMenu = new Menu("Help");
        MenuItem aboutItem = new MenuItem("About " + APP_NAME);
        aboutItem.setOnAction(e -> showAboutDialog());
        helpMenu.getItems().add(aboutItem);
        menuBar.getMenus().addAll(fileMenu, helpMenu);
        return menuBar;
    }

    private Node createSemesterInputSection() {
        VBox sectionContainer = new VBox(10);
        sectionContainer.setPadding(new Insets(10, 0, 10, 0));
        sectionContainer.setStyle("-fx-border-color:#6ca0dc;-fx-border-width: 2 0 0 0;-fx-padding: 15 0 15 0;");
        sectionContainer.setId("semesterSectionBox");
        Label titleLabel = new Label("Semester Configuration");
        titleLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");
        Label gridTitle = new Label("Semester/Class Names & Subjects:");
        gridTitle.setStyle("-fx-font-size: 14px; -fx-font-weight: bold;");
        semCountComboBox = new ComboBox<>();
        semCountComboBox.setPromptText("Number of Working Semesters/Classes");
        for (int i = 1; i <= 12; i++) semCountComboBox.getItems().add(i);
        semesterSubjectGrid = new GridPane();
        semesterSubjectGrid.setStyle("-fx-border-color:#6ca0dc;-fx-border-thickness:2px;-fx-border-radius:4px; -fx-padding: 10;");
        semesterSubjectGrid.getStyleClass().add(MAIN_CONTAINER_STYLE_CLASS);
        semesterSubjectGrid.setHgap(10);
        semesterSubjectGrid.setVgap(10);
        ColumnConstraints subCol0 = new ColumnConstraints();
        subCol0.setHgrow(Priority.NEVER);
        subCol0.setMinWidth(150);
        subCol0.setPrefWidth(150);
        ColumnConstraints subCol1 = new ColumnConstraints();
        subCol1.setHgrow(Priority.ALWAYS);
        semesterSubjectGrid.getColumnConstraints().addAll(subCol0, subCol1);
        semCountComboBox.setOnAction(e -> buildSemesterSubjectGrid());
        sectionContainer.getChildren().addAll(titleLabel, semCountComboBox, gridTitle, semesterSubjectGrid);
        return sectionContainer;
    }


    /**
     * REWRITTEN: Dynamically adds or removes semester rows without clearing the whole grid,
     * preserving the state of existing, unaffected rows.
     */
    private void buildSemesterSubjectGrid() {
        Integer newSelectedCount = Optional.ofNullable(semCountComboBox.getValue()).orElse(0);

        // Determine current number of actual semester rows
        int currentDisplayedSemCount = (int) semesterSubjectGrid.getChildren().stream()
                .map(GridPane::getRowIndex).filter(Objects::nonNull).distinct().count();
        if (currentDisplayedSemCount > 0 && semesterSubjectGrid.lookup(".button") != null) {
            currentDisplayedSemCount--; // Don't count the button row
        }


        hideTeacherSection();
        hideGenerateButton();

        // Remove the "Next" button before altering rows
        semesterSubjectGrid.getChildren().removeIf(node -> node instanceof Button && "Next -> Define Teachers".equals(((Button) node).getText()));

        // Case 1: ADD Semesters
        if (newSelectedCount > currentDisplayedSemCount) {
            for (int semIndexToAdd = currentDisplayedSemCount; semIndexToAdd < newSelectedCount; semIndexToAdd++) {
                createAndAddSemesterRowUI(semIndexToAdd, null);
            }
        }
        // Case 2: REMOVE Semesters
        else if (newSelectedCount < currentDisplayedSemCount) {
            semesterSubjectGrid.getChildren().removeIf(node -> {
                Integer r = GridPane.getRowIndex(node);
                return r != null && r >= newSelectedCount;
            });
        }

        // Re-add the "Next" button if needed
        if (newSelectedCount > 0) {
            Button nextButton = new Button("Next -> Define Teachers");
            nextButton.setStyle("-fx-background-color: #2196F3; -fx-text-fill: white; -fx-font-size: 14px; -fx-padding: 6 16 6 16; -fx-background-radius: 6;");
            nextButton.setOnAction(e -> handleSemesterNextClick(semesterSubjectGrid, newSelectedCount));
            semesterSubjectGrid.add(nextButton, 0, newSelectedCount, 2, 1);
            GridPane.setHalignment(nextButton, Pos.CENTER.getHpos());
            GridPane.setMargin(nextButton, new Insets(15, 0, 0, 0));
        }
    }

    /**
     * REWRITTEN: Helper method to create UI for a single semester row.
     * Includes fixes for lambda variable scoping issues.
     */
    private void createAndAddSemesterRowUI(int semIndex, TempSemesterUIData existingSemData) {
        int maxSubjectsToShow = Math.min(ABSOLUTE_MAX_SUBJECTS, Optional.of(maxWeeklyHoursPerSemester / Math.max(1, MIN_SUBJECT_HOURS)).orElse(25));

        TextField semNameField = new TextField();
        semNameField.setPromptText("Semester/Class " + (semIndex + 1) + " Name");
        semNameField.setId("semName_" + semIndex);

        ComboBox<Integer> subCountCombo = new ComboBox<>();
        subCountCombo.setPromptText("Subject Count");
        for (int j = 0; j <= maxSubjectsToShow; j++) subCountCombo.getItems().add(j);
        subCountCombo.setId("subCount_" + semIndex);

        FlowPane subDetailPane = new FlowPane(Orientation.HORIZONTAL, 10, 5);
        subDetailPane.setId("subDetailPane_" + semIndex);
        subDetailPane.setPrefWrapLength(600);

        subCountCombo.setOnAction(e -> {
            int newSubjectCount = Optional.ofNullable(subCountCombo.getValue()).orElse(0);

            List<HBox> currentHBoxes = subDetailPane.getChildren().stream()
                    .filter(HBox.class::isInstance).map(HBox.class::cast).collect(Collectors.toList());
            int currentSubjectHBoxCount = currentHBoxes.size();

            // ADD new subject fields if needed
            if (newSubjectCount > currentSubjectHBoxCount) {
                for (int subIdxToAdd = currentSubjectHBoxCount; subIdxToAdd < newSubjectCount; subIdxToAdd++) {
                    TextField newSubNameField = new TextField();
                    newSubNameField.setPromptText("Subject " + (subIdxToAdd + 1) + " Name");
                    newSubNameField.setId("subName_" + semIndex + "_" + subIdxToAdd);
                    newSubNameField.setPrefWidth(200);

                    Spinner<Integer> newHoursSpinner = new Spinner<>();
                    SpinnerValueFactory.IntegerSpinnerValueFactory valueFactory =
                            new SpinnerValueFactory.IntegerSpinnerValueFactory(MIN_SUBJECT_HOURS, MAX_SUBJECT_HOURS_SPINNER, DEFAULT_SUBJECT_HOURS);
                    newHoursSpinner.setValueFactory(valueFactory);
                    newHoursSpinner.setEditable(true);
                    newHoursSpinner.setPrefWidth(70);
                    newHoursSpinner.setId("subHours_" + semIndex + "_" + subIdxToAdd);

                    // FIX: Ensure lambdas use effectively final variables
                    final FlowPane parentPaneForListener = subDetailPane;
                    newHoursSpinner.valueProperty().addListener((obs, oldVal, newVal) -> {
                        if (newVal != null && !isAdjustingHours.get()) {
                            adjustHoursWithinSemester(parentPaneForListener, newHoursSpinner);
                        }
                    });

                    HBox entryBox = new HBox(5, newSubNameField, new Label("Hrs/Week:"), newHoursSpinner);
                    entryBox.setAlignment(Pos.CENTER_LEFT);
                    subDetailPane.getChildren().add(entryBox);
                }
            }
            // REMOVE excess subject fields
            else if (newSubjectCount < currentSubjectHBoxCount) {
                subDetailPane.getChildren().remove(newSubjectCount, currentSubjectHBoxCount);
            }

            // Always run adjustment logic after changes
            if (newSubjectCount > 0) {
                adjustHoursWithinSemester(subDetailPane, null);
            }
        });

        // Pre-fill with existing data if provided
        if (existingSemData != null) {
            semNameField.setText(existingSemData.semesterName());
            Optional.ofNullable(existingSemData.subjectCount()).ifPresent(count -> {
                subCountCombo.setValue(count);
                Platform.runLater(() -> {
                    for (int j = 0; j < existingSemData.subjects().size(); j++) {
                        if (j < subDetailPane.getChildren().size() && subDetailPane.getChildren().get(j) instanceof HBox) {
                            HBox box = (HBox) subDetailPane.getChildren().get(j);
                            ((TextField) box.getChildren().get(0)).setText(existingSemData.subjects().get(j).subjectName());
                            ((Spinner<Integer>) box.getChildren().get(2)).getValueFactory().setValue(existingSemData.subjects().get(j).hours());
                        }
                    }
                });
            });
        }

        VBox subjectInputArea = new VBox(5, subCountCombo, subDetailPane);
        subjectInputArea.setId("subjectInputArea_" + semIndex);
        semesterSubjectGrid.add(semNameField, 0, semIndex);
        semesterSubjectGrid.add(subjectInputArea, 1, semIndex);
        GridPane.setHgrow(subjectInputArea, Priority.ALWAYS);
    }

    private VBox createTimeConfigurationSection() {
        VBox container = new VBox(10);
        container.setPadding(new Insets(0, 0, 10, 0));
        container.setId("timeConfigBox");
        Label timeTitle = new Label("Time Configuration");
        timeTitle.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");
        GridPane timeGrid = new GridPane();
        timeGrid.setHgap(10);
        timeGrid.setVgap(8);
        timeGrid.setPadding(new Insets(0, 0, 0, 10));
        ObservableList<String> timeOptions = FXCollections.observableArrayList();
        for (int hour = 7; hour <= 18; hour++) {
            timeOptions.add(String.format("%02d:00", hour));
            timeOptions.add(String.format("%02d:30", hour));
        }
        workStartTimeCombo = new ComboBox<>(timeOptions);
        workStartTimeCombo.setPromptText("Work Start");
        workStartTimeCombo.setValue("10:00");
        workEndTimeCombo = new ComboBox<>(timeOptions);
        workEndTimeCombo.setPromptText("Work End");
        workEndTimeCombo.setValue("16:00");
        breakStartTimeCombo = new ComboBox<>(timeOptions);
        breakStartTimeCombo.setPromptText("Break Start");
        breakStartTimeCombo.setValue("13:00");
        breakEndTimeCombo = new ComboBox<>(timeOptions);
        breakEndTimeCombo.setPromptText("Break End");
        breakEndTimeCombo.setValue("14:00");
        slotDurationCombo = new ComboBox<>();
        slotDurationCombo.setPromptText("Slot Duration (min)");
        slotDurationCombo.getItems().addAll(30, 45, 50, 55, 60, 90);
        slotDurationCombo.setValue(60);

        maxConsecutiveSubjectSpinner = new Spinner<>(1, 8, 3);
        maxConsecutiveSubjectSpinner.setPrefWidth(70);
        maxConsecutiveSubjectSpinner.setEditable(true);

        maxTeacherSlotsSpinner = new Spinner<>(1, 8, 2);
        maxTeacherSlotsSpinner.setPrefWidth(70);
        maxTeacherSlotsSpinner.setEditable(true);

        workStartTimeCombo.valueProperty().addListener((obs, ov, nv) -> updateWorkingHoursDisplay());
        workEndTimeCombo.valueProperty().addListener((obs, ov, nv) -> updateWorkingHoursDisplay());
        breakStartTimeCombo.valueProperty().addListener((obs, ov, nv) -> updateWorkingHoursDisplay());
        breakEndTimeCombo.valueProperty().addListener((obs, ov, nv) -> updateWorkingHoursDisplay());
        workingHoursLabel = new Label("Working Hours/Day: --");
        workingHoursLabel.setStyle("-fx-font-style: italic; -fx-padding: 0 0 0 10px;");
        timeGrid.addRow(0, new Label("Working Hours:"), workStartTimeCombo, new Label("to"), workEndTimeCombo);
        timeGrid.addRow(1, new Label("Break Time:"), breakStartTimeCombo, new Label("to"), breakEndTimeCombo);
        timeGrid.addRow(2, new Label("Class Duration:"), slotDurationCombo);
        timeGrid.addRow(3, new Label("Max Classes/Teacher/Day:"), maxTeacherSlotsSpinner);
        timeGrid.addRow(4, new Label("Consecutive Classes/Subject"), maxConsecutiveSubjectSpinner);
        timeGrid.add(workingHoursLabel, 1, 5, 3, 1);
        Button timeNextButton = new Button("Next -> Define Semesters");
        timeNextButton.setStyle("-fx-background-color: #2196F3; -fx-text-fill: white; -fx-font-size: 14px; -fx-padding: 6 16 6 16; -fx-background-radius: 6;");
        timeNextButton.setOnAction(e -> handleTimeConfigNextClick());
        container.getChildren().addAll(timeTitle, timeGrid, timeNextButton);
        container.setAlignment(Pos.CENTER_LEFT);
        VBox.setMargin(timeNextButton, new Insets(15, 0, 0, 0));
        updateWorkingHoursDisplay();
        return container;
    }
    private Node createTeacherMappingSection() {
        VBox sectionContainer = new VBox(5);
        sectionContainer.setPadding(new Insets(10, 0, 10, 0));
        sectionContainer.setStyle("-fx-border-color:#6ca0dc;-fx-border-width: 2 0 0 0;-fx-padding: 15 0 15 0;");
        sectionContainer.setId("teacherSectionBox");
        teacherSectionTitle = new Label("Teaching Staff Configuration");
        teacherSectionTitle.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");
        teacherCountComboBox = new ComboBox<>();
        teacherCountComboBox.setPromptText("Select Number of Teachers");
        for (int t = 1; t <= 25; t++) teacherCountComboBox.getItems().add(t);
        teacherMapTitle = new Label("Teacher - Subject Mapping");
        teacherMapTitle.setStyle("-fx-font-size: 14px; -fx-font-weight: bold;");
        teacherMappingGrid = new GridPane();
        teacherMappingGrid.setStyle("-fx-border-color:#6ca0dc;-fx-border-thickness:2px;-fx-border-radius:4px;-fx-padding:10px;");
        teacherMappingGrid.getStyleClass().add(MAIN_CONTAINER_STYLE_CLASS);
        teacherMappingGrid.setVgap(10);
        teacherMappingGrid.setHgap(10);
        ColumnConstraints teachCol0 = new ColumnConstraints();
        teachCol0.setHgrow(Priority.NEVER);
        teachCol0.setMinWidth(150);
        teachCol0.setPrefWidth(150);
        ColumnConstraints teachCol1 = new ColumnConstraints();
        teachCol1.setHgrow(Priority.ALWAYS);
        teacherMappingGrid.getColumnConstraints().addAll(teachCol0, teachCol1);

        // ===================================================================
        // THE FIX IS HERE
        // ===================================================================
        // This logic now captures the state before modifying the teacher grid.
        teacherCountComboBox.setOnAction(event -> {
            // Step 1: Capture existing selections before the UI is modified.
            Map<String, SubjectContext> selectionsToRestore = new HashMap<>();
            teacherMappingGrid.getChildren().stream()
                    .filter(node -> node instanceof VBox && GridPane.getColumnIndex(node) == 1) // Find VBox in col 1
                    .flatMap(vbox -> ((VBox) vbox).getChildren().stream())         // Get children (Combo, FlowPane)
                    .filter(fpane -> fpane instanceof FlowPane)                   // Find the FlowPane
                    .flatMap(fpane -> ((FlowPane) fpane).getChildren().stream()) // Get subject ComboBoxes
                    .filter(combo -> combo instanceof ComboBox && ((ComboBox<?>) combo).getValue() != null)
                    .forEach(combo -> {
                        // Store the current selection using the ComboBox's unique ID as the key
                        selectionsToRestore.put(combo.getId(), (SubjectContext) ((ComboBox<?>) combo).getValue());
                    });

            // Step 2: Call the main population logic, now passing the captured state.
            // This will add/remove teacher rows and restore the selections for the remaining rows.
            populateTeacherMappingGrid(allSubjectsObservableList, selectionsToRestore);
        });
        // ===================================================================
        // END OF FIX
        // ===================================================================

        sectionContainer.getChildren().addAll(teacherSectionTitle, teacherCountComboBox, teacherMapTitle, teacherMappingGrid);
        return sectionContainer;
    }

    private Node createSchoolInfoSection() {
        VBox sectionContainer = new VBox(10);
        sectionContainer.setPadding(new Insets(10, 0, 10, 0));
        sectionContainer.setId("schoolInfoSectionBox");
        Label titleLabel = new Label("Institution Information");
        titleLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");
        GridPane infoGrid = new GridPane();
        infoGrid.setHgap(10);
        infoGrid.setVgap(8);
        infoGrid.setPadding(new Insets(0, 0, 0, 10));
        schoolNameField = new TextField();
        schoolNameField.setPromptText("Enter School/College Name");
        schoolNameField.setPrefWidth(300);
        departmentNameField = new TextField();
        departmentNameField.setPromptText("Enter Department Name");
        departmentNameField.setPrefWidth(300);
        infoGrid.addRow(0, new Label("School/College:"), schoolNameField);
        infoGrid.addRow(1, new Label("Department:"), departmentNameField);
        schoolNameField.textProperty().addListener((obs, oldVal, newVal) -> schoolName = newVal.trim());
        departmentNameField.textProperty().addListener((obs, oldVal, newVal) -> departmentName = newVal.trim());
        sectionContainer.getChildren().addAll(titleLabel, infoGrid);
        return sectionContainer;
    }

    private Node createGenerateButtonSection() {
        generateButton = new Button("Generate Timetable");
        generateButton.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white; -fx-font-size: 16px; -fx-padding: 10 20 10 20; -fx-background-radius: 6;");
        generateButton.setOnAction(e -> handleGenerateClick());
        HBox buttonBox = new HBox(generateButton);
        buttonBox.setAlignment(Pos.CENTER);
        buttonBox.setPadding(new Insets(20, 0, 10, 0));
        return buttonBox;
    }

    private Tab createTimetableTab() {
        timetableDisplayGrid = new GridPane();
        timetableDisplayGrid.setPadding(new Insets(10));
        timetableDisplayGrid.setHgap(2);
        timetableDisplayGrid.setVgap(2);
        timetableDisplayGrid.setAlignment(Pos.TOP_LEFT);
        timetableDisplayGrid.setGridLinesVisible(true);

        Label schoolNameLabelForTab = new Label();
        schoolNameLabelForTab.setFont(Font.font("System", FontWeight.BOLD, 16));
        Label departmentNameLabelForTab = new Label();
        departmentNameLabelForTab.setFont(Font.font("System", FontWeight.NORMAL, 14));

        generatedScrollPane = new ScrollPane(timetableDisplayGrid);
        generatedScrollPane.setFitToWidth(true);
        generatedScrollPane.setFitToHeight(true);
        generatedScrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        generatedScrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);

        exportExcelButton = new Button("Export to Excel");
        exportExcelButton.setOnAction(e -> handleExportExcel());
        exportPdfButton = new Button("Export to PDF");
        exportPdfButton.setOnAction(e -> handleExportPdf());

        HBox exportButtonBox = new HBox(10, exportExcelButton, exportPdfButton);
        exportButtonBox.setAlignment(Pos.CENTER);
        exportButtonBox.setPadding(new Insets(10, 0, 0, 0));

        VBox tabContent = new VBox(10);
        tabContent.setPadding(new Insets(10));
        tabContent.setAlignment(Pos.CENTER);

        tabContent.getChildren().addAll(schoolNameLabelForTab, departmentNameLabelForTab, generatedScrollPane, exportButtonBox);
        VBox.setVgrow(generatedScrollPane, Priority.ALWAYS);

        generatedTimetableTab = new Tab("Generated Timetable", tabContent);
        generatedTimetableTab.setClosable(false);
        generatedTimetableTab.selectedProperty().addListener((obs, wasSelected, isNowSelected) -> {
            if (isNowSelected) {
                String schoolText = schoolNameField.getText().trim();
                String deptText = departmentNameField.getText().trim();
                schoolNameLabelForTab.setText(schoolText);
                departmentNameLabelForTab.setText(deptText);
                schoolNameLabelForTab.setVisible(!schoolText.isEmpty());
                schoolNameLabelForTab.setManaged(!schoolText.isEmpty());
                departmentNameLabelForTab.setVisible(!deptText.isEmpty());
                departmentNameLabelForTab.setManaged(!deptText.isEmpty());
            }
        });
        return generatedTimetableTab;
    }


    //=========================================================================
    // Event Handling and Logic Methods
    //=========================================================================

    /**
     * MODIFIED: Captures teacher assignments before updating subject list to preserve them.
     */
    /**
     * MODIFIED: Now pre-filters captured teacher assignments to ensure any assignments
     * to a now-deleted subject are automatically cleared.
     */
    /**
     * CORRECTED: This version removes the local 'record' definition, as it's now
     * defined at the class level, fixing the "Cannot resolve symbol" error.
     */
    private void handleSemesterNextClick(GridPane currentSemesterSubjectGrid, int expectedSemCount) {
        // Step 1: Capture the UI state of all current teacher assignments.
        Map<String, SubjectContext> capturedSelections = new HashMap<>();
        teacherMappingGrid.getChildren().stream()
                .filter(node -> node instanceof VBox && GridPane.getColumnIndex(node) == 1)
                .flatMap(vbox -> ((VBox) vbox).getChildren().stream())
                .filter(fpane -> fpane instanceof FlowPane)
                .flatMap(fpane -> ((FlowPane) fpane).getChildren().stream())
                .filter(combo -> combo instanceof ComboBox && ((ComboBox<?>) combo).getValue() != null)
                .forEach(combo -> capturedSelections.put(combo.getId(), (SubjectContext) ((ComboBox<?>) combo).getValue()));

        // Step 2: Build the new, definitive list of valid subjects from the semester grid.
        semesterList.clear();
        allSubjectsObservableList.clear();
        boolean isValid = true;
        List<SubjectContext> tempSubjectContexts = new ArrayList<>();
        Map<Integer, Semester> tempSemesters = new HashMap<>();
        for (int k = 0; k < expectedSemCount; k++) {
            VBox subjectInputArea = (VBox) findNodeInGrid(currentSemesterSubjectGrid, k, 1);
            if (subjectInputArea == null) { isValid = false; break; }
            Semester currentSemester = new Semester(((TextField)findNodeInGrid(currentSemesterSubjectGrid, k, 0)).getText().trim());
            tempSemesters.put(k, currentSemester);
            FlowPane subDetailPane = (FlowPane) subjectInputArea.getChildren().get(1);
            for(Node node : subDetailPane.getChildren()){
                HBox box = (HBox) node;
                String subName = ((TextField)box.getChildren().get(0)).getText().trim();
                Integer hours = ((Spinner<Integer>)box.getChildren().get(2)).getValue();
                Subject subject = new Subject(subName, hours);
                currentSemester.getSubjects().add(subject);
                tempSubjectContexts.add(new SubjectContext(subject, currentSemester));
            }
        }
        if (!isValid) { showErrorAlert("Validation Error", "Please ensure all semester and subject names are filled."); return; }

        semesterList.addAll(tempSemesters.values());
        allSubjectsObservableList.setAll(tempSubjectContexts);
        allSubjectsObservableList.sort(Comparator.comparing(sc -> sc.getSubject().getName()));

        // Step 3: Reconcile old assignments with the new list of valid subjects.
        Map<Integer, TeacherRestoreState> restorePlan = new HashMap<>();
        Map<Integer, List<SubjectContext>> oldAssignmentsByTeacher = new HashMap<>();
        capturedSelections.forEach((id, subjectContext) -> {
            int teacherIndex = Integer.parseInt(id.split("_")[1]);
            oldAssignmentsByTeacher.computeIfAbsent(teacherIndex, k -> new ArrayList<>()).add(subjectContext);
        });

        oldAssignmentsByTeacher.forEach((teacherIndex, oldAssignments) -> {
            List<SubjectContext> validAssignments = oldAssignments.stream()
                    .filter(allSubjectsObservableList::contains)
                    .collect(Collectors.toList());
            restorePlan.put(teacherIndex, new TeacherRestoreState(validAssignments.size(), validAssignments));
        });

        // Step 4: Show the teacher section and execute the restore plan.
        showTeacherSection();
        hideGenerateButton();
        if (teacherCountComboBox.getValue() != null) {
            updateTeacherGridWithRestorePlan(restorePlan);
        }
    }

    /**
     * CORRECTED: This method can now see the class-level 'TeacherRestoreState' record
     * and will compile correctly.
     */
    private void updateTeacherGridWithRestorePlan(Map<Integer, TeacherRestoreState> restorePlan) {
        Integer teacherCount = teacherCountComboBox.getValue();
        if (teacherCount == null) return;

        // First, update the subject count for each affected teacher.
        restorePlan.forEach((teacherIndex, state) -> {
            if (teacherIndex < teacherCount) {
                VBox teacherInputsVBox = (VBox) findNodeInGrid(teacherMappingGrid, teacherIndex, 1);
                if (teacherInputsVBox != null) {
                    ComboBox<Integer> subTeachCountCombo = (ComboBox<Integer>) teacherInputsVBox.getChildren().get(0);
                    // This triggers the onAction to add/remove subject ComboBoxes
                    subTeachCountCombo.setValue(state.newSubjectCount());
                }
            }
        });

        // Use Platform.runLater to let the UI update before we set values.
        Platform.runLater(() -> {
            globallySelectedSubjectContexts.clear();
            restorePlan.forEach((teacherIndex, state) -> {
                if (teacherIndex < teacherCount) {
                    VBox teacherInputsVBox = (VBox) findNodeInGrid(teacherMappingGrid, teacherIndex, 1);
                    if (teacherInputsVBox != null) {
                        FlowPane tSubMapFlowPane = (FlowPane) teacherInputsVBox.getChildren().get(1);
                        List<SubjectContext> subjectsToAssign = state.validSubjects();

                        for (int i = 0; i < subjectsToAssign.size(); i++) {
                            if (i < tSubMapFlowPane.getChildren().size()) {
                                ComboBox<SubjectContext> subjectCombo = (ComboBox<SubjectContext>) tSubMapFlowPane.getChildren().get(i);
                                SubjectContext subjectToAssign = subjectsToAssign.get(i);

                                if(!globallySelectedSubjectContexts.contains(subjectToAssign)){
                                    subjectCombo.setItems(allSubjectsObservableList);
                                    subjectCombo.setValue(subjectToAssign);
                                    globallySelectedSubjectContexts.add(subjectToAssign);
                                }
                            }
                        }
                    }
                }
            });
            refreshAllTeacherSubjectComboBoxes();
            checkIfReadyToGenerate();
        });
    }

    private void handleTimeConfigNextClick() {
        if (validateAndStoreTimeConfig()) {
            Duration workDuration = Duration.between(workStartTime, workEndTime);
            Duration breakDuration = Duration.between(breakStartTime, breakEndTime);
            netWorkingMinutesPerDay = workDuration.minus(breakDuration).toMinutes();
            maxWeeklyHoursPerSemester = (int) (netWorkingMinutesPerDay * WORKING_DAYS.size()) / 60;
            updateWorkingHoursDisplay();
            showSemesterSection();
            hideTeacherSection();
            hideGenerateButton();
            if (semCountComboBox.getValue() != null) {
                buildSemesterSubjectGrid();
            }
        } else {
            netWorkingMinutesPerDay = 0;
            maxWeeklyHoursPerSemester = 0;
            updateWorkingHoursDisplay();
            hideSemesterSection();
            hideTeacherSection();
            hideGenerateButton();
        }
    }

    private boolean validateAndStoreTimeConfig() {
        String errorMsg = "";
        Node nodeToHighlight = null;
        try {
            String ws = workStartTimeCombo.getValue();
            String we = workEndTimeCombo.getValue();
            String bs = breakStartTimeCombo.getValue();
            String be = breakEndTimeCombo.getValue();
            Integer duration = slotDurationCombo.getValue();
            Integer maxSlots = maxTeacherSlotsSpinner.getValue();
            Integer maxConsecutiveSub = maxConsecutiveSubjectSpinner.getValue();

            if (ws == null) {
                errorMsg = "Select Work Start Time.";
                nodeToHighlight = workStartTimeCombo;
            } else if (we == null) {
                errorMsg = "Select Work End Time.";
                nodeToHighlight = workEndTimeCombo;
            } else if (bs == null) {
                errorMsg = "Select Break Start Time.";
                nodeToHighlight = breakStartTimeCombo;
            } else if (be == null) {
                errorMsg = "Select Break End Time.";
                nodeToHighlight = breakEndTimeCombo;
            } else if (duration == null) {
                errorMsg = "Select Slot Duration.";
                nodeToHighlight = slotDurationCombo;
            } else if (maxSlots == null) {
                errorMsg = "Select Max Classes/Teacher/Day.";
                nodeToHighlight = maxTeacherSlotsSpinner;
            } else if (maxConsecutiveSub == null) {
                errorMsg = "Select Max Consecutive Slots for Same Subject.";
                nodeToHighlight = maxConsecutiveSubjectSpinner;
            } else {
                workStartTime = LocalTime.parse(ws, TIME_FORMATTER);
                workEndTime = LocalTime.parse(we, TIME_FORMATTER);
                breakStartTime = LocalTime.parse(bs, TIME_FORMATTER);
                breakEndTime = LocalTime.parse(be, TIME_FORMATTER);
                slotDurationMinutes = duration;
                maxTeacherSlotsPerDay = maxSlots;
                maxConsecutiveSlotsPerSubject = maxConsecutiveSub;

                if (workEndTime.isBefore(workStartTime) || workEndTime.equals(workStartTime)) {
                    errorMsg = "Work End Time must be after Work Start Time.";
                    nodeToHighlight = workEndTimeCombo;
                } else if (breakEndTime.isBefore(breakStartTime) || breakEndTime.equals(breakStartTime)) {
                    errorMsg = "Break End Time must be after Break Start Time.";
                    nodeToHighlight = breakEndTimeCombo;
                } else if (breakStartTime.isBefore(workStartTime) || breakEndTime.isAfter(workEndTime)) {
                    errorMsg = "Break time must be fully within working hours.";
                    nodeToHighlight = breakStartTimeCombo;
                } else if (Duration.between(workStartTime, workEndTime).toMinutes() < slotDurationMinutes) {
                    errorMsg = "Total working duration is less than a single slot duration.";
                    nodeToHighlight = slotDurationCombo;
                } else if (maxTeacherSlotsPerDay <= 0) {
                    errorMsg = "Max Classes/Teacher/Day must be at least 1.";
                    nodeToHighlight = maxTeacherSlotsSpinner;
                }
            }
        } catch (DateTimeParseException | NumberFormatException e) {
            errorMsg = "Invalid time or number format selected.";
            nodeToHighlight = workStartTimeCombo;
        }

        resetHighlight(workStartTimeCombo);
        resetHighlight(workEndTimeCombo);
        resetHighlight(breakStartTimeCombo);
        resetHighlight(breakEndTimeCombo);
        resetHighlight(slotDurationCombo);
        resetHighlight(maxTeacherSlotsSpinner);

        if (!errorMsg.isEmpty()) {
            if (nodeToHighlight != null) highlightError(nodeToHighlight);
            showErrorAlert("Time Configuration Error", errorMsg);
            return false;
        }
        return true;
    }

    private void updateWorkingHoursDisplay() {
        String wsStr = workStartTimeCombo.getValue();
        String weStr = workEndTimeCombo.getValue();
        String bsStr = breakStartTimeCombo.getValue();
        String beStr = breakEndTimeCombo.getValue();
        if (wsStr == null || weStr == null || bsStr == null || beStr == null) {
            workingHoursLabel.setText("Working Hours/Day: --");
            return;
        }
        try {
            LocalTime ws = LocalTime.parse(wsStr, TIME_FORMATTER);
            LocalTime we = LocalTime.parse(weStr, TIME_FORMATTER);
            LocalTime bs = LocalTime.parse(bsStr, TIME_FORMATTER);
            LocalTime be = LocalTime.parse(beStr, TIME_FORMATTER);
            if (we.isBefore(ws) || be.isBefore(bs) || bs.isBefore(ws) || be.isAfter(we)) {
                workingHoursLabel.setText("Working Hours/Day: Invalid");
                netWorkingMinutesPerDay = 0;
                maxWeeklyHoursPerSemester = 0;
                return;
            }
            Duration netDuration = Duration.between(ws, we).minus(Duration.between(bs, be));
            netWorkingMinutesPerDay = netDuration.toMinutes();
            maxWeeklyHoursPerSemester = (int) (netWorkingMinutesPerDay * WORKING_DAYS.size()) / 60;
            workingHoursLabel.setText(String.format("Working Hours/Day: %dh %02dm (%d min)", netDuration.toHours(), netDuration.toMinutesPart(), netWorkingMinutesPerDay));
        } catch (DateTimeParseException e) {
            workingHoursLabel.setText("Working Hours/Day: Invalid Time");
        }
    }

    private void adjustHoursWithinSemester(FlowPane subjectDetailPane, Spinner<Integer> changedSpinner) {
        if (isAdjustingHours.getAndSet(true)) return;
        try {
            if (maxWeeklyHoursPerSemester <= 0) return;

            List<Spinner<Integer>> allSpinners = subjectDetailPane.getChildren().stream()
                    .filter(HBox.class::isInstance)
                    .map(node -> (Spinner<Integer>) ((HBox) node).getChildren().get(2))
                    .collect(Collectors.toList());

            int currentTotalHours = allSpinners.stream().mapToInt(Spinner::getValue).sum();
            int overloadHours = currentTotalHours - maxWeeklyHoursPerSemester;

            if (overloadHours > 0) {
                allSpinners.sort(Comparator.comparing(Spinner<Integer>::getValue).reversed());
                int remainingOverload = overloadHours;
                for (Spinner<Integer> spinner : allSpinners) {
                    if (remainingOverload <= 0) break;
                    if (spinner == changedSpinner && allSpinners.size() > 1) continue;
                    int reduction = Math.min(remainingOverload, spinner.getValue() - MIN_SUBJECT_HOURS);
                    if (reduction > 0) {
                        spinner.getValueFactory().setValue(spinner.getValue() - reduction);
                        remainingOverload -= reduction;
                    }
                }
                if (remainingOverload > 0 && changedSpinner != null) {
                    int cappedValue = Math.max(MIN_SUBJECT_HOURS, changedSpinner.getValue() - remainingOverload);
                    changedSpinner.getValueFactory().setValue(cappedValue);
                }
                adjustmentAlertPending = true;
            }
        } finally {
            isAdjustingHours.set(false);
        }
    }


    /**
     * Overloaded method to handle initial population.
     */
    private void populateTeacherMappingGrid(ObservableList<SubjectContext> newAvailableSubjectContexts) {
        populateTeacherMappingGrid(newAvailableSubjectContexts, Collections.emptyMap());
    }

    /**
     * REWRITTEN: Main logic to build and refresh the teacher mapping grid.
     * It's now non-destructive and preserves state.
     */
    private void populateTeacherMappingGrid(ObservableList<SubjectContext> newAvailableSubjectContexts, Map<String, SubjectContext> selectionsToRestore) {
        Integer desiredTeacherCount = Optional.ofNullable(teacherCountComboBox.getValue()).orElse(0);

        // Determine current teacher rows
        int currentTeacherRows = (int) teacherMappingGrid.getChildren().stream()
                .map(GridPane::getRowIndex).filter(Objects::nonNull).distinct().count();

        hideGenerateButton();

        // Add or remove rows dynamically
        if (desiredTeacherCount > currentTeacherRows) {
            for (int i = currentTeacherRows; i < desiredTeacherCount; i++) {
                createAndAddTeacherRowUI(i, newAvailableSubjectContexts);
            }
        } else if (desiredTeacherCount < currentTeacherRows) {
            teacherMappingGrid.getChildren().removeIf(node -> Optional.ofNullable(GridPane.getRowIndex(node)).map(r -> r >= desiredTeacherCount).orElse(false));
        }

        // --- Refresh data for all existing rows ---
        globallySelectedSubjectContexts.clear();
        for (int i = 0; i < desiredTeacherCount; i++) {
            VBox teacherInputsVBox = (VBox) findNodeInGrid(teacherMappingGrid, i, 1);
            if (teacherInputsVBox == null) continue;

            ComboBox<Integer> subTeachCountCombo = (ComboBox<Integer>) teacherInputsVBox.getChildren().get(0);
            FlowPane tSubMapFlowPane = (FlowPane) teacherInputsVBox.getChildren().get(1);

            int maxSubs = newAvailableSubjectContexts.size();
            Integer currentSubCount = subTeachCountCombo.getValue();
            subTeachCountCombo.getItems().clear();
            for (int l = 0; l <= Math.min(maxSubs, 15); l++) subTeachCountCombo.getItems().add(l);
            if (currentSubCount != null && currentSubCount <= maxSubs) subTeachCountCombo.setValue(currentSubCount);

            tSubMapFlowPane.getChildren().stream()
                    .map(ComboBox.class::cast)
                    .forEach(subjectCombo -> {
                        subjectCombo.setItems(newAvailableSubjectContexts);
                        SubjectContext oldSelection = selectionsToRestore.get(subjectCombo.getId());
                        if (oldSelection != null && newAvailableSubjectContexts.contains(oldSelection) && !globallySelectedSubjectContexts.contains(oldSelection)) {
                            subjectCombo.setValue(oldSelection);
                            globallySelectedSubjectContexts.add(oldSelection);
                        } else {
                            subjectCombo.setValue(null);
                        }
                    });
        }
        rebuildGlobalSubjectSet(teacherMappingGrid); // Final consistency check
        refreshAllTeacherSubjectComboBoxes();
        checkIfReadyToGenerate();
    }

    /**
     * Helper to create the UI for a single teacher row.
     */
    private void createAndAddTeacherRowUI(int teacherIndex, ObservableList<SubjectContext> availableSubjects) {
        TextField teachNameField = new TextField();
        teachNameField.setPromptText("Teacher " + (teacherIndex + 1) + " Name");
        teachNameField.setId("teachName_" + teacherIndex);

        ComboBox<Integer> subTeachCountCombo = new ComboBox<>();
        subTeachCountCombo.setPromptText("No. of Subjects");

        FlowPane tSubMapFlowPane = new FlowPane(Orientation.HORIZONTAL, 5, 5);
        tSubMapFlowPane.setId("teachSubMapFlowPane_" + teacherIndex);
        tSubMapFlowPane.setPrefWrapLength(600);

        setupDynamicSubjectCombosForTeacher(subTeachCountCombo, tSubMapFlowPane, teacherIndex, availableSubjects);

        VBox teacherSubjectInputs = new VBox(5, subTeachCountCombo, tSubMapFlowPane);
        teacherSubjectInputs.setId("teacherInputsVBox_" + teacherIndex);
        teacherMappingGrid.addRow(teacherIndex, teachNameField, teacherSubjectInputs);
    }


    private Callback<ListView<SubjectContext>, ListCell<SubjectContext>> getCellFactoryForTeacherSubjects() {
        return lv -> new ListCell<>() {
            @Override
            protected void updateItem(SubjectContext item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setDisable(false);
                } else {
                    setText(item.toString());
                    boolean isSelectedInThisComboBox = item.equals(getListView().getSelectionModel().getSelectedItem());
                    boolean isSelectedElsewhere = globallySelectedSubjectContexts.contains(item) && !isSelectedInThisComboBox;
                    setDisable(isSelectedElsewhere);
                    setStyle(isSelectedElsewhere ? "-fx-strikethrough: true; -fx-text-fill: grey;" : "");
                }
            }
        };
    }

    private ListCell<SubjectContext> createButtonCellForTeacherSubjects(String promptText) {
        return new ListCell<>() {
            @Override
            protected void updateItem(SubjectContext item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? promptText : item.toString());
            }
        };
    }

    private void refreshAllTeacherSubjectComboBoxes() {
        if (teacherMappingGrid == null) return;
        Callback<ListView<SubjectContext>, ListCell<SubjectContext>> factory = getCellFactoryForTeacherSubjects();
        teacherMappingGrid.getChildren().stream()
                .filter(VBox.class::isInstance)
                .flatMap(vbox -> ((VBox) vbox).getChildren().stream())
                .filter(FlowPane.class::isInstance)
                .flatMap(fpane -> ((FlowPane) fpane).getChildren().stream())
                .map(ComboBox.class::cast)
                .forEach(combo -> {
                    combo.setCellFactory(factory);
                    combo.setButtonCell(createButtonCellForTeacherSubjects(combo.getPromptText()));
                });
    }

    private void setupDynamicSubjectCombosForTeacher(ComboBox<Integer> subTeachCountCombo, FlowPane tSubMapFlowPane, int teacherIndex, ObservableList<SubjectContext> availableSubjectContextsForAllTeachers) {
        subTeachCountCombo.setOnAction(p -> {
            int desiredCount = Optional.ofNullable(subTeachCountCombo.getValue()).orElse(0);
            int currentCount = tSubMapFlowPane.getChildren().size();

            if (desiredCount > currentCount) {
                for (int i = currentCount; i < desiredCount; i++) {
                    ComboBox<SubjectContext> selSubCombo = new ComboBox<>();
                    selSubCombo.setPromptText("Select Subject " + (i + 1));
                    selSubCombo.setItems(availableSubjectContextsForAllTeachers);
                    selSubCombo.setId("selSub_" + teacherIndex + "_" + i);
                    selSubCombo.setCellFactory(getCellFactoryForTeacherSubjects());
                    selSubCombo.setButtonCell(createButtonCellForTeacherSubjects(selSubCombo.getPromptText()));
                    selSubCombo.valueProperty().addListener((obs, oldValue, newValue) -> {
                        if (oldValue != null) globallySelectedSubjectContexts.remove(oldValue);
                        if (newValue != null) globallySelectedSubjectContexts.add(newValue);
                        refreshAllTeacherSubjectComboBoxes();
                        checkIfReadyToGenerate();
                    });
                    tSubMapFlowPane.getChildren().add(selSubCombo);
                }
            } else if (desiredCount < currentCount) {
                tSubMapFlowPane.getChildren().subList(desiredCount, currentCount).forEach(node -> {
                    SubjectContext removedVal = ((ComboBox<SubjectContext>) node).getValue();
                    if (removedVal != null) globallySelectedSubjectContexts.remove(removedVal);
                });
                tSubMapFlowPane.getChildren().remove(desiredCount, currentCount);
            }
            rebuildGlobalSubjectSet(teacherMappingGrid);
            refreshAllTeacherSubjectComboBoxes();
            checkIfReadyToGenerate();
        });
    }

    private void rebuildGlobalSubjectSet(GridPane currentTeacherMappingGrid) {
        globallySelectedSubjectContexts.clear();
        if (currentTeacherMappingGrid == null) return;
        currentTeacherMappingGrid.getChildren().stream()
                .filter(node -> node instanceof VBox && GridPane.getColumnIndex(node) == 1)
                .flatMap(vbox -> ((VBox) vbox).getChildren().stream())
                .filter(fpane -> fpane instanceof FlowPane)
                .flatMap(fpane -> ((FlowPane) fpane).getChildren().stream())
                .map(combo -> ((ComboBox<SubjectContext>) combo).getValue())
                .filter(Objects::nonNull)
                .forEach(globallySelectedSubjectContexts::add);
    }

    private void checkIfReadyToGenerate() {
        Integer teacherCount = teacherCountComboBox.getValue();
        if (teacherCount == null || teacherCount <= 0) {
            hideGenerateButton();
            return;
        }
        for (int i = 0; i < teacherCount; i++) {
            TextField nameField = (TextField) findNodeInGrid(teacherMappingGrid, i, 0);
            VBox inputsVBox = (VBox) findNodeInGrid(teacherMappingGrid, i, 1);
            if (nameField == null || nameField.getText().trim().isEmpty() || inputsVBox == null) {
                hideGenerateButton();
                return;
            }
            ComboBox<Integer> countCombo = (ComboBox<Integer>) inputsVBox.getChildren().get(0);
            FlowPane subjectPane = (FlowPane) inputsVBox.getChildren().get(1);
            if (countCombo.getValue() == null || countCombo.getValue() == 0) {
                if(countCombo.getValue() == null){ //If no subjects selected then allow generation
                    hideGenerateButton();
                    return;
                }
            } else {
                if (countCombo.getValue() != subjectPane.getChildren().size() ||
                        subjectPane.getChildren().stream().anyMatch(c -> ((ComboBox<?>) c).getValue() == null)) {
                    hideGenerateButton();
                    return;
                }
            }
        }
        showGenerateButton();
    }


    private void handleGenerateClick() {
        System.out.println("DEBUG: Generate button clicked.");
        lastGeneratedTimetable = null;
        disableExportButtons();

        if (!validateAndStoreTimeConfig()) {
            System.err.println("DEBUG: Time validation failed.");
            showErrorAlert("Prerequisite Error", "Please fix Time Configuration.");
            return;
        }

        teacherList.clear();
        boolean teachersValid = true;
        String teacherError = "";
        Integer expectedTeacherCount = teacherCountComboBox.getValue();
        if (expectedTeacherCount == null || expectedTeacherCount <= 0) {
            showErrorAlert("Teacher Error", "Please select number of teachers and assign subjects.");
            return;
        }

        Map<Teacher, List<SubjectContext>> teacherAssignmentsMap = new HashMap<>();
        for (int v = 0; v < expectedTeacherCount; v++) {
            TextField teachNameField = (TextField) findNodeInGrid(teacherMappingGrid, v, 0);
            VBox teacherInputsVBox = (VBox) findNodeInGrid(teacherMappingGrid, v, 1);
            if (teachNameField == null || teacherInputsVBox == null) {
                teachersValid = false; teacherError = "UI Error finding controls for Teacher " + (v+1); break;
            }
            ComboBox<Integer> subTeachCountBox = (ComboBox<Integer>) teacherInputsVBox.getChildren().get(0);
            FlowPane tSubMapFlowPane = (FlowPane) teacherInputsVBox.getChildren().get(1);

            String teachName = teachNameField.getText().trim();
            if (teachName.isEmpty()) {
                teachersValid = false;
                teacherError = "Teacher " + (v + 1) + " name empty.";
                highlightError(teachNameField);
                break;
            } else {
                resetHighlight(teachNameField);
            }
            Teacher currentTeacher = new Teacher(teachName);
            List<SubjectContext> currentAssignments = tSubMapFlowPane.getChildren().stream()
                    .map(node -> ((ComboBox<SubjectContext>) node).getValue())
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList());

            // Validation for subject selection
            if (subTeachCountBox.getValue() != null && subTeachCountBox.getValue() > 0 && currentAssignments.size() != subTeachCountBox.getValue()) {
                teachersValid = false;
                teacherError = "Please select all subjects for teacher '" + teachName + "'.";
                highlightError(tSubMapFlowPane); // Highlight the container of combos
                break;
            }

            currentAssignments.forEach(sc -> currentTeacher.getSubjectsTaught().add(sc.getSubject()));
            teacherList.add(currentTeacher);
            teacherAssignmentsMap.put(currentTeacher, currentAssignments);
        }

        if (teachersValid) {
            System.out.println("--- Input Data Ready for Generation ---");
            lastGeneratedTimetable = generateTimetableAlgorithmV2(semesterList, teacherList, teacherAssignmentsMap, workStartTime, workEndTime, breakStartTime, breakEndTime, slotDurationMinutes, WORKING_DAYS, maxTeacherSlotsPerDay, maxConsecutiveSlotsPerSubject);
            if (lastGeneratedTimetable != null) {
                displayTimetableGrid(lastGeneratedTimetable);
                mainTabPane.getSelectionModel().select(generatedTimetableTab);
                if (lastGeneratedTimetable.isEmpty()) {
                    showInfoAlert("Generation Note", "Timetable generated, but no classes could be scheduled with the given constraints.");
                    disableExportButtons();
                } else {
                    enableExportButtons();
                }
            } else {
                showErrorAlert("Generation Failed", "The timetable generation algorithm failed unexpectedly.");
                generatedScrollPane.setContent(new Label("Timetable generation failed."));
                disableExportButtons();
            }
        } else {
            showErrorAlert("Teacher Validation Error", teacherError);
            disableExportButtons();
        }
    }


    private List<TimetableEntry> generateTimetableAlgorithmV2(List<Semester> semesters, List<Teacher> teachers, Map<Teacher, List<SubjectContext>> teacherAssignments, LocalTime workStart, LocalTime workEnd, LocalTime breakStart, LocalTime breakEnd, int slotMinutes, List<String> days, int maxSlotsPerTeacherPerDay, int maxConsecutiveSlotsPerSubject) {
        System.out.println("DEBUG: Starting Algorithm V2 with Max Teacher Slots/Day: " + maxSlotsPerTeacherPerDay + ", Max Consecutive Slots/Subject: " + maxConsecutiveSlotsPerSubject);

        List<TimetableEntry> timetable = new ArrayList<>();
        if (semesters.isEmpty() || teachers.isEmpty()) {
            System.err.println("AlgoV2: Missing semesters or teachers.");
            return timetable;
        }
        List<LocalTime> slotStartTimes = calculateTimeSlots(workStart, workEnd, breakStart, breakEnd, slotMinutes);
        if (slotStartTimes.isEmpty()) {
            System.err.println("AlgoV2: No valid time slots calculated.");
            return timetable;
        }

        Map<Subject, List<Teacher>> subjectToTeacherMap = new HashMap<>();
        for (Teacher teacher : teachers) {
            for (Subject subject : teacher.getSubjectsTaught()) {
                subjectToTeacherMap.computeIfAbsent(subject, k -> new ArrayList<>()).add(teacher);
            }
        }

        Map<SubjectContext, Integer> requiredSlotsMap = new HashMap<>();
        for (Semester sem : semesters) {
            for (Subject sub : sem.getSubjects()) {
                SubjectContext sc = new SubjectContext(sub, sem);
                int requiredSlots = (int) Math.ceil((double) sub.getWeeklyHours() * 60.0 / slotMinutes);
                requiredSlotsMap.put(sc, requiredSlots);
            }
        }

        Map<String, Map<LocalTime, Set<Teacher>>> teacherBusyMap = new HashMap<>();
        Map<SubjectContext, Integer> scheduledSlotsMap = new HashMap<>();
        Map<String, Map<Semester, LinkedList<Subject>>> recentSubjectsMap = new HashMap<>();
        Map<String, Map<Teacher, Integer>> teacherDailyLoadMap = new HashMap<>();

        List<Semester> sortedSemesters = semesters.stream().sorted(Comparator.comparing(Semester::getName)).collect(Collectors.toList());

        for (String day : days) {
            teacherBusyMap.put(day, new HashMap<>());
            recentSubjectsMap.put(day, new HashMap<>());
            teacherDailyLoadMap.put(day, new HashMap<>());
            System.out.println("DEBUG: Processing Day: " + day);

            boolean postBreakResetDoneForDay = false;

            for (LocalTime slotStart : slotStartTimes) {
                teacherBusyMap.get(day).computeIfAbsent(slotStart, k -> new HashSet<>());

                boolean clearRecentHistoryForThisSlot = false;
                if (!postBreakResetDoneForDay && breakEnd != null && !slotStart.isBefore(breakEnd)) {
                    clearRecentHistoryForThisSlot = true;
                    postBreakResetDoneForDay = true;
                    System.out.println("DEBUG: Day " + day + ", Slot " + slotStart.format(TIME_FORMATTER) + " is post-break. Resetting consecutive history.");
                }

                for (Semester semester : sortedSemesters) {
                    LinkedList<Subject> recentSubjects = recentSubjectsMap.get(day).computeIfAbsent(semester, k -> new LinkedList<>());
                    if (clearRecentHistoryForThisSlot) {
                        recentSubjects.clear();
                    }

                    Set<Teacher> busyTeachersThisSlot = teacherBusyMap.get(day).get(slotStart);
                    Map<Teacher, Integer> dailyLoad = teacherDailyLoadMap.get(day);
                    boolean slotFilledForThisSem = false;

                    List<Subject> subjectsToTry = new ArrayList<>(semester.getSubjects());
                    Collections.shuffle(subjectsToTry);

                    for (Subject subject : subjectsToTry) {
                        SubjectContext currentSC = new SubjectContext(subject, semester);
                        if (scheduledSlotsMap.getOrDefault(currentSC, 0) >= requiredSlotsMap.getOrDefault(currentSC, 0)) continue;

                        if (maxConsecutiveSlotsPerSubject > 0 && recentSubjects.size() == maxConsecutiveSlotsPerSubject && recentSubjects.stream().allMatch(s -> subject.equals(s))) {
                            continue;
                        }

                        Teacher availableTeacher = subjectToTeacherMap.getOrDefault(subject, Collections.emptyList()).stream()
                                .filter(t -> !busyTeachersThisSlot.contains(t) && dailyLoad.getOrDefault(t, 0) < maxSlotsPerTeacherPerDay)
                                .findAny().orElse(null);

                        if (availableTeacher != null) {
                            LocalTime slotEnd = slotStart.plusMinutes(slotMinutes);
                            timetable.add(new TimetableEntry(day, slotStart, slotEnd, semester, subject, availableTeacher));
                            busyTeachersThisSlot.add(availableTeacher);
                            scheduledSlotsMap.merge(currentSC, 1, Integer::sum);
                            dailyLoad.merge(availableTeacher, 1, Integer::sum);

                            recentSubjects.addLast(subject);
                            if (maxConsecutiveSlotsPerSubject > 0 && recentSubjects.size() > maxConsecutiveSlotsPerSubject) recentSubjects.removeFirst();
                            slotFilledForThisSem = true;
                            break;
                        }
                    }
                    if (!slotFilledForThisSem) {
                        recentSubjects.addLast(null);
                        if (maxConsecutiveSlotsPerSubject > 0 && recentSubjects.size() > maxConsecutiveSlotsPerSubject) recentSubjects.removeFirst();
                    }
                }
            }
        }
        printUnmetNeeds(requiredSlotsMap, scheduledSlotsMap);
        printTeacherLoad(teacherDailyLoadMap);
        return timetable;
    }

    private void printUnmetNeeds(Map<SubjectContext, Integer> required, Map<SubjectContext, Integer> scheduled) {
        System.out.println("--- Scheduling Needs Report ---");
        boolean allMet = true;
        for (Map.Entry<SubjectContext, Integer> entry : required.entrySet()) {
            int reqSlots = entry.getValue();
            int schedSlots = scheduled.getOrDefault(entry.getKey(), 0);
            if (schedSlots < reqSlots) {
                allMet = false;
                System.out.println(" - Subject: " + entry.getKey().toString() + " | Required Slots: " + reqSlots + " | Scheduled Slots: " + schedSlots);
            }
        }
        if (allMet) System.out.println("All subject hour requirements appear to be met.");
        System.out.println("-----------------------------");
    }

    private void printTeacherLoad(Map<String, Map<Teacher, Integer>> dailyLoadMap) {
        System.out.println("--- Teacher Daily Load Report ---");
        for (String day : WORKING_DAYS) {
            System.out.println(" " + day + ":");
            Map<Teacher, Integer> load = dailyLoadMap.getOrDefault(day, Collections.emptyMap());
            if (load.isEmpty()) {
                System.out.println("    No teachers scheduled.");
                continue;
            }
            load.forEach((teacher, count) -> System.out.println("    - " + teacher.getName() + ": " + count + " slots"));
        }
        System.out.println("-------------------------------");
    }


    private List<LocalTime> calculateTimeSlots(LocalTime workStart, LocalTime workEnd, LocalTime breakStart, LocalTime breakEnd, int slotMinutes) {
        List<LocalTime> slots = new ArrayList<>();
        LocalTime current = workStart;
        while (current.isBefore(workEnd)) {
            LocalTime slotEnd = current.plusMinutes(slotMinutes);
            if (slotEnd.isAfter(workEnd)) break;
            boolean isDuringBreak = !current.isBefore(breakStart) && !slotEnd.isAfter(breakEnd);
            if (!isDuringBreak) {
                slots.add(current);
            }
            current = slotEnd;
        }
        return slots;
    }


    private void displayTimetableGrid(List<TimetableEntry> timetable) {
        timetableDisplayGrid.getChildren().clear();
        timetableDisplayGrid.getColumnConstraints().clear();
        timetableDisplayGrid.getRowConstraints().clear();
        timetableDisplayGrid.setGridLinesVisible(true);

        if (timetable.isEmpty()) {
            generatedScrollPane.setContent(new Label("No timetable entries generated."));
            disableExportButtons();
            return;
        }
        generatedScrollPane.setContent(timetableDisplayGrid);
        enableExportButtons();

        Map<String, Map<Semester, Map<LocalTime, TimetableEntry>>> groupedEntries = timetable.stream().collect(Collectors.groupingBy(TimetableEntry::getDay, Collectors.groupingBy(TimetableEntry::getSemester, Collectors.toMap(TimetableEntry::getStartTime, e -> e, (e1, e2) -> e1))));
        List<LocalTime> classSlots = calculateTimeSlots(workStartTime, workEndTime, breakStartTime, breakEndTime, slotDurationMinutes);
        if (classSlots.isEmpty()) {
            generatedScrollPane.setContent(new Label("No valid class time slots defined."));
            return;
        }

        int breakColumnIndex = -1;
        for (int i = 0; i < classSlots.size(); i++) {
            if (!classSlots.get(i).isBefore(breakStartTime)) {
                breakColumnIndex = i + 1;
                break;
            }
        }
        if (breakColumnIndex == -1 && breakStartTime != null && !breakStartTime.isAfter(workEndTime)) {
            breakColumnIndex = classSlots.size() + 1;
        }

        timetableDisplayGrid.add(createHeaderLabel("Day / Sem", 12), 0, 0);
        int headerCol = 1;
        for (int i = 0; i < classSlots.size(); i++) {
            if (headerCol == breakColumnIndex) {
                timetableDisplayGrid.add(createHeaderLabel("BREAK", 11), headerCol++, 0);
            }
            LocalTime start = classSlots.get(i);
            String headerText = start.format(TIME_FORMATTER) + " - " + start.plusMinutes(slotDurationMinutes).format(TIME_FORMATTER);
            timetableDisplayGrid.add(createHeaderLabel(headerText, 11), headerCol++, 0);
        }
        if (breakColumnIndex == classSlots.size() + 1) {
            timetableDisplayGrid.add(createHeaderLabel("BREAK", 11), headerCol++, 0);
        }
        int totalColumns = headerCol;

        int gridRow = 1;
        List<Semester> sortedSemesters = semesterList.stream().sorted(Comparator.comparing(Semester::getName)).collect(Collectors.toList());

        for (String day : WORKING_DAYS) {
            Label dayLabel = createDayHeaderLabel(day);
            timetableDisplayGrid.add(dayLabel, 0, gridRow++, totalColumns, 1);
            for (Semester semester : sortedSemesters) {
                timetableDisplayGrid.add(createSemesterHeaderLabel(semester.getName()), 0, gridRow);
                Map<LocalTime, TimetableEntry> daySemEntries = groupedEntries.getOrDefault(day, Collections.emptyMap()).getOrDefault(semester, Collections.emptyMap());
                int dataCol = 1;
                for (LocalTime slotStart : classSlots) {
                    if (dataCol == breakColumnIndex) {
                        timetableDisplayGrid.add(createBreakCell(), dataCol++, gridRow);
                    }
                    TimetableEntry entry = daySemEntries.get(slotStart);
                    Node cellContent = (entry != null) ? createEntryCell(entry) : createEmptyCell();
                    timetableDisplayGrid.add(cellContent, dataCol++, gridRow);
                }
                if (dataCol == breakColumnIndex) {
                    timetableDisplayGrid.add(createBreakCell(), dataCol++, gridRow);
                }
                gridRow++;
            }
        }
        setupGridConstraints(totalColumns);
    }

    private VBox createEntryCell(TimetableEntry entry) {
        VBox cellBox = new VBox(1);
        cellBox.setAlignment(Pos.CENTER);
        Label subjectLabel = new Label(entry.getSubject().getName());
        subjectLabel.setFont(Font.font("System", FontWeight.BOLD, 10));
        Label teacherLabel = new Label("(" + entry.getTeacher().getName() + ")");
        teacherLabel.setFont(Font.font("System", Font.getDefault().getSize() * 0.85));
        cellBox.getChildren().addAll(subjectLabel, teacherLabel);
        cellBox.setStyle("-fx-background-color: white; -fx-border-color: #cccccc; -fx-border-width: 0.5px; -fx-padding: 2px;");
        return cellBox;
    }
    private Pane createEmptyCell() {
        Pane emptyPane = new Pane();
        emptyPane.setMinHeight(35);
        emptyPane.setStyle("-fx-background-color: #f9f9f9; -fx-border-color: #cccccc; -fx-border-width: 0.5px;");
        return emptyPane;
    }
    private void setupGridConstraints(int totalColumns) {
        timetableDisplayGrid.getColumnConstraints().clear();
        ColumnConstraints colDaySem = new ColumnConstraints();
        colDaySem.setPrefWidth(120);
        colDaySem.setMinWidth(100);
        colDaySem.setHgrow(Priority.NEVER);
        timetableDisplayGrid.getColumnConstraints().add(colDaySem);
        for (int j = 1; j < totalColumns; j++) {
            ColumnConstraints col = new ColumnConstraints();
            col.setHgrow(Priority.ALWAYS);
            col.setMinWidth(60);
            timetableDisplayGrid.getColumnConstraints().add(col);
        }
    }


    // --- Helper Methods for Header Styling ---
    private Label createHeaderLabel(String text, double fontSize) { Label label = new Label(text); label.setFont(Font.font("System", FontWeight.BOLD, fontSize)); label.setAlignment(Pos.CENTER); label.setMaxWidth(Double.MAX_VALUE); label.setMinWidth(50); label.setPrefHeight(40); label.setPadding(new Insets(5)); label.setStyle("-fx-background-color: #cce0ff; -fx-border-color: #aaaaaa; -fx-border-width: 0.5px;"); label.setTextAlignment(javafx.scene.text.TextAlignment.CENTER); return label; }
    private Label createDayHeaderLabel(String text) { Label label = new Label(text); label.setFont(Font.font("System", FontWeight.BOLD, 14)); label.setAlignment(Pos.CENTER_LEFT); label.setMaxWidth(Double.MAX_VALUE); label.setPadding(new Insets(6, 10, 6, 10)); label.setStyle("-fx-background-color: #aaccff; -fx-border-color: #8888aa; -fx-border-width: 1px 0 1px 0;"); return label; }
    private Label createSemesterHeaderLabel(String text) { Label label = new Label(text); label.setFont(Font.font("System", FontWeight.NORMAL, 11)); label.setAlignment(Pos.CENTER_RIGHT); label.setMaxWidth(Double.MAX_VALUE); label.setPadding(new Insets(5, 8, 5, 5)); label.setStyle("-fx-background-color: #e6f0ff; -fx-border-color: #cccccc; -fx-border-width: 0 0.5px 0.5px 0.5px;"); label.setWrapText(true); return label; }
    private Label createBreakCell() {
        Label label = new Label("BREAK");
        label.setFont(Font.font("System", FontWeight.BOLD, 10));
        label.setAlignment(Pos.CENTER);
        label.setMaxWidth(Double.MAX_VALUE);
        label.setMinHeight(35);
        label.setStyle("-fx-background-color: #e0e0e0; -fx-border-color: #cccccc; -fx-border-width: 0.5px; -fx-text-fill: #555555;");
        GridPane.setVgrow(label, Priority.SOMETIMES);
        GridPane.setHgrow(label, Priority.ALWAYS);
        return label;
    }


    // --- Visibility Control Methods ---
    private void hideTimeConfigSection() { if(timeConfigSection!=null){timeConfigSection.setVisible(false);timeConfigSection.setManaged(false);}}
    private void showTimeConfigSection() { if(timeConfigSection!=null){timeConfigSection.setVisible(true); timeConfigSection.setManaged(true); }}
    private void hideSemesterSection() { if(semesterSectionContainer!=null){semesterSectionContainer.setVisible(false);semesterSectionContainer.setManaged(false);}}
    private void showSemesterSection() { if(semesterSectionContainer!=null){semesterSectionContainer.setVisible(true); semesterSectionContainer.setManaged(true); }}
    private void hideTeacherSection() { if(teacherSectionContainer != null) {teacherSectionContainer.setVisible(false); teacherSectionContainer.setManaged(false);} hideGenerateButton(); }
    private void showTeacherSection() { if(teacherSectionContainer != null) {teacherSectionContainer.setVisible(true); teacherSectionContainer.setManaged(true);} }
    private void hideGenerateButton() { if(generateButton != null) { generateButton.setVisible(false); generateButton.setManaged(false);}}
    private void showGenerateButton() { if(generateButton != null) { generateButton.setVisible(true); generateButton.setManaged(true);}}
    private void disableExportButtons() {
        if(exportExcelButton != null) exportExcelButton.setDisable(true);
        if(exportPdfButton != null) exportPdfButton.setDisable(true);
    }
    private void enableExportButtons() {
        if(exportExcelButton != null) exportExcelButton.setDisable(false);
        if(exportPdfButton != null) exportPdfButton.setDisable(false);
    }


    // --- Utility Methods ---
    private Node findNodeInGrid(GridPane gridPane, int row, int col) {
        return gridPane.getChildren().stream()
                .filter(node -> Optional.ofNullable(GridPane.getRowIndex(node)).map(r -> r == row).orElse(false) &&
                        Optional.ofNullable(GridPane.getColumnIndex(node)).map(c -> c == col).orElse(false))
                .findFirst().orElse(null);
    }
    private void showErrorAlert(String title, String message) { Alert alert = new Alert(Alert.AlertType.WARNING); alert.setTitle(title); alert.setHeaderText(null); alert.setContentText(message); Stage stage = (Stage) alert.getDialogPane().getScene().getWindow(); loadStageIcon(stage); alert.showAndWait(); }
    private void showInfoAlert(String title, String content) { Alert alert = new Alert(Alert.AlertType.INFORMATION); alert.setTitle(title); alert.setHeaderText(null); alert.setContentText(content); alert.showAndWait(); }
    private void highlightError(Node node) { if(node!=null) node.setStyle("-fx-border-color: red; -fx-border-width: 1.5px; -fx-border-radius: 3px;"); }
    private void resetHighlight(Node node) { if(node!=null) node.setStyle(""); }
    private void loadStageIcon(Stage stage) { try (InputStream iconStream = getClass().getResourceAsStream("/icon.png")) { if (iconStream != null) stage.getIcons().add(new Image(iconStream)); else System.err.println("Warning: icon.png not found."); } catch (Exception e) { System.err.println("Error loading icon: " + e.getMessage());}}
    private void setupTabPaneSelectionStyle(TabPane tabPane) { tabPane.getSelectionModel().selectedItemProperty().addListener((obs, oldTab, newTab) -> { if (oldTab != null) oldTab.setStyle(""); if (newTab != null) newTab.setStyle("-fx-border-color: #3c7fb1; -fx-border-width: 2 2 0 2; -fx-border-insets: 0 0 -2 0;"); }); Platform.runLater(() -> Optional.ofNullable(tabPane.getSelectionModel().getSelectedItem()).ifPresent(tab -> tab.setStyle("-fx-border-color: #3c7fb1; -fx-border-width: 2 2 0 2; -fx-border-insets: 0 0 -2 0;"))); }


    // --- Export Methods ---
    private void handleExportExcel() {
        if (lastGeneratedTimetable == null || lastGeneratedTimetable.isEmpty()) {
            showInfoAlert("Export Info", "No timetable data available to export.");
            return;
        }
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Save Timetable as Excel");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Excel Workbook (*.xlsx)", "*.xlsx"));
        fileChooser.setInitialFileName("timetable.xlsx");
        File file = fileChooser.showSaveDialog(mainTabPane.getScene().getWindow());
        if (file == null) return;

        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("Timetable");
            List<LocalTime> classSlots = calculateTimeSlots(workStartTime, workEndTime, breakStartTime, this.breakEndTime, slotDurationMinutes);
            if (classSlots.isEmpty()) {
                showErrorAlert("Export Error", "No valid class time slots found for export.");
                return;
            }

            int breakColumnIndex = -1;
            if (breakStartTime != null) {
                for (int i = 0; i < classSlots.size(); i++) {
                    if (!classSlots.get(i).isBefore(breakStartTime)) {
                        breakColumnIndex = i + 1;
                        break;
                    }
                }
                if (breakColumnIndex == -1 && workEndTime != null && !breakStartTime.isAfter(workEndTime)) {
                    breakColumnIndex = classSlots.size() + 1;
                }
            }

            CellStyle headerStyle = createExcelHeaderStyle(workbook);
            CellStyle dayHeaderStyle = createExcelDayHeaderStyle(workbook);
            CellStyle semesterHeaderStyle = createExcelSemesterHeaderStyle(workbook);
            CellStyle breakStyle = createExcelBreakStyle(workbook);
            CellStyle entryStyle = createExcelEntryStyle(workbook);
            CellStyle emptyStyle = createExcelEmptyStyle(workbook);
            CellStyle titleStyle = workbook.createCellStyle();
            org.apache.poi.ss.usermodel.Font titleFont = workbook.createFont();
            titleFont.setBold(true);
            titleFont.setFontHeightInPoints((short) 14);
            titleStyle.setFont(titleFont);
            titleStyle.setAlignment(HorizontalAlignment.CENTER);
            titleStyle.setVerticalAlignment(CENTER);

            CellStyle subTitleStyle = workbook.createCellStyle();
            org.apache.poi.ss.usermodel.Font subTitleFont = workbook.createFont();
            subTitleFont.setFontHeightInPoints((short) 12);
            subTitleStyle.setFont(subTitleFont);
            subTitleStyle.setAlignment(HorizontalAlignment.CENTER);
            subTitleStyle.setVerticalAlignment(CENTER);

            int currentRowNum = 0;
            int numTimetableGridColumns = 1 + classSlots.size() + (breakColumnIndex != -1 ? 1 : 0);

            if (!schoolName.isEmpty()) {
                Row schoolNameRow = sheet.createRow(currentRowNum++);
                Cell schoolCell = schoolNameRow.createCell(0);
                schoolCell.setCellValue(schoolName);
                schoolCell.setCellStyle(titleStyle);
                if (numTimetableGridColumns > 1) sheet.addMergedRegion(new CellRangeAddress(currentRowNum - 1, currentRowNum - 1, 0, numTimetableGridColumns - 1));
            }
            if (!departmentName.isEmpty()) {
                Row deptNameRow = sheet.createRow(currentRowNum++);
                Cell deptCell = deptNameRow.createCell(0);
                deptCell.setCellValue(departmentName);
                deptCell.setCellStyle(subTitleStyle);
                if (numTimetableGridColumns > 1) sheet.addMergedRegion(new CellRangeAddress(currentRowNum - 1, currentRowNum - 1, 0, numTimetableGridColumns - 1));
            }
            if (!schoolName.isEmpty() || !departmentName.isEmpty()) currentRowNum++;

            Row timetableHeaderRow = sheet.createRow(currentRowNum++);
            Cell topLeftCell = timetableHeaderRow.createCell(0);
            topLeftCell.setCellValue("Day / Sem");
            topLeftCell.setCellStyle(headerStyle);

            int currentExcelCol = 1;
            for (int i = 0; i < classSlots.size(); i++) {
                if (breakColumnIndex != -1 && currentExcelCol == breakColumnIndex) {
                    timetableHeaderRow.createCell(currentExcelCol++).setCellValue("BREAK");
                }
                timetableHeaderRow.createCell(currentExcelCol++).setCellValue(classSlots.get(i).format(TIME_FORMATTER) + "-" + classSlots.get(i).plusMinutes(slotDurationMinutes).format(TIME_FORMATTER));
            }
            if (breakColumnIndex != -1 && currentExcelCol == numTimetableGridColumns) {
                timetableHeaderRow.createCell(currentExcelCol-1).setCellValue("BREAK");
            }
            for(int i=0; i < numTimetableGridColumns; i++) timetableHeaderRow.getCell(i).setCellStyle(headerStyle);


            Map<String, Map<Semester, Map<LocalTime, TimetableEntry>>> groupedEntries = lastGeneratedTimetable.stream().collect(Collectors.groupingBy(TimetableEntry::getDay, Collectors.groupingBy(TimetableEntry::getSemester, Collectors.toMap(TimetableEntry::getStartTime, e -> e, (e1, e2) -> e1))));
            List<Semester> sortedSemesters = semesterList.stream().sorted(Comparator.comparing(Semester::getName)).collect(Collectors.toList());

            for (String day : WORKING_DAYS) {
                Row dayRow = sheet.createRow(currentRowNum++);
                Cell dayCell = dayRow.createCell(0);
                dayCell.setCellValue(day);
                dayCell.setCellStyle(dayHeaderStyle);
                if (numTimetableGridColumns > 1) sheet.addMergedRegion(new CellRangeAddress(currentRowNum - 1, currentRowNum - 1, 0, numTimetableGridColumns - 1));

                for (Semester semester : sortedSemesters) {
                    Row semRow = sheet.createRow(currentRowNum++);
                    semRow.createCell(0).setCellValue(semester.getName());
                    semRow.getCell(0).setCellStyle(semesterHeaderStyle);

                    Map<LocalTime, TimetableEntry> semesterDayEntries = groupedEntries.getOrDefault(day, Collections.emptyMap()).getOrDefault(semester, Collections.emptyMap());

                    currentExcelCol = 1;
                    for (LocalTime slotStart : classSlots) {
                        if (breakColumnIndex != -1 && currentExcelCol == breakColumnIndex) {
                            semRow.createCell(currentExcelCol++).setCellValue("BREAK");
                        }
                        Cell cell = semRow.createCell(currentExcelCol++);
                        TimetableEntry entry = semesterDayEntries.get(slotStart);
                        if (entry != null) {
                            cell.setCellValue(entry.getSubject().getName() + "\n(" + entry.getTeacher().getName() + ")");
                            cell.setCellStyle(entryStyle);
                        } else {
                            cell.setCellStyle(emptyStyle);
                        }
                    }
                    if (breakColumnIndex != -1 && currentExcelCol == numTimetableGridColumns) {
                        semRow.createCell(currentExcelCol-1).setCellValue("BREAK");
                    }
                    // Apply styles
                    for(int i=1; i < numTimetableGridColumns; i++){
                        Cell c = semRow.getCell(i);
                        if(c.getStringCellValue().equals("BREAK")) c.setCellStyle(breakStyle);
                    }
                }
            }

            for (int i = 0; i < numTimetableGridColumns; i++) sheet.autoSizeColumn(i);
            sheet.setColumnWidth(0, 30 * 256);

            try (FileOutputStream fileOut = new FileOutputStream(file)) {
                workbook.write(fileOut);
                showInfoAlert("Export Successful", "Timetable exported to:\n" + file.getName());
            }
        } catch (IOException e) {
            showErrorAlert("Export Error", "Could not export timetable to Excel.\nError: " + e.getMessage());
        }
    }
    private void handleExportPdf() {
        if (lastGeneratedTimetable == null || lastGeneratedTimetable.isEmpty()) {
            showInfoAlert("Export Info", "No timetable data available to export.");
            return;
        }
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Save Timetable as PDF");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("PDF Document (*.pdf)", "*.pdf"));
        fileChooser.setInitialFileName("timetable.pdf");
        File file = fileChooser.showSaveDialog(mainTabPane.getScene().getWindow());
        if (file == null) return;

        try (PdfWriter writer = new PdfWriter(file); PdfDocument pdf = new PdfDocument(writer); Document document = new Document(pdf, PageSize.A4.rotate())) {
            document.setMargins(20, 20, 20, 20);
            if (!schoolName.isEmpty()) document.add(new Paragraph(schoolName).setFont(PdfFontFactory.createFont(StandardFonts.HELVETICA_BOLD)).setFontSize(16).setTextAlignment(TextAlignment.CENTER).setMarginBottom(5));
            if (!departmentName.isEmpty()) document.add(new Paragraph(departmentName).setFont(PdfFontFactory.createFont(StandardFonts.HELVETICA)).setFontSize(14).setTextAlignment(TextAlignment.CENTER).setMarginBottom(10));
            document.add(createPdfTimetableTable(lastGeneratedTimetable));
            showInfoAlert("Export Successful", "Timetable exported to:\n" + file.getName());
        } catch (IOException e) {
            showErrorAlert("Export Error", "Could not export timetable to PDF.\nIO Error: " + e.getMessage());
        }
    }
    private CellStyle createExcelHeaderStyle(Workbook wb) { CellStyle style = wb.createCellStyle(); org.apache.poi.ss.usermodel.Font font = wb.createFont(); font.setBold(true); font.setFontHeightInPoints((short) 10); style.setFont(font); style.setAlignment(HorizontalAlignment.CENTER); style.setVerticalAlignment(CENTER); style.setFillForegroundColor(IndexedColors.LIGHT_CORNFLOWER_BLUE.getIndex()); style.setFillPattern(FillPatternType.SOLID_FOREGROUND); style.setBorderTop(BorderStyle.THIN); style.setBorderBottom(BorderStyle.THIN); style.setBorderLeft(BorderStyle.THIN); style.setBorderRight(BorderStyle.THIN); style.setWrapText(true); return style; }
    private CellStyle createExcelDayHeaderStyle(Workbook wb) { CellStyle style = wb.createCellStyle(); org.apache.poi.ss.usermodel.Font font = wb.createFont(); font.setBold(true); font.setFontHeightInPoints((short) 12); style.setFont(font); style.setAlignment(HorizontalAlignment.LEFT); style.setVerticalAlignment(CENTER); style.setFillForegroundColor(IndexedColors.PALE_BLUE.getIndex()); style.setFillPattern(FillPatternType.SOLID_FOREGROUND); style.setBorderTop(BorderStyle.MEDIUM); style.setBorderBottom(BorderStyle.MEDIUM); return style; }
    private CellStyle createExcelSemesterHeaderStyle(Workbook wb) { CellStyle style = wb.createCellStyle(); org.apache.poi.ss.usermodel.Font font = wb.createFont(); font.setFontHeightInPoints((short) 9); style.setFont(font); style.setAlignment(HorizontalAlignment.RIGHT); style.setVerticalAlignment(CENTER); style.setFillForegroundColor(IndexedColors.LIGHT_TURQUOISE.getIndex()); style.setFillPattern(FillPatternType.SOLID_FOREGROUND); style.setBorderBottom(BorderStyle.THIN); style.setBorderLeft(BorderStyle.THIN); style.setBorderRight(BorderStyle.THIN); style.setWrapText(true); return style; }
    private CellStyle createExcelBreakStyle(Workbook wb) { CellStyle style = wb.createCellStyle(); org.apache.poi.ss.usermodel.Font font = wb.createFont(); font.setBold(true); font.setFontHeightInPoints((short) 9); style.setFont(font); style.setAlignment(HorizontalAlignment.CENTER); style.setVerticalAlignment(CENTER); style.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex()); style.setFillPattern(FillPatternType.SOLID_FOREGROUND); style.setBorderTop(BorderStyle.THIN); style.setBorderBottom(BorderStyle.THIN); style.setBorderLeft(BorderStyle.THIN); style.setBorderRight(BorderStyle.THIN); return style; }
    private CellStyle createExcelEntryStyle(Workbook wb) { CellStyle style = wb.createCellStyle(); org.apache.poi.ss.usermodel.Font font = wb.createFont(); font.setFontHeightInPoints((short) 9); style.setFont(font); style.setAlignment(HorizontalAlignment.CENTER); style.setVerticalAlignment(CENTER); style.setBorderTop(BorderStyle.THIN); style.setBorderBottom(BorderStyle.THIN); style.setBorderLeft(BorderStyle.THIN); style.setBorderRight(BorderStyle.THIN); style.setWrapText(true); return style; }
    private CellStyle createExcelEmptyStyle(Workbook wb) { CellStyle style = wb.createCellStyle(); style.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex()); style.setFillPattern(FillPatternType.SOLID_FOREGROUND); style.setBorderTop(BorderStyle.THIN); style.setBorderBottom(BorderStyle.THIN); style.setBorderLeft(BorderStyle.THIN); style.setBorderRight(BorderStyle.THIN); return style; }

    private Table createPdfTimetableTable(List<TimetableEntry> timetable) throws IOException {
        List<LocalTime> classSlots = calculateTimeSlots(workStartTime, workEndTime, breakStartTime, breakEndTime, slotDurationMinutes);
        if (classSlots.isEmpty()) return new Table(1).addCell("No valid class slots found.");

        int breakColumnIndex = -1;
        for (int i = 0; i < classSlots.size(); i++) {
            if (!classSlots.get(i).isBefore(breakStartTime)) {
                breakColumnIndex = i + 1;
                break;
            }
        }
        if (breakColumnIndex == -1 && breakStartTime != null && !breakStartTime.isAfter(workEndTime)) {
            breakColumnIndex = classSlots.size() + 1;
        }

        int totalPdfCols = 1 + classSlots.size() + (breakColumnIndex != -1 ? 1 : 0);
        float[] columnWidths = new float[totalPdfCols];
        Arrays.fill(columnWidths, 2f);
        columnWidths[0] = 3f;
        Table table = new Table(UnitValue.createPercentArray(columnWidths)).useAllAvailableWidth();

        PdfFont boldFont = PdfFontFactory.createFont(StandardFonts.HELVETICA_BOLD);
        PdfFont regularFont = PdfFontFactory.createFont(StandardFonts.HELVETICA);
        Border defaultBorder = new SolidBorder(ColorConstants.GRAY, 0.5f);

        // Header Row
        table.addHeaderCell(new com.itextpdf.layout.element.Cell().add(new Paragraph("Day / Sem").setFont(boldFont).setFontSize(9)).setTextAlignment(TextAlignment.CENTER).setVerticalAlignment(VerticalAlignment.MIDDLE).setBackgroundColor(new DeviceRgb(204, 224, 255)).setBorder(defaultBorder));
        int currentPdfCol = 1;
        for (LocalTime slotStart : classSlots) {
            if (currentPdfCol == breakColumnIndex) {
                table.addHeaderCell(new com.itextpdf.layout.element.Cell().add(new Paragraph("BREAK").setFont(boldFont).setFontSize(8)).setTextAlignment(TextAlignment.CENTER).setVerticalAlignment(VerticalAlignment.MIDDLE).setBackgroundColor(ColorConstants.LIGHT_GRAY).setBorder(defaultBorder));
                currentPdfCol++;
            }
            table.addHeaderCell(new com.itextpdf.layout.element.Cell().add(new Paragraph(slotStart.format(TIME_FORMATTER) + "-\n" + slotStart.plusMinutes(slotDurationMinutes).format(TIME_FORMATTER)).setFont(boldFont).setFontSize(8)).setTextAlignment(TextAlignment.CENTER).setVerticalAlignment(VerticalAlignment.MIDDLE).setBackgroundColor(new DeviceRgb(204, 224, 255)).setBorder(defaultBorder));
            currentPdfCol++;
        }
        if (breakColumnIndex == classSlots.size() + 1) table.addHeaderCell(new com.itextpdf.layout.element.Cell().add(new Paragraph("BREAK").setFont(boldFont).setFontSize(8)).setTextAlignment(TextAlignment.CENTER).setVerticalAlignment(VerticalAlignment.MIDDLE).setBackgroundColor(ColorConstants.LIGHT_GRAY).setBorder(defaultBorder));

        // Data Rows
        Map<String, Map<Semester, Map<LocalTime, TimetableEntry>>> grouped = timetable.stream().collect(Collectors.groupingBy(TimetableEntry::getDay, Collectors.groupingBy(TimetableEntry::getSemester, Collectors.toMap(TimetableEntry::getStartTime, e -> e))));
        List<Semester> sortedSemesters = semesterList.stream().sorted(Comparator.comparing(Semester::getName)).collect(Collectors.toList());

        for (String day : WORKING_DAYS) {
            table.addCell(new com.itextpdf.layout.element.Cell(1, totalPdfCols).add(new Paragraph(day).setFont(boldFont).setFontSize(10)).setBackgroundColor(new DeviceRgb(170, 204, 255)).setPaddingLeft(5).setBorder(new SolidBorder(ColorConstants.DARK_GRAY, 1f)));
            for (Semester semester : sortedSemesters) {
                table.addCell(new com.itextpdf.layout.element.Cell().add(new Paragraph(semester.getName()).setFont(regularFont).setFontSize(8)).setTextAlignment(TextAlignment.RIGHT).setVerticalAlignment(VerticalAlignment.MIDDLE).setBackgroundColor(new DeviceRgb(230, 240, 255)).setBorder(defaultBorder));
                Map<LocalTime, TimetableEntry> daySemEntries = grouped.getOrDefault(day, Collections.emptyMap()).getOrDefault(semester, Collections.emptyMap());

                currentPdfCol = 1;
                for (LocalTime slotStart : classSlots) {
                    if (currentPdfCol == breakColumnIndex) {
                        table.addCell(new com.itextpdf.layout.element.Cell().add(new Paragraph(" ")).setBackgroundColor(ColorConstants.LIGHT_GRAY).setBorder(defaultBorder));
                        currentPdfCol++;
                    }
                    TimetableEntry entry = daySemEntries.get(slotStart);
                    if (entry != null) {
                        table.addCell(new com.itextpdf.layout.element.Cell().add(new Paragraph(entry.getSubject().getName() + "\n(" + entry.getTeacher().getName() + ")").setFont(regularFont).setFontSize(7)).setTextAlignment(TextAlignment.CENTER).setVerticalAlignment(VerticalAlignment.MIDDLE).setBorder(defaultBorder));
                    } else {
                        table.addCell(new com.itextpdf.layout.element.Cell().add(new Paragraph(" ")).setBackgroundColor(new DeviceRgb(249, 249, 249)).setBorder(defaultBorder));
                    }
                    currentPdfCol++;
                }
                if (breakColumnIndex == classSlots.size() + 1) table.addCell(new com.itextpdf.layout.element.Cell().add(new Paragraph(" ")).setBackgroundColor(ColorConstants.LIGHT_GRAY).setBorder(defaultBorder));
            }
        }
        return table;
    }

    private void showAboutDialog() {
        Alert aboutAlert = new Alert(Alert.AlertType.INFORMATION);
        aboutAlert.setTitle("About " + APP_NAME);
        aboutAlert.setHeaderText(APP_NAME + " - Version " + APP_VERSION);

        ImageView logoView = null;
        try (InputStream logoStream = getClass().getResourceAsStream("/logo.png")) {
            if (logoStream != null) {
                logoView = new ImageView(new Image(logoStream));
                logoView.setFitWidth(80);
                logoView.setFitHeight(80);
                Circle clip = new Circle(40, 40, 40);
                logoView.setClip(clip);
            }
        } catch (Exception e) { System.err.println("Error loading logo image."); }

        String contentText = String.format("%s Version: %s\n\nDeveloped by: %s\nMail: %s\nContact: %s\n\nThis application helps generate class timetables.", APP_NAME, APP_VERSION, DEVELOPER_NAME, DEVELOPER_EMAIL, DEVELOPER_CONTACT);
        VBox dialogLayout = new VBox(10);
        dialogLayout.setAlignment(Pos.CENTER);
        if (logoView != null) dialogLayout.getChildren().add(logoView);
        dialogLayout.getChildren().add(new Label(contentText));
        aboutAlert.getDialogPane().setContent(dialogLayout);
        loadStageIcon((Stage) aboutAlert.getDialogPane().getScene().getWindow());
        aboutAlert.showAndWait();
    }
    private void handleLoadConfiguration(Stage stage) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Load Configuration File");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("JSON Files (*.json)", "*.json"));
        File file = fileChooser.showOpenDialog(stage);
        if (file == null) return;

        try {
            ObjectMapper objectMapper = new ObjectMapper();
            AppConfiguration loadedConfig = objectMapper.readValue(file, AppConfiguration.class);
            resetUIAndDataToDefaults();

            schoolNameField.setText(loadedConfig.schoolName);
            departmentNameField.setText(loadedConfig.departmentName);

            if (loadedConfig.timeSettings != null) {
                workStartTimeCombo.setValue(loadedConfig.timeSettings.workStart());
                workEndTimeCombo.setValue(loadedConfig.timeSettings.workEnd());
                breakStartTimeCombo.setValue(loadedConfig.timeSettings.breakStart());
                breakEndTimeCombo.setValue(loadedConfig.timeSettings.breakEnd());
                slotDurationCombo.setValue(loadedConfig.timeSettings.slotDuration());
                maxTeacherSlotsSpinner.getValueFactory().setValue(loadedConfig.timeSettings.maxTeacherSlots());
            }
            if (loadedConfig.maxConsecutiveSlotsPerSubject != null) {
                maxConsecutiveSubjectSpinner.getValueFactory().setValue(loadedConfig.maxConsecutiveSlotsPerSubject);
            }

            if (validateAndStoreTimeConfig()) {
                showSemesterSection();
            }

            if (loadedConfig.semesterConfigurations != null) {
                semCountComboBox.setValue(loadedConfig.semesterConfigurations.size());
                Platform.runLater(() -> {
                    populateSemesterGridFromConfig(loadedConfig.semesterConfigurations);

                    semesterList.clear();
                    allSubjectsObservableList.clear();
                    for (SemesterConfigData semConfig : loadedConfig.semesterConfigurations) {
                        Semester sem = new Semester(semConfig.name());
                        for(SubjectDetailConfig subConfig : semConfig.subjects()){
                            Subject sub = new Subject(subConfig.name(), subConfig.hours());
                            sem.getSubjects().add(sub);
                            allSubjectsObservableList.add(new SubjectContext(sub, sem));
                        }
                        semesterList.add(sem);
                    }
                    allSubjectsObservableList.sort(Comparator.comparing(sc -> sc.getSubject().getName()));

                    if (loadedConfig.teacherConfigurations != null) {
                        showTeacherSection();
                        teacherCountComboBox.setValue(loadedConfig.teacherConfigurations.size());
                        Platform.runLater(() -> {
                            populateTeacherGridFromConfig(loadedConfig.teacherConfigurations);
                            checkIfReadyToGenerate();
                        });
                    }
                });
            }
            configLoadedFromFile = true;
            showInfoAlert("Load Configuration", "Configuration loaded successfully.");

        } catch (IOException e) {
            showErrorAlert("Load Error", "Error loading configuration file:\n" + e.getMessage());
        }
    }
    private void resetUIAndDataToDefaults() {
        schoolNameField.clear();
        departmentNameField.clear();
        workStartTimeCombo.setValue("10:00");
        workEndTimeCombo.setValue("16:00");
        breakStartTimeCombo.setValue("13:00");
        breakEndTimeCombo.setValue("14:00");
        slotDurationCombo.setValue(60);
        maxTeacherSlotsSpinner.getValueFactory().setValue(2);
        maxConsecutiveSubjectSpinner.getValueFactory().setValue(3);
        semCountComboBox.setValue(null);
        semesterSubjectGrid.getChildren().clear();
        semesterList.clear();
        teacherCountComboBox.setValue(null);
        teacherMappingGrid.getChildren().clear();
        teacherList.clear();
        allSubjectsObservableList.clear();
        globallySelectedSubjectContexts.clear();
        hideSemesterSection();
        hideTeacherSection();
        hideGenerateButton();
        disableExportButtons();
        timetableDisplayGrid.getChildren().clear();
        generatedScrollPane.setContent(new Label("Please provide input configuration first."));
        lastGeneratedTimetable = null;
        configLoadedFromFile = false;
    }
    private void populateSemesterGridFromConfig(List<SemesterConfigData> semesterConfigurations) {
        for (int i = 0; i < semesterConfigurations.size(); i++) {
            SemesterConfigData semConfig = semesterConfigurations.get(i);
            TextField semNameField = (TextField) findNodeInGrid(semesterSubjectGrid, i, 0);
            VBox subjectInputArea = (VBox) findNodeInGrid(semesterSubjectGrid, i, 1);
            if (semNameField != null) semNameField.setText(semConfig.name());
            if (subjectInputArea != null) {
                ComboBox<Integer> subCountCombo = (ComboBox<Integer>) subjectInputArea.getChildren().get(0);
                FlowPane subDetailPane = (FlowPane) subjectInputArea.getChildren().get(1);
                subCountCombo.setValue(semConfig.subjects().size());
                Platform.runLater(() -> {
                    for (int j = 0; j < semConfig.subjects().size(); j++) {
                        if (j < subDetailPane.getChildren().size()) {
                            HBox entryBox = (HBox) subDetailPane.getChildren().get(j);
                            ((TextField) entryBox.getChildren().get(0)).setText(semConfig.subjects().get(j).name());
                            ((Spinner<Integer>) entryBox.getChildren().get(2)).getValueFactory().setValue(semConfig.subjects().get(j).hours());
                        }
                    }
                });
            }
        }
    }
    private void populateTeacherGridFromConfig(List<TeacherConfigData> teacherConfigurations) {
        for (int i = 0; i < teacherConfigurations.size(); i++) {
            TeacherConfigData teacherConfig = teacherConfigurations.get(i);
            TextField teachNameField = (TextField) findNodeInGrid(teacherMappingGrid, i, 0);
            VBox teacherInputsVBox = (VBox) findNodeInGrid(teacherMappingGrid, i, 1);
            if (teachNameField != null) teachNameField.setText(teacherConfig.name());
            if (teacherInputsVBox != null) {
                ComboBox<Integer> subTeachCountCombo = (ComboBox<Integer>) teacherInputsVBox.getChildren().get(0);
                FlowPane tSubMapFlowPane = (FlowPane) teacherInputsVBox.getChildren().get(1);
                subTeachCountCombo.setValue(teacherConfig.assignedSubjects().size());
                Platform.runLater(() -> {
                    for (int j = 0; j < teacherConfig.assignedSubjects().size(); j++) {
                        TeacherAssignmentConfig assignment = teacherConfig.assignedSubjects().get(j);
                        ComboBox<SubjectContext> subjectSelectCombo = (ComboBox<SubjectContext>) tSubMapFlowPane.getChildren().get(j);
                        allSubjectsObservableList.stream()
                                .filter(sc -> sc.getSubject().getName().equals(assignment.subjectName()) && sc.getSemester().getName().equals(assignment.semesterName()))
                                .findFirst()
                                .ifPresent(subjectSelectCombo::setValue);
                    }
                });
            }
        }
    }
    private void handleSaveConfiguration(Stage stage) {
        AppConfiguration config = new AppConfiguration();
        config.schoolName = schoolNameField.getText();
        config.departmentName = departmentNameField.getText();

        if (workStartTimeCombo.getValue() == null || maxTeacherSlotsSpinner.getValue() == null) {
            showErrorAlert("Save Error", "Incomplete time configuration.");
            return;
        }
        config.timeSettings = new TimeConfiguration(workStartTimeCombo.getValue(), workEndTimeCombo.getValue(), breakStartTimeCombo.getValue(), breakEndTimeCombo.getValue(), slotDurationCombo.getValue(), maxTeacherSlotsSpinner.getValue());
        config.maxConsecutiveSlotsPerSubject = maxConsecutiveSubjectSpinner.getValue();

        config.semesterConfigurations = semesterList.stream().map(sem -> {
            List<SubjectDetailConfig> subjectDetails = sem.getSubjects().stream().map(sub -> new SubjectDetailConfig(sub.getName(), sub.getWeeklyHours())).collect(Collectors.toList());
            return new SemesterConfigData(sem.getName(), subjectDetails);
        }).collect(Collectors.toList());

        config.teacherConfigurations = new ArrayList<>();
        if (teacherCountComboBox.getValue() != null) {
            for (int i = 0; i < teacherCountComboBox.getValue(); i++) {
                TextField teachNameField = (TextField) findNodeInGrid(teacherMappingGrid, i, 0);
                VBox teacherInputsVBox = (VBox) findNodeInGrid(teacherMappingGrid, i, 1);
                if (teachNameField == null || teachNameField.getText().trim().isEmpty()) continue;
                FlowPane tSubMapFlowPane = (FlowPane) teacherInputsVBox.getChildren().get(1);
                List<TeacherAssignmentConfig> assignments = tSubMapFlowPane.getChildren().stream()
                        .map(node -> ((ComboBox<SubjectContext>) node).getValue())
                        .filter(Objects::nonNull)
                        .map(sc -> new TeacherAssignmentConfig(sc.getSubject().getName(), sc.getSemester().getName()))
                        .collect(Collectors.toList());
                config.teacherConfigurations.add(new TeacherConfigData(teachNameField.getText(), assignments));
            }
        }

        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Save Configuration File");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("JSON Files (*.json)", "*.json"));
        fileChooser.setInitialFileName("classmesh_config.json");
        File file = fileChooser.showSaveDialog(stage);
        if (file == null) return;

        try {
            new ObjectMapper().enable(SerializationFeature.INDENT_OUTPUT).writeValue(file, config);
            showInfoAlert("Save Configuration", "Configuration saved successfully.");
        } catch (IOException e) {
            showErrorAlert("Save Error", "Error saving configuration file:\n" + e.getMessage());
        }
    }

    private boolean isOperationAllowed() {
        if ("XX-XX-XX-XX-XX-XX".equalsIgnoreCase(ALLOWED_MAC_ADDRESS) || ALLOWED_MAC_ADDRESS.trim().isEmpty()) {
            List<String> currentMacs = getCurrentMacAddresses();
            showErrorAlert("MAC Address Not Configured", "The application's allowed MAC address is not set.\n\nCurrent system MACs: " + currentMacs);
            return false; // Secure by default
        }
        return getCurrentMacAddresses().stream().anyMatch(ALLOWED_MAC_ADDRESS::equalsIgnoreCase);
    }
    private List<String> getCurrentMacAddresses() {
        List<String> macAddressList = new ArrayList<>();
        try {
            Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
            while (interfaces.hasMoreElements()) {
                NetworkInterface ni = interfaces.nextElement();
                if (ni.isUp() && !ni.isLoopback() && !ni.isVirtual()) {
                    byte[] mac = ni.getHardwareAddress();
                    if (mac != null) {
                        StringBuilder sb = new StringBuilder();
                        for (int i = 0; i < mac.length; i++) {
                            sb.append(String.format("%02X%s", mac[i], (i < mac.length - 1) ? "-" : ""));
                        }
                        macAddressList.add(sb.toString());
                    }
                }
            }
        } catch (SocketException e) {
            System.err.println("Security Check: Could not retrieve MAC address. " + e.getMessage());
        }
        return macAddressList;
    }

    // --- Main Method ---
    public static void main(String[] args) {
        launch(args);
    }
}

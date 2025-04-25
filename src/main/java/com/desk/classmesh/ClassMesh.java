//Ver 1.2
package com.desk.classmesh; // Make sure this matches your project structure

// JavaFX Imports
import javafx.application.Application;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Orientation; // Keep if FlowPane is used elsewhere
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.layout.*;
import javafx.scene.paint.Color; // Import Color
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.FileChooser; // Import FileChooser
import javafx.stage.Stage;
import javafx.util.Callback; // Import Callback
// iText 7 (for PDF Export)
import com.itextpdf.kernel.colors.ColorConstants; // Use iText's Color
import com.itextpdf.kernel.colors.DeviceRgb;      // For custom colors
import com.itextpdf.kernel.font.PdfFont;
import com.itextpdf.kernel.font.PdfFontFactory;
import com.itextpdf.kernel.geom.PageSize;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.borders.Border;       // iText Border
import com.itextpdf.layout.borders.SolidBorder;   // iText Border Style
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.layout.properties.UnitValue;
import com.itextpdf.layout.properties.VerticalAlignment;   // Correct iText VerticalAlignment import
import com.itextpdf.io.font.constants.StandardFonts;     // Standard PDF fonts

// Also make sure you have the standard Java IO imports needed by handleExportPdf:
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
// Java Standard Library Imports
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.Duration;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

// --- External Library Imports (Add these to your project dependencies!) ---

// Apache POI (for Excel Export)
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.usermodel.Cell; // Explicit import for Cell
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.apache.poi.ss.util.CellRangeAddress; // For merging cells
import org.apache.poi.xssf.usermodel.XSSFFont; // For specific font styling

// Removed Apache PDFBox imports

import static org.apache.poi.ss.usermodel.VerticalAlignment.CENTER;

// --- End External Library Imports ---


// --- Data Model Classes ---
// (Data models unchanged)
class Subject { String name; int weeklyHours; public Subject(String name, int hours) { this.name = name; this.weeklyHours = hours; } public String getName() { return name; } public int getWeeklyHours() { return weeklyHours; } @Override public String toString() { return name; } @Override public boolean equals(Object o) { if (this == o) return true; if (o == null || getClass() != o.getClass()) return false; Subject subject = (Subject) o; return weeklyHours == subject.weeklyHours && Objects.equals(name, subject.name); } @Override public int hashCode() { return Objects.hash(name, weeklyHours); } }
class Semester { String name; List<Subject> subjects = new ArrayList<>(); public Semester(String name) { this.name = name; } public String getName() { return name; } public List<Subject> getSubjects() { return subjects; } @Override public String toString() { return name; } @Override public boolean equals(Object o) { if (this == o) return true; if (o == null || getClass() != o.getClass()) return false; Semester semester = (Semester) o; return Objects.equals(name, semester.name); } @Override public int hashCode() { return Objects.hash(name); } }
class Teacher { String name; List<Subject> subjectsTaught = new ArrayList<>(); public Teacher(String name) { this.name = name; } public String getName() { return name; } public List<Subject> getSubjectsTaught() { return subjectsTaught; } public boolean canTeach(Subject subject) { return subjectsTaught.stream().anyMatch(taughtSub -> taughtSub.equals(subject)); } @Override public String toString() { return name; } @Override public boolean equals(Object o) { if (this == o) return true; if (o == null || getClass() != o.getClass()) return false; Teacher teacher = (Teacher) o; return Objects.equals(name, teacher.name); } @Override public int hashCode() { return Objects.hash(name); } }
class SubjectContext { private final Subject subject; private final Semester semester; public SubjectContext(Subject subject, Semester semester) { this.subject = subject; this.semester = semester; } public Subject getSubject() { return subject; } public Semester getSemester() { return semester; } public String getUniqueId() { return subject.getName() + "|" + semester.getName(); } @Override public String toString() { return subject.getName() + " (" + semester.getName() + ")"; } @Override public boolean equals(Object o) { if (this == o) return true; if (o == null || getClass() != o.getClass()) return false; SubjectContext that = (SubjectContext) o; return Objects.equals(subject, that.subject) && Objects.equals(semester, that.semester); } @Override public int hashCode() { return Objects.hash(subject, semester); } }
class TimetableEntry { String day; LocalTime startTime; LocalTime endTime; Semester semester; Subject subject; Teacher teacher; public TimetableEntry(String day, LocalTime startTime, LocalTime endTime, Semester semester, Subject subject, Teacher teacher) { this.day = day; this.startTime = startTime; this.endTime = endTime; this.semester = semester; this.subject = subject; this.teacher = teacher; } public String getDay() { return day; } public LocalTime getStartTime() { return startTime; } public LocalTime getEndTime() { return endTime; } public Semester getSemester() { return semester; } public Subject getSubject() { return subject; } public Teacher getTeacher() { return teacher; } public String getTimeSlotString() { DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm"); return startTime.format(formatter) + " - " + endTime.format(formatter); } }
// --- End Data Model Classes ---

// --- Data Structures for Save Simulation ---
record SubjectDetail(String name, int hours) { public Map<String, Object> toMap() { Map<String, Object> map = new LinkedHashMap<>(); map.put("name", name); map.put("hours", hours); return map; } }
record SemesterConfig(String name, List<SubjectDetail> subjects) { public Map<String, Object> toMap() { Map<String, Object> map = new LinkedHashMap<>(); map.put("name", name); map.put("subjects", subjects.stream().map(SubjectDetail::toMap).collect(Collectors.toList())); return map; } }
record TeacherAssignment(String subjectName, String semesterName) { public Map<String, Object> toMap() { Map<String, Object> map = new LinkedHashMap<>(); map.put("subjectName", subjectName); map.put("semesterName", semesterName); return map; } }
record TeacherConfig(String name, List<TeacherAssignment> assignedSubjects) { public Map<String, Object> toMap() { Map<String, Object> map = new LinkedHashMap<>(); map.put("name", name); map.put("assignedSubjects", assignedSubjects.stream().map(TeacherAssignment::toMap).collect(Collectors.toList())); return map; } }
record TimeConfiguration(String workStart, String workEnd, String breakStart, String breakEnd, int slotDuration, int maxTeacherSlots) { // Added maxTeacherSlots
    public Map<String, Object> toMap() { Map<String, Object> map = new LinkedHashMap<>(); map.put("workStart", workStart); map.put("workEnd", workEnd); map.put("breakStart", breakStart); map.put("breakEnd", breakEnd); map.put("slotDuration", slotDuration); map.put("maxTeacherSlots", maxTeacherSlots); return map; } // Added maxTeacherSlots
    public static TimeConfiguration fromJsonSnippet(String jsonSnippet) { String ws = extractJsonStringValue(jsonSnippet, "workStart"); String we = extractJsonStringValue(jsonSnippet, "workEnd"); String bs = extractJsonStringValue(jsonSnippet, "breakStart"); String be = extractJsonStringValue(jsonSnippet, "breakEnd"); int sd = extractJsonIntValue(jsonSnippet, "slotDuration"); int mts = extractJsonIntValue(jsonSnippet, "maxTeacherSlots"); // Extract maxTeacherSlots
        if (ws != null && we != null && bs != null && be != null && sd != -1 && mts != -1) { // Check mts
            return new TimeConfiguration(ws, we, bs, be, sd, mts); // Add mts
        } return null; }
    private static String extractJsonStringValue(String json, String key) { Matcher m = Pattern.compile("\"" + key + "\":\\s*\"([^\"]*)\"").matcher(json); return m.find() ? m.group(1) : null; }
    private static int extractJsonIntValue(String json, String key) { Matcher m = Pattern.compile("\"" + key + "\":\\s*(\\d+)").matcher(json); return m.find() ? Integer.parseInt(m.group(1)) : -1; }
}
// --- End Data Structures for Save Simulation ---


public class ClassMesh extends Application {
    // --- Constants ---
    private static final String MAIN_CONTAINER_STYLE_CLASS = "main-config-pane";
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm");
    private static final List<String> WORKING_DAYS = List.of("Monday", "Tuesday", "Wednesday", "Thursday", "Friday","Saturday");
    // private static final int MAX_TEACHER_SLOTS_PER_DAY = 2; // Removed constant
    private static final int MIN_SUBJECT_HOURS = 1; // Minimum hours for a subject
    private static final int DEFAULT_SUBJECT_HOURS = 3; // Fallback default if calculation fails
    private static final int MAX_SUBJECT_HOURS_SPINNER = 50; // Max value for the spinner
    private static final int ABSOLUTE_MAX_SUBJECTS = 50; // Absolute max subjects in dropdown

    // --- UI Elements ---
    private ComboBox<Integer> semCountComboBox; private GridPane semesterSubjectGrid; private Node semesterSectionContainer;
    private VBox timeConfigSection; private ComboBox<String> workStartTimeCombo; private ComboBox<String> workEndTimeCombo; private ComboBox<String> breakStartTimeCombo; private ComboBox<String> breakEndTimeCombo; private ComboBox<Integer> slotDurationCombo; private Label workingHoursLabel;
    private Spinner<Integer> maxTeacherSlotsSpinner; // New spinner for teacher slots
    private Label teacherSectionTitle; private Label teacherMapTitle; private ComboBox<Integer> teacherCountComboBox; private GridPane teacherMappingGrid; private Node teacherSectionContainer;
    private Button generateButton;
    private VBox mainContentVBox; private TabPane mainTabPane; private Tab generatedTimetableTab; private GridPane timetableDisplayGrid; private ScrollPane generatedScrollPane;
    // Export Buttons
    private Button exportExcelButton;
    private Button exportPdfButton;


    // --- Data Storage ---
    private final List<Semester> semesterList = new ArrayList<>();
    private final List<Teacher> teacherList = new ArrayList<>();
    private final ObservableList<SubjectContext> allSubjectsObservableList = FXCollections.observableArrayList();
    private LocalTime workStartTime; private LocalTime workEndTime; private LocalTime breakStartTime; private LocalTime breakEndTime; private int slotDurationMinutes;
    private int maxTeacherSlotsPerDay = 2; // Default value, will be updated from UI
    private long netWorkingMinutesPerDay = 0;
    private int maxWeeklyHoursPerSemester = 0; // Max allowed weekly hours based on working time
    private final Set<SubjectContext> globallySelectedSubjectContexts = new HashSet<>();
    private List<TimetableEntry> lastGeneratedTimetable = null; // Store generated timetable for export

    // --- State for Loaded Configuration ---
    private String loadedJsonData = null; private boolean configLoadedFromFile = false;

    // Flag to prevent recursive updates during auto-adjustment
    private final AtomicBoolean isAdjustingHours = new AtomicBoolean(false);
    // Flag to show alert only once after adjustment sequence
    private boolean adjustmentAlertPending = false;


    @Override
    public void start(Stage stage) {
        // --- Root Layout Setup ---
        BorderPane mainLayout = new BorderPane(); mainContentVBox = new VBox(); mainContentVBox.setPadding(new Insets(10)); mainContentVBox.setStyle("-fx-border-color:#6ca0dc;-fx-border-width: 0 2 2 2;-fx-border-radius:0 0 4 4;"); VBox.setVgrow(mainContentVBox, Priority.ALWAYS);
        mainLayout.setTop(createMenuBar(stage));
        ScrollPane inputScrollPane = new ScrollPane(); inputScrollPane.setContent(mainContentVBox); inputScrollPane.setFitToWidth(true); inputScrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED); inputScrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER); inputScrollPane.setStyle("-fx-background-color: transparent;"); mainLayout.setCenter(inputScrollPane);

        // --- Create UI Sections ---
        timeConfigSection = createTimeConfigurationSection();
        semesterSectionContainer = createSemesterInputSection();
        teacherSectionContainer = createTeacherMappingSection();
        Node generateButtonSection = createGenerateButtonSection();

        // --- Add Sections to mainContentVBox in the NEW desired order ---
        mainContentVBox.getChildren().addAll(timeConfigSection, semesterSectionContainer, teacherSectionContainer, generateButtonSection);

        // --- TabPane Setup ---
        mainTabPane = new TabPane(); Tab inputTab = new Tab("Input Details", mainLayout); inputTab.setClosable(false);
        generatedTimetableTab = createTimetableTab(); // Creates tab with placeholder and export buttons
        mainTabPane.getTabs().addAll(inputTab, generatedTimetableTab);
        setupTabPaneSelectionStyle(mainTabPane);

        // --- Initial Visibility ---
        hideSemesterSection(); hideTeacherSection(); hideGenerateButton();
        disableExportButtons(); // Initially disable export buttons

        // --- Scene and Stage Setup ---
        Scene scene = new Scene(mainTabPane, 1200, 800); loadStageIcon(stage); stage.setScene(scene); stage.setTitle("ClassMesh - TimeTable Management"); stage.show();
    }

    //=========================================================================
    // UI Creation Helper Methods
    //=========================================================================

    /** Creates the Menu Bar */
    private MenuBar createMenuBar(Stage stage) {
        MenuBar menuBar = new MenuBar(); Menu fileMenu = new Menu("File"); MenuItem loadItem = new MenuItem("Load Configuration"); MenuItem saveItem = new MenuItem("Save Configuration");
        loadItem.setOnAction(e -> handleLoadConfiguration(stage)); saveItem.setOnAction(e -> handleSaveConfiguration(stage)); fileMenu.getItems().addAll(loadItem, saveItem); menuBar.getMenus().add(fileMenu);
        return menuBar;
    }

    /** Creates the Semester/Subject Input Section */
    private Node createSemesterInputSection() {
        VBox sectionContainer = new VBox(10); sectionContainer.setPadding(new Insets(10, 0, 10, 0)); sectionContainer.setStyle("-fx-border-color:#6ca0dc;-fx-border-width: 2 0 0 0;-fx-padding: 15 0 15 0;"); sectionContainer.setId("semesterSectionBox");
        Label titleLabel = new Label("Semester Configuration"); titleLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;"); titleLabel.setPadding(new Insets(0, 0, 5, 0));
        Label gridTitle = new Label("Semester/Class Names & Subjects:"); gridTitle.setStyle("-fx-font-size: 14px; -fx-font-weight: bold;"); gridTitle.setPadding(new Insets(10, 0, 5, 0));
        semCountComboBox = new ComboBox<>(); semCountComboBox.setPromptText("Number of Working Semesters/Classes"); for (int i = 1; i <= 12; i++) semCountComboBox.getItems().add(i);
        semesterSubjectGrid = new GridPane(); semesterSubjectGrid.setStyle("-fx-border-color:#6ca0dc;-fx-border-thickness:2px;-fx-border-radius:4px; -fx-padding: 10;"); semesterSubjectGrid.getStyleClass().add(MAIN_CONTAINER_STYLE_CLASS); semesterSubjectGrid.setHgap(10); semesterSubjectGrid.setVgap(10);
        ColumnConstraints subCol0 = new ColumnConstraints(); subCol0.setHgrow(Priority.NEVER); subCol0.setMinWidth(150); subCol0.setPrefWidth(150);
        ColumnConstraints subCol1 = new ColumnConstraints(); subCol1.setHgrow(Priority.ALWAYS); semesterSubjectGrid.getColumnConstraints().addAll(subCol0, subCol1);
        semCountComboBox.setOnAction(e -> buildSemesterSubjectGrid());
        sectionContainer.getChildren().addAll(titleLabel, semCountComboBox, gridTitle, semesterSubjectGrid);
        return sectionContainer;
    }

    /** Builds the content of the semesterSubjectGrid based on semCountComboBox */
    private void buildSemesterSubjectGrid() {
        semesterSubjectGrid.getChildren().clear(); hideTeacherSection();
        Integer selectedCount = semCountComboBox.getValue(); if (selectedCount == null || selectedCount <= 0) return;

        int maxPossibleSubjects = (maxWeeklyHoursPerSemester > 0 && MIN_SUBJECT_HOURS > 0) ? (maxWeeklyHoursPerSemester / MIN_SUBJECT_HOURS) : 0;
        int maxSubjectsToShow = Math.min(ABSOLUTE_MAX_SUBJECTS, maxPossibleSubjects);
        System.out.println("DEBUG: Max possible subjects calculated: " + maxPossibleSubjects + ", Showing up to: " + maxSubjectsToShow);

        for (int semIndex = 0; semIndex < selectedCount; semIndex++) {
            TextField semNameField = new TextField(); semNameField.setPromptText("Semester/Class " + (semIndex + 1) + " Name"); semNameField.setId("semName_" + semIndex);
            ComboBox<Integer> subCountCombo = new ComboBox<>(); subCountCombo.setPromptText("Subject Count");
            subCountCombo.getItems().add(0); // Allow zero subjects
            for (int j = 1; j <= maxSubjectsToShow; j++) { subCountCombo.getItems().add(j); }
            subCountCombo.setId("subCount_" + semIndex);

            FlowPane subDetailPane = new FlowPane(Orientation.HORIZONTAL, 10, 5); subDetailPane.setId("subDetailPane_" + semIndex); subDetailPane.setPrefWrapLength(400);
            int finalSemIndex = semIndex;
            subCountCombo.setOnAction(e -> {
                subDetailPane.getChildren().clear();
                Integer numSubs = subCountCombo.getValue();
                if (numSubs == null || numSubs <= 0) return;

                int defaultHours = Math.min(DEFAULT_SUBJECT_HOURS, MAX_SUBJECT_HOURS_SPINNER); // Use simple default

                for (int subIndex = 0; subIndex < numSubs; subIndex++) {
                    TextField subNameField = new TextField(); subNameField.setPromptText("Subject " + (subIndex + 1) + " Name"); subNameField.setId("subName_" + finalSemIndex + "_" + subIndex);
                    Spinner<Integer> hoursSpinner = new Spinner<>(MIN_SUBJECT_HOURS, MAX_SUBJECT_HOURS_SPINNER, defaultHours);
                    hoursSpinner.setPrefWidth(70); hoursSpinner.setEditable(true); hoursSpinner.setId("subHours_" + finalSemIndex + "_" + subIndex);
                    HBox entryBox = new HBox(5, subNameField, new Label("Hrs/Week:"), hoursSpinner); entryBox.setAlignment(Pos.CENTER_LEFT);
                    // Attach listener for auto-adjustment
                    hoursSpinner.valueProperty().addListener((obs, oldValue, newValue) -> {
                        if (!isAdjustingHours.get()) { // Only adjust if not already adjusting
                            adjustHoursWithinSemester(subDetailPane, hoursSpinner);
                            // Show alert AFTER adjustment attempt, if needed
                            Platform.runLater(() -> {
                                if (adjustmentAlertPending) {
                                    // Alert removed, just keep the flag logic for now
                                    // showInfoAlert("Hours Adjusted", "Subject hours automatically adjusted to fit within the calculated weekly limit ("+ maxWeeklyHoursPerSemester +" hrs) for this semester.");
                                    adjustmentAlertPending = false; // Reset flag
                                }
                            });
                        }
                    });
                    subDetailPane.getChildren().add(entryBox);
                }
                // Initial check/adjustment after creating spinners
                adjustHoursWithinSemester(subDetailPane, null);
                // Show alert immediately if initial adjustment was needed
                Platform.runLater(() -> {
                    if (adjustmentAlertPending) {

                        // showInfoAlert("Hours Adjusted", "Initial subject hours automatically adjusted to fit within the calculated weekly limit ("+ maxWeeklyHoursPerSemester +" hrs) for this semester.");
                        adjustmentAlertPending = false; // Reset flag
                    }
                });
            });
            VBox subjectInputArea = new VBox(5, subCountCombo, subDetailPane); subjectInputArea.setId("subjectInputArea_" + semIndex);
            semesterSubjectGrid.add(semNameField, 0, semIndex); semesterSubjectGrid.add(subjectInputArea, 1, semIndex);
            GridPane.setHgrow(semNameField, Priority.SOMETIMES); GridPane.setHgrow(subjectInputArea, Priority.ALWAYS);
        }
        Button nextButton = new Button("Next -> Define Teachers"); nextButton.setStyle("-fx-background-color: #2196F3; -fx-text-fill: white; -fx-font-size: 14px; -fx-padding: 6 16 6 16; -fx-background-radius: 6;");
        nextButton.setOnAction(e -> handleSemesterNextClick(semesterSubjectGrid, selectedCount));
        semesterSubjectGrid.add(nextButton, 0, selectedCount, 2, 1); GridPane.setHalignment(nextButton, Pos.CENTER.getHpos()); GridPane.setMargin(nextButton, new Insets(15, 0, 0, 0));
    }


    /** Creates the Time Configuration Section UI */
    private VBox createTimeConfigurationSection() {
        VBox container = new VBox(10); container.setPadding(new Insets(0, 0, 10, 0)); container.setId("timeConfigBox");
        Label timeTitle = new Label("Time Configuration"); timeTitle.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;"); timeTitle.setPadding(new Insets(0, 0, 5, 0));
        GridPane timeGrid = new GridPane(); timeGrid.setHgap(10); timeGrid.setVgap(8); timeGrid.setPadding(new Insets(0, 0, 0, 10));
        ObservableList<String> timeOptions = FXCollections.observableArrayList(); for (int hour = 7; hour <= 18; hour++) { timeOptions.add(String.format("%02d:00", hour)); timeOptions.add(String.format("%02d:30", hour)); }
        workStartTimeCombo = new ComboBox<>(timeOptions); workStartTimeCombo.setPromptText("Work Start"); workStartTimeCombo.setValue("10:00");
        workEndTimeCombo = new ComboBox<>(timeOptions); workEndTimeCombo.setPromptText("Work End"); workEndTimeCombo.setValue("16:00");
        breakStartTimeCombo = new ComboBox<>(timeOptions); breakStartTimeCombo.setPromptText("Break Start"); breakStartTimeCombo.setValue("13:00");
        breakEndTimeCombo = new ComboBox<>(timeOptions); breakEndTimeCombo.setPromptText("Break End"); breakEndTimeCombo.setValue("14:00");
        slotDurationCombo = new ComboBox<>(); slotDurationCombo.setPromptText("Slot Duration (min)"); slotDurationCombo.getItems().addAll(30, 45, 50, 55, 60, 90); slotDurationCombo.setValue(60);
        // New Spinner for Max Teacher Slots
        maxTeacherSlotsSpinner = new Spinner<>(1, 8, 2); // Min 1, Max 8, Default 2
        maxTeacherSlotsSpinner.setPrefWidth(70);
        maxTeacherSlotsSpinner.setEditable(true);

        workStartTimeCombo.valueProperty().addListener((obs, ov, nv) -> updateWorkingHoursDisplay()); workEndTimeCombo.valueProperty().addListener((obs, ov, nv) -> updateWorkingHoursDisplay()); breakStartTimeCombo.valueProperty().addListener((obs, ov, nv) -> updateWorkingHoursDisplay()); breakEndTimeCombo.valueProperty().addListener((obs, ov, nv) -> updateWorkingHoursDisplay());
        workingHoursLabel = new Label("Working Hours/Day: --"); workingHoursLabel.setStyle("-fx-font-style: italic; -fx-padding: 0 0 0 10px;");
        timeGrid.addRow(0, new Label("Working Hours:"), workStartTimeCombo, new Label("to"), workEndTimeCombo);
        timeGrid.addRow(1, new Label("Break Time:"), breakStartTimeCombo, new Label("to"), breakEndTimeCombo);
        timeGrid.addRow(2, new Label("Class Duration:"), slotDurationCombo);
        timeGrid.addRow(3, new Label("Max Classes/Teacher/Day:"), maxTeacherSlotsSpinner); // Add new row
        timeGrid.add(workingHoursLabel, 1, 4, 3, 1); // Move working hours label down
        Button timeNextButton = new Button("Next -> Define Semesters"); timeNextButton.setStyle("-fx-background-color: #2196F3; -fx-text-fill: white; -fx-font-size: 14px; -fx-padding: 6 16 6 16; -fx-background-radius: 6;");
        timeNextButton.setOnAction(e -> handleTimeConfigNextClick());
        container.getChildren().addAll(timeTitle, timeGrid, timeNextButton); container.setAlignment(Pos.CENTER_LEFT); VBox.setMargin(timeNextButton, new Insets(15, 0, 0, 0));
        updateWorkingHoursDisplay(); // Calculate initial hours
        return container;
    }

    /** Creates the Teacher Mapping Section (Container and Title) */
    private Node createTeacherMappingSection() {
        VBox sectionContainer = new VBox(5); sectionContainer.setPadding(new Insets(10, 0, 10, 0)); sectionContainer.setStyle("-fx-border-color:#6ca0dc;-fx-border-width: 2 0 0 0;-fx-padding: 15 0 15 0;"); sectionContainer.setId("teacherSectionBox");
        teacherSectionTitle = new Label("Teaching Staff Configuration"); teacherSectionTitle.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;"); teacherSectionTitle.setPadding(new Insets(0, 0, 5, 0));
        teacherCountComboBox = new ComboBox<>(); teacherCountComboBox.setPromptText("Select Number of Teachers"); for (int t = 1; t <= 25; t++) teacherCountComboBox.getItems().add(t);
        teacherMapTitle = new Label("Teacher - Subject Mapping"); teacherMapTitle.setStyle("-fx-font-size: 14px; -fx-font-weight: bold;"); teacherMapTitle.setPadding(new Insets(10, 0, 5, 0));
        teacherMappingGrid = new GridPane(); teacherMappingGrid.setStyle("-fx-border-color:#6ca0dc;-fx-border-thickness:2px;-fx-border-radius:4px;-fx-padding:10px;"); teacherMappingGrid.getStyleClass().add(MAIN_CONTAINER_STYLE_CLASS); teacherMappingGrid.setVgap(10); teacherMappingGrid.setHgap(10);
        ColumnConstraints teachCol0 = new ColumnConstraints(); teachCol0.setHgrow(Priority.NEVER); teachCol0.setMinWidth(150); teachCol0.setPrefWidth(150);
        ColumnConstraints teachCol1 = new ColumnConstraints(); teachCol1.setHgrow(Priority.ALWAYS); teacherMappingGrid.getColumnConstraints().addAll(teachCol0, teachCol1);
        teacherCountComboBox.setOnAction(f -> populateTeacherMappingGrid(allSubjectsObservableList)); // Setup listener
        sectionContainer.getChildren().addAll(teacherSectionTitle, teacherCountComboBox, teacherMapTitle, teacherMappingGrid);
        return sectionContainer;
    }

    /** Creates the Generate Button Section */
    private Node createGenerateButtonSection() {
        generateButton = new Button("Generate Timetable"); generateButton.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white; -fx-font-size: 16px; -fx-padding: 10 20 10 20; -fx-background-radius: 6;");
        generateButton.setOnAction(e -> handleGenerateClick());
        HBox buttonBox = new HBox(generateButton); buttonBox.setAlignment(Pos.CENTER); buttonBox.setPadding(new Insets(20, 0, 10, 0));
        return buttonBox;
    }

    /** Creates the Tab for displaying the timetable */
    private Tab createTimetableTab() {
        timetableDisplayGrid = new GridPane();
        timetableDisplayGrid.setPadding(new Insets(10)); timetableDisplayGrid.setHgap(2); timetableDisplayGrid.setVgap(2);
        timetableDisplayGrid.setAlignment(Pos.TOP_LEFT); timetableDisplayGrid.setGridLinesVisible(true);

        generatedScrollPane = new ScrollPane(timetableDisplayGrid);
        generatedScrollPane.setFitToWidth(true);
        generatedScrollPane.setFitToHeight(true);
        // FIX: Enable horizontal scrollbar
        generatedScrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        generatedScrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);


        // Create Export Buttons
        exportExcelButton = new Button("Export to Excel");
        exportExcelButton.setOnAction(e -> handleExportExcel());
        // --- Pdf export added on feedback from teacher---
        exportPdfButton = new Button("Export to PDF"); // Instantiate PDF Button
        exportPdfButton.setOnAction(e -> handleExportPdf());   // Set PDF Action


        HBox exportButtonBox = new HBox(10, exportExcelButton,exportPdfButton); //  Excel button & Pdf button
        exportButtonBox.setAlignment(Pos.CENTER);
        exportButtonBox.setPadding(new Insets(10, 0, 0, 0)); // Padding above buttons

        VBox tabContent = new VBox(10);
        tabContent.setPadding(new Insets(10));
        tabContent.setAlignment(Pos.CENTER);
        Label placeholderLabel = new Label("Generated Timetable");
        // Add ScrollPane and then Button Box
        tabContent.getChildren().addAll(placeholderLabel, generatedScrollPane, exportButtonBox);
        VBox.setVgrow(generatedScrollPane, Priority.ALWAYS); // Timetable grid takes up most space

        generatedTimetableTab = new Tab("Generated Timetable", tabContent);
        generatedTimetableTab.setClosable(false);
        return generatedTimetableTab;
    }


    //=========================================================================
    // Event Handling and Logic Methods
    //=========================================================================

    /** Handle click after Semester/Subject input validation */
    private void handleSemesterNextClick(GridPane currentSemesterSubjectGrid, int expectedSemCount) {
        semesterList.clear(); allSubjectsObservableList.clear(); // Clear previous data
        boolean isValid = true; String errorMessage = "";
        Map<Integer, Semester> tempSemesters = new HashMap<>(); List<SubjectContext> tempSubjectContexts = new ArrayList<>();

        // --- Basic Semester/Subject Validation ---
        for (int k = 0; k < expectedSemCount; k++) {
            TextField semNameField = (TextField) findNodeInGrid(currentSemesterSubjectGrid, k, 0); VBox subjectInputArea = (VBox) findNodeInGrid(currentSemesterSubjectGrid, k, 1); ComboBox<Integer> subCountBox = null; FlowPane subDetailPane = null; if(subjectInputArea != null) { for(Node n : subjectInputArea.getChildren()){ if(n instanceof ComboBox) subCountBox = (ComboBox<Integer>) n; else if(n instanceof FlowPane) subDetailPane = (FlowPane) n; } } if (semNameField == null || subCountBox == null) { isValid = false; errorMessage = "Internal UI Error finding controls for row " + k; break; } String semName = semNameField.getText().trim(); if (semName.isEmpty()) { isValid = false; errorMessage = "Semester " + (k + 1) + " name empty."; highlightError(semNameField); break; } else { resetHighlight(semNameField); } Integer subCount = subCountBox.getValue(); if (subCount == null) { isValid = false; errorMessage = "Select subject count for '" + semName + "'."; highlightError(subCountBox); break; } else { resetHighlight(subCountBox); } Semester currentSemester = new Semester(semName); tempSemesters.put(k, currentSemester);

            // --- Subject Count vs Time Validation ---
            if (maxWeeklyHoursPerSemester > 0 && MIN_SUBJECT_HOURS > 0 && subCount != null && subCount > 0) { // Check only if count > 0
                int minPossibleWeeklyHours = subCount * MIN_SUBJECT_HOURS;
                if (minPossibleWeeklyHours > maxWeeklyHoursPerSemester) {
                    isValid = false;
                    // FIX: Remove alert, just set error message and highlight
                    errorMessage = String.format("Input Error: Semester '%s' has too many subjects (%d).\nMinimum required hours (%d) exceed the maximum possible weekly hours (%d) based on working time.\nPlease reduce the number of subjects or adjust working times.",
                            semName, subCount, minPossibleWeeklyHours, maxWeeklyHoursPerSemester);
                    highlightError(subCountBox);
                    break; // Stop validation for this semester
                }
            }
            // --- End Subject Count vs Time Validation ---

            if (subDetailPane != null) { int actualSubjectFields = 0; for (Node node : subDetailPane.getChildren()) { if (node instanceof HBox) { actualSubjectFields++; HBox entryBox = (HBox) node; TextField subNameField = null; Spinner<Integer> hoursSpinner = null; for(Node item : entryBox.getChildren()) { if (item instanceof TextField) subNameField = (TextField) item; else if (item instanceof Spinner) hoursSpinner = (Spinner<Integer>) item; } if (subNameField == null || hoursSpinner == null) { isValid = false; errorMessage = "Subject field/spinner missing in HBox for " + semName; break; } String subName = subNameField.getText().trim(); Integer hours = hoursSpinner.getValue(); if (subName.isEmpty()) { isValid = false; errorMessage = "Subject name empty for '" + semName + "'."; highlightError(subNameField); break; } else { resetHighlight(subNameField); Subject subject = new Subject(subName, hours); currentSemester.getSubjects().add(subject); tempSubjectContexts.add(new SubjectContext(subject, currentSemester)); } } } if (!isValid) break; if(actualSubjectFields != subCount) { isValid = false; errorMessage = "Mismatch subject count ("+subCount+") and fields found ("+actualSubjectFields+") for '" + semName + "'."; highlightError(subCountBox); break; }
            } else if (subCount > 0) { isValid = false; errorMessage = "Subject detail pane missing for '" + semName + "'"; break;} if (!isValid) break;
        } // End semester loop

        // --- Process Validation Result ---
        if (isValid) {
            semesterList.clear(); // Ensure list is clear before adding
            semesterList.addAll(tempSemesters.values()); // Add validated semesters

            // --- Validation removed: Auto-adjust handles hour limits ---

            // Proceed if basic validation passed
            tempSubjectContexts.sort(Comparator.comparing((SubjectContext sc) -> sc.getSubject().getName()).thenComparing(sc -> sc.getSemester().getName()));
            allSubjectsObservableList.setAll(tempSubjectContexts); // Update observable list
            showTeacherSection(); // Show next section (Teachers)
            hideGenerateButton(); // Ensure generate button is hidden
            if (teacherCountComboBox.getOnAction() == null) { teacherCountComboBox.setOnAction(f -> populateTeacherMappingGrid(allSubjectsObservableList)); }
            if (teacherCountComboBox.getValue() != null) { populateTeacherMappingGrid(allSubjectsObservableList); }
        } else {
            hideTeacherSection(); hideGenerateButton(); // Hide on error
            // FIX: Only show alert if error message is set (avoid showing for highlight-only errors)
            if (errorMessage != null && !errorMessage.isEmpty()) {
                showErrorAlert("Input Validation Error", errorMessage);
            }
        }
    }

    /** Handle click after Time Configuration input */
    private void handleTimeConfigNextClick() {
        if (validateAndStoreTimeConfig()) { // This now also validates and stores maxTeacherSlotsPerDay
            // Calculate and store net working minutes AND max weekly hours
            Duration workDuration = Duration.between(workStartTime, workEndTime);
            Duration breakDuration = Duration.between(breakStartTime, breakEndTime);
            netWorkingMinutesPerDay = workDuration.minus(breakDuration).toMinutes();
            // Calculate max total weekly hours based on net daily minutes and number of working days
            maxWeeklyHoursPerSemester = (int) (netWorkingMinutesPerDay * WORKING_DAYS.size()) / 60;

            updateWorkingHoursDisplay(); // Ensure label is updated
            System.out.println("DEBUG: Net working minutes/day: " + netWorkingMinutesPerDay + ", Max Weekly Hours/Sem: " + maxWeeklyHoursPerSemester);

            showSemesterSection(); // Show Semesters next
            hideTeacherSection(); hideGenerateButton();

            // FIX: If semester grid is already built, rebuild it to update subject count options
            if (semCountComboBox.getValue() != null) {
                buildSemesterSubjectGrid();
            }

        } else {
            netWorkingMinutesPerDay = 0; maxWeeklyHoursPerSemester = 0; // Reset calculated values
            updateWorkingHoursDisplay(); // Update label to show invalid state
            hideSemesterSection(); hideTeacherSection(); hideGenerateButton();
        }
    }

    /** Validates the time configuration inputs and stores them if valid */
    private boolean validateAndStoreTimeConfig() {
        String errorMsg = ""; Node nodeToHighlight = null;
        try {
            String ws = workStartTimeCombo.getValue(); String we = workEndTimeCombo.getValue();
            String bs = breakStartTimeCombo.getValue(); String be = breakEndTimeCombo.getValue();
            Integer duration = slotDurationCombo.getValue();
            Integer maxSlots = maxTeacherSlotsSpinner.getValue(); // Get value from new spinner

            if (ws == null) { errorMsg = "Select Work Start Time."; nodeToHighlight = workStartTimeCombo; }
            else if (we == null) { errorMsg = "Select Work End Time."; nodeToHighlight = workEndTimeCombo; }
            else if (bs == null) { errorMsg = "Select Break Start Time."; nodeToHighlight = breakStartTimeCombo; }
            else if (be == null) { errorMsg = "Select Break End Time."; nodeToHighlight = breakEndTimeCombo; }
            else if (duration == null) { errorMsg = "Select Slot Duration."; nodeToHighlight = slotDurationCombo; }
            else if (maxSlots == null) { errorMsg = "Select Max Classes/Teacher/Day."; nodeToHighlight = maxTeacherSlotsSpinner; } // Validate new input
            else {
                workStartTime = LocalTime.parse(ws, TIME_FORMATTER); workEndTime = LocalTime.parse(we, TIME_FORMATTER);
                breakStartTime = LocalTime.parse(bs, TIME_FORMATTER); breakEndTime = LocalTime.parse(be, TIME_FORMATTER);
                slotDurationMinutes = duration;
                maxTeacherSlotsPerDay = maxSlots; // Store the value

                if (workEndTime.isBefore(workStartTime) || workEndTime.equals(workStartTime)) { errorMsg = "Work End Time must be after Work Start Time."; nodeToHighlight = workEndTimeCombo; }
                else if (breakEndTime.isBefore(breakStartTime) || breakEndTime.equals(breakStartTime)) { errorMsg = "Break End Time must be after Break Start Time."; nodeToHighlight = breakEndTimeCombo; }
                else if (breakStartTime.isBefore(workStartTime) || breakEndTime.isAfter(workEndTime)) { errorMsg = "Break time must be fully within working hours."; nodeToHighlight = breakStartTimeCombo; }
                else if (Duration.between(workStartTime, workEndTime).toMinutes() < slotDurationMinutes) { errorMsg = "Total working duration is less than a single slot duration."; nodeToHighlight = slotDurationCombo; }
                else if (maxTeacherSlotsPerDay <= 0) { errorMsg = "Max Classes/Teacher/Day must be at least 1."; nodeToHighlight = maxTeacherSlotsSpinner; } // Add validation for new field
            }
        } catch (DateTimeParseException e) { errorMsg = "Invalid time format selected."; nodeToHighlight = workStartTimeCombo; }
        catch (NumberFormatException e) { errorMsg = "Invalid number entered for Max Classes/Teacher/Day."; nodeToHighlight = maxTeacherSlotsSpinner; } // Catch potential spinner edit errors
        catch (Exception e) { errorMsg = "Error validating time settings."; nodeToHighlight = workStartTimeCombo; }

        resetHighlight(workStartTimeCombo); resetHighlight(workEndTimeCombo); resetHighlight(breakStartTimeCombo); resetHighlight(breakEndTimeCombo); resetHighlight(slotDurationCombo); resetHighlight(maxTeacherSlotsSpinner);

        if (!errorMsg.isEmpty()) { if (nodeToHighlight != null) highlightError(nodeToHighlight); showErrorAlert("Time Configuration Error", errorMsg); return false; }
        return true;
    }

    /** Updates the display label for calculated working hours */
    private void updateWorkingHoursDisplay() {
        String wsStr = workStartTimeCombo.getValue(); String weStr = workEndTimeCombo.getValue();
        String bsStr = breakStartTimeCombo.getValue(); String beStr = breakEndTimeCombo.getValue();
        if (wsStr == null || weStr == null || bsStr == null || beStr == null) { workingHoursLabel.setText("Working Hours/Day: --"); netWorkingMinutesPerDay = 0; maxWeeklyHoursPerSemester = 0; return; }
        try { LocalTime ws = LocalTime.parse(wsStr, TIME_FORMATTER); LocalTime we = LocalTime.parse(weStr, TIME_FORMATTER); LocalTime bs = LocalTime.parse(bsStr, TIME_FORMATTER); LocalTime be = LocalTime.parse(beStr, TIME_FORMATTER);
            if (we.isBefore(ws) || be.isBefore(bs) || bs.isBefore(ws) || be.isAfter(we)) { workingHoursLabel.setText("Working Hours/Day: Invalid"); netWorkingMinutesPerDay = 0; maxWeeklyHoursPerSemester = 0; return; }
            Duration workDuration = Duration.between(ws, we); Duration breakDuration = Duration.between(bs, be); Duration netDuration = workDuration.minus(breakDuration); netWorkingMinutesPerDay = netDuration.toMinutes(); maxWeeklyHoursPerSemester = (int) (netWorkingMinutesPerDay * WORKING_DAYS.size()) / 60; long hours = netDuration.toHours(); long minutes = netDuration.toMinutesPart(); workingHoursLabel.setText(String.format("Working Hours/Day: %dh %02dm (%d min)", hours, minutes, netWorkingMinutesPerDay));
        } catch (DateTimeParseException e) { workingHoursLabel.setText("Working Hours/Day: Invalid Time"); netWorkingMinutesPerDay = 0; maxWeeklyHoursPerSemester = 0;}
        catch (Exception e) { workingHoursLabel.setText("Working Hours/Day: Error"); netWorkingMinutesPerDay = 0; maxWeeklyHoursPerSemester = 0; System.err.println("Error calculating working hours: " + e.getMessage()); }
    }

    /** Auto-adjusts hours within a semester if the total exceeds the limit */
    private void adjustHoursWithinSemester(FlowPane subjectDetailPane, Spinner<Integer> changedSpinner) {
        if (isAdjustingHours.getAndSet(true)) { return; } // Prevent recursion

        try {
            if (maxWeeklyHoursPerSemester <= 0) { System.err.println("Cannot adjust hours: Max weekly hours not calculated or invalid."); return; }

            int currentTotalHours = 0;
            List<Spinner<Integer>> allSpinners = new ArrayList<>(); // Collect all spinners
            // Iterate through HBoxes in the FlowPane
            for (Node node : subjectDetailPane.getChildren()) {
                if (node instanceof HBox) {
                    Spinner<Integer> spinner = null;
                    for (Node item : ((HBox) node).getChildren()) { if (item instanceof Spinner) { spinner = (Spinner<Integer>) item; break; } }
                    if (spinner != null) {
                        currentTotalHours += spinner.getValue();
                        allSpinners.add(spinner); // Collect all
                    }
                }
            }

            int overloadHours = currentTotalHours - maxWeeklyHoursPerSemester;
            if (overloadHours > 0) {
                // Sort spinners by value descending, so we reduce larger values first
                allSpinners.sort((s1, s2) -> s2.getValue().compareTo(s1.getValue()));

                int remainingOverload = overloadHours;
                for (Spinner<Integer> spinner : allSpinners) {
                    if (remainingOverload <= 0) break;
                    // Don't reduce the spinner that triggered the event unless necessary
                    if (spinner == changedSpinner && allSpinners.size() > 1) continue;

                    int currentValue = spinner.getValue();
                    int reducibleAmount = currentValue - MIN_SUBJECT_HOURS;
                    if (reducibleAmount > 0) {
                        int reduction = Math.min(remainingOverload, reducibleAmount);
                        spinner.getValueFactory().setValue(currentValue - reduction);
                        remainingOverload -= reduction;
                    }
                }

                // If overload still exists (e.g., all were at minimum or only one spinner),
                // force reduction on the changed spinner (if provided)
                if (remainingOverload > 0 && changedSpinner != null) {
                    int originalValue = changedSpinner.getValue();
                    int cappedValue = Math.max(MIN_SUBJECT_HOURS, originalValue - remainingOverload);
                    if (originalValue != cappedValue) {
                        changedSpinner.getValueFactory().setValue(cappedValue);
                    }
                }
                // Set flag to show alert AFTER the listener finishes
                adjustmentAlertPending = true;
            }
        } finally {
            isAdjustingHours.set(false); // Release the lock
        }
    }


    /** Populates the teacher name and subject mapping grid (teacherMappingGrid) */
    private void populateTeacherMappingGrid(ObservableList<SubjectContext> availableSubjectContexts) {
        // (Logic unchanged - includes fix for button cell and removal of refresh call)
        teacherMappingGrid.getChildren().clear(); teacherList.clear(); globallySelectedSubjectContexts.clear(); hideGenerateButton();
        Integer guruCount = teacherCountComboBox.getValue();
        if (guruCount == null || guruCount <= 0 || availableSubjectContexts.isEmpty()) { if (guruCount != null && guruCount > 0 && availableSubjectContexts.isEmpty()) teacherMappingGrid.add(new Label("No subjects defined to assign."), 0, 0); return; }
        Callback<ListView<SubjectContext>, ListCell<SubjectContext>> cellFactory = lv -> new ListCell<>() { @Override protected void updateItem(SubjectContext item, boolean empty) { super.updateItem(item, empty); if (empty || item == null) { setText(null); setTextFill(Color.BLACK); setStyle(""); setDisable(false); } else { String text = item.toString(); boolean isSelectedGlobally = globallySelectedSubjectContexts.contains(item); SubjectContext currentSelectionInThisComboBox = null; try { if(this.getListView() != null && this.getListView().getSelectionModel() != null) currentSelectionInThisComboBox = this.getListView().getSelectionModel().getSelectedItem(); } catch (Exception e) {} boolean disableCell = isSelectedGlobally && !item.equals(currentSelectionInThisComboBox); if (disableCell) { setText(text + " [Assigned]"); setTextFill(Color.GRAY); setStyle("-fx-background-color: #eeeeee;"); setDisable(true); } else { setText(text); setTextFill(Color.BLACK); setStyle(""); setDisable(false); } } } };
        for (int teacherIndex = 0; teacherIndex < guruCount; teacherIndex++) { TextField teachNameField = new TextField(); teachNameField.setPromptText("Teacher " + (teacherIndex + 1) + " Name"); teachNameField.setId("teachName_" + teacherIndex); ComboBox<Integer> subTeachCountCombo = new ComboBox<>(); subTeachCountCombo.setPromptText("No. of Subjects"); int maxSubjectsPossible = availableSubjectContexts.size(); for (int l = 0; l <= Math.min(maxSubjectsPossible, 15); l++) subTeachCountCombo.getItems().add(l); subTeachCountCombo.setId("teachSubCount_" + teacherIndex); FlowPane tSubMapFlowPane = new FlowPane(Orientation.HORIZONTAL, 5, 5); tSubMapFlowPane.setId("teachSubMapFlowPane_" + teacherIndex); tSubMapFlowPane.setPrefWrapLength(400);
            int finalTeacherIndex = teacherIndex; // Effectively final for lambda
            subTeachCountCombo.setOnAction(p -> { tSubMapFlowPane.getChildren().clear(); rebuildGlobalSubjectSet(teacherMappingGrid); Integer teachSubCountVal = subTeachCountCombo.getValue(); if (teachSubCountVal == null) return; for (int subjectIndex = 0; subjectIndex < teachSubCountVal; subjectIndex++) { ComboBox<SubjectContext> selSubCombo = new ComboBox<>(); selSubCombo.setPromptText("Select Subject " + (subjectIndex + 1)); selSubCombo.setItems(availableSubjectContexts); selSubCombo.setId("selSub_" + finalTeacherIndex + "_" + subjectIndex); selSubCombo.setCellFactory(cellFactory); selSubCombo.setButtonCell(new ListCell<SubjectContext>() { @Override protected void updateItem(SubjectContext item, boolean empty) { super.updateItem(item, empty); setText((empty || item == null) ? selSubCombo.getPromptText() : item.toString()); } }); selSubCombo.valueProperty().addListener((obs, oldValue, newValue) -> { if (oldValue != null) globallySelectedSubjectContexts.remove(oldValue); if (newValue != null) globallySelectedSubjectContexts.add(newValue); checkIfReadyToGenerate(); }); tSubMapFlowPane.getChildren().add(selSubCombo); } checkIfReadyToGenerate(); });
            VBox teacherSubjectInputs = new VBox(5, subTeachCountCombo, tSubMapFlowPane); teacherSubjectInputs.setId("teacherInputsVBox_" + teacherIndex);
            teacherMappingGrid.addRow(teacherIndex, teachNameField, teacherSubjectInputs); GridPane.setHgrow(teachNameField, Priority.SOMETIMES); GridPane.setHgrow(teacherSubjectInputs, Priority.ALWAYS); }
        checkIfReadyToGenerate(); // Initial check
    }

    /** Rebuilds the global set based on current ComboBox selections */
    private void rebuildGlobalSubjectSet(GridPane currentTeacherMappingGrid) {
        // (Logic unchanged)
        globallySelectedSubjectContexts.clear(); if (currentTeacherMappingGrid == null) return;
        for (Node node : currentTeacherMappingGrid.getChildren()) { if (node instanceof VBox && GridPane.getColumnIndex(node) == 1) { for (Node innerNode : ((VBox) node).getChildren()) { if (innerNode instanceof FlowPane) { for (Node cbNode : ((FlowPane) innerNode).getChildren()) { if (cbNode instanceof ComboBox) { @SuppressWarnings("unchecked") ComboBox<SubjectContext> comboBox = (ComboBox<SubjectContext>) cbNode; SubjectContext selected = comboBox.getValue(); if (selected != null) globallySelectedSubjectContexts.add(selected); } } } } } }
    }

    // Removed unused refreshAllComboBoxListViews method

    /** Checks if all teacher inputs seem valid enough to enable Generate button */
    private void checkIfReadyToGenerate() {
        // (Logic unchanged)
        Integer expectedTeacherCount = teacherCountComboBox.getValue(); if (expectedTeacherCount == null || expectedTeacherCount < 0) { hideGenerateButton(); return; } boolean allTeacherFieldsPotentiallyValid = true;
        for (int v = 0; v < expectedTeacherCount; v++) { TextField teachNameField = (TextField) findNodeInGrid(teacherMappingGrid, v, 0); VBox teacherInputsVBox = (VBox) findNodeInGrid(teacherMappingGrid, v, 1); ComboBox<Integer> subTeachCountBox = null; FlowPane tSubMapFlowPane = null; if (teacherInputsVBox != null) { for(Node n : teacherInputsVBox.getChildren()){ if(n instanceof ComboBox) subTeachCountBox = (ComboBox<Integer>) n; else if (n instanceof FlowPane) tSubMapFlowPane = (FlowPane) n; }} else { allTeacherFieldsPotentiallyValid = false; System.err.println("CheckReady: VBox not found for teacher row " + v); break; } if (teachNameField == null || teachNameField.getText().trim().isEmpty()) { allTeacherFieldsPotentiallyValid = false; break; } if (subTeachCountBox == null || subTeachCountBox.getValue() == null) { allTeacherFieldsPotentiallyValid = false; break; } int expectedSubjectCount = subTeachCountBox.getValue(); int actualComboBoxes = 0; boolean anySubjectNotSelected = false; if (tSubMapFlowPane != null) { for (Node node : tSubMapFlowPane.getChildren()) { if (node instanceof ComboBox) { actualComboBoxes++; if (((ComboBox<?>)node).getValue() == null) { anySubjectNotSelected = true; break; } } } } else if (expectedSubjectCount > 0) { allTeacherFieldsPotentiallyValid = false; break; } if (anySubjectNotSelected || actualComboBoxes != expectedSubjectCount) { allTeacherFieldsPotentiallyValid = false; break; } }
        if (allTeacherFieldsPotentiallyValid) { showGenerateButton(); } else { hideGenerateButton(); }
    }


    /** Handle Generate button click - Now uses new algorithm */
    private void handleGenerateClick() {
        System.out.println("DEBUG: Generate button clicked.");
        lastGeneratedTimetable = null; // Clear previous results
        disableExportButtons(); // Disable export buttons initially

        if (!validateAndStoreTimeConfig()) { System.err.println("DEBUG: Time validation failed."); showErrorAlert("Prerequisite Error", "Please fix Time Configuration."); return; }
        System.out.println("DEBUG: Time validation passed.");
        teacherList.clear(); boolean teachersValid = true; String teacherError = ""; Integer expectedTeacherCount = teacherCountComboBox.getValue(); if (expectedTeacherCount == null || expectedTeacherCount <= 0) { System.err.println("DEBUG: Teacher count invalid or zero."); showErrorAlert("Teacher Error", "Please select number of teachers and assign subjects."); return; }
        System.out.println("DEBUG: Expected teacher count: " + expectedTeacherCount); Map<Teacher, List<SubjectContext>> teacherAssignmentsMap = new HashMap<>();
        for (int v = 0; v < expectedTeacherCount; v++) { TextField teachNameField = (TextField) findNodeInGrid(teacherMappingGrid, v, 0); VBox teacherInputsVBox = (VBox) findNodeInGrid(teacherMappingGrid, v, 1); ComboBox<Integer> subTeachCountBox = null; FlowPane tSubMapFlowPane = null; if (teacherInputsVBox != null) { for(Node n : teacherInputsVBox.getChildren()){ if(n instanceof ComboBox) subTeachCountBox = (ComboBox<Integer>) n; else if (n instanceof FlowPane) tSubMapFlowPane = (FlowPane) n; }} if (teachNameField == null || subTeachCountBox == null) { teachersValid = false; teacherError = "UI Error finding controls for Teacher " + (v+1); System.err.println("DEBUG: " + teacherError); break; } String teachName = teachNameField.getText().trim(); if (teachName.isEmpty()) { teachersValid = false; teacherError = "Teacher " + (v+1) + " name empty."; highlightError(teachNameField); System.err.println("DEBUG: " + teacherError); break; } else { resetHighlight(teachNameField); } Integer subTeachCount = subTeachCountBox.getValue(); if (subTeachCount == null) { teachersValid = false; teacherError = "Select subject count for '" + teachName + "'."; highlightError(subTeachCountBox); System.err.println("DEBUG: " + teacherError); break; } else { resetHighlight(subTeachCountBox); } Teacher currentTeacher = new Teacher(teachName); List<SubjectContext> currentAssignments = new ArrayList<>(); int actualComboBoxes = 0;
            if (tSubMapFlowPane != null) { for (Node cbNode : tSubMapFlowPane.getChildren()) { if (cbNode instanceof ComboBox) { actualComboBoxes++; @SuppressWarnings("unchecked") ComboBox<SubjectContext> selSubBox = (ComboBox<SubjectContext>) cbNode; SubjectContext selectedContext = selSubBox.getValue(); if (selectedContext == null) { teachersValid = false; teacherError = "Select Subject for '" + teachName + "'."; highlightError(selSubBox); System.err.println("DEBUG: " + teacherError); break; } else { if (currentAssignments.contains(selectedContext)) { teachersValid = false; teacherError = "Teacher '" + teachName + "' has duplicate: " + selectedContext; highlightError(selSubBox); System.err.println("DEBUG: " + teacherError); break; } currentAssignments.add(selectedContext); currentTeacher.getSubjectsTaught().add(selectedContext.getSubject()); resetHighlight(selSubBox); } } } if (!teachersValid) break; if (actualComboBoxes != subTeachCount) { teachersValid = false; teacherError = "Mismatch between subject count ("+subTeachCount+") and fields ("+actualComboBoxes+") for '" + teachName + "'."; highlightError(subTeachCountBox); System.err.println("DEBUG: " + teacherError); break; }
            } else if (subTeachCount > 0) { teachersValid = false; teacherError = "Subject grid missing for " + teachName; System.err.println("DEBUG: " + teacherError); break; } if (!teachersValid) break; teacherList.add(currentTeacher); teacherAssignmentsMap.put(currentTeacher, currentAssignments); System.out.println("DEBUG: Validated Teacher: " + currentTeacher.getName() + " with " + currentAssignments.size() + " subjects."); } // End loop through teachers
        if (teachersValid) { System.out.println("--- Input Data Ready for Generation ---"); System.out.println("DEBUG: Semesters: " + semesterList.size() + ", Teachers: " + teacherList.size());
            // Pass the configured max slots per teacher
            lastGeneratedTimetable = generateTimetableAlgorithmV2( semesterList, teacherList, teacherAssignmentsMap, workStartTime, workEndTime, breakStartTime, breakEndTime, slotDurationMinutes, WORKING_DAYS, maxTeacherSlotsPerDay );
            System.out.println("DEBUG: Algorithm returned " + (lastGeneratedTimetable == null ? "null" : lastGeneratedTimetable.size() + " entries."));
            if (lastGeneratedTimetable != null) {
                displayTimetableGrid(lastGeneratedTimetable);
                System.out.println("DEBUG: Switched to timetable tab.");
                mainTabPane.getSelectionModel().select(generatedTimetableTab);
                if(lastGeneratedTimetable.isEmpty()){
                    showInfoAlert("Generation Note", "Timetable generated, but no classes could be scheduled with the given constraints.");
                    disableExportButtons(); // Disable if empty
                } else {
                    enableExportButtons(); // Enable if timetable generated
                }
            } else {
                System.err.println("DEBUG: Algorithm returned null list.");
                showErrorAlert("Generation Failed", "The timetable generation algorithm failed unexpectedly.");
                generatedScrollPane.setContent(new Label("Timetable generation failed."));
                disableExportButtons();
            }
        } else {
            System.err.println("DEBUG: Teacher validation failed.");
            showErrorAlert("Teacher Validation Error", teacherError);
            disableExportButtons();
        }
    }


    /**
     * Timetable Generation Algorithm based on user description (V2).
     * Attempts to schedule subjects respecting teacher availability, subject hours, and consecutive constraints.
     */
    private List<TimetableEntry> generateTimetableAlgorithmV2(
            List<Semester> semesters, List<Teacher> teachers,
            Map<Teacher, List<SubjectContext>> teacherAssignments, // Map of teacher to subjects they ARE assigned in UI
            LocalTime workStart, LocalTime workEnd,
            LocalTime breakStart, LocalTime breakEnd,
            int slotMinutes, List<String> days,
            int maxSlotsPerTeacherPerDay) // Added parameter
    {
        System.out.println("DEBUG: Starting Algorithm V2 with Max Teacher Slots/Day: " + maxSlotsPerTeacherPerDay); // Log the value used
        List<TimetableEntry> timetable = new ArrayList<>(); if (semesters.isEmpty() || teachers.isEmpty()) { System.err.println("AlgoV2: Missing semesters or teachers."); return timetable; }
        List<LocalTime> slotStartTimes = calculateTimeSlots(workStart, workEnd, breakStart, breakEnd, slotMinutes); if (slotStartTimes.isEmpty()) { System.err.println("AlgoV2: No valid time slots calculated."); return timetable; }
        Map<Subject, List<Teacher>> subjectToTeacherMap = new HashMap<>(); for (Teacher teacher : teachers) { for (Subject subject : teacher.getSubjectsTaught()) { subjectToTeacherMap.computeIfAbsent(subject, k -> new ArrayList<>()).add(teacher); } }
        Map<SubjectContext, Integer> requiredSlotsMap = new HashMap<>(); for (Semester sem : semesters) { for (Subject sub : sem.getSubjects()) { SubjectContext sc = new SubjectContext(sub, sem); int requiredSlots = (int) Math.ceil((double) sub.getWeeklyHours() * 60.0 / slotMinutes); requiredSlotsMap.put(sc, requiredSlots); } }
        Map<String, Map<LocalTime, Set<Teacher>>> teacherBusyMap = new HashMap<>(); Map<SubjectContext, Integer> scheduledSlotsMap = new HashMap<>(); Map<String, Map<Semester, LinkedList<Subject>>> recentSubjectsMap = new HashMap<>(); Map<String, Map<Teacher, Integer>> teacherDailyLoadMap = new HashMap<>();
        List<Semester> sortedSemesters = semesters.stream().sorted(Comparator.comparing(Semester::getName)).collect(Collectors.toList());
        for (String day : days) { teacherBusyMap.put(day, new HashMap<>()); recentSubjectsMap.put(day, new HashMap<>()); teacherDailyLoadMap.put(day, new HashMap<>()); System.out.println("DEBUG: Processing Day: " + day);
            for (LocalTime slotStart : slotStartTimes) { teacherBusyMap.get(day).computeIfAbsent(slotStart, k -> new HashSet<>());
                for (Semester semester : sortedSemesters) { recentSubjectsMap.get(day).computeIfAbsent(semester, k -> new LinkedList<>()); Set<Teacher> busyTeachersThisSlot = teacherBusyMap.get(day).get(slotStart); LinkedList<Subject> recentSubjects = recentSubjectsMap.get(day).get(semester); Map<Teacher, Integer> dailyLoad = teacherDailyLoadMap.get(day); boolean slotFilledForThisSem = false; List<Subject> subjectsToTry = new ArrayList<>(semester.getSubjects()); Collections.shuffle(subjectsToTry);
                    for (Subject subject : subjectsToTry) { SubjectContext currentSubjectContext = new SubjectContext(subject, semester); int required = requiredSlotsMap.getOrDefault(currentSubjectContext, 0); int scheduled = scheduledSlotsMap.getOrDefault(currentSubjectContext, 0); if (scheduled >= required) continue; if (recentSubjects.size() >= 2 && recentSubjects.get(0) != null && recentSubjects.get(1) != null && recentSubjects.get(0).equals(subject) && recentSubjects.get(1).equals(subject)) continue; List<Teacher> potentialTeachers = subjectToTeacherMap.getOrDefault(subject, Collections.emptyList()); if (potentialTeachers.isEmpty()) continue; Teacher availableTeacher = null; Collections.shuffle(potentialTeachers); for (Teacher teacher : potentialTeachers) {
                        int currentDailySlots = dailyLoad.getOrDefault(teacher, 0);
                        // Use the parameter instead of the constant
                        if (!busyTeachersThisSlot.contains(teacher) && currentDailySlots < maxSlotsPerTeacherPerDay) {
                            availableTeacher = teacher; break;
                        }
                    }
                        if (availableTeacher != null) { LocalTime slotEnd = slotStart.plusMinutes(slotMinutes); if (slotEnd.isAfter(workEnd)) slotEnd = workEnd; timetable.add(new TimetableEntry(day, slotStart, slotEnd, semester, subject, availableTeacher)); busyTeachersThisSlot.add(availableTeacher); scheduledSlotsMap.put(currentSubjectContext, scheduled + 1); recentSubjects.addLast(subject); if (recentSubjects.size() > 2) recentSubjects.removeFirst(); dailyLoad.put(availableTeacher, dailyLoad.getOrDefault(availableTeacher, 0) + 1); slotFilledForThisSem = true; break; }
                    } // End subject loop
                    if (!slotFilledForThisSem) { recentSubjects.addLast(null); if (recentSubjects.size() > 2) recentSubjects.removeFirst(); } // Update recent even if slot empty
                } // End semester loop
            } // End slot loop
        } // End day loop
        printUnmetNeeds(requiredSlotsMap, scheduledSlotsMap); printTeacherLoad(teacherDailyLoadMap); System.out.println("DEBUG: Finished Algorithm V2."); return timetable;
    }

    /** Helper to print subjects that didn't meet required hours */
    private void printUnmetNeeds(Map<SubjectContext, Integer> required, Map<SubjectContext, Integer> scheduled) {
        // (Logic unchanged)
        System.out.println("--- Scheduling Needs Report ---"); boolean allMet = true; for (Map.Entry<SubjectContext, Integer> entry : required.entrySet()) { SubjectContext sc = entry.getKey(); int reqSlots = entry.getValue(); int schedSlots = scheduled.getOrDefault(sc, 0); if (schedSlots < reqSlots) { allMet = false; System.out.println(" - Subject: " + sc.toString() + " | Required Slots: " + reqSlots + " | Scheduled Slots: " + schedSlots); } } if (allMet) System.out.println("All subject hour requirements appear to be met by the schedule."); else System.out.println("Note: Some subject hour requirements were not fully met."); System.out.println("-----------------------------");
    }

    /** Helper to print teacher load per day */
    private void printTeacherLoad(Map<String, Map<Teacher, Integer>> dailyLoadMap) {
        // (Logic unchanged)
        System.out.println("--- Teacher Daily Load Report ---"); for (String day : WORKING_DAYS) { System.out.println(" " + day + ":"); Map<Teacher, Integer> load = dailyLoadMap.getOrDefault(day, Collections.emptyMap()); if (load.isEmpty()) { System.out.println("    No teachers scheduled."); continue; } for (Map.Entry<Teacher, Integer> entry : load.entrySet()) { System.out.println("    - " + entry.getKey().getName() + ": " + entry.getValue() + " slots"); } } System.out.println("-------------------------------");
    }


    /** Calculates the start times of valid time slots */
    private List<LocalTime> calculateTimeSlots(LocalTime workStart, LocalTime workEnd, LocalTime breakStart, LocalTime breakEnd, int slotMinutes) {
        // (Calculation logic unchanged from user's code)
        List<LocalTime> slots = new ArrayList<>(); LocalTime current = workStart; while (current.isBefore(workEnd)) { LocalTime slotEnd = current.plusMinutes(slotMinutes); if (slotEnd.isAfter(workEnd)) break; boolean isDuringBreak = !current.isBefore(breakStart) && !slotEnd.isAfter(breakEnd); if (!isDuringBreak) slots.add(current); current = slotEnd; } return slots;
    }


    /** Displays the timetable in a Grid Format with Semesters as sub-rows */
    private void displayTimetableGrid(List<TimetableEntry> timetable) {
        System.out.println("DEBUG: Starting displayTimetableGrid...");
        timetableDisplayGrid.getChildren().clear(); timetableDisplayGrid.getColumnConstraints().clear(); timetableDisplayGrid.getRowConstraints().clear(); timetableDisplayGrid.setGridLinesVisible(true);

        if (timetable.isEmpty() && !configLoadedFromFile && semesterList.isEmpty()) { generatedScrollPane.setContent(new Label("Please provide input configuration first.")); disableExportButtons(); return; }
        else if (timetable.isEmpty()) { generatedScrollPane.setContent(new Label("No timetable entries generated based on input/constraints.")); disableExportButtons(); return; }
        else { generatedScrollPane.setContent(timetableDisplayGrid); enableExportButtons();} // Enable export if grid is populated

        Map<String, Map<Semester, Map<LocalTime, TimetableEntry>>> groupedEntries = timetable.stream().collect(Collectors.groupingBy(TimetableEntry::getDay, Collectors.groupingBy(TimetableEntry::getSemester, Collectors.toMap(TimetableEntry::getStartTime, entry -> entry, (e1, e2) -> e1))));
        // Get only the valid CLASS slots for display columns
        List<LocalTime> classSlots = calculateTimeSlots(workStartTime, workEndTime, breakStartTime, breakEndTime, slotDurationMinutes);
        if (classSlots.isEmpty()) { generatedScrollPane.setContent(new Label("No valid class time slots defined.")); disableExportButtons(); return; }

        // Determine break column index relative to class slots
        int breakColumnIndex = -1;
        for (int i = 0; i < classSlots.size(); i++) {
            if (!classSlots.get(i).isBefore(breakStartTime)) {
                breakColumnIndex = i + 1; // +1 because column 0 is Day/Sem
                break;
            }
        }
        // If break is after all classes, put it at the end
        if (breakColumnIndex == -1 && breakStartTime != null && !breakStartTime.isAfter(workEndTime)) {
            breakColumnIndex = classSlots.size() + 1;
        }

        // --- Header Row ---
        Label topLeft = createHeaderLabel("Day / Sem", 12);
        GridPane.setHgrow(topLeft, Priority.NEVER);
        timetableDisplayGrid.add(topLeft, 0, 0);

        int currentHeaderCol = 1;
        for (int i = 0; i < classSlots.size(); i++) {
            // Insert break header if this is the correct index
            if (currentHeaderCol == breakColumnIndex) {
                Label breakHeader = createHeaderLabel("BREAK", 11);
                breakHeader.setStyle("-fx-background-color: #e0e0e0; -fx-border-color: #aaaaaa; -fx-border-width: 0.5px; -fx-font-weight: bold;");
                GridPane.setHgrow(breakHeader, Priority.SOMETIMES);
                timetableDisplayGrid.add(breakHeader, currentHeaderCol++, 0);
            }
            // Add class slot header
            LocalTime slotStart = classSlots.get(i);
            LocalTime slotEnd = slotStart.plusMinutes(slotDurationMinutes);
            String headerText = slotStart.format(TIME_FORMATTER) + "-\n" + slotEnd.format(TIME_FORMATTER);
            Label timeHeader = createHeaderLabel(headerText, 11);
            GridPane.setHgrow(timeHeader, Priority.ALWAYS);
            timetableDisplayGrid.add(timeHeader, currentHeaderCol++, 0);
        }
        // Add break header if it goes after all class slots
        if (breakColumnIndex == classSlots.size() + 1) {
            Label breakHeader = createHeaderLabel("BREAK", 11);
            breakHeader.setStyle("-fx-background-color: #e0e0e0; -fx-border-color: #aaaaaa; -fx-border-width: 0.5px; -fx-font-weight: bold;");
            GridPane.setHgrow(breakHeader, Priority.SOMETIMES);
            timetableDisplayGrid.add(breakHeader, currentHeaderCol++, 0);
        }
        int totalColumns = currentHeaderCol; // Total number of columns added

        // --- Data Rows ---
        int currentGridRow = 1;
        List<Semester> sortedSemesters = semesterList.stream().sorted(Comparator.comparing(Semester::getName)).collect(Collectors.toList());

        for (String day : WORKING_DAYS) {
            Label dayLabel = createDayHeaderLabel(day);
            timetableDisplayGrid.add(dayLabel, 0, currentGridRow, totalColumns, 1); // Span all columns
            currentGridRow++;
            Map<Semester, Map<LocalTime, TimetableEntry>> dayEntriesBySemester = groupedEntries.getOrDefault(day, Collections.emptyMap());

            if (sortedSemesters.isEmpty()) {
                Label noSemLabel = new Label("No Semesters Defined"); noSemLabel.setPadding(new Insets(5));
                timetableDisplayGrid.add(noSemLabel, 0, currentGridRow, totalColumns, 1);
                currentGridRow++;
            } else {
                for (Semester semester : sortedSemesters) {
                    Label semLabel = createSemesterHeaderLabel(semester.getName());
                    timetableDisplayGrid.add(semLabel, 0, currentGridRow);
                    Map<LocalTime, TimetableEntry> semesterDayEntries = dayEntriesBySemester.getOrDefault(semester, Collections.emptyMap());

                    int currentDataCol = 1;
                    for (int i = 0; i < classSlots.size(); i++) {
                        // Insert break cell if this is the correct index
                        if (currentDataCol == breakColumnIndex) {
                            Label breakCell = createBreakCell();
                            timetableDisplayGrid.add(breakCell, currentDataCol++, currentGridRow);
                        }
                        // Add class slot cell
                        LocalTime slotStart = classSlots.get(i);
                        TimetableEntry entry = semesterDayEntries.get(slotStart);
                        Node cellContent;
                        if (entry != null) {
                            VBox cellBox = new VBox(1); cellBox.setAlignment(Pos.CENTER);
                            Label subjectLabel = new Label(entry.getSubject().getName()); subjectLabel.setFont(Font.font("System", FontWeight.BOLD, 10));
                            Label teacherLabel = new Label("(" + entry.getTeacher().getName() + ")"); teacherLabel.setFont(Font.font("System", Font.getDefault().getSize() * 0.85));
                            cellBox.getChildren().addAll(subjectLabel, teacherLabel);
                            cellBox.setStyle("-fx-background-color: white; -fx-border-color: #cccccc; -fx-border-width: 0.5px; -fx-padding: 2px;");
                            cellContent = cellBox;
                        } else {
                            Pane emptyPane = new Pane(); emptyPane.setMinHeight(35);
                            emptyPane.setStyle("-fx-background-color: #f9f9f9; -fx-border-color: #cccccc; -fx-border-width: 0.5px;");
                            cellContent = emptyPane;
                        }
                        timetableDisplayGrid.add(cellContent, currentDataCol++, currentGridRow);
                        GridPane.setVgrow(cellContent, Priority.SOMETIMES);
                        GridPane.setHgrow(cellContent, Priority.ALWAYS);
                    }
                    // Add break cell if it goes after all class slots
                    if (breakColumnIndex == classSlots.size() + 1) {
                        Label breakCell = createBreakCell();
                        timetableDisplayGrid.add(breakCell, currentDataCol++, currentGridRow);
                    }
                    currentGridRow++;
                }
            }
        } // End day loop

        // --- Column Constraints ---
        timetableDisplayGrid.getColumnConstraints().clear();
        ColumnConstraints colDaySem = new ColumnConstraints(); colDaySem.setPrefWidth(120); colDaySem.setMinWidth(100); colDaySem.setHgrow(Priority.NEVER);
        timetableDisplayGrid.getColumnConstraints().add(colDaySem); // Column 0

        int numDataCols = totalColumns - 1; // Number of time/break columns
        if (numDataCols > 0) {
            // Use Hgrow instead of percent width for better flexibility
            for (int j = 0; j < numDataCols; j++) {
                ColumnConstraints colJ = new ColumnConstraints();
                colJ.setHgrow(Priority.ALWAYS); // Let columns grow
                colJ.setMinWidth(60); // Maintain minimum width
                timetableDisplayGrid.getColumnConstraints().add(colJ);
            }
        }

        Platform.runLater(() -> generatedScrollPane.requestLayout()); // Ensure layout update
        System.out.println("DEBUG: Finished displayTimetableGrid.");
    }


    // --- Helper Methods for Header Styling ---
    private Label createHeaderLabel(String text, double fontSize) { Label label = new Label(text); label.setFont(Font.font("System", FontWeight.BOLD, fontSize)); label.setAlignment(Pos.CENTER); label.setMaxWidth(Double.MAX_VALUE); label.setMinWidth(50); label.setPrefHeight(40); label.setPadding(new Insets(5)); label.setStyle("-fx-background-color: #cce0ff; -fx-border-color: #aaaaaa; -fx-border-width: 0.5px;"); label.setTextAlignment(javafx.scene.text.TextAlignment.CENTER); return label; }
    private Label createDayHeaderLabel(String text) { Label label = new Label(text); label.setFont(Font.font("System", FontWeight.BOLD, 14)); label.setAlignment(Pos.CENTER_LEFT); label.setMaxWidth(Double.MAX_VALUE); label.setPadding(new Insets(6, 10, 6, 10)); label.setStyle("-fx-background-color: #aaccff; -fx-border-color: #8888aa; -fx-border-width: 1px 0 1px 0;"); return label; }
    private Label createSemesterHeaderLabel(String text) { Label label = new Label(text); label.setFont(Font.font("System", FontWeight.NORMAL, 11)); label.setAlignment(Pos.CENTER_RIGHT); label.setMaxWidth(Double.MAX_VALUE); label.setPadding(new Insets(5, 8, 5, 5)); label.setStyle("-fx-background-color: #e6f0ff; -fx-border-color: #cccccc; -fx-border-width: 0 0.5px 0.5px 0.5px;"); label.setWrapText(true); return label; }
    // New helper for break cells
    private Label createBreakCell() {
        Label label = new Label("BREAK");
        label.setFont(Font.font("System", FontWeight.BOLD, 10));
        label.setAlignment(Pos.CENTER);
        label.setMaxWidth(Double.MAX_VALUE);
        label.setMinHeight(35); // Match empty pane height
        label.setStyle("-fx-background-color: #e0e0e0; -fx-border-color: #cccccc; -fx-border-width: 0.5px; -fx-text-fill: #555555;");
        GridPane.setVgrow(label, Priority.SOMETIMES);
        GridPane.setHgrow(label, Priority.ALWAYS);
        return label;
    }


    // --- Visibility Control Methods ---
    private void hideTimeConfigSection() { Node node = findNodeInParent(mainContentVBox, "timeConfigBox"); if(node!=null){node.setVisible(false);node.setManaged(false);}}
    private void showTimeConfigSection() { Node node = findNodeInParent(mainContentVBox, "timeConfigBox"); if(node!=null){node.setVisible(true); node.setManaged(true); }}
    private void hideSemesterSection() { Node node = findNodeInParent(mainContentVBox, "semesterSectionBox"); if(node!=null){node.setVisible(false);node.setManaged(false);}}
    private void showSemesterSection() { Node node = findNodeInParent(mainContentVBox, "semesterSectionBox"); if(node!=null){node.setVisible(true); node.setManaged(true); }}
    private void hideTeacherSection() { if(teacherMapTitle != null) teacherMapTitle.setVisible(false); if(teacherSectionTitle != null) teacherSectionTitle.setVisible(false); if(teacherCountComboBox != null) teacherCountComboBox.setVisible(false); if(teacherMappingGrid != null) { teacherMappingGrid.setVisible(false); teacherMappingGrid.setManaged(false); } hideGenerateButton(); }
    private void showTeacherSection() { if(teacherMapTitle != null) teacherMapTitle.setVisible(true); if(teacherSectionTitle != null) teacherSectionTitle.setVisible(true); if(teacherCountComboBox != null) teacherCountComboBox.setVisible(true); if(teacherMappingGrid != null) { teacherMappingGrid.setVisible(true); teacherMappingGrid.setManaged(true); } }
    private void hideGenerateButton() { if(generateButton != null) { generateButton.setVisible(false); generateButton.setManaged(false); generateButton.setDisable(true); }}
    private void showGenerateButton() { if(generateButton != null) { generateButton.setVisible(true); generateButton.setManaged(true); generateButton.setDisable(false); } }
    // New methods for export buttons
    private void disableExportButtons() {
        if(exportExcelButton != null) exportExcelButton.setDisable(true);
        if(exportPdfButton != null) exportPdfButton.setDisable(true);
    }

    private void enableExportButtons() {
        if(exportExcelButton != null) exportExcelButton.setDisable(false);
        if(exportPdfButton != null) exportPdfButton.setDisable(false);
    }


    // --- Utility Methods ---
    private Node findNodeInParent(Pane parent, String id) { if(parent == null || id == null) return null; for (Node node : parent.getChildrenUnmodifiable()) { if (id.equals(node.getId())) return node; if (node instanceof Pane) { Node found = findNodeInParent((Pane) node, id); if (found != null) return found; } } return null; }
    private Node findNodeById(GridPane parent, String id) { return findNodeInParent(parent, id); }
    private void showErrorAlert(String title, String message) { Alert alert = new Alert(Alert.AlertType.WARNING); alert.setTitle(title); alert.setHeaderText(null); alert.setContentText(message); Stage alertStage = (Stage) alert.getDialogPane().getScene().getWindow(); loadStageIcon(alertStage); DialogPane dialogPane = alert.getDialogPane(); Platform.runLater(() -> { Label contentLabel = (Label)dialogPane.lookup(".content.label"); if(contentLabel != null) { contentLabel.setStyle("-fx-font-size: 14px;-fx-text-fill: #d32f2f;-fx-alignment: center-left;-fx-wrap-text: true;"); }}); dialogPane.setPrefWidth(400); alert.showAndWait(); }
    private void showInfoAlert(String title, String content) { Alert alert = new Alert(Alert.AlertType.INFORMATION); alert.setTitle(title); alert.setHeaderText(null); alert.setContentText(content); alert.showAndWait(); }
    private void highlightError(Node node) { if(node!=null) node.setStyle("-fx-border-color: red; -fx-border-width: 1.5px; -fx-border-radius: 3px;"); }
    private void resetHighlight(Node node) { if(node!=null) node.setStyle(""); }
    private void loadStageIcon(Stage stage) { if(stage == null) return; try (InputStream iconStream = getClass().getResourceAsStream("/icon.png")) { if (iconStream != null) stage.getIcons().add(new Image(iconStream)); else System.err.println("Warning: icon.png not found in classpath root."); } catch (Exception e) { System.err.println("Error loading icon: " + e.getMessage());}}
    private void setupTabPaneSelectionStyle(TabPane tabPane) { tabPane.getSelectionModel().selectedItemProperty().addListener((obs, oldTab, newTab) -> { if (oldTab != null) oldTab.setStyle(""); if (newTab != null) newTab.setStyle("-fx-border-color: #3c7fb1; -fx-border-width: 2 2 0 2; -fx-border-insets: 0 0 -2 0;"); }); Platform.runLater(() -> { Tab selected = tabPane.getSelectionModel().getSelectedItem(); if (selected != null) selected.setStyle("-fx-border-color: #3c7fb1; -fx-border-width: 2 2 0 2; -fx-border-insets: 0 0 -2 0;"); }); }
    private Node findNodeInGrid(GridPane gridPane, int row, int col) { if(gridPane == null) return null; for (Node node : gridPane.getChildrenUnmodifiable()) { Integer r = GridPane.getRowIndex(node); Integer c = GridPane.getColumnIndex(node); if (r != null && c != null && r == row && c == col) return node; } return null; }

    // --- Export Methods ---

    /** Handles Export to Excel action */
    private void handleExportExcel() {
        if (lastGeneratedTimetable == null || lastGeneratedTimetable.isEmpty()) { showInfoAlert("Export Info", "No timetable data available to export."); return; }
        FileChooser fileChooser = new FileChooser(); fileChooser.setTitle("Save Timetable as Excel"); fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Excel Workbook (*.xlsx)", "*.xlsx")); fileChooser.setInitialFileName("timetable.xlsx"); File file = fileChooser.showSaveDialog(mainTabPane.getScene().getWindow());
        if (file == null) return;

        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("Timetable");
            // --- Populate Excel Sheet (Mirroring displayTimetableGrid) ---
            List<LocalTime> classSlots = calculateTimeSlots(workStartTime, workEndTime, breakStartTime, breakEndTime, slotDurationMinutes);
            if (classSlots.isEmpty()) { showErrorAlert("Export Error", "No valid class time slots found for export."); return; }

            // Determine break column index
            int breakColumnIndex = -1;
            for (int i = 0; i < classSlots.size(); i++) { if (!classSlots.get(i).isBefore(breakStartTime)) { breakColumnIndex = i + 1; break; } }
            if (breakColumnIndex == -1 && breakStartTime != null && !breakStartTime.isAfter(workEndTime)) { breakColumnIndex = classSlots.size() + 1; }

            // Styles
            CellStyle headerStyle = createExcelHeaderStyle(workbook);
            CellStyle dayHeaderStyle = createExcelDayHeaderStyle(workbook);
            CellStyle semesterHeaderStyle = createExcelSemesterHeaderStyle(workbook);
            CellStyle breakStyle = createExcelBreakStyle(workbook);
            CellStyle entryStyle = createExcelEntryStyle(workbook);
            CellStyle emptyStyle = createExcelEmptyStyle(workbook);

            // Header Row
            Row headerRow = sheet.createRow(0);
            Cell topLeftCell = headerRow.createCell(0); topLeftCell.setCellValue("Day / Sem"); topLeftCell.setCellStyle(headerStyle);
            int excelCol = 1;
            for (int i = 0; i < classSlots.size(); i++) {
                if (excelCol == breakColumnIndex) { Cell cell = headerRow.createCell(excelCol++); cell.setCellValue("BREAK"); cell.setCellStyle(headerStyle); }
                LocalTime slotStart = classSlots.get(i); LocalTime slotEnd = slotStart.plusMinutes(slotDurationMinutes); Cell cell = headerRow.createCell(excelCol++); cell.setCellValue(slotStart.format(TIME_FORMATTER) + "-" + slotEnd.format(TIME_FORMATTER)); cell.setCellStyle(headerStyle);
            }
            if (breakColumnIndex == classSlots.size() + 1) { Cell cell = headerRow.createCell(excelCol++); cell.setCellValue("BREAK"); cell.setCellStyle(headerStyle); }
            int totalExcelCols = excelCol;

            // Data Rows
            int excelRow = 1;
            List<Semester> sortedSemesters = semesterList.stream().sorted(Comparator.comparing(Semester::getName)).collect(Collectors.toList());
            Map<String, Map<Semester, Map<LocalTime, TimetableEntry>>> groupedEntries = lastGeneratedTimetable.stream().collect(Collectors.groupingBy(TimetableEntry::getDay, Collectors.groupingBy(TimetableEntry::getSemester, Collectors.toMap(TimetableEntry::getStartTime, entry -> entry, (e1, e2) -> e1))));

            for (String day : WORKING_DAYS) {
                Row dayRow = sheet.createRow(excelRow++); Cell dayCell = dayRow.createCell(0); dayCell.setCellValue(day); dayCell.setCellStyle(dayHeaderStyle);
                sheet.addMergedRegion(new CellRangeAddress(excelRow - 1, excelRow - 1, 0, totalExcelCols - 1)); // Merge day cell

                for (Semester semester : sortedSemesters) {
                    Row semRow = sheet.createRow(excelRow++); Cell semCell = semRow.createCell(0); semCell.setCellValue(semester.getName()); semCell.setCellStyle(semesterHeaderStyle);
                    Map<LocalTime, TimetableEntry> semesterDayEntries = groupedEntries.getOrDefault(day, Collections.emptyMap()).getOrDefault(semester, Collections.emptyMap());
                    excelCol = 1;
                    for (int i = 0; i < classSlots.size(); i++) {
                        if (excelCol == breakColumnIndex) { Cell cell = semRow.createCell(excelCol++); cell.setCellValue("BREAK"); cell.setCellStyle(breakStyle); }
                        LocalTime slotStart = classSlots.get(i); Cell cell = semRow.createCell(excelCol++); TimetableEntry entry = semesterDayEntries.get(slotStart); if (entry != null) { cell.setCellValue(entry.getSubject().getName() + "\n(" + entry.getTeacher().getName() + ")"); cell.setCellStyle(entryStyle); } else { cell.setCellStyle(emptyStyle); }
                    }
                    if (breakColumnIndex == classSlots.size() + 1) { Cell cell = semRow.createCell(excelCol++); cell.setCellValue("BREAK"); cell.setCellStyle(breakStyle); }
                }
            }

            // Auto-size columns (adjust as needed)
            for (int i = 0; i < totalExcelCols; i++) { sheet.autoSizeColumn(i); }
            sheet.setColumnWidth(0, 30 * 256); // Wider first column

            try (FileOutputStream fileOut = new FileOutputStream(file)) { workbook.write(fileOut); showInfoAlert("Export Successful", "Timetable exported to:\n" + file.getName()); }
        } catch (IOException e) { System.err.println("Error exporting to Excel: " + e.getMessage()); e.printStackTrace(); showErrorAlert("Export Error", "Could not export timetable to Excel.\nError: " + e.getMessage()); }
        catch (Exception e) { System.err.println("Unexpected error during Excel export: " + e.getMessage()); e.printStackTrace(); showErrorAlert("Export Error", "An unexpected error occurred during Excel export."); }
    }
    /**
     * Handles Export to PDF action
     */
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
        if (file == null) {
            return;
        }

        try {
            // --- Setup PDF Document ---
            PdfWriter writer = new PdfWriter(file);
            PdfDocument pdf = new PdfDocument(writer);
            // Use Landscape for potentially wide timetables
            Document document = new Document(pdf, PageSize.A4.rotate());
            document.setMargins(20, 20, 20, 20); // Top, Right, Bottom, Left

            // --- Create PDF Table ---
            Table pdfTable = createPdfTimetableTable(lastGeneratedTimetable);
            document.add(pdfTable);

            document.close(); // Saves and closes the document/writer

            showInfoAlert("Export Successful", "Timetable exported to:\n" + file.getName());

        } catch (FileNotFoundException e) {
            System.err.println("Error exporting to PDF (File Not Found): " + e.getMessage());
            showErrorAlert("Export Error", "Could not create PDF file (permission issue or invalid path?).\nError: " + e.getMessage());
        } catch (IOException e) {
            System.err.println("Error exporting to PDF (IO): " + e.getMessage());
            e.printStackTrace();
            showErrorAlert("Export Error", "Could not export timetable to PDF.\nIO Error: " + e.getMessage());
        } catch (Exception e) {
            System.err.println("Unexpected error during PDF export: " + e.getMessage());
            e.printStackTrace();
            showErrorAlert("Export Error", "An unexpected error occurred during PDF export.");
        }
    }
    // Helper methods for Excel Styles
    private CellStyle createExcelHeaderStyle(Workbook wb) { CellStyle style = wb.createCellStyle(); org.apache.poi.ss.usermodel.Font font = wb.createFont(); font.setBold(true); font.setFontHeightInPoints((short) 10); style.setFont(font); style.setAlignment(HorizontalAlignment.CENTER); style.setVerticalAlignment(CENTER); style.setFillForegroundColor(IndexedColors.LIGHT_CORNFLOWER_BLUE.getIndex()); style.setFillPattern(FillPatternType.SOLID_FOREGROUND); style.setBorderTop(BorderStyle.THIN); style.setBorderBottom(BorderStyle.THIN); style.setBorderLeft(BorderStyle.THIN); style.setBorderRight(BorderStyle.THIN); style.setWrapText(true); return style; }
    private CellStyle createExcelDayHeaderStyle(Workbook wb) { CellStyle style = wb.createCellStyle(); org.apache.poi.ss.usermodel.Font font = wb.createFont(); font.setBold(true); font.setFontHeightInPoints((short) 12); style.setFont(font); style.setAlignment(HorizontalAlignment.LEFT); style.setVerticalAlignment(CENTER); style.setFillForegroundColor(IndexedColors.PALE_BLUE.getIndex()); style.setFillPattern(FillPatternType.SOLID_FOREGROUND); style.setBorderTop(BorderStyle.MEDIUM); style.setBorderBottom(BorderStyle.MEDIUM); return style; }
    private CellStyle createExcelSemesterHeaderStyle(Workbook wb) { CellStyle style = wb.createCellStyle(); org.apache.poi.ss.usermodel.Font font = wb.createFont(); ((org.apache.poi.ss.usermodel.Font) font).setFontHeightInPoints((short) 9); style.setFont(font); style.setAlignment(HorizontalAlignment.RIGHT); style.setVerticalAlignment(CENTER); style.setFillForegroundColor(IndexedColors.LIGHT_TURQUOISE.getIndex()); style.setFillPattern(FillPatternType.SOLID_FOREGROUND); style.setBorderBottom(BorderStyle.THIN); style.setBorderLeft(BorderStyle.THIN); style.setBorderRight(BorderStyle.THIN); style.setWrapText(true); return style; }
    private CellStyle createExcelBreakStyle(Workbook wb) { CellStyle style = wb.createCellStyle(); org.apache.poi.ss.usermodel.Font font = wb.createFont(); ((org.apache.poi.ss.usermodel.Font) font).setBold(true); font.setFontHeightInPoints((short) 9); style.setFont(font); style.setAlignment(HorizontalAlignment.CENTER); style.setVerticalAlignment(CENTER); style.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex()); style.setFillPattern(FillPatternType.SOLID_FOREGROUND); style.setBorderTop(BorderStyle.THIN); style.setBorderBottom(BorderStyle.THIN); style.setBorderLeft(BorderStyle.THIN); style.setBorderRight(BorderStyle.THIN); return style; }
    private CellStyle createExcelEntryStyle(Workbook wb) { CellStyle style = wb.createCellStyle(); org.apache.poi.ss.usermodel.Font font = wb.createFont(); ((org.apache.poi.ss.usermodel.Font) font).setFontHeightInPoints((short) 9); style.setFont(font); style.setAlignment(HorizontalAlignment.CENTER); style.setVerticalAlignment(CENTER); style.setBorderTop(BorderStyle.THIN); style.setBorderBottom(BorderStyle.THIN); style.setBorderLeft(BorderStyle.THIN); style.setBorderRight(BorderStyle.THIN); style.setWrapText(true); return style; }
    private CellStyle createExcelEmptyStyle(Workbook wb) { CellStyle style = wb.createCellStyle(); style.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex()); style.setFillPattern(FillPatternType.SOLID_FOREGROUND); style.setBorderTop(BorderStyle.THIN); style.setBorderBottom(BorderStyle.THIN); style.setBorderLeft(BorderStyle.THIN); style.setBorderRight(BorderStyle.THIN); return style; }

    // Removed handleExportPdf and drawPdfCell methods


    // --- Load/Save Placeholders (Adapted) ---
    /** Handles Load Configuration action - Reads raw JSON data and enables Generate button. */
    private void handleLoadConfiguration(Stage stage) {
        FileChooser fileChooser = new FileChooser(); fileChooser.setTitle("Load Configuration File"); fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("JSON Files", "*.json")); File file = fileChooser.showOpenDialog(stage);
        if (file != null) { try { loadedJsonData = Files.readString(Paths.get(file.getAbsolutePath())); configLoadedFromFile = true; System.out.println("Successfully loaded configuration file: " + file.getAbsolutePath()); generateButton.setDisable(false); hideTimeConfigSection(); hideSemesterSection(); hideTeacherSection(); showInfoAlert("Load Configuration", "Configuration data loaded.\nGenerate Timetable button is now enabled.\n(UI not updated with loaded values)"); } catch (IOException e) { System.err.println("Error reading configuration file: " + e.getMessage()); showErrorAlert("Load Error", "Error reading configuration file:\n" + e.getMessage()); resetLoadedState(); } catch (Exception e) { System.err.println("An unexpected error occurred during file load: " + e.getMessage()); showErrorAlert("Load Error", "An unexpected error occurred during file load."); resetLoadedState(); } }
    }
    /** Resets the loaded configuration state */
    private void resetLoadedState() { loadedJsonData = null; configLoadedFromFile = false; generateButton.setDisable(true); }
    /** Handles Save Configuration action - Gathers data from UI and Writes manually constructed JSON to file. */
    private void handleSaveConfiguration(Stage stage) {
        System.out.println("--- Gathering Configuration for Save ---");
        List<SemesterConfig> semesters = gatherSemesterDataFromUI(); List<TeacherConfig> teachers = gatherTeacherDataFromUI(); TimeConfiguration timeConfig = gatherTimeConfigDataFromUI();
        if (semesters.isEmpty() || teachers.isEmpty() || timeConfig == null) { showErrorAlert("Save Error", "Cannot save - incomplete configuration data gathered from UI. Please ensure all steps are filled and validated."); return; }
        Map<String, Object> fullConfig = new LinkedHashMap<>(); fullConfig.put("semesters", semesters.stream().map(SemesterConfig::toMap).collect(Collectors.toList())); fullConfig.put("teachers", teachers.stream().map(TeacherConfig::toMap).collect(Collectors.toList())); fullConfig.put("timeSettings", timeConfig.toMap());
        String jsonOutput = convertMapToJson(fullConfig); // Use manual JSON writer
        FileChooser fileChooser = new FileChooser(); fileChooser.setTitle("Save Configuration File"); fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("JSON Files", "*.json")); fileChooser.setInitialFileName("classmesh_config.json");
        File file = fileChooser.showSaveDialog(stage);
        if (file != null) { String filePath = file.getAbsolutePath(); if (!filePath.toLowerCase().endsWith(".json")) file = new File(filePath + ".json"); System.out.println("Save configuration to: " + file.getAbsolutePath()); try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) { writer.write(jsonOutput); showInfoAlert("Save Configuration", "Configuration saved successfully to:\n" + file.getName()); } catch (IOException e) { System.err.println("Error saving configuration file: " + e.getMessage()); showErrorAlert("Save Error","Error saving configuration file:\n" + e.getMessage()); } }
    }
    // --- Adapted Data Gathering for Save ---
    private List<SemesterConfig> gatherSemesterDataFromUI() {
        List<SemesterConfig> semesters = new ArrayList<>();
        if (semesterList != null && !semesterList.isEmpty()) { for(Semester sem : semesterList) { List<SubjectDetail> details = sem.getSubjects().stream().map(sub -> new SubjectDetail(sub.getName(), sub.getWeeklyHours())).collect(Collectors.toList()); semesters.add(new SemesterConfig(sem.getName(), details)); } }
        else { System.err.println("Save Warning: Semester list empty, cannot gather semester data for save."); } return semesters;
    }
    private List<TeacherConfig> gatherTeacherDataFromUI() {
        List<TeacherConfig> teachers = new ArrayList<>();
        if (teacherList != null && !teacherList.isEmpty()){ for(Teacher teacher : teacherList) { List<TeacherAssignment> assignments = new ArrayList<>(); int teacherIndex = teacherList.indexOf(teacher); FlowPane currentTeacherSubFlow = null; VBox teacherInputsVBox = (VBox) findNodeInGrid(teacherMappingGrid, teacherIndex, 1); if(teacherInputsVBox != null) { for(Node n : teacherInputsVBox.getChildren()){ if(n instanceof FlowPane) currentTeacherSubFlow = (FlowPane) n;} } if (currentTeacherSubFlow != null) { for (Node subSelectNode : currentTeacherSubFlow.getChildren()) { if (subSelectNode instanceof ComboBox) { @SuppressWarnings("unchecked") ComboBox<SubjectContext> subjectSelectCombo = (ComboBox<SubjectContext>) subSelectNode; SubjectContext selected = subjectSelectCombo.getValue(); if (selected != null) { assignments.add(new TeacherAssignment(selected.getSubject().getName(), selected.getSemester().getName())); } } } } else { System.err.println("Save Warning: Could not find subject selection UI for teacher " + teacher.getName()); } teachers.add(new TeacherConfig(teacher.getName(), assignments)); } }
        else { System.err.println("Save Warning: Teacher list empty, cannot gather teacher data for save."); } return teachers;
    }
    private TimeConfiguration gatherTimeConfigDataFromUI() { String ws = workStartTimeCombo != null ? workStartTimeCombo.getValue() : null; String we = workEndTimeCombo != null ? workEndTimeCombo.getValue() : null; String bs = breakStartTimeCombo != null ? breakStartTimeCombo.getValue() : null; String be = breakEndTimeCombo != null ? breakEndTimeCombo.getValue() : null; Integer sd = slotDurationCombo != null ? slotDurationCombo.getValue() : null; Integer mts = maxTeacherSlotsSpinner != null ? maxTeacherSlotsSpinner.getValue() : null; // Get max teacher slots
        if (ws == null || we == null || bs == null || be == null || sd == null || mts == null) return null; // Check mts
        return new TimeConfiguration(ws, we, bs, be, sd, mts); // Add mts
    }
    /**
     * Helper method to create the iText Table for PDF export
     */
    private Table createPdfTimetableTable(List<TimetableEntry> timetable) throws IOException {
        // Ensure you have this method available or copy it if needed:
        List<LocalTime> classSlots = calculateTimeSlots(workStartTime, workEndTime, breakStartTime, breakEndTime, slotDurationMinutes);

        if (classSlots.isEmpty()) {
            // Return an empty table or throw an exception if this case needs specific handling
            return new Table(1).addCell("No valid class slots found.");
        }

        // Determine break column index (1-based for PDF Table)
        int breakColumnIndex = -1;
        for (int i = 0; i < classSlots.size(); i++) {
            if (!classSlots.get(i).isBefore(breakStartTime)) {
                breakColumnIndex = i + 1; // +1 because column 0 is Day/Sem
                break;
            }
        }
        if (breakColumnIndex == -1 && breakStartTime != null && !breakStartTime.isAfter(workEndTime)) {
            breakColumnIndex = classSlots.size() + 1;
        }

        // Calculate total columns needed
        int numTimeCols = classSlots.size();
        int numBreakCols = (breakColumnIndex != -1) ? 1 : 0;
        int totalPdfCols = 1 + numTimeCols + numBreakCols; // 1 for Day/Sem + time slots + break

        // Define column widths (adjust as needed)
        float[] columnWidths = new float[totalPdfCols];
        columnWidths[0] = 3f; // Wider first column for Day/Sem
        for (int i = 1; i < totalPdfCols; i++) {
            columnWidths[i] = 2f; // Equal width for others
        }

        Table table = new Table(UnitValue.createPercentArray(columnWidths)).useAllAvailableWidth();

        // --- Define Fonts and Styles ---
        PdfFont boldFont = PdfFontFactory.createFont(StandardFonts.HELVETICA_BOLD);
        PdfFont regularFont = PdfFontFactory.createFont(StandardFonts.HELVETICA);
        PdfFont smallFont = PdfFontFactory.createFont(StandardFonts.HELVETICA); // Use regular for small for now
        float headerFontSize = 9f;
        float dayFontSize = 10f;
        float semesterFontSize = 8f;
        float entryFontSize = 7.5f;
        float breakFontSize = 8f;

        Border defaultBorder = new SolidBorder(ColorConstants.GRAY, 0.5f);

        // --- Header Row ---
        com.itextpdf.layout.element.Cell topLeftCell = new com.itextpdf.layout.element.Cell()
                .add(new Paragraph("Day / Sem").setFont(boldFont).setFontSize(headerFontSize))
                .setTextAlignment(TextAlignment.CENTER)
                .setVerticalAlignment(VerticalAlignment.MIDDLE) // Use correct VerticalAlignment
                .setBackgroundColor(new DeviceRgb(204, 224, 255)) // Light blue similar to #cce0ff
                .setBorder(defaultBorder)
                .setMinHeight(20); // Min height for header
        table.addHeaderCell(topLeftCell);

        int currentPdfCol = 1;
        for (int i = 0; i < classSlots.size(); i++) {
            // Insert break header
            if (currentPdfCol == breakColumnIndex) {
                com.itextpdf.layout.element.Cell breakHeaderCell = new com.itextpdf.layout.element.Cell()
                        .add(new Paragraph("BREAK").setFont(boldFont).setFontSize(breakFontSize))
                        .setTextAlignment(TextAlignment.CENTER)
                        .setVerticalAlignment(VerticalAlignment.MIDDLE) // Use correct VerticalAlignment
                        .setBackgroundColor(ColorConstants.LIGHT_GRAY) // Use standard light gray
                        .setBorder(defaultBorder)
                        .setMinHeight(20);
                table.addHeaderCell(breakHeaderCell);
                currentPdfCol++;
            }
            // Add class slot header
            LocalTime slotStart = classSlots.get(i);
            LocalTime slotEnd = slotStart.plusMinutes(slotDurationMinutes);
            String headerText = slotStart.format(TIME_FORMATTER) + "-\n" + slotEnd.format(TIME_FORMATTER);
            com.itextpdf.layout.element.Cell timeHeaderCell = new com.itextpdf.layout.element.Cell()
                    .add(new Paragraph(headerText).setFont(boldFont).setFontSize(headerFontSize))
                    .setTextAlignment(TextAlignment.CENTER)
                    .setVerticalAlignment(VerticalAlignment.MIDDLE) // Use correct VerticalAlignment
                    .setBackgroundColor(new DeviceRgb(204, 224, 255))
                    .setBorder(defaultBorder)
                    .setMinHeight(20);
            table.addHeaderCell(timeHeaderCell);
            currentPdfCol++;
        }
        // Add break header if it's the last column
        if (breakColumnIndex == classSlots.size() + 1) {
            com.itextpdf.layout.element.Cell breakHeaderCell = new com.itextpdf.layout.element.Cell()
                    .add(new Paragraph("BREAK").setFont(boldFont).setFontSize(breakFontSize))
                    .setTextAlignment(TextAlignment.CENTER)
                    .setVerticalAlignment(VerticalAlignment.MIDDLE) // Use correct VerticalAlignment
                    .setBackgroundColor(ColorConstants.LIGHT_GRAY)
                    .setBorder(defaultBorder)
                    .setMinHeight(20);
            table.addHeaderCell(breakHeaderCell);
        }

        // Ensure header rows are repeated on new pages if table splits
        table.setSkipFirstHeader(false);
        table.getHeader().setHeight(25); // Give header a bit more definite height


        // --- Data Rows ---
        List<Semester> sortedSemesters = semesterList.stream()
                .sorted(Comparator.comparing(Semester::getName))
                .collect(Collectors.toList());
        Map<String, Map<Semester, Map<LocalTime, TimetableEntry>>> groupedEntries = timetable.stream()
                .collect(Collectors.groupingBy(TimetableEntry::getDay,
                        Collectors.groupingBy(TimetableEntry::getSemester,
                                Collectors.toMap(TimetableEntry::getStartTime, entry -> entry, (e1, e2) -> e1)))); // Keep first on conflict

        for (String day : WORKING_DAYS) {
            // Day Header Row (Spanning all columns)
            com.itextpdf.layout.element.Cell dayCell = new com.itextpdf.layout.element.Cell(1, totalPdfCols) // Span 1 row, all columns
                    .add(new Paragraph(day).setFont(boldFont).setFontSize(dayFontSize))
                    .setTextAlignment(TextAlignment.LEFT)
                    .setVerticalAlignment(VerticalAlignment.MIDDLE) // Use correct VerticalAlignment
                    .setBackgroundColor(new DeviceRgb(170, 204, 255)) // Darker blue #aaccff
                    .setBorderTop(new SolidBorder(ColorConstants.DARK_GRAY, 1f))
                    .setBorderBottom(new SolidBorder(ColorConstants.DARK_GRAY, 1f))
                    .setBorderLeft(Border.NO_BORDER)
                    .setBorderRight(Border.NO_BORDER)
                    .setPaddingLeft(5)
                    .setMinHeight(18);
            table.addCell(dayCell);

            Map<Semester, Map<LocalTime, TimetableEntry>> dayEntriesBySemester = groupedEntries.getOrDefault(day, Collections.emptyMap());

            if (sortedSemesters.isEmpty()) {
                com.itextpdf.layout.element.Cell noSemCell = new com.itextpdf.layout.element.Cell(1, totalPdfCols)
                        .add(new Paragraph("No Semesters Defined").setFont(regularFont).setFontSize(semesterFontSize))
                        .setBorder(defaultBorder);
                table.addCell(noSemCell);
            } else {
                for (Semester semester : sortedSemesters) {
                    // Semester Name Cell
                    com.itextpdf.layout.element.Cell semCell = new com.itextpdf.layout.element.Cell()
                            .add(new Paragraph(semester.getName()).setFont(regularFont).setFontSize(semesterFontSize))
                            .setTextAlignment(TextAlignment.RIGHT)
                            .setVerticalAlignment(VerticalAlignment.MIDDLE) // Use correct VerticalAlignment
                            .setBackgroundColor(new DeviceRgb(230, 240, 255)) // Very light blue #e6f0ff
                            .setBorder(defaultBorder)
                            .setPaddingRight(4)
                            .setMinHeight(30); // Give data cells some height
                    table.addCell(semCell);

                    Map<LocalTime, TimetableEntry> semesterDayEntries = dayEntriesBySemester.getOrDefault(semester, Collections.emptyMap());

                    // Add cells for each time slot for this semester
                    currentPdfCol = 1;
                    for (int i = 0; i < classSlots.size(); i++) {
                        // Insert break cell
                        if (currentPdfCol == breakColumnIndex) {
                            com.itextpdf.layout.element.Cell breakCell = new com.itextpdf.layout.element.Cell()
                                    .add(new Paragraph("BREAK").setFont(boldFont).setFontSize(breakFontSize).setFontColor(ColorConstants.DARK_GRAY))
                                    .setTextAlignment(TextAlignment.CENTER)
                                    .setVerticalAlignment(VerticalAlignment.MIDDLE) // Use correct VerticalAlignment
                                    .setBackgroundColor(ColorConstants.LIGHT_GRAY)
                                    .setBorder(defaultBorder)
                                    .setMinHeight(30);
                            table.addCell(breakCell);
                            currentPdfCol++;
                        }

                        // Add class/empty cell
                        LocalTime slotStart = classSlots.get(i);
                        TimetableEntry entry = semesterDayEntries.get(slotStart);
                        com.itextpdf.layout.element.Cell dataCell;
                        if (entry != null) {
                            Paragraph subjectP = new Paragraph(entry.getSubject().getName())
                                    .setFont(boldFont).setFontSize(entryFontSize);
                            Paragraph teacherP = new Paragraph("(" + entry.getTeacher().getName() + ")")
                                    .setFont(smallFont).setFontSize(entryFontSize - 1); // Slightly smaller for teacher
                            dataCell = new com.itextpdf.layout.element.Cell()
                                    .add(subjectP)
                                    .add(teacherP)
                                    .setTextAlignment(TextAlignment.CENTER)
                                    .setVerticalAlignment(VerticalAlignment.MIDDLE) // Use correct VerticalAlignment
                                    .setBorder(defaultBorder)
                                    .setMinHeight(30)
                                    .setPadding(1);
                        } else {
                            // Empty cell
                            dataCell = new com.itextpdf.layout.element.Cell()
                                    .add(new Paragraph(" ")) // Add space to ensure height is respected
                                    .setBackgroundColor(new DeviceRgb(249, 249, 249)) // Very light gray #f9f9f9
                                    .setBorder(defaultBorder)
                                    .setMinHeight(30);
                        }
                        table.addCell(dataCell);
                        currentPdfCol++;
                    }

                    // Add trailing break cell if needed
                    if (breakColumnIndex == classSlots.size() + 1) {
                        com.itextpdf.layout.element.Cell breakCell = new com.itextpdf.layout.element.Cell()
                                .add(new Paragraph("BREAK").setFont(boldFont).setFontSize(breakFontSize).setFontColor(ColorConstants.DARK_GRAY))
                                .setTextAlignment(TextAlignment.CENTER)
                                .setVerticalAlignment(VerticalAlignment.MIDDLE) // Use correct VerticalAlignment
                                .setBackgroundColor(ColorConstants.LIGHT_GRAY)
                                .setBorder(defaultBorder)
                                .setMinHeight(30);
                        table.addCell(breakCell);
                    }
                } // End semester loop
            } // End else (semesters exist)
        } // End day loop

        return table;
    }
    // --- Manual JSON Conversion Helpers ---
    private String convertMapToJson(Map<String, Object> map) { return convertMapToJson(map, 1); }
    private String convertMapToJson(Map<String, Object> map, int indent) { String indentStr = "  ".repeat(indent); String innerIndentStr = "  ".repeat(indent + 1); StringBuilder sb = new StringBuilder("{\n"); List<String> keys = new ArrayList<>(map.keySet()); for (int i = 0; i < keys.size(); i++) { String key = keys.get(i); Object value = map.get(key); sb.append(innerIndentStr).append(escapeJsonString(key)).append(": "); sb.append(convertToJsonValue(value, indent + 1)); if (i < keys.size() - 1) sb.append(","); sb.append("\n"); } sb.append(indentStr).append("}"); return sb.toString(); }
    private String convertListToJson(List<?> list, int indent) { String indentStr = "  ".repeat(indent); String innerIndentStr = "  ".repeat(indent + 1); StringBuilder sb = new StringBuilder("[\n"); for (int i = 0; i < list.size(); i++) { sb.append(innerIndentStr).append(convertToJsonValue(list.get(i), indent + 1)); if (i < list.size() - 1) sb.append(","); sb.append("\n"); } sb.append(indentStr).append("]"); return sb.toString(); }
    private String convertToJsonValue(Object value, int indent) { if (value == null) return "null"; else if (value instanceof String) return escapeJsonString((String) value); else if (value instanceof Number || value instanceof Boolean) return value.toString(); else if (value instanceof Map) return convertMapToJson((Map<String, Object>) value, indent); else if (value instanceof List) return convertListToJson((List<?>) value, indent); else return escapeJsonString(value.toString()); }
    private String escapeJsonString(String input) { if (input == null) return "null"; StringBuilder sb = new StringBuilder(); sb.append('"'); for (char c : input.toCharArray()) { switch (c) { case '"': sb.append("\\\""); break; case '\\': sb.append("\\\\"); break; case '\b': sb.append("\\b"); break; case '\f': sb.append("\\f"); break; case '\n': sb.append("\\n"); break; case '\r': sb.append("\\r"); break; case '\t': sb.append("\\t"); break; default: sb.append(c); } } sb.append('"'); return sb.toString(); }
    // --- End JSON Helpers ---

    // --- Main Method ---
    public static void main(String[] args) {
        launch(args);
    }
}

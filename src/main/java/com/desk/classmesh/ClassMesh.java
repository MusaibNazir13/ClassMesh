//Ver 1.3
package com.desk.classmesh; // Make sure this matches your project structure

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
import javafx.geometry.Insets;
import javafx.geometry.Orientation; // Keep if FlowPane is used elsewhere
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color; // Import Color
import javafx.scene.shape.Circle;
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
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Subject subject = (Subject) o;
        return weeklyHours == subject.weeklyHours && Objects.equals(name, subject.name);
    }
    @Override
    public int hashCode() {
        return Objects.hash(name, weeklyHours);
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
//    public boolean canTeach(Subject subject) {
//        return subjectsTaught.stream().anyMatch(taughtSub -> taughtSub.equals(subject));
//    }
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
    record TimeConfiguration(String workStart, String workEnd, String breakStart, String breakEnd, int slotDuration, int maxTeacherSlots) {// Added maxTeacherSlots


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

    // Main configuration container class
    // Inside public class ClassMesh extends Application { ...
    static class AppConfiguration { // <--- Add static keyword
        public String schoolName;
        public String departmentName;
        public TimeConfiguration timeSettings;
        public List<SemesterConfigData> semesterConfigurations;
        public List<TeacherConfigData> teacherConfigurations;
        public Integer maxConsecutiveSlotsPerSubject;

        // Default constructor for Jackson (already there, which is good)
        public AppConfiguration() {}

        // Getters and setters are still good practice
    }
    // ...
    private static final String APP_NAME = "ClassMesh";
    private static final String APP_VERSION = "1.3.0";
    private static final String DEVELOPER_NAME = "Musaib Nazir";
    private static final String DEVELOPER_EMAIL = "grmusa9797@gmail.com";
    private static final String DEVELOPER_CONTACT = "+91-9541757976";


    // --- Constants ---
    private static final String MAIN_CONTAINER_STYLE_CLASS = "main-config-pane";
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm");
    private static final List<String> WORKING_DAYS = List.of("Monday", "Tuesday", "Wednesday", "Thursday", "Friday","Saturday");
    // private static final int MAX_TEACHER_SLOTS_PER_DAY = 2; // Removed constant
    private static final int MIN_SUBJECT_HOURS = 1; // Minimum hours for a subject
    private static final int DEFAULT_SUBJECT_HOURS = 3; // Fallback default if calculation fails
    private static final int MAX_SUBJECT_HOURS_SPINNER = 50; // Max value for the spinner
    private static final int ABSOLUTE_MAX_SUBJECTS = 50; // Absolute max subjects in dropdown
    private static final String ALLOWED_MAC_ADDRESS = "74-13-EA-30-C9-5C";
    private int maxConsecutiveSlotsPerSubject = 0;


    // --- UI Elements ---
    private TextField schoolNameField; // For School/College Name input
    private TextField departmentNameField; // For Department Name input
    private String schoolName = ""; // To store the School/College Name
    private String departmentName = ""; // To store the Department Name
    private ComboBox<Integer> semCountComboBox; private GridPane semesterSubjectGrid; private Node semesterSectionContainer;
    private VBox timeConfigSection; private ComboBox<String> workStartTimeCombo; private ComboBox<String> workEndTimeCombo; private ComboBox<String> breakStartTimeCombo; private ComboBox<String> breakEndTimeCombo; private ComboBox<Integer> slotDurationCombo; private Label workingHoursLabel;
    private Spinner<Integer> maxTeacherSlotsSpinner; // New spinner for teacher slots
    private Spinner<Integer> maxConsecutiveSubjectSpinner;
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

    private boolean configLoadedFromFile = false;

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
        if (!isOperationAllowed()) {
            showErrorAlert("Security Restriction",
                    "This application is not authorized to run on this computer.\n" +
                            "Please contact the application provider for assistance.");
            Platform.exit(); // Exit JavaFX application
            System.exit(0);  // Ensure JVM terminates
            return;          // Stop further execution of the start method
        }
        // --- Create UI Sections ---

        timeConfigSection = createTimeConfigurationSection();
        semesterSectionContainer = createSemesterInputSection();
        teacherSectionContainer = createTeacherMappingSection();

        // Container for the new section
        Node schoolInfoSectionContainer = createSchoolInfoSection(); // ADD THIS LINE
        timeConfigSection = createTimeConfigurationSection();
        semesterSectionContainer = createSemesterInputSection();

        Node generateButtonSection = createGenerateButtonSection();


        // --- Add Sections to mainContentVBox in the NEW desired order ---
        // --- Add Sections to mainContentVBox in the NEW desired order ---
        mainContentVBox.getChildren().addAll(schoolInfoSectionContainer, timeConfigSection, semesterSectionContainer, teacherSectionContainer, generateButtonSection); //added College Name input section in last modification

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
        MenuBar menuBar = new MenuBar();

        // --- File Menu (Existing) ---
        Menu fileMenu = new Menu("File");
        MenuItem loadItem = new MenuItem("Load Configuration");
        MenuItem saveItem = new MenuItem("Save Configuration");
        loadItem.setOnAction(e -> handleLoadConfiguration(stage));
        saveItem.setOnAction(e -> handleSaveConfiguration(stage));
        fileMenu.getItems().addAll(loadItem, saveItem);

        // --- Help Menu (New) ---
        Menu helpMenu = new Menu("Help");
        MenuItem aboutItem = new MenuItem("About " + APP_NAME);
        aboutItem.setOnAction(e -> showAboutDialog()); // Call new method
        helpMenu.getItems().add(aboutItem);

        // --- Add Menus to Bar ---
        menuBar.getMenus().addAll(fileMenu, helpMenu); // Add both menus

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

    /** Builds the content of the semesterSubjectGrid based on semCountComboBox, preserving existing data where possible */
    // Ensure these helper records are defined in your ClassMesh class or accessible:
    // private record TempSubjectUIData(String subjectName, Integer hours) {}
    // private record TempSemesterUIData(String semesterName, Integer subjectCount, List<TempSubjectUIData> subjects) {}

    private void buildSemesterSubjectGrid() {
        System.out.println("--- buildSemesterSubjectGrid CALLED ---"); // DEBUG
        // --- 1. Read and Store Existing Data from the UI (using your provided logic) ---
        List<TempSemesterUIData> oldDataList = new ArrayList<>();
        if (!semesterSubjectGrid.getChildren().isEmpty()) {
            int maxCurrentSemRow = -1;
            for (Node node : semesterSubjectGrid.getChildrenUnmodifiable()) {
                Integer r = GridPane.getRowIndex(node);
                if (r != null) {
                    if ((GridPane.getColumnIndex(node) == 0 && node instanceof TextField) ||
                            (GridPane.getColumnIndex(node) == 1 && node instanceof VBox && node.getId() != null && node.getId().startsWith("subjectInputArea_"))) {
                        if (r > maxCurrentSemRow) maxCurrentSemRow = r;
                    }
                }
            }

            for (int i = 0; i <= maxCurrentSemRow; i++) {
                TextField semNameFieldNode = (TextField) findNodeInGrid(semesterSubjectGrid, i, 0);
                VBox subjectInputAreaNode = (VBox) findNodeInGrid(semesterSubjectGrid, i, 1);

                if (semNameFieldNode == null && subjectInputAreaNode == null) continue; // Skip if row is completely empty

                String semName = (semNameFieldNode != null) ? semNameFieldNode.getText() : "Semester " + (i + 1); // Default name if field somehow null
                ComboBox<Integer> subCountComboNode = null;
                FlowPane subDetailPaneNode = null;

                if (subjectInputAreaNode != null) {
                    for (Node n : subjectInputAreaNode.getChildrenUnmodifiable()) {
                        if (n instanceof ComboBox) subCountComboNode = (ComboBox<Integer>) n;
                        else if (n instanceof FlowPane) subDetailPaneNode = (FlowPane) n;
                    }
                }

                Integer subCount = (subCountComboNode != null) ? subCountComboNode.getValue() : 0;

                List<TempSubjectUIData> subjectsData = new ArrayList<>();
                if (subDetailPaneNode != null && subCount > 0) {
                    int collectedSubjects = 0;
                    for (Node entryBoxNode : subDetailPaneNode.getChildrenUnmodifiable()) {
                        if (collectedSubjects >= subCount) break;
                        if (entryBoxNode instanceof HBox) {
                            TextField subNameNode = null;
                            Spinner<Integer> hoursSpinnerNode = null;
                            for (Node item : ((HBox) entryBoxNode).getChildrenUnmodifiable()) {
                                if (item instanceof TextField) subNameNode = (TextField) item;
                                else if (item instanceof Spinner) hoursSpinnerNode = (Spinner<Integer>) item;
                            }
                            if (subNameNode != null && hoursSpinnerNode != null && hoursSpinnerNode.getValue() != null) {
                                subjectsData.add(new TempSubjectUIData(subNameNode.getText(), hoursSpinnerNode.getValue()));
                                collectedSubjects++;
                            }
                        }
                    }
                }
                oldDataList.add(new TempSemesterUIData(semName, subCount, subjectsData));
            }
        }
        System.out.println("buildSemesterSubjectGrid - Extracted oldDataList size: " + oldDataList.size());


        // --- 2. Clear the visual grid and get the new semester count ---
        semesterSubjectGrid.getChildren().clear();
        hideTeacherSection();

        Integer selectedCount = semCountComboBox.getValue();
        if (selectedCount == null || selectedCount <= 0) {
            System.out.println("buildSemesterSubjectGrid - selectedCount is invalid or zero, returning.");
            // Remove "Next" button if it exists from a previous build with selectedCount > 0
            Node oldNextButton = null;
            for(Node child : semesterSubjectGrid.getChildrenUnmodifiable()){
                if(child instanceof Button && "Next -> Define Teachers".equals(((Button)child).getText())){
                    oldNextButton = child;
                    break;
                }
            }
            if(oldNextButton != null) semesterSubjectGrid.getChildren().remove(oldNextButton);
            return;
        }
        System.out.println("buildSemesterSubjectGrid - New selectedCount from ComboBox: " + selectedCount);


        int maxPossibleSubjects = (maxWeeklyHoursPerSemester > 0 && MIN_SUBJECT_HOURS > 0) ? (maxWeeklyHoursPerSemester / MIN_SUBJECT_HOURS) : DEFAULT_SUBJECT_HOURS * 5;
        int maxSubjectsToShow = Math.min(ABSOLUTE_MAX_SUBJECTS, maxPossibleSubjects);
        if (maxSubjectsToShow <= 0 && maxWeeklyHoursPerSemester > 0) maxSubjectsToShow = DEFAULT_SUBJECT_HOURS * 5;
        else if (maxSubjectsToShow <= 0) maxSubjectsToShow = 10;


        // --- 3. Rebuild the grid, pre-filling with old data where available ---
        for (int semIndexLoop = 0; semIndexLoop < selectedCount; semIndexLoop++) {
            final int semIndex = semIndexLoop; // Effectively final for use in lambdas

            final TextField semNameField = new TextField();
            semNameField.setPromptText("Semester/Class " + (semIndex + 1) + " Name");
            semNameField.setId("semName_" + semIndex);

            final ComboBox<Integer> subCountCombo = new ComboBox<>();
            subCountCombo.setPromptText("Subject Count");
            subCountCombo.getItems().add(0);
            for (int j = 1; j <= maxSubjectsToShow; j++) {
                subCountCombo.getItems().add(j);
            }
            subCountCombo.setId("subCount_" + semIndex);

            final FlowPane subDetailPane = new FlowPane(Orientation.HORIZONTAL, 10, 5);
            subDetailPane.setId("subDetailPane_" + semIndex);
            subDetailPane.setPrefWrapLength(400);

            // This setOnAction will *only* build the UI structure for subjects
            subCountCombo.setOnAction(e -> {
                System.out.println("subCountCombo onAction for semIndex " + semIndex + ", new sub count: " + subCountCombo.getValue());
                subDetailPane.getChildren().clear();
                Integer numSubs = subCountCombo.getValue();
                if (numSubs == null || numSubs <= 0) return;

                int defaultHoursVal = Math.min(DEFAULT_SUBJECT_HOURS, MAX_SUBJECT_HOURS_SPINNER);

                for (int subIdx = 0; subIdx < numSubs; subIdx++) {
                    TextField newSubNameField = new TextField();
                    newSubNameField.setPromptText("Subject " + (subIdx + 1) + " Name");
                    newSubNameField.setId("subName_" + semIndex + "_" + subIdx);

                    // --- INTEGRATED ENHANCED SPINNER LOGIC ---
                    final Spinner<Integer> newHoursSpinner = new Spinner<>(); // Make final for listeners
                    final SpinnerValueFactory.IntegerSpinnerValueFactory valueFactory = new SpinnerValueFactory.IntegerSpinnerValueFactory(
                            MIN_SUBJECT_HOURS, MAX_SUBJECT_HOURS_SPINNER, defaultHoursVal);
                    newHoursSpinner.setValueFactory(valueFactory);
                    newHoursSpinner.setEditable(true);
                    newHoursSpinner.setPrefWidth(70);
                    newHoursSpinner.setId("subHours_" + semIndex + "_" + subIdx);

                    final TextField editor = newHoursSpinner.getEditor();
                    // Capture subDetailPane for use in adjustHoursWithinSemester if needed from valueProperty
                    final FlowPane currentSemesterSubDetailPane = subDetailPane;

                    Runnable commitHandler = () -> {
                        String text = editor.getText();
                        try {
                            Integer value = valueFactory.getConverter().fromString(text);
                            if (value != null) {
                                valueFactory.setValue(value); // Factory handles clamping
                            } else { // Revert if converter returns null (e.g. for empty string)
                                editor.setText(valueFactory.getConverter().toString(valueFactory.getValue()));
                            }
                        } catch (NumberFormatException nfe) { // Revert if not a number
                            editor.setText(valueFactory.getConverter().toString(valueFactory.getValue()));
                        } catch (Exception ex) { // Catch any other parsing issues
                            System.err.println("Error parsing spinner value: " + text + " - " + ex.getMessage());
                            editor.setText(valueFactory.getConverter().toString(valueFactory.getValue()));
                        }
                    };

                    editor.setOnAction(event -> {
                        commitHandler.run();
                        event.consume();
                    });

                    editor.focusedProperty().addListener((obs, oldFocus, newFocus) -> {
                        if (!newFocus) { // Lost focus
                            commitHandler.run();
                        }
                    });

                    newHoursSpinner.valueProperty().addListener((obs, oldValue, newValue) -> {
                        if (newValue != null) {
                            if (!isAdjustingHours.get()) {
                                adjustHoursWithinSemester(currentSemesterSubDetailPane, newHoursSpinner);
                                Platform.runLater(() -> {
                                    if (adjustmentAlertPending) adjustmentAlertPending = false;
                                });
                            }
                        }
                    });
                    // --- END OF ENHANCED SPINNER LOGIC ---

                    HBox entryBox = new HBox(5, newSubNameField, new Label("Hrs/Week:"), newHoursSpinner);
                    entryBox.setAlignment(Pos.CENTER_LEFT);
                    subDetailPane.getChildren().add(entryBox);
                }
                // Initial adjustment after creating spinners for this semester if any subjects added
                if (numSubs > 0) {
                    adjustHoursWithinSemester(subDetailPane, null);
                    Platform.runLater(() -> { if (adjustmentAlertPending) adjustmentAlertPending = false; });
                }
            });

            // Apply old data if available for this semester index
            final TempSemesterUIData existingSemData = (semIndex < oldDataList.size()) ? oldDataList.get(semIndex) : null;

            if (existingSemData != null) {
                semNameField.setText(existingSemData.semesterName());
                final Integer savedSubjectCount = existingSemData.subjectCount(); // Make effectively final

                if (savedSubjectCount != null) {
                    System.out.println("Applying old data for semIndex " + semIndex + ": SemName=" + existingSemData.semesterName() + ", SubCount=" + savedSubjectCount);
                    subCountCombo.setValue(savedSubjectCount); // Triggers onAction, building empty HBoxes

                    // Schedule the population of these newly created fields on the JavaFX thread
                    Platform.runLater(() -> {
                        System.out.println("Platform.runLater for semIndex: " + semIndex +
                                " - Children in subDetailPane: " + subDetailPane.getChildren().size() +
                                ". Expected subjects from old data: " + (existingSemData.subjectCount() != null ? existingSemData.subjectCount() : "N/A"));

                        if (existingSemData.subjectCount() != null) { // Re-check existingSemData inside lambda
                            int expectedCount = existingSemData.subjectCount();
                            // Check if UI structure matches expected data
                            if (subDetailPane.getChildren().size() == expectedCount &&
                                    existingSemData.subjects() != null && !existingSemData.subjects().isEmpty()) {
                                System.out.println("Proceeding to fill " + expectedCount + " subject(s) for semIndex: " + semIndex);
                                for (int subIndex = 0; subIndex < expectedCount; subIndex++) {
                                    if (subIndex < subDetailPane.getChildren().size() && subIndex < existingSemData.subjects().size()) {
                                        Node entryBoxNode = subDetailPane.getChildren().get(subIndex);
                                        if (entryBoxNode instanceof HBox) {
                                            TempSubjectUIData oldSub = existingSemData.subjects().get(subIndex);
                                            TextField subjectNameFieldInBox = null;
                                            Spinner<Integer> hoursSpinnerInBox = null;
                                            for (Node item : ((HBox) entryBoxNode).getChildrenUnmodifiable()) {
                                                if (item instanceof TextField) subjectNameFieldInBox = (TextField) item;
                                                else if (item instanceof Spinner) hoursSpinnerInBox = (Spinner<Integer>) item;
                                            }
                                            if (subjectNameFieldInBox != null && oldSub.subjectName() != null) subjectNameFieldInBox.setText(oldSub.subjectName());
                                            if (hoursSpinnerInBox != null && oldSub.hours() != null) hoursSpinnerInBox.getValueFactory().setValue(oldSub.hours());
                                        }
                                    } else { System.out.println("DEBUG WARNING: Index out of bounds during subject fill for semIndex: " + semIndex + ", subIndex: " + subIndex); }
                                }
                            } else {
                                System.out.println("Mismatch or no subjects to fill (Platform.runLater) for semIndex: " + semIndex +
                                        ". Actual Children: " + subDetailPane.getChildren().size() +
                                        ", Expected: " + expectedCount +
                                        ", Old subjects list empty: " + (existingSemData.subjects() == null || existingSemData.subjects().isEmpty()));
                            }
                        } else { System.out.println("No valid subject count in existingSemData for semIndex: " + semIndex + " (within Platform.runLater)"); }
                    });
                }
            }

            VBox subjectInputArea = new VBox(5, subCountCombo, subDetailPane);
            subjectInputArea.setId("subjectInputArea_" + semIndex);

            semesterSubjectGrid.add(semNameField, 0, semIndex);
            semesterSubjectGrid.add(subjectInputArea, 1, semIndex);
            GridPane.setHgrow(semNameField, Priority.SOMETIMES);
            GridPane.setHgrow(subjectInputArea, Priority.ALWAYS);
        }

        // --- 4. Add "Next" button ---
        Button nextButton = new Button("Next -> Define Teachers");
        nextButton.setStyle("-fx-background-color: #2196F3; -fx-text-fill: white; -fx-font-size: 14px; -fx-padding: 6 16 6 16; -fx-background-radius: 6;");
        nextButton.setOnAction(e -> handleSemesterNextClick(semesterSubjectGrid, selectedCount));
        semesterSubjectGrid.add(nextButton, 0, selectedCount, 2, 1);
        GridPane.setHalignment(nextButton, Pos.CENTER.getHpos());
        GridPane.setMargin(nextButton, new Insets(15, 0, 0, 0));
        System.out.println("--- buildSemesterSubjectGrid FINISHED ---");
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

        maxConsecutiveSubjectSpinner = new Spinner<>(1, 8, 3); // Min 1, Max 5, Default 2 (Adjust range as needed)
        maxConsecutiveSubjectSpinner.setPrefWidth(70);
        maxConsecutiveSubjectSpinner.setEditable(true);

        // New Spinner for Max Teacher Slots
        maxTeacherSlotsSpinner = new Spinner<>(1, 8, 2); // Min 1, Max 8, Default 2
        maxTeacherSlotsSpinner.setPrefWidth(70);
        maxTeacherSlotsSpinner.setEditable(true);

        workStartTimeCombo.valueProperty().addListener((obs, ov, nv) -> updateWorkingHoursDisplay()); workEndTimeCombo.valueProperty().addListener((obs, ov, nv) -> updateWorkingHoursDisplay()); breakStartTimeCombo.valueProperty().addListener((obs, ov, nv) -> updateWorkingHoursDisplay()); breakEndTimeCombo.valueProperty().addListener((obs, ov, nv) -> updateWorkingHoursDisplay());
        workingHoursLabel = new Label("Working Hours/Day: --"); workingHoursLabel.setStyle("-fx-font-style: italic; -fx-padding: 0 0 0 10px;");
        timeGrid.addRow(0, new Label("Working Hours:"), workStartTimeCombo, new Label("to"), workEndTimeCombo);
        timeGrid.addRow(1, new Label("Break Time:"), breakStartTimeCombo, new Label("to"), breakEndTimeCombo);
        timeGrid.addRow(2, new Label("Class Duration:"), slotDurationCombo);
        timeGrid.addRow(3, new Label("Max Classes/Teacher/Day:"), maxTeacherSlotsSpinner);
        timeGrid.addRow(4,new Label("Consecutive Classes/Subject"),maxConsecutiveSubjectSpinner);// Add new row
        timeGrid.add(workingHoursLabel, 1, 5, 3, 1); // Move working hours label down
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
    /** Creates the School/College and Department Information Input Section */
    private Node createSchoolInfoSection() {
        VBox sectionContainer = new VBox(10);
        sectionContainer.setPadding(new Insets(10, 0, 10, 0));
        // Optional: Add a border like other sections if desired
        // sectionContainer.setStyle("-fx-border-color:#6ca0dc;-fx-border-width: 0 0 2 0;-fx-padding: 15 0 15 0;");
        sectionContainer.setId("schoolInfoSectionBox");

        Label titleLabel = new Label("Institution Information");
        titleLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");
        titleLabel.setPadding(new Insets(0, 0, 5, 0));

        GridPane infoGrid = new GridPane();
        infoGrid.setHgap(10);
        infoGrid.setVgap(8);
        infoGrid.setPadding(new Insets(0, 0, 0, 10)); // Indent content slightly

        schoolNameField = new TextField();
        schoolNameField.setPromptText("Enter School/College Name");
        schoolNameField.setPrefWidth(300); // Adjust width as needed

        departmentNameField = new TextField();
        departmentNameField.setPromptText("Enter Department Name");
        departmentNameField.setPrefWidth(300); // Adjust width as needed

        infoGrid.addRow(0, new Label("School/College:"), schoolNameField);
        infoGrid.addRow(1, new Label("Department:"), departmentNameField);

        // Listener to update instance variables when text changes
        schoolNameField.textProperty().addListener((obs, oldVal, newVal) -> schoolName = newVal.trim());
        departmentNameField.textProperty().addListener((obs, oldVal, newVal) -> departmentName = newVal.trim());


        sectionContainer.getChildren().addAll(titleLabel, infoGrid);
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

        Label schoolNameLabelForTab = new Label();
        schoolNameLabelForTab.setFont(Font.font("System", FontWeight.BOLD, 16));
        Label departmentNameLabelForTab = new Label();
        departmentNameLabelForTab.setFont(Font.font("System", FontWeight.NORMAL, 14));

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
//        Label placeholderLabel = new Label("Generated Timetable");
//        placeholderLabel.setFont(Font.font("System", FontWeight.BOLD, 12)); // Style it a bit
//        placeholderLabel.setPadding(new Insets(5,0,0,0));

        // Add ScrollPane and then Button Box
        tabContent.getChildren().addAll(schoolNameLabelForTab,departmentNameLabelForTab, generatedScrollPane, exportButtonBox);
        VBox.setVgrow(generatedScrollPane, Priority.ALWAYS); // Timetable grid takes up most space

        generatedTimetableTab = new Tab("Generated Timetable", tabContent);
        generatedTimetableTab.setClosable(false);
        generatedTimetableTab.selectedProperty().addListener((obs, wasSelected, isNowSelected) -> {
            if (isNowSelected) {
                schoolNameLabelForTab.setText(schoolNameField.getText().trim()); // Or use stored schoolName
                departmentNameLabelForTab.setText(departmentNameField.getText().trim()); // Or use stored departmentName
                schoolNameLabelForTab.setVisible(!schoolNameField.getText().trim().isEmpty());
                schoolNameLabelForTab.setManaged(!schoolNameField.getText().trim().isEmpty());
                departmentNameLabelForTab.setVisible(!departmentNameField.getText().trim().isEmpty());
                departmentNameLabelForTab.setManaged(!departmentNameField.getText().trim().isEmpty());
            }
        });
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
            Integer maxConsecutiveSub = maxConsecutiveSubjectSpinner.getValue();

            if (ws == null) { errorMsg = "Select Work Start Time."; nodeToHighlight = workStartTimeCombo; }
            else if (we == null) { errorMsg = "Select Work End Time."; nodeToHighlight = workEndTimeCombo; }
            else if (bs == null) { errorMsg = "Select Break Start Time."; nodeToHighlight = breakStartTimeCombo; }
            else if (be == null) { errorMsg = "Select Break End Time."; nodeToHighlight = breakEndTimeCombo; }
            else if (duration == null) { errorMsg = "Select Slot Duration."; nodeToHighlight = slotDurationCombo; }
            else if (maxSlots == null) { errorMsg = "Select Max Classes/Teacher/Day."; nodeToHighlight = maxTeacherSlotsSpinner; }
            else if (maxConsecutiveSub == null) {
                errorMsg = "Select Max Consecutive Slots for Same Subject.";
                nodeToHighlight = maxConsecutiveSubjectSpinner;
            }// Validate new input
            else {
                workStartTime = LocalTime.parse(ws, TIME_FORMATTER);
                workEndTime = LocalTime.parse(we, TIME_FORMATTER);
                breakStartTime = LocalTime.parse(bs, TIME_FORMATTER);
                breakEndTime = LocalTime.parse(be, TIME_FORMATTER);
                slotDurationMinutes = duration;
                maxTeacherSlotsPerDay = maxSlots;
                maxConsecutiveSlotsPerSubject = maxConsecutiveSub;  // Store the value

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
            lastGeneratedTimetable = generateTimetableAlgorithmV2( semesterList, teacherList, teacherAssignmentsMap, workStartTime, workEndTime, breakStartTime, breakEndTime, slotDurationMinutes, WORKING_DAYS, maxTeacherSlotsPerDay, maxConsecutiveSlotsPerSubject );
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
            Map<Teacher, List<SubjectContext>> teacherAssignments,
            LocalTime workStart, LocalTime workEnd,
            LocalTime breakStart, // breakStart is used implicitly by calculateTimeSlots
            LocalTime breakEnd,   // breakEnd is what we'll use to identify post-break slots
            int slotMinutes, List<String> days,
            int maxSlotsPerTeacherPerDay,
            int maxConsecutiveSlotsPerSubject)
    {
        System.out.println("DEBUG: Starting Algorithm V2 with Max Teacher Slots/Day: " + maxSlotsPerTeacherPerDay +
                ", Max Consecutive Slots/Subject: " + maxConsecutiveSlotsPerSubject);

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
            recentSubjectsMap.put(day, new HashMap<>()); // Initialize recent subjects map for the day
            teacherDailyLoadMap.put(day, new HashMap<>());
            System.out.println("DEBUG: Processing Day: " + day);

            // --- ADDED: Flag to track if the post-break reset has occurred for this day ---
            boolean postBreakResetDoneForDay = false;

            for (LocalTime slotStart : slotStartTimes) {
                teacherBusyMap.get(day).computeIfAbsent(slotStart, k -> new HashSet<>());

                // --- ADDED: Logic to determine if recent history should be cleared for this slot ---
                boolean clearRecentHistoryForThisSlotForAllSemesters = false;
                if (!postBreakResetDoneForDay && breakEnd != null && !slotStart.isBefore(breakEnd)) {
                    // This condition means:
                    // 1. We haven't done a post-break reset yet on this day.
                    // 2. A breakEnd time is defined.
                    // 3. The current slotStart is AT or AFTER the breakEnd time.
                    clearRecentHistoryForThisSlotForAllSemesters = true;
                    postBreakResetDoneForDay = true; // Mark that reset for this break transition is done for the day
                    System.out.println("DEBUG: Day " + day + ", Slot " + slotStart.format(TIME_FORMATTER) +
                            " is the first at/after break end (" + breakEnd.format(TIME_FORMATTER) +
                            "). Resetting consecutive history for all semesters.");
                }
                // --- END ADDITION ---

                for (Semester semester : sortedSemesters) {
                    // Ensure LinkedList exists for this semester on this day
                    LinkedList<Subject> recentSubjects = recentSubjectsMap.get(day)
                            .computeIfAbsent(semester, k -> new LinkedList<>());

                    // --- ADDED: Clear recent history for this specific semester if flagged ---
                    if (clearRecentHistoryForThisSlotForAllSemesters) {
                        if (!recentSubjects.isEmpty()) { // Avoid redundant operations if already empty
                            // System.out.println("DEBUG: Clearing recent subjects for " + semester.getName()); // Optional: more verbose log
                            recentSubjects.clear();
                        }
                    }
                    // --- END ADDITION ---

                    Set<Teacher> busyTeachersThisSlot = teacherBusyMap.get(day).get(slotStart);
                    Map<Teacher, Integer> dailyLoad = teacherDailyLoadMap.get(day);
                    boolean slotFilledForThisSem = false;
                    List<Subject> subjectsToTry = new ArrayList<>(semester.getSubjects());
                    Collections.shuffle(subjectsToTry);

                    for (Subject subject : subjectsToTry) {
                        SubjectContext currentSubjectContext = new SubjectContext(subject, semester);
                        int required = requiredSlotsMap.getOrDefault(currentSubjectContext, 0);
                        int scheduled = scheduledSlotsMap.getOrDefault(currentSubjectContext, 0);

                        if (scheduled >= required) continue;

                        // Consecutive Class Check (using the corrected logic from previous discussions)
                        if (maxConsecutiveSlotsPerSubject > 0 && recentSubjects.size() == maxConsecutiveSlotsPerSubject) {
                            boolean allSameAndNotNull = true;
                            for (Subject recentSub : recentSubjects) {
                                if (recentSub == null || !recentSub.equals(subject)) {
                                    allSameAndNotNull = false;
                                    break;
                                }
                            }
                            if (allSameAndNotNull) {
                                continue;
                            }
                        }

                        List<Teacher> potentialTeachers = subjectToTeacherMap.getOrDefault(subject, Collections.emptyList());
                        if (potentialTeachers.isEmpty()) continue;

                        Teacher availableTeacher = null;
                        Collections.shuffle(potentialTeachers);
                        for (Teacher teacher : potentialTeachers) {
                            int currentDailySlots = dailyLoad.getOrDefault(teacher, 0);
                            if (!busyTeachersThisSlot.contains(teacher) && currentDailySlots < maxSlotsPerTeacherPerDay) {
                                availableTeacher = teacher;
                                break;
                            }
                        }

                        if (availableTeacher != null) {
                            LocalTime slotEnd = slotStart.plusMinutes(slotMinutes);
                            if (slotEnd.isAfter(workEnd)) slotEnd = workEnd; // Ensure slot doesn't exceed workEnd

                            timetable.add(new TimetableEntry(day, slotStart, slotEnd, semester, subject, availableTeacher));
                            busyTeachersThisSlot.add(availableTeacher);
                            scheduledSlotsMap.put(currentSubjectContext, scheduled + 1);

                            recentSubjects.addLast(subject);
                            if (maxConsecutiveSlotsPerSubject > 0 && recentSubjects.size() > maxConsecutiveSlotsPerSubject) {
                                recentSubjects.removeFirst();
                            }

                            dailyLoad.put(availableTeacher, dailyLoad.getOrDefault(availableTeacher, 0) + 1);
                            slotFilledForThisSem = true;
                            break;
                        }
                    } // End subject loop

                    if (!slotFilledForThisSem) {
                        recentSubjects.addLast(null);
                        if (maxConsecutiveSlotsPerSubject > 0 && recentSubjects.size() > maxConsecutiveSlotsPerSubject) {
                            recentSubjects.removeFirst();
                        }
                    }
                } // End semester loop
            } // End slot loop
        } // End day loop

        printUnmetNeeds(requiredSlotsMap, scheduledSlotsMap);
        printTeacherLoad(teacherDailyLoadMap);
        System.out.println("DEBUG: Finished Algorithm V2.");
        return timetable;
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
            String headerText = slotStart.format(TIME_FORMATTER) +" - "+ slotEnd.format(TIME_FORMATTER);
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

        String currentSchoolName = schoolNameField.getText().trim();
        String currentDepartmentName = departmentNameField.getText().trim();

        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("Timetable");

            // --- Step 1: Calculate and DEBUG classSlots ---
            List<LocalTime> classSlots = calculateTimeSlots(workStartTime, workEndTime, breakStartTime, breakEndTime, slotDurationMinutes);
            System.out.println("DEBUG EXCEL EXPORT: workStartTime=" + workStartTime + ", workEndTime=" + workEndTime +
                    ", breakStartTime=" + breakStartTime + ", breakEndTime=" + this.breakEndTime + // Use instance variable if that's what you mean
                    ", slotDurationMinutes=" + slotDurationMinutes);
            System.out.println("DEBUG EXCEL EXPORT: Calculated classSlots for Excel: " + classSlots);
            System.out.println("DEBUG EXCEL EXPORT: Number of classSlots: " + classSlots.size());

            if (classSlots.isEmpty()) {
                showErrorAlert("Export Error", "No valid class time slots found for export. Check time configurations.");
                return;
            }

            // --- Step 2: Determine break column index (1-based for Excel columns) ---
            int breakColumnIndex = -1; // -1 means no break column
            if (breakStartTime != null) { // Only if a break start time is defined
                for (int i = 0; i < classSlots.size(); i++) {
                    if (!classSlots.get(i).isBefore(breakStartTime)) {
                        breakColumnIndex = i + 1; // +1 because Day/Sem is col 0, slots start effectively at col 1 from excelCol perspective
                        break;
                    }
                }
                if (breakColumnIndex == -1 && workEndTime != null && !breakStartTime.isAfter(workEndTime)) { // Break is after all class slots but within work time
                    breakColumnIndex = classSlots.size() + 1;
                }
            }

            // --- Step 3: Define Styles ---
            CellStyle headerStyle = createExcelHeaderStyle(workbook);
            CellStyle dayHeaderStyle = createExcelDayHeaderStyle(workbook);
            CellStyle semesterHeaderStyle = createExcelSemesterHeaderStyle(workbook);
            CellStyle breakStyle = createExcelBreakStyle(workbook);
            CellStyle entryStyle = createExcelEntryStyle(workbook);
            CellStyle emptyStyle = createExcelEmptyStyle(workbook);
            // Define title styles if you haven't already (example)
            CellStyle titleStyle = workbook.createCellStyle(); // from createExcelHeaderStyle(workbook);
            org.apache.poi.ss.usermodel.Font titleFont = workbook.createFont(); // from createExcelHeaderStyle(workbook);
            titleFont.setBold(true); titleFont.setFontHeightInPoints((short) 14); titleStyle.setFont(titleFont);
            titleStyle.setAlignment(HorizontalAlignment.CENTER); titleStyle.setVerticalAlignment(CENTER);

            CellStyle subTitleStyle = workbook.createCellStyle(); // from createExcelHeaderStyle(workbook);
            org.apache.poi.ss.usermodel.Font subTitleFont = workbook.createFont(); // from createExcelHeaderStyle(workbook);
            subTitleFont.setFontHeightInPoints((short) 12); subTitleStyle.setFont(subTitleFont);
            subTitleStyle.setAlignment(HorizontalAlignment.CENTER); subTitleStyle.setVerticalAlignment(CENTER);


            // --- Step 4: Manage Row Indexing and Add Titles ---
            int currentRowNum = 0; // Current row index for sheet.createRow()

            // Calculate the number of columns the actual timetable grid will use.
            // This is 1 (for Day/Sem) + number of classSlots + (1 if a break column is added).
            int numTimetableGridColumns = 1 + classSlots.size();
            if (breakColumnIndex != -1) { // If a break column will be inserted
                numTimetableGridColumns++;
            }

            if (!currentSchoolName.isEmpty()) {
                Row schoolNameRow = sheet.createRow(currentRowNum++);
                Cell schoolCell = schoolNameRow.createCell(0);
                schoolCell.setCellValue(currentSchoolName);
                schoolCell.setCellStyle(titleStyle); // Apply your title style
                if (numTimetableGridColumns > 1) {
                    sheet.addMergedRegion(new CellRangeAddress(schoolNameRow.getRowNum(), schoolNameRow.getRowNum(), 0, numTimetableGridColumns - 1));
                }
                schoolNameRow.setHeightInPoints(20);
            }

            if (!currentDepartmentName.isEmpty()) {
                Row deptNameRow = sheet.createRow(currentRowNum++);
                Cell deptCell = deptNameRow.createCell(0);
                deptCell.setCellValue(currentDepartmentName);
                deptCell.setCellStyle(subTitleStyle); // Apply your subtitle style
                if (numTimetableGridColumns > 1) {
                    sheet.addMergedRegion(new CellRangeAddress(deptNameRow.getRowNum(), deptNameRow.getRowNum(), 0, numTimetableGridColumns - 1));
                }
                deptNameRow.setHeightInPoints(18);
            }

            if (!currentSchoolName.isEmpty() || !currentDepartmentName.isEmpty()) {
                currentRowNum++; // Add a spacer row
            }

            // --- Step 5: Create Timetable Header Row ---
            Row timetableHeaderRow = sheet.createRow(currentRowNum++); // Use current row number
            Cell topLeftCell = timetableHeaderRow.createCell(0);
            topLeftCell.setCellValue("Day / Sem");
            topLeftCell.setCellStyle(headerStyle);

            int currentExcelCol = 1; // For columns after "Day / Sem"
            for (int i = 0; i < classSlots.size(); i++) {
                // Check if this is the position for the break column header
                if (breakColumnIndex != -1 && currentExcelCol == breakColumnIndex) {
                    Cell cell = timetableHeaderRow.createCell(currentExcelCol++);
                    cell.setCellValue("BREAK");
                    cell.setCellStyle(headerStyle); // Or a specific break header style
                }
                // Add the class slot header
                LocalTime slotStart = classSlots.get(i);
                LocalTime slotEnd = slotStart.plusMinutes(slotDurationMinutes);
                Cell cell = timetableHeaderRow.createCell(currentExcelCol++);
                cell.setCellValue(slotStart.format(TIME_FORMATTER) + "-" + slotEnd.format(TIME_FORMATTER));
                cell.setCellStyle(headerStyle);
            }
            // If break is the very last column (after all class slots)
            if (breakColumnIndex != -1 && breakColumnIndex == classSlots.size() + 1) {
                Cell cell = timetableHeaderRow.createCell(currentExcelCol++);
                cell.setCellValue("BREAK");
                cell.setCellStyle(headerStyle); // Or a specific break header style
            }
            // int totalExcelColsPopulated = currentExcelCol; // This is now the count of populated columns (1-based next index)

            // --- Step 6: Populate Data Rows ---
            List<Semester> sortedSemesters = semesterList.stream().sorted(Comparator.comparing(Semester::getName)).collect(Collectors.toList());
            Map<String, Map<Semester, Map<LocalTime, TimetableEntry>>> groupedEntries = lastGeneratedTimetable.stream()
                    .collect(Collectors.groupingBy(TimetableEntry::getDay,
                            Collectors.groupingBy(TimetableEntry::getSemester,
                                    Collectors.toMap(TimetableEntry::getStartTime, entry -> entry, (e1, e2) -> e1))));

            for (String day : WORKING_DAYS) {
                Row dayRow = sheet.createRow(currentRowNum++);
                Cell dayCell = dayRow.createCell(0);
                dayCell.setCellValue(day);
                dayCell.setCellStyle(dayHeaderStyle);
                // Merge the day cell across the width of the timetable grid
                if (numTimetableGridColumns > 1) {
                    sheet.addMergedRegion(new CellRangeAddress(dayRow.getRowNum(), dayRow.getRowNum(), 0, numTimetableGridColumns - 1));
                }


                for (Semester semester : sortedSemesters) {
                    Row semRow = sheet.createRow(currentRowNum++);
                    Cell semCell = semRow.createCell(0);
                    semCell.setCellValue(semester.getName());
                    semCell.setCellStyle(semesterHeaderStyle);

                    Map<LocalTime, TimetableEntry> semesterDayEntries = groupedEntries.getOrDefault(day, Collections.emptyMap())
                            .getOrDefault(semester, Collections.emptyMap());

                    currentExcelCol = 1; // Reset for each semester row
                    for (int i = 0; i < classSlots.size(); i++) {
                        if (breakColumnIndex != -1 && currentExcelCol == breakColumnIndex) {
                            Cell cell = semRow.createCell(currentExcelCol++);
                            cell.setCellValue("BREAK");
                            cell.setCellStyle(breakStyle);
                        }
                        LocalTime slotStart = classSlots.get(i);
                        Cell cell = semRow.createCell(currentExcelCol++);
                        TimetableEntry entry = semesterDayEntries.get(slotStart);
                        if (entry != null) {
                            cell.setCellValue(entry.getSubject().getName() + "\n(" + entry.getTeacher().getName() + ")");
                            cell.setCellStyle(entryStyle);
                        } else {
                            cell.setCellStyle(emptyStyle); // Just apply style for empty cells
                        }
                    }
                    if (breakColumnIndex != -1 && breakColumnIndex == classSlots.size() + 1) {
                        Cell cell = semRow.createCell(currentExcelCol++);
                        cell.setCellValue("BREAK");
                        cell.setCellStyle(breakStyle);
                    }
                }
            }

            // --- Step 7: Auto-size columns ---
            // Use numTimetableGridColumns as it represents the actual number of columns in the timetable grid.
            for (int i = 0; i < numTimetableGridColumns; i++) {
                sheet.autoSizeColumn(i);
            }
            // Ensure Day/Sem column is wide enough if autoSize is not perfect
            if (numTimetableGridColumns > 0) {
                sheet.setColumnWidth(0, 30 * 256); // Wider first column
            }


            try (FileOutputStream fileOut = new FileOutputStream(file)) {
                workbook.write(fileOut);
                showInfoAlert("Export Successful", "Timetable exported to:\n" + file.getName());
            }
        } catch (IOException e) {
            System.err.println("Error exporting to Excel: " + e.getMessage());
            e.printStackTrace();
            showErrorAlert("Export Error", "Could not export timetable to Excel.\nError: " + e.getMessage());
        } catch (Exception e) {
            System.err.println("Unexpected error during Excel export: " + e.getMessage());
            e.printStackTrace();
            showErrorAlert("Export Error", "An unexpected error occurred during Excel export.");
        }
    }
    /**
     * Handles Export to PDF action
     */
    private void handleExportPdf() {
        if (lastGeneratedTimetable == null || lastGeneratedTimetable.isEmpty()) {
            showInfoAlert("Export Info", "No timetable data available to export.");
            return;
        }

        String currentSchoolName = schoolNameField.getText().trim();
        String currentDepartmentName = departmentNameField.getText().trim();

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
            // --- ADDED: Add School/Department Name to PDF Document ---
            if (!currentSchoolName.isEmpty()) {
                Paragraph schoolP = new Paragraph(currentSchoolName)
                        .setFont(PdfFontFactory.createFont(StandardFonts.HELVETICA_BOLD))
                        .setFontSize(16)
                        .setTextAlignment(TextAlignment.CENTER)
                        .setMarginBottom(5); // Space below school name
                document.add(schoolP);
            }

            if (!currentDepartmentName.isEmpty()) {
                Paragraph deptP = new Paragraph(currentDepartmentName)
                        .setFont(PdfFontFactory.createFont(StandardFonts.HELVETICA))
                        .setFontSize(14)
                        .setTextAlignment(TextAlignment.CENTER)
                        .setMarginBottom(10); // Space below department name, before table
                document.add(deptP);
            }
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
    // In createMenuBar():
// loadItem.setOnAction(e -> handleLoadConfiguration(stage)); // Uncomment this

// New or modified handleLoadConfiguration method:
    private void handleLoadConfiguration(Stage stage) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Load Configuration File");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("JSON Files (*.json)", "*.json"));
        File file = fileChooser.showOpenDialog(stage);

        if (file != null) {
            try {
                ObjectMapper objectMapper = new ObjectMapper();
                AppConfiguration loadedConfig = objectMapper.readValue(file, AppConfiguration.class);

                // --- Clear existing state ---
                resetUIAndDataToDefaults(); // You'll need a helper method for this

                // --- Populate UI and internal state from loadedConfig ---
                schoolNameField.setText(loadedConfig.schoolName);
                departmentNameField.setText(loadedConfig.departmentName);

                // Time Configuration
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
                updateWorkingHoursDisplay(); // Recalculate and display derived values
                // Simulate "Next" click for time config to enable semester section if times are valid
                if (validateAndStoreTimeConfig()) { // This also stores values internally
                    showSemesterSection();
                } else {
                    showErrorAlert("Load Warning", "Loaded time configuration is invalid. Please review.");
                    // Decide how to proceed - maybe stop loading or let user fix
                }


                // Semester Configuration
                if (loadedConfig.semesterConfigurations != null && !loadedConfig.semesterConfigurations.isEmpty()) {
                    semCountComboBox.setValue(loadedConfig.semesterConfigurations.size()); // This triggers buildSemesterSubjectGrid

                    // buildSemesterSubjectGrid will create the basic structure.
                    // We need to wait for it and then populate the values.
                    // This part is tricky due to UI updates. Platform.runLater might be needed.
                    Platform.runLater(() -> {
                        populateSemesterGridFromConfig(loadedConfig.semesterConfigurations);
                        // After semesters are loaded, their subjects are known.
                        // We can now populate allSubjectsObservableList, which is needed for teacher assignment.
                        // This logic should be similar to what handleSemesterNextClick does after validation.
                        semesterList.clear(); // Clear before repopulating from loaded config
                        allSubjectsObservableList.clear();

                        for (SemesterConfigData semConfig : loadedConfig.semesterConfigurations) {
                            Semester semester = new Semester(semConfig.name());
                            for (SubjectDetailConfig subConfig : semConfig.subjects()) {
                                Subject subject = new Subject(subConfig.name(), subConfig.hours());
                                semester.getSubjects().add(subject);
                                allSubjectsObservableList.add(new SubjectContext(subject, semester));
                            }
                            semesterList.add(semester);
                        }
                        allSubjectsObservableList.sort(Comparator.comparing((SubjectContext sc) -> sc.getSubject().getName()).thenComparing(sc -> sc.getSemester().getName()));


                        // Teacher Configuration (should run after semesters are fully processed and allSubjectsObservableList is ready)
                        if (loadedConfig.teacherConfigurations != null && !loadedConfig.teacherConfigurations.isEmpty()) {
                            showTeacherSection(); // Make sure it's visible
                            teacherCountComboBox.setValue(loadedConfig.teacherConfigurations.size()); // Triggers populateTeacherMappingGrid

                            Platform.runLater(() -> { // Another Platform.runLater might be needed for teacher grid population
                                populateTeacherGridFromConfig(loadedConfig.teacherConfigurations);
                                checkIfReadyToGenerate(); // Enable generate button if everything is valid
                            });
                        } else {
                            hideTeacherSection();
                        }
                    });
                } else {
                    hideSemesterSection();
                    hideTeacherSection();
                }


                showInfoAlert("Load Configuration", "Configuration loaded successfully from:\n" + file.getName() + "\nPlease review the loaded settings.");
                configLoadedFromFile = true; // Your existing flag
                // generateButton.setDisable(false); // This should be handled by checkIfReadyToGenerate()

            } catch (IOException e) {
                System.err.println("Error loading configuration file: " + e.getMessage());
                e.printStackTrace();
                showErrorAlert("Load Error", "Error loading configuration file:\n" + e.getMessage());
            } catch (Exception e) {
                System.err.println("An unexpected error occurred during configuration load: " + e.getMessage());
                e.printStackTrace();
                showErrorAlert("Load Error", "An unexpected error occurred during configuration load.");
            }
        }
    }

    // Helper method to reset UI (you'll need to expand this)
    private void resetUIAndDataToDefaults() {
        schoolNameField.clear();
        departmentNameField.clear();

        // Reset time combos to defaults or clear them
        workStartTimeCombo.setValue("10:00"); // Or some initial default
        workEndTimeCombo.setValue("16:00");
        // ... and so on for other time fields, spinners

        semCountComboBox.setValue(null); // This should clear the semester grid
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
        generatedScrollPane.setContent(new Label("Please provide input configuration first.")); // Reset timetable tab
        if (mainTabPane.getSelectionModel().getSelectedItem() == generatedTimetableTab) {
            mainTabPane.getSelectionModel().selectFirst(); // Switch to input tab
        }
        lastGeneratedTimetable = null;
        // --- State for Loaded Configuration ---
        String loadedJsonData = null;
        configLoadedFromFile = false;
    }


    // Helper to populate semester grid from loaded config
    private void populateSemesterGridFromConfig(List<SemesterConfigData> semesterConfigurations) {
        for (int i = 0; i < semesterConfigurations.size(); i++) {
            SemesterConfigData semConfig = semesterConfigurations.get(i);
            TextField semNameField = (TextField) findNodeInGrid(semesterSubjectGrid, i, 0);
            VBox subjectInputArea = (VBox) findNodeInGrid(semesterSubjectGrid, i, 1);

            if (semNameField != null) semNameField.setText(semConfig.name());

            if (subjectInputArea != null) {
                ComboBox<Integer> subCountCombo = null;
                FlowPane subDetailPane = null;
                for(Node n : subjectInputArea.getChildren()){
                    if(n instanceof ComboBox) subCountCombo = (ComboBox<Integer>) n;
                    else if (n instanceof FlowPane) subDetailPane = (FlowPane) n;
                }

                if (subCountCombo != null) {
                    subCountCombo.setValue(semConfig.subjects().size()); // This triggers creation of subject rows in subDetailPane

                    // Now populate the dynamically created subject rows
                    final FlowPane finalSubDetailPane = subDetailPane; // For use in runLater
                    final List<SubjectDetailConfig> finalSubjectDetails = semConfig.subjects();
                    Platform.runLater(()-> { // Ensure UI elements are ready
                        if (finalSubDetailPane != null && finalSubDetailPane.getChildren().size() == finalSubjectDetails.size()) {
                            for (int j = 0; j < finalSubjectDetails.size(); j++) {
                                SubjectDetailConfig subDetail = finalSubjectDetails.get(j);
                                Node entryBoxNode = finalSubDetailPane.getChildren().get(j);
                                if (entryBoxNode instanceof HBox) {
                                    TextField subNameNode = null;
                                    Spinner<Integer> hoursSpinnerNode = null;
                                    for (Node item : ((HBox) entryBoxNode).getChildrenUnmodifiable()) {
                                        if (item instanceof TextField) subNameNode = (TextField) item;
                                        else if (item instanceof Spinner) hoursSpinnerNode = (Spinner<Integer>) item;
                                    }
                                    if (subNameNode != null) subNameNode.setText(subDetail.name());
                                    if (hoursSpinnerNode != null) hoursSpinnerNode.getValueFactory().setValue(subDetail.hours());
                                }
                            }
                        } else {
                            System.err.println("Load warning: Mismatch in subject detail pane structure for semester " + semConfig.name());
                        }
                    });
                }
            }
        }
    }

    // Helper to populate teacher grid from loaded config
    private void populateTeacherGridFromConfig(List<TeacherConfigData> teacherConfigurations) {
        for (int i = 0; i < teacherConfigurations.size(); i++) {
            TeacherConfigData teacherConfig = teacherConfigurations.get(i);
            TextField teachNameField = (TextField) findNodeInGrid(teacherMappingGrid, i, 0);
            VBox teacherInputsVBox = (VBox) findNodeInGrid(teacherMappingGrid, i, 1);

            if (teachNameField != null) teachNameField.setText(teacherConfig.name());

            if (teacherInputsVBox != null) {
                ComboBox<Integer> subTeachCountCombo = null;
                FlowPane tSubMapFlowPane = null;
                for(Node n : teacherInputsVBox.getChildren()){
                    if(n instanceof ComboBox) subTeachCountCombo = (ComboBox<Integer>) n;
                    else if (n instanceof FlowPane) tSubMapFlowPane = (FlowPane) n;
                }

                if (subTeachCountCombo != null) {
                    subTeachCountCombo.setValue(teacherConfig.assignedSubjects().size()); // Triggers creation of subject ComboBoxes

                    final FlowPane finalTSubMapFlowPane = tSubMapFlowPane;
                    final List<TeacherAssignmentConfig> finalAssignments = teacherConfig.assignedSubjects();
                    Platform.runLater(() -> { // Ensure UI elements (subject ComboBoxes) are ready
                        if (finalTSubMapFlowPane != null && finalTSubMapFlowPane.getChildren().size() == finalAssignments.size()) {
                            for (int j = 0; j < finalAssignments.size(); j++) {
                                TeacherAssignmentConfig assignment = finalAssignments.get(j);
                                Node cbNode = finalTSubMapFlowPane.getChildren().get(j);
                                if (cbNode instanceof ComboBox) {
                                    @SuppressWarnings("unchecked")
                                    ComboBox<SubjectContext> subjectSelectCombo = (ComboBox<SubjectContext>) cbNode;
                                    // Find the matching SubjectContext from allSubjectsObservableList
                                    Optional<SubjectContext> contextToSelect = allSubjectsObservableList.stream()
                                            .filter(sc -> sc.getSubject().getName().equals(assignment.subjectName()) &&
                                                    sc.getSemester().getName().equals(assignment.semesterName()))
                                            .findFirst();
                                    contextToSelect.ifPresent(subjectSelectCombo::setValue);
                                }
                            }
                        } else {
                            System.err.println("Load warning: Mismatch in teacher subject assignment structure for " + teacherConfig.name());
                        }
                    });
                }
            }
        }
    }
    // In createMenuBar():
// saveItem.setOnAction(e -> handleSaveConfiguration(stage)); // Uncomment this

    // New or modified handleSaveConfiguration method:
    private void handleSaveConfiguration(Stage stage) {
        AppConfiguration currentConfig = new AppConfiguration();

        // 1. Gather Data from UI and internal state
        currentConfig.schoolName = schoolNameField.getText();
        currentConfig.departmentName = departmentNameField.getText();

        // Time settings
        String ws = workStartTimeCombo.getValue();
        String we = workEndTimeCombo.getValue();
        String bs = breakStartTimeCombo.getValue();
        String be = breakEndTimeCombo.getValue();
        Integer sd = slotDurationCombo.getValue();
        Integer mts = maxTeacherSlotsSpinner.getValue();
        currentConfig.maxConsecutiveSlotsPerSubject = maxConsecutiveSubjectSpinner.getValue();


        if (ws == null || we == null || bs == null || be == null || sd == null || mts == null || currentConfig.maxConsecutiveSlotsPerSubject == null) {
            showErrorAlert("Save Error", "Cannot save - incomplete time configuration data. Please ensure all time settings are filled.");
            return;
        }
        currentConfig.timeSettings = new TimeConfiguration(ws, we, bs, be, sd, mts); // Assuming your TimeConfiguration record

        // Semester Configurations
        currentConfig.semesterConfigurations = new ArrayList<>();
        // Iterate through your semesterSubjectGrid or the semesterList if it's reliably populated by this point
        // For simplicity, let's assume semesterList and their subjects are correctly populated
        // This requires that handleSemesterNextClick successfully populated semesterList.
        if (semesterList.isEmpty() && semCountComboBox.getValue() != null && semCountComboBox.getValue() > 0) {
            showErrorAlert("Save Error", "Semester data not fully processed. Please click 'Next' in the semester section first.");
            return;
        }
        for (Semester sem : semesterList) {
            List<SubjectDetailConfig> subjectDetails = sem.getSubjects().stream()
                    .map(sub -> new SubjectDetailConfig(sub.getName(), sub.getWeeklyHours()))
                    .collect(Collectors.toList());
            currentConfig.semesterConfigurations.add(new SemesterConfigData(sem.getName(), subjectDetails));
        }

        // Teacher Configurations
        currentConfig.teacherConfigurations = new ArrayList<>();
        // This is more complex as you need to get data from the teacherMappingGrid
        Integer expectedTeacherCount = teacherCountComboBox.getValue();
        if (expectedTeacherCount == null || expectedTeacherCount <= 0) {
            // No teachers to save, or not configured yet. Decide if this is an error or just save empty.
        } else {
            for (int i = 0; i < expectedTeacherCount; i++) {
                TextField teachNameField = (TextField) findNodeInGrid(teacherMappingGrid, i, 0);
                VBox teacherInputsVBox = (VBox) findNodeInGrid(teacherMappingGrid, i, 1);
                if (teachNameField == null || teacherInputsVBox == null) continue; // Should not happen if UI is consistent

                String teacherName = teachNameField.getText();
                if (teacherName.trim().isEmpty()) continue; // Skip unnamed teachers

                List<TeacherAssignmentConfig> assignments = new ArrayList<>();
                FlowPane tSubMapFlowPane = null;
                for(Node n : teacherInputsVBox.getChildren()){
                    if(n instanceof FlowPane) tSubMapFlowPane = (FlowPane) n;
                }

                if (tSubMapFlowPane != null) {
                    for (Node cbNode : tSubMapFlowPane.getChildren()) {
                        if (cbNode instanceof ComboBox) {
                            @SuppressWarnings("unchecked")
                            ComboBox<SubjectContext> subjectSelectCombo = (ComboBox<SubjectContext>) cbNode;
                            SubjectContext selected = subjectSelectCombo.getValue();
                            if (selected != null) {
                                assignments.add(new TeacherAssignmentConfig(selected.getSubject().getName(), selected.getSemester().getName()));
                            }
                        }
                    }
                }
                currentConfig.teacherConfigurations.add(new TeacherConfigData(teacherName, assignments));
            }
        }


        // 2. Choose File
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Save Configuration File");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("JSON Files (*.json)", "*.json"));
        fileChooser.setInitialFileName("classmesh_config.json");
        File file = fileChooser.showSaveDialog(stage);

        if (file != null) {
            try {
                ObjectMapper objectMapper = new ObjectMapper();
                // Make the JSON output human-readable (pretty print)
                objectMapper.enable(SerializationFeature.INDENT_OUTPUT);
                objectMapper.writeValue(file, currentConfig);
                showInfoAlert("Save Configuration", "Configuration saved successfully to:\n" + file.getName());
            } catch (IOException e) {
                System.err.println("Error saving configuration file: " + e.getMessage());
                e.printStackTrace();
                showErrorAlert("Save Error", "Error saving configuration file:\n" + e.getMessage());
            }
        }
    }
//    /**
     /* Helper method to create the iText Table for PDF export
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
            String headerText = slotStart.format(TIME_FORMATTER) + " - " + slotEnd.format(TIME_FORMATTER);
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
    /**
     * Displays the About dialog box, including a round/circular application logo.
     */
    private void showAboutDialog() {
        Alert aboutAlert = new Alert(Alert.AlertType.INFORMATION);
        aboutAlert.setTitle("About " + APP_NAME);
        aboutAlert.setHeaderText(APP_NAME + " - Version " + APP_VERSION);

        ImageView logoView = null;
        double fitSize = 80.0; // Define the desired diameter for the circular logo

        try (InputStream logoStream = getClass().getResourceAsStream("/logo.png")) { // Adjust filename if needed
            if (logoStream != null) {
                Image logoImage = new Image(logoStream);
                logoView = new ImageView(logoImage);
                // Set both fitWidth and fitHeight to the same value for a square aspect ratio before clipping
                logoView.setFitWidth(fitSize);
                logoView.setFitHeight(fitSize);
                // Note: setPreserveRatio(true) might conflict if original image isn't square and you set both width/height.
                // We'll control aspect via fitWidth/fitHeight being equal for the circular clip.
                // logoView.setPreserveRatio(true); // Can be commented out or removed when setting both dimensions
                logoView.setSmooth(true);

                // --- Create and Apply Circular Clip ---
                double radius = fitSize / 2.0;
                Circle clip = new Circle(radius);
                // Set the center of the circle to be the center of the ImageView's defined size
                clip.setCenterX(radius);
                clip.setCenterY(radius);
                logoView.setClip(clip);
                // --- End Circular Clip ---

            } else {
                System.err.println("Warning: logo.png not found in classpath root.");
            }
        } catch (Exception e) {
            System.err.println("Error loading logo image: " + e.getMessage());
        }


        String contentText = String.format(
                        "%s Version: %s\n\n" +
                        "Developed by: %s\n" +
                        "Mail: %s\n" +
                        "Contact: %s\n\n" +
                        "This application helps generate class timetables automatically without teacher conflictions.\n\n" ,
                APP_NAME, APP_VERSION,
                DEVELOPER_NAME,
                DEVELOPER_EMAIL,
                DEVELOPER_CONTACT
        );

        Label contentLabel = new Label(contentText);
        contentLabel.setWrapText(true);
        contentLabel.setPadding(new Insets(10, 0, 0, 0));

        // Create Layout for Dialog Content (same as before)
        VBox dialogLayout = new VBox(10);
        dialogLayout.setAlignment(Pos.CENTER);
        dialogLayout.setPadding(new Insets(10));

        if (logoView != null) {
            dialogLayout.getChildren().add(logoView);
        }
        dialogLayout.getChildren().add(contentLabel);

        // Set the custom layout as the content (same as before)
        aboutAlert.getDialogPane().setContent(dialogLayout);
        aboutAlert.getDialogPane().setPrefWidth(400);

        // Set Icon for the Dialog Window (same as before)
        Stage alertStage = (Stage) aboutAlert.getDialogPane().getScene().getWindow();
        loadStageIcon(alertStage);

        aboutAlert.showAndWait();


    }
    // --- Manual JSON Conversion Helpers ---
    private String convertMapToJson(Map<String, Object> map) { return convertMapToJson(map, 1); }
    private String convertMapToJson(Map<String, Object> map, int indent) { String indentStr = "  ".repeat(indent); String innerIndentStr = "  ".repeat(indent + 1); StringBuilder sb = new StringBuilder("{\n"); List<String> keys = new ArrayList<>(map.keySet()); for (int i = 0; i < keys.size(); i++) { String key = keys.get(i); Object value = map.get(key); sb.append(innerIndentStr).append(escapeJsonString(key)).append(": "); sb.append(convertToJsonValue(value, indent + 1)); if (i < keys.size() - 1) sb.append(","); sb.append("\n"); } sb.append(indentStr).append("}"); return sb.toString(); }
    private String convertListToJson(List<?> list, int indent) { String indentStr = "  ".repeat(indent); String innerIndentStr = "  ".repeat(indent + 1); StringBuilder sb = new StringBuilder("[\n"); for (int i = 0; i < list.size(); i++) { sb.append(innerIndentStr).append(convertToJsonValue(list.get(i), indent + 1)); if (i < list.size() - 1) sb.append(","); sb.append("\n"); } sb.append(indentStr).append("]"); return sb.toString(); }
    private String convertToJsonValue(Object value, int indent) { if (value == null) return "null"; else if (value instanceof String) return escapeJsonString((String) value); else if (value instanceof Number || value instanceof Boolean) return value.toString(); else if (value instanceof Map) return convertMapToJson((Map<String, Object>) value, indent); else if (value instanceof List) return convertListToJson((List<?>) value, indent); else return escapeJsonString(value.toString()); }
    private String escapeJsonString(String input) { if (input == null) return "null"; StringBuilder sb = new StringBuilder(); sb.append('"'); for (char c : input.toCharArray()) { switch (c) { case '"': sb.append("\\\""); break; case '\\': sb.append("\\\\"); break; case '\b': sb.append("\\b"); break; case '\f': sb.append("\\f"); break; case '\n': sb.append("\\n"); break; case '\r': sb.append("\\r"); break; case '\t': sb.append("\\t"); break; default: sb.append(c); } } sb.append('"'); return sb.toString(); }
    // --- End JSON Helpers ---

    private List<String> getCurrentMacAddresses() {
        List<String> macAddressList = new ArrayList<>();
        try {
            Enumeration<NetworkInterface> networkInterfaces = NetworkInterface.getNetworkInterfaces();
            while (networkInterfaces.hasMoreElements()) {
                NetworkInterface ni = networkInterfaces.nextElement();
                if (ni.isLoopback() || ni.isVirtual() || !ni.isUp()) {
                    continue;
                }
                byte[] hardwareAddress = ni.getHardwareAddress();
                if (hardwareAddress != null) {
                    StringBuilder macBuilder = new StringBuilder();
                    for (int i = 0; i < hardwareAddress.length; i++) {
                        macBuilder.append(String.format("%02X%s", hardwareAddress[i], (i < hardwareAddress.length - 1) ? "-" : ""));
                    }
                    macAddressList.add(macBuilder.toString());
                }
            }
        } catch (SocketException e) {
            System.err.println("Security Check: Could not retrieve MAC address. " + e.getMessage());
            // Optionally, show an error to the user here or log more extensively
            // For simplicity, we'll return an empty list, which will cause the check to fail
            // if ALLOWED_MAC_ADDRESS is not "none" or something.
        }
        return macAddressList;
    }

    /**
     * Checks if the application is allowed to run on the current machine
     * based on its MAC address.
     * @return true if allowed, false otherwise.
     */
    private boolean isOperationAllowed() {
        if (ALLOWED_MAC_ADDRESS == null || ALLOWED_MAC_ADDRESS.trim().isEmpty() || "XX-XX-XX-XX-XX-XX".equalsIgnoreCase(ALLOWED_MAC_ADDRESS)) {
            // Fallback for placeholder or unconfigured MAC - you might want to make this stricter
            System.out.println("Security Warning: Allowed MAC Address is not configured or is a placeholder. Allowing execution for development.");

            List<String> currentMacs = getCurrentMacAddresses();
            if (currentMacs.isEmpty()) {
                System.out.println("Security Check: No MAC addresses found on this system.");
            } else {
                System.out.println("Security Check: Current system MAC Addresses: " + currentMacs);
            }
            System.out.println("Security Check: Please configure a valid ALLOWED_MAC_ADDRESS in the code.");
            // For the purpose of this example, if the default placeholder is still there, we'll print MACs and allow.
            // Change this to `return false;` for a production build with a real MAC.
            if ("XX-XX-XX-XX-XX-XX".equalsIgnoreCase(ALLOWED_MAC_ADDRESS)) {
                showErrorAlert("MAC Address Not Configured",
                        "The application's allowed MAC address is not configured.\n\n" +
                                "Current system MACs: " + currentMacs + "\n\n" +
                                "Please contact support or the developer.");
                return false; // More secure default for unconfigured
            }
            return true; // Or handle as an error
        }

        List<String> currentMacs = getCurrentMacAddresses();
        if (currentMacs.isEmpty()) {
            System.err.println("Security Check: Failed to retrieve any MAC addresses from this system.");
            return false;
        }

        System.out.println("Security Check: Verifying against Allowed MAC: " + ALLOWED_MAC_ADDRESS);
        System.out.println("Security Check: Current system MAC Addresses: " + currentMacs);

        for (String currentMac : currentMacs) {
            if (ALLOWED_MAC_ADDRESS.equalsIgnoreCase(currentMac)) {
                System.out.println("Security Check: MAC Address match found. Application allowed.");
                return true;
            }
        }

        System.err.println("Security Check: MAC Address mismatch. Application not allowed on this device.");
        return false;
    }

    // --- Main Method ---
    public static void main(String[] args) {
        launch(args);
    }
}

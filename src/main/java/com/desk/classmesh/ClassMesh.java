package com.desk.classmesh; // Make sure this matches your project structure

import javafx.application.Application;
import javafx.application.Platform;
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

import java.io.BufferedWriter; // Keep for potential Save later
import java.io.File; // Keep for potential Save/Load later
import java.io.FileWriter; // Keep for potential Save later
import java.io.IOException; // Keep for potential Save/Load later
import java.io.InputStream;
import java.nio.file.Files; // Keep for potential Load later
import java.nio.file.Paths; // Keep for potential Load later
import java.time.Duration;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.*;
import java.util.regex.Matcher; // Keep for potential Load later
import java.util.regex.Pattern; // Keep for potential Load later
import java.util.stream.Collectors;

// --- Data Model Classes (From User's Code) ---

class Subject {
    String name;
    int weeklyHours; // Added hours

    public Subject(String name, int hours) {
        this.name = name;
        this.weeklyHours = hours;
    }
    public String getName() { return name; }
    public int getWeeklyHours() { return weeklyHours; }

    // Unique identifier including hours for tracking needs
    public String getUniqueId() { return name + "_hrs" + weeklyHours; }

    @Override public String toString() { return name; }
    // Equals/HashCode should consider name and potentially hours if it defines uniqueness
    @Override public boolean equals(Object o) { if (this == o) return true; if (o == null || getClass() != o.getClass()) return false; Subject subject = (Subject) o; return weeklyHours == subject.weeklyHours && Objects.equals(name, subject.name); }
    @Override public int hashCode() { return Objects.hash(name, weeklyHours); }
}

class Semester {
    String name;
    List<Subject> subjects = new ArrayList<>(); // List of Subject objects
    public Semester(String name) { this.name = name; }
    public String getName() { return name; }
    public List<Subject> getSubjects() { return subjects; }
    @Override public String toString() { return name; }
    @Override public boolean equals(Object o) { if (this == o) return true; if (o == null || getClass() != o.getClass()) return false; Semester semester = (Semester) o; return Objects.equals(name, semester.name); }
    @Override public int hashCode() { return Objects.hash(name); }
}

class Teacher {
    String name;
    List<Subject> subjectsTaught = new ArrayList<>(); // Subjects this teacher CAN teach
    public Teacher(String name) { this.name = name; }
    public String getName() { return name; }
    public List<Subject> getSubjectsTaught() { return subjectsTaught; }
    // Helper to check if teacher can teach a specific subject instance (name + hours)
    public boolean canTeach(Subject subject) {
        // Check if teacher can teach a subject with the same name and hours
        return subjectsTaught.stream().anyMatch(taughtSub -> taughtSub.equals(subject));
    }
    @Override public String toString() { return name; }
    @Override public boolean equals(Object o) { if (this == o) return true; if (o == null || getClass() != o.getClass()) return false; Teacher teacher = (Teacher) o; return Objects.equals(name, teacher.name); }
    @Override public int hashCode() { return Objects.hash(name); }
}

// Wrapper class for ComboBox display and tracking assignments
class SubjectContext {
    private final Subject subject; private final Semester semester;
    public SubjectContext(Subject subject, Semester semester) { this.subject = subject; this.semester = semester; }
    public Subject getSubject() { return subject; } public Semester getSemester() { return semester; }
    // Unique identifier for tracking required/scheduled hours
    public String getUniqueId() { return subject.getName() + "|" + semester.getName(); } // Use Name only for uniqueness across semesters? Or include hours? Let's use Name + Semester
    @Override public String toString() { return subject.getName() + " (" + semester.getName() + ")"; }
    @Override public boolean equals(Object o) { if (this == o) return true; if (o == null || getClass() != o.getClass()) return false; SubjectContext that = (SubjectContext) o; return Objects.equals(subject, that.subject) && Objects.equals(semester, that.semester); }
    @Override public int hashCode() { return Objects.hash(subject, semester); }
}

// Timetable Entry - Represents one scheduled slot
class TimetableEntry {
    String day; LocalTime startTime; LocalTime endTime; Semester semester; Subject subject; Teacher teacher;
    public TimetableEntry(String day, LocalTime startTime, LocalTime endTime, Semester semester, Subject subject, Teacher teacher) { this.day = day; this.startTime = startTime; this.endTime = endTime; this.semester = semester; this.subject = subject; this.teacher = teacher; }
    public String getDay() { return day; } public LocalTime getStartTime() { return startTime; } public LocalTime getEndTime() { return endTime; } public Semester getSemester() { return semester; } public Subject getSubject() { return subject; } public Teacher getTeacher() { return teacher; }
    public String getTimeSlotString() { DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm"); return startTime.format(formatter) + " - " + endTime.format(formatter); }
}
// --- End Data Model Classes ---

// --- Data Structures for Save Simulation (Keep for now, but gathering needs update) ---
record SubjectDetail(String name, int hours) { public Map<String, Object> toMap() { Map<String, Object> map = new LinkedHashMap<>(); map.put("name", name); map.put("hours", hours); return map; } }
record SemesterConfig(String name, List<SubjectDetail> subjects) { public Map<String, Object> toMap() { Map<String, Object> map = new LinkedHashMap<>(); map.put("name", name); map.put("subjects", subjects.stream().map(SubjectDetail::toMap).collect(Collectors.toList())); return map; } }
record TeacherAssignment(String subjectName, String semesterName) { public Map<String, Object> toMap() { Map<String, Object> map = new LinkedHashMap<>(); map.put("subjectName", subjectName); map.put("semesterName", semesterName); return map; } }
record TeacherConfig(String name, List<TeacherAssignment> assignedSubjects) { public Map<String, Object> toMap() { Map<String, Object> map = new LinkedHashMap<>(); map.put("name", name); map.put("assignedSubjects", assignedSubjects.stream().map(TeacherAssignment::toMap).collect(Collectors.toList())); return map; } }
record TimeConfiguration(String workStart, String workEnd, String breakStart, String breakEnd, int slotDuration) {
    public Map<String, Object> toMap() { Map<String, Object> map = new LinkedHashMap<>(); map.put("workStart", workStart); map.put("workEnd", workEnd); map.put("breakStart", breakStart); map.put("breakEnd", breakEnd); map.put("slotDuration", slotDuration); return map; }
    // Keep basic regex parser for potential use if Load is revisited
    public static TimeConfiguration fromJsonSnippet(String jsonSnippet) { String ws = extractJsonStringValue(jsonSnippet, "workStart"); String we = extractJsonStringValue(jsonSnippet, "workEnd"); String bs = extractJsonStringValue(jsonSnippet, "breakStart"); String be = extractJsonStringValue(jsonSnippet, "breakEnd"); int sd = extractJsonIntValue(jsonSnippet, "slotDuration"); if (ws != null && we != null && bs != null && be != null && sd != -1) { return new TimeConfiguration(ws, we, bs, be, sd); } return null; }
    private static String extractJsonStringValue(String json, String key) { Matcher m = Pattern.compile("\"" + key + "\":\\s*\"([^\"]*)\"").matcher(json); return m.find() ? m.group(1) : null; }
    private static int extractJsonIntValue(String json, String key) { Matcher m = Pattern.compile("\"" + key + "\":\\s*(\\d+)").matcher(json); return m.find() ? Integer.parseInt(m.group(1)) : -1; }
}
// --- End Data Structures for Save Simulation ---


public class ClassMesh extends Application {
    // --- UI Elements ---
    private Label mapTitle; private Label tCountTitle; private ComboBox<Integer> teachCount; private GridPane mapSubs; private GridPane subGrid;
    private Label timeConfigTitle; private FlowPane timeConfigPane; private ComboBox<String> workStartTimeCombo; private ComboBox<String> workEndTimeCombo; private ComboBox<String> breakStartTimeCombo; private ComboBox<String> breakEndTimeCombo; private ComboBox<Integer> slotDurationCombo;
    private Button generateButton; private ComboBox<Integer> semCount;
    private VBox root; private TabPane mainTabPane; private Tab genTimeTab; private GridPane timetableGrid;
    private ScrollPane generatedScrollPane;

    // --- Data Storage (Using User's Model) ---
    private final List<Semester> semesterList = new ArrayList<>();
    private final List<Teacher> teacherList = new ArrayList<>();
    private final ObservableList<SubjectContext> allSubjectsObservableList = FXCollections.observableArrayList();

    // Time Configuration Data
    private LocalTime workStartTime; private LocalTime workEndTime; private LocalTime breakStartTime; private LocalTime breakEndTime; private int slotDurationMinutes;
    private final DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm");
    private final List<String> workingDays = Arrays.asList("Monday", "Tuesday", "Wednesday", "Thursday", "Friday");

    // Set to track assigned SubjectContexts globally for UI feedback
    private final Set<SubjectContext> globallySelectedSubjectContexts = new HashSet<>();

    // --- State for Loaded Configuration ---
    private String loadedJsonData = null; private boolean configLoadedFromFile = false;

    private static final String MAIN_CONTAINER_STYLE_CLASS = "main-config-pane";


    @Override
    public void start(Stage stage) {
        // --- Root Layout, MenuBar, ScrollPane Setup ---
        BorderPane mainLayout = new BorderPane(); root = new VBox(); root.setPadding(new Insets(10)); root.setStyle("-fx-border-color:#6ca0dc;-fx-border-width: 0 2 2 2;-fx-border-radius:0 0 4 4;"); VBox.setVgrow(root, Priority.ALWAYS);
        MenuBar menuBar = new MenuBar(); Menu fileMenu = new Menu("File"); MenuItem loadItem = new MenuItem("Load Configuration"); MenuItem saveItem = new MenuItem("Save Configuration"); loadItem.setOnAction(e -> handleLoadConfiguration(stage)); saveItem.setOnAction(e -> handleSaveConfiguration(stage)); fileMenu.getItems().addAll(loadItem, saveItem); menuBar.getMenus().add(fileMenu); mainLayout.setTop(menuBar);
        ScrollPane inputScrollPane = new ScrollPane(); inputScrollPane.setContent(root); inputScrollPane.setFitToWidth(true); inputScrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED); inputScrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER); inputScrollPane.setStyle("-fx-background-color: transparent;"); mainLayout.setCenter(inputScrollPane);

        // --- Title Labels ---
        Label titleLabel = new Label("Semester Configuration"); titleLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;"); titleLabel.setPadding(new Insets(10, 0, 5, 0));
        Label gridTitle = new Label("Semester/Class Names & Subjects:"); gridTitle.setStyle("-fx-font-size: 14px; -fx-font-weight: bold;"); gridTitle.setPadding(new Insets(10, 0, 5, 0));

        // --- Semester Count Selection ---
        semCount = new ComboBox<>(); semCount.setPromptText("Number of Working Semesters/Classes"); for (int i = 1; i <= 12; i++) semCount.getItems().add(i);

        // --- Semester/Subject Input Grid ---
        subGrid = new GridPane(); subGrid.setStyle("-fx-border-color:#6ca0dc;-fx-border-thickness:2px;-fx-border-radius:4px; -fx-padding: 10;"); subGrid.getStyleClass().add(MAIN_CONTAINER_STYLE_CLASS); subGrid.setHgap(10); subGrid.setVgap(10);
        ColumnConstraints subCol0 = new ColumnConstraints(); subCol0.setHgrow(Priority.NEVER); subCol0.setMinWidth(150); subCol0.setPrefWidth(150);
        ColumnConstraints subCol1 = new ColumnConstraints(); subCol1.setHgrow(Priority.ALWAYS); subGrid.getColumnConstraints().addAll(subCol0, subCol1);

        // --- Time Configuration Section ---
        VBox timeConfigBox = createTimeConfigurationSection();

        // --- Teacher Count & Mapping Section (initially hidden) ---
        tCountTitle = new Label("Teaching Staff Configuration"); tCountTitle.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;"); tCountTitle.setPadding(new Insets(20, 0, 5, 0));
        teachCount = new ComboBox<>(); teachCount.setPromptText("Select Number of Teachers"); for (int t = 1; t <= 25; t++) teachCount.getItems().add(t);
        mapSubs = new GridPane(); mapSubs.setStyle("-fx-border-color:#6ca0dc;-fx-border-thickness:2px;-fx-border-radius:4px;-fx-padding:10px;"); mapSubs.getStyleClass().add(MAIN_CONTAINER_STYLE_CLASS); mapSubs.setVgap(10); mapSubs.setHgap(10);
        ColumnConstraints mapCol0 = new ColumnConstraints(); mapCol0.setHgrow(Priority.NEVER); mapCol0.setMinWidth(150); mapCol0.setPrefWidth(150);
        ColumnConstraints mapCol1 = new ColumnConstraints(); mapCol1.setHgrow(Priority.ALWAYS); mapSubs.getColumnConstraints().addAll(mapCol0, mapCol1);
        mapTitle = new Label("Teacher - Subject Mapping"); mapTitle.setStyle("-fx-font-size: 14px; -fx-font-weight: bold;"); mapTitle.setPadding(new Insets(10, 0, 5, 0));

        // --- Generate Button (initially hidden) ---
        generateButton = new Button("Generate Timetable"); generateButton.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white; -fx-font-size: 16px; -fx-padding: 10 20 10 20; -fx-background-radius: 6;");
        generateButton.setOnAction(e -> handleGenerateClick());
        HBox buttonBox = new HBox(generateButton); buttonBox.setAlignment(Pos.CENTER); buttonBox.setPadding(new Insets(20, 0, 10, 0));

        // --- Semester Count Action ---
        semCount.setOnAction(actionEvent -> {
            // (Code for generating semester/subject UI - unchanged)
            subGrid.getChildren().clear(); hideTimeConfigSection(); hideTeacherSection();
            Integer selectedCount = semCount.getValue(); if (selectedCount == null) return;
            int selSemCount = selectedCount;
            for (int k = 0; k < selSemCount; k++) { int currentRowIndex = k - 1; TextField semName = new TextField(); semName.setPromptText("Semester/Class " + (k + 1) + " Name"); semName.setId("semName_" + k); ComboBox<Integer> subCount = new ComboBox<>(); subCount.setPromptText("Subject Count"); for (int j = 1; j <= 10; j++) subCount.getItems().add(j); subCount.setId("subCount_" + k); VBox subDetailBox = new VBox(5); subDetailBox.setId("subDetailBox_" + k); int finalK = k;
                subCount.setOnAction(e -> { subDetailBox.getChildren().clear(); Integer numSubs = subCount.getValue(); if (numSubs == null) return; for (int s = 0; s < numSubs; s++) { TextField subName = new TextField(); subName.setPromptText("Subject " + (s + 1) + " Name"); subName.setId("subName_" + finalK + "_" + s); Spinner<Integer> hoursSpinner = new Spinner<>(1, 10, 3); hoursSpinner.setPrefWidth(70); hoursSpinner.setEditable(true); hoursSpinner.setId("subHours_" + finalK + "_" + s); HBox entryBox = new HBox(5, subName, new Label("Hrs:"), hoursSpinner); entryBox.setAlignment(Pos.CENTER_LEFT); subDetailBox.getChildren().add(entryBox); } });
                VBox subjectInputBox = new VBox(5, subCount, subDetailBox); subGrid.add(semName, 0, k); subGrid.add(subjectInputBox, 1, k); GridPane.setHgrow(semName, Priority.SOMETIMES); GridPane.setHgrow(subjectInputBox, Priority.ALWAYS); }
            Button subGridNxt = new Button("Next -> Configure Time"); subGridNxt.setStyle("-fx-background-color: #2196F3; -fx-text-fill: white; -fx-font-size: 14px; -fx-padding: 6 16 6 16; -fx-background-radius: 6;");
            subGridNxt.setOnAction(e -> handleSemesterNextClick(subGrid, selSemCount, timeConfigBox));
            subGrid.add(subGridNxt, 0, selSemCount, 2, 1); GridPane.setHalignment(subGridNxt, Pos.CENTER.getHpos()); GridPane.setMargin(subGridNxt, new Insets(15, 0, 0, 0));
        });


        // --- TabPane Setup ---
        mainTabPane = new TabPane();
        Tab inputTab = new Tab("Input Details", mainLayout);
        inputTab.setClosable(false);

        // Setup Timetable Tab Content Area
        timetableGrid = new GridPane(); timetableGrid.setPadding(new Insets(10)); timetableGrid.setHgap(2); timetableGrid.setVgap(2); timetableGrid.setAlignment(Pos.TOP_LEFT); timetableGrid.setGridLinesVisible(true);
        generatedScrollPane = new ScrollPane(timetableGrid); // Initialize member variable
        generatedScrollPane.setFitToWidth(true); generatedScrollPane.setFitToHeight(true);
        VBox genTimeTabContent = new VBox(10); genTimeTabContent.setPadding(new Insets(10)); genTimeTabContent.setAlignment(Pos.CENTER); Label placeholderLabel = new Label("Timetable will be displayed here.");
        genTimeTabContent.getChildren().addAll(placeholderLabel, generatedScrollPane); VBox.setVgrow(generatedScrollPane, Priority.ALWAYS);
        genTimeTab = new Tab("Generated TimeTable", genTimeTabContent); // Add VBox to tab
        genTimeTab.setClosable(false);
        mainTabPane.getTabs().addAll(inputTab, genTimeTab);
        setupTabPaneSelectionStyle(mainTabPane);


        // --- Initial Visibility ---
        hideTimeConfigSection(); hideTeacherSection(); hideGenerateButton();

        // --- Add elements to root VBox IN ORDER ---
        root.getChildren().addAll(titleLabel, semCount, gridTitle, subGrid, timeConfigBox, tCountTitle, teachCount, mapTitle, mapSubs, buttonBox);

        // --- Scene and Stage Setup ---
        Scene scene = new Scene(mainTabPane, 1200, 800); // Use TabPane as root, increased size
        loadStageIcon(stage); stage.setScene(scene); stage.setTitle("ClassMesh - TimeTable Management"); stage.show();
    }

    /** Creates the Time Configuration Section UI */
    private VBox createTimeConfigurationSection() {
        // (Code unchanged)
        VBox container = new VBox(10); container.setPadding(new Insets(20, 0, 10, 0)); container.setStyle("-fx-border-color:#6ca0dc;-fx-border-width: 2 0 0 0;-fx-padding: 15 0 15 0;");
        Label timeTitle = new Label("Time Configuration"); timeTitle.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");
        GridPane timeGrid = new GridPane(); timeGrid.setHgap(10); timeGrid.setVgap(8);
        ObservableList<String> timeOptions = FXCollections.observableArrayList(); for (int hour = 7; hour <= 18; hour++) { timeOptions.add(String.format("%02d:00", hour)); timeOptions.add(String.format("%02d:30", hour)); }
        workStartTimeCombo = new ComboBox<>(timeOptions); workStartTimeCombo.setPromptText("Work Start"); workStartTimeCombo.setValue("09:00");
        workEndTimeCombo = new ComboBox<>(timeOptions); workEndTimeCombo.setPromptText("Work End"); workEndTimeCombo.setValue("17:00");
        breakStartTimeCombo = new ComboBox<>(timeOptions); breakStartTimeCombo.setPromptText("Break Start"); breakStartTimeCombo.setValue("12:30");
        breakEndTimeCombo = new ComboBox<>(timeOptions); breakEndTimeCombo.setPromptText("Break End"); breakEndTimeCombo.setValue("13:30");
        slotDurationCombo = new ComboBox<>(); slotDurationCombo.setPromptText("Slot Duration (min)"); slotDurationCombo.getItems().addAll(30, 45, 50, 55, 60, 90); slotDurationCombo.setValue(45);
        timeGrid.addRow(0, new Label("Working Hours:"), workStartTimeCombo, new Label("to"), workEndTimeCombo);
        timeGrid.addRow(1, new Label("Break Time:"), breakStartTimeCombo, new Label("to"), breakEndTimeCombo);
        timeGrid.addRow(2, new Label("Class Duration:"), slotDurationCombo);
        Button timeNextButton = new Button("Next -> Define Teachers"); timeNextButton.setStyle("-fx-background-color: #2196F3; -fx-text-fill: white; -fx-font-size: 14px; -fx-padding: 6 16 6 16; -fx-background-radius: 6;");
        timeNextButton.setOnAction(e -> handleTimeConfigNextClick());
        container.getChildren().addAll(timeTitle, timeGrid, timeNextButton); container.setAlignment(Pos.CENTER_LEFT); VBox.setMargin(timeNextButton, new Insets(15, 0, 0, 0)); container.setId("timeConfigBox");
        return container;
    }


    /** Handle click after Semester/Subject input */
    private void handleSemesterNextClick(GridPane subGrid, int expectedSemCount, VBox timeConfigBox) {
        // (Validation logic updated to use Subject with hours)
        semesterList.clear(); allSubjectsObservableList.clear(); boolean isValid = true; String errorMessage = ""; Map<Integer, Semester> tempSemesters = new HashMap<>(); List<SubjectContext> tempSubjectContexts = new ArrayList<>();
        for (int k = 0; k < expectedSemCount; k++) { TextField semNameField = (TextField) findNodeInParent(subGrid, "semName_" + k); ComboBox<Integer> subCountBox = (ComboBox<Integer>) findNodeInParent(subGrid, "subCount_" + k); VBox subDetailBox = (VBox) findNodeInParent(subGrid, "subDetailBox_" + k); if (semNameField == null || subCountBox == null) { isValid = false; errorMessage = "Internal UI Error"; break; } String semName = semNameField.getText().trim(); if (semName.isEmpty()) { isValid = false; errorMessage = "Semester " + (k + 1) + " name empty."; highlightError(semNameField); break; } else { resetHighlight(semNameField); } Integer subCount = subCountBox.getValue(); if (subCount == null) { isValid = false; errorMessage = "Select subject count for '" + semName + "'."; highlightError(subCountBox); break; } else { resetHighlight(subCountBox); } Semester currentSemester = new Semester(semName); tempSemesters.put(k, currentSemester);
            if (subDetailBox != null) { int actualSubjectFields = 0; for (Node node : subDetailBox.getChildren()) { if (node instanceof HBox) { actualSubjectFields++; HBox entryBox = (HBox) node; TextField subNameField = null; Spinner<Integer> hoursSpinner = null; for(Node item : entryBox.getChildren()) { if (item instanceof TextField) subNameField = (TextField) item; else if (item instanceof Spinner) hoursSpinner = (Spinner<Integer>) item; } if (subNameField == null || hoursSpinner == null) { isValid = false; errorMessage = "Subject field/spinner missing"; break; } String subName = subNameField.getText().trim(); Integer hours = hoursSpinner.getValue(); if (subName.isEmpty()) { isValid = false; errorMessage = "Subject name empty for '" + semName + "'."; highlightError(subNameField); break; } else { resetHighlight(subNameField); Subject subject = new Subject(subName, hours); currentSemester.getSubjects().add(subject); tempSubjectContexts.add(new SubjectContext(subject, currentSemester)); } } } if (!isValid) break; if(actualSubjectFields != subCount) { isValid = false; errorMessage = "Mismatch subject count and fields for '" + semName + "'."; highlightError(subCountBox); break; }
            } else if (subCount > 0) { isValid = false; errorMessage = "Subject detail box missing for '" + semName + "'"; break;} if (!isValid) break;
        } // End semester loop
        if (isValid) { semesterList.addAll(tempSemesters.values()); tempSubjectContexts.sort(Comparator.comparing((SubjectContext sc) -> sc.getSubject().getName()).thenComparing(sc -> sc.getSemester().getName())); allSubjectsObservableList.setAll(tempSubjectContexts); showTimeConfigSection(); hideTeacherSection(); hideGenerateButton(); }
        else { hideTimeConfigSection(); hideTeacherSection(); hideGenerateButton(); showErrorAlert("Input Validation Error", errorMessage); }
    }

    /** Handle click after Time Configuration input */
    private void handleTimeConfigNextClick() {
        // (Logic unchanged)
        if (validateAndStoreTimeConfig()) { showTeacherSection(); teachCount.setOnAction(f -> populateTeacherMappingGrid(allSubjectsObservableList)); if (teachCount.getValue() != null) { populateTeacherMappingGrid(allSubjectsObservableList); } }
        else { hideTeacherSection(); hideGenerateButton(); }
    }

    /** Validates the time configuration inputs and stores them if valid */
    private boolean validateAndStoreTimeConfig() {
        // (Logic unchanged)
        String errorMsg = ""; Node nodeToHighlight = null;
        try { String ws = workStartTimeCombo.getValue(); String we = workEndTimeCombo.getValue(); String bs = breakStartTimeCombo.getValue(); String be = breakEndTimeCombo.getValue(); Integer duration = slotDurationCombo.getValue(); if (ws == null) { errorMsg = "Select Work Start Time."; nodeToHighlight = workStartTimeCombo; } else if (we == null) { errorMsg = "Select Work End Time."; nodeToHighlight = workEndTimeCombo; } else if (bs == null) { errorMsg = "Select Break Start Time."; nodeToHighlight = breakStartTimeCombo; } else if (be == null) { errorMsg = "Select Break End Time."; nodeToHighlight = breakEndTimeCombo; } else if (duration == null) { errorMsg = "Select Slot Duration."; nodeToHighlight = slotDurationCombo; } else { workStartTime = LocalTime.parse(ws, timeFormatter); workEndTime = LocalTime.parse(we, timeFormatter); breakStartTime = LocalTime.parse(bs, timeFormatter); breakEndTime = LocalTime.parse(be, timeFormatter); slotDurationMinutes = duration; if (workEndTime.isBefore(workStartTime) || workEndTime.equals(workStartTime)) { errorMsg = "Work End Time must be after Work Start Time."; nodeToHighlight = workEndTimeCombo; } else if (breakEndTime.isBefore(breakStartTime) || breakEndTime.equals(breakStartTime)) { errorMsg = "Break End Time must be after Break Start Time."; nodeToHighlight = breakEndTimeCombo; } else if (breakStartTime.isBefore(workStartTime) || breakEndTime.isAfter(workEndTime)) { errorMsg = "Break time must be fully within working hours."; nodeToHighlight = breakStartTimeCombo; } else if (Duration.between(workStartTime, workEndTime).toMinutes() < slotDurationMinutes) { errorMsg = "Total working duration is less than a single slot duration."; nodeToHighlight = slotDurationCombo; } }
        } catch (DateTimeParseException e) { errorMsg = "Invalid time format selected."; nodeToHighlight = workStartTimeCombo; } catch (Exception e) { errorMsg = "Error validating time settings."; nodeToHighlight = workStartTimeCombo; }
        resetHighlight(workStartTimeCombo); resetHighlight(workEndTimeCombo); resetHighlight(breakStartTimeCombo); resetHighlight(breakEndTimeCombo); resetHighlight(slotDurationCombo);
        if (!errorMsg.isEmpty()) { if (nodeToHighlight != null) highlightError(nodeToHighlight); showErrorAlert("Time Configuration Error", errorMsg); return false; } return true;
    }


    /** Populates the teacher name and subject mapping grid (mapSubs) */
    private void populateTeacherMappingGrid(ObservableList<SubjectContext> availableSubjectContexts) {
        // (Logic unchanged - includes fix for button cell and removal of refresh call)
        mapSubs.getChildren().clear(); teacherList.clear(); globallySelectedSubjectContexts.clear(); hideGenerateButton();
        Integer guruCount = teachCount.getValue();
        if (guruCount == null || guruCount <= 0 || availableSubjectContexts.isEmpty()) { if (guruCount != null && guruCount > 0 && availableSubjectContexts.isEmpty()) mapSubs.add(new Label("No subjects defined to assign."), 0, 0); return; }
        Callback<ListView<SubjectContext>, ListCell<SubjectContext>> cellFactory = lv -> new ListCell<>() { @Override protected void updateItem(SubjectContext item, boolean empty) { super.updateItem(item, empty); if (empty || item == null) { setText(null); setTextFill(Color.BLACK); setStyle(""); setDisable(false); } else { String text = item.toString(); boolean isSelectedGlobally = globallySelectedSubjectContexts.contains(item); SubjectContext currentSelectionInThisComboBox = null; try { if(this.getListView() != null && this.getListView().getSelectionModel() != null) currentSelectionInThisComboBox = this.getListView().getSelectionModel().getSelectedItem(); } catch (Exception e) {} boolean disableCell = isSelectedGlobally && !item.equals(currentSelectionInThisComboBox); if (disableCell) { setText(text + " [Assigned]"); setTextFill(Color.GRAY); setStyle("-fx-background-color: #eeeeee;"); setDisable(true); } else { setText(text); setTextFill(Color.BLACK); setStyle(""); setDisable(false); } } } };
        for (int v = 0; v < guruCount; v++) { TextField teachName = new TextField(); teachName.setPromptText("Teacher " + (v + 1) + " Name"); teachName.setId("teachName_" + v); ComboBox<Integer> subTeachCount = new ComboBox<>(); subTeachCount.setPromptText("Subjects Taught"); int maxSubjectsPossible = availableSubjectContexts.size(); for (int l = 0; l <= Math.min(maxSubjectsPossible, 15); l++) subTeachCount.getItems().add(l); subTeachCount.setId("teachSubCount_" + v); FlowPane tSubMapFlowPane = new FlowPane(Orientation.HORIZONTAL, 5, 5); tSubMapFlowPane.setId("teachSubMapFlowPane_" + v); final int teacherIndex = v;
            subTeachCount.setOnAction(p -> { tSubMapFlowPane.getChildren().clear(); rebuildGlobalSubjectSet(mapSubs); Integer teachSubCountVal = subTeachCount.getValue(); if (teachSubCountVal == null) return; for (int m = 0; m < teachSubCountVal; m++) { ComboBox<SubjectContext> selSub = new ComboBox<>(); selSub.setPromptText("Select Subject " + (m + 1)); selSub.setItems(availableSubjectContexts); selSub.setId("selSub_" + teacherIndex + "_" + m); selSub.setCellFactory(cellFactory); selSub.setButtonCell(new ListCell<SubjectContext>() { @Override protected void updateItem(SubjectContext item, boolean empty) { super.updateItem(item, empty); setText((empty || item == null) ? selSub.getPromptText() : item.toString()); } }); selSub.valueProperty().addListener((obs, oldValue, newValue) -> { if (oldValue != null) globallySelectedSubjectContexts.remove(oldValue); if (newValue != null) globallySelectedSubjectContexts.add(newValue); checkIfReadyToGenerate(); }); tSubMapFlowPane.getChildren().add(selSub); } checkIfReadyToGenerate(); });
            mapSubs.addRow(v, teachName, new VBox(5, subTeachCount, tSubMapFlowPane)); GridPane.setHgrow(teachName, Priority.SOMETIMES); GridPane.setHgrow(mapSubs.getChildren().get(mapSubs.getChildren().size()-1), Priority.ALWAYS); }
        checkIfReadyToGenerate();
    }

    /** Rebuilds the global set based on current ComboBox selections */
    private void rebuildGlobalSubjectSet(GridPane mapSubsGrid) {
        // (Logic unchanged)
        globallySelectedSubjectContexts.clear(); if (mapSubsGrid == null) return;
        for (Node node : mapSubsGrid.getChildren()) { if (node instanceof VBox && GridPane.getColumnIndex(node) == 1) { for (Node innerNode : ((VBox) node).getChildren()) { if (innerNode instanceof FlowPane) { for (Node cbNode : ((FlowPane) innerNode).getChildren()) { if (cbNode instanceof ComboBox) { @SuppressWarnings("unchecked") ComboBox<SubjectContext> comboBox = (ComboBox<SubjectContext>) cbNode; SubjectContext selected = comboBox.getValue(); if (selected != null) globallySelectedSubjectContexts.add(selected); } } } } } }
    }

    // Removed unused refreshAllComboBoxListViews method

    /** Checks if all teacher inputs seem valid enough to enable Generate button */
    private void checkIfReadyToGenerate() {
        // (Logic unchanged)
        Integer expectedTeacherCount = teachCount.getValue(); if (expectedTeacherCount == null) { hideGenerateButton(); return; } boolean allTeacherFieldsPotentiallyValid = true;
        for (int v = 0; v < expectedTeacherCount; v++) { TextField teachNameField = (TextField) findNodeInGrid(mapSubs, v, 0); VBox teacherInputsVBox = (VBox) findNodeInGrid(mapSubs, v, 1); ComboBox<Integer> subTeachCountBox = null; FlowPane tSubMapFlowPane = null; if (teacherInputsVBox != null) { for(Node n : teacherInputsVBox.getChildren()){ if(n instanceof ComboBox) subTeachCountBox = (ComboBox<Integer>) n; else if (n instanceof FlowPane) tSubMapFlowPane = (FlowPane) n; }} if (teachNameField == null || teachNameField.getText().trim().isEmpty()) { allTeacherFieldsPotentiallyValid = false; break; } if (subTeachCountBox == null || subTeachCountBox.getValue() == null) { allTeacherFieldsPotentiallyValid = false; break; } int expectedSubjectCount = subTeachCountBox.getValue(); int actualComboBoxes = 0; boolean anySubjectNotSelected = false; if (tSubMapFlowPane != null) { for (Node node : tSubMapFlowPane.getChildren()) { if (node instanceof ComboBox) { actualComboBoxes++; if (((ComboBox<?>)node).getValue() == null) { anySubjectNotSelected = true; break; } } } } if (anySubjectNotSelected || actualComboBoxes != expectedSubjectCount) { allTeacherFieldsPotentiallyValid = false; break; } }
        if (allTeacherFieldsPotentiallyValid) { showGenerateButton(); } else { hideGenerateButton(); }
    }


    /** Handle Generate button click - Now uses new algorithm */
    private void handleGenerateClick() {
        // Re-validate time config first
        if (!validateAndStoreTimeConfig()) { showErrorAlert("Prerequisite Error", "Please fix Time Configuration."); return; }

        // Gather/Validate Teacher data - needed for algorithm
        teacherList.clear(); // Clear previous list
        boolean teachersValid = true;
        String teacherError = "";
        Integer expectedTeacherCount = teachCount.getValue();
        if (expectedTeacherCount == null || expectedTeacherCount == 0) { showErrorAlert("Teacher Error", "Please select number of teachers and assign subjects."); return; }

        // Map to hold the validated teacher assignments from UI
        Map<Teacher, List<SubjectContext>> teacherAssignmentsMap = new HashMap<>();

        for (int v = 0; v < expectedTeacherCount; v++) {
            TextField teachNameField = (TextField) findNodeInGrid(mapSubs, v, 0);
            VBox teacherInputsVBox = (VBox) findNodeInGrid(mapSubs, v, 1);
            ComboBox<Integer> subTeachCountBox = null; FlowPane tSubMapFlowPane = null;
            if (teacherInputsVBox != null) { for(Node n : teacherInputsVBox.getChildren()){ if(n instanceof ComboBox) subTeachCountBox = (ComboBox<Integer>) n; else if (n instanceof FlowPane) tSubMapFlowPane = (FlowPane) n; }}

            if (teachNameField == null || subTeachCountBox == null) { teachersValid = false; teacherError = "UI Error finding controls for Teacher " + (v+1); break; }
            String teachName = teachNameField.getText().trim();
            if (teachName.isEmpty()) { teachersValid = false; teacherError = "Teacher " + (v+1) + " name empty."; highlightError(teachNameField); break; } else { resetHighlight(teachNameField); }
            Integer subTeachCount = subTeachCountBox.getValue();
            if (subTeachCount == null) { teachersValid = false; teacherError = "Select subject count for '" + teachName + "'."; highlightError(subTeachCountBox); break; } else { resetHighlight(subTeachCountBox); }

            Teacher currentTeacher = new Teacher(teachName);
            List<SubjectContext> currentAssignments = new ArrayList<>();
            int actualComboBoxes = 0;

            if (tSubMapFlowPane != null) {
                for (Node cbNode : tSubMapFlowPane.getChildren()) {
                    if (cbNode instanceof ComboBox) {
                        actualComboBoxes++;
                        @SuppressWarnings("unchecked") ComboBox<SubjectContext> selSubBox = (ComboBox<SubjectContext>) cbNode;
                        SubjectContext selectedContext = selSubBox.getValue();
                        if (selectedContext == null) { teachersValid = false; teacherError = "Select Subject for '" + teachName + "'."; highlightError(selSubBox); break; }
                        else {
                            // Check for duplicates within this teacher's assignment
                            if (currentAssignments.contains(selectedContext)) { teachersValid = false; teacherError = "Teacher '" + teachName + "' has duplicate: " + selectedContext; highlightError(selSubBox); break; }
                            currentAssignments.add(selectedContext);
                            currentTeacher.getSubjectsTaught().add(selectedContext.getSubject()); // Add subject to teacher's capability list
                            resetHighlight(selSubBox);
                        }
                    }
                }
                if (!teachersValid) break;
                if (actualComboBoxes != subTeachCount) { teachersValid = false; teacherError = "Mismatch between subject count and fields for '" + teachName + "'."; highlightError(subTeachCountBox); break; }
            } else if (subTeachCount > 0) { teachersValid = false; teacherError = "Subject grid missing for " + teachName; break; }
            if (!teachersValid) break;
            teacherList.add(currentTeacher); // Add teacher to list
            teacherAssignmentsMap.put(currentTeacher, currentAssignments); // Store assignments
        } // End loop through teachers

        // --- Generation and Display ---
        if (teachersValid) {
            System.out.println("--- Input Data Ready for Generation ---");
            // Pass the validated data to the new algorithm
            // Pass teacherAssignmentsMap for the algorithm to know who teaches what specifically
            List<TimetableEntry> generatedTimetable = generateTimetableAlgorithmV2(
                    semesterList, teacherList, teacherAssignmentsMap, // Pass assignments
                    workStartTime, workEndTime, breakStartTime, breakEndTime, slotDurationMinutes, workingDays
            );
            if (generatedTimetable != null) { // Allow empty timetable result
                displayTimetableGrid(generatedTimetable); // Use display method
                mainTabPane.getSelectionModel().select(genTimeTab); // Switch tab
                if(generatedTimetable.isEmpty()){
                    showInfoAlert("Generation Note", "Timetable generated, but no classes could be scheduled with the given constraints.");
                }
            } else { // Should not happen if algorithm returns list, but handle null
                showErrorAlert("Generation Failed", "The timetable generation algorithm failed unexpectedly.");
                generatedScrollPane.setContent(new Label("Timetable generation failed."));
            }
        } else { showErrorAlert("Teacher Validation Error", teacherError); }
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
            int slotMinutes, List<String> days)
    {
        List<TimetableEntry> timetable = new ArrayList<>();
        if (semesters.isEmpty() || teachers.isEmpty()) { System.err.println("Cannot generate: Missing semesters or teachers."); return timetable; }

        // 1. Data Preparation
        List<LocalTime> slotStartTimes = calculateTimeSlots(workStart, workEnd, breakStart, breakEnd, slotMinutes);
        if (slotStartTimes.isEmpty()) { System.err.println("No valid time slots calculated."); return timetable; }

        // Map SubjectContext to Teachers ASSIGNED to teach it in the UI
        Map<SubjectContext, List<Teacher>> subjectContextToAssignedTeachers = new HashMap<>();
        for (Map.Entry<Teacher, List<SubjectContext>> entry : teacherAssignments.entrySet()) {
            Teacher teacher = entry.getKey();
            for (SubjectContext sc : entry.getValue()) {
                subjectContextToAssignedTeachers.computeIfAbsent(sc, k -> new ArrayList<>()).add(teacher);
            }
        }

        // Map required slots per SubjectContext
        Map<SubjectContext, Integer> requiredSlotsMap = new HashMap<>();
        for (Semester sem : semesters) {
            for (Subject sub : sem.getSubjects()) {
                SubjectContext sc = new SubjectContext(sub, sem);
                int requiredSlots = (int) Math.ceil((double) sub.getWeeklyHours() * 60.0 / slotMinutes);
                requiredSlotsMap.put(sc, requiredSlots);
            }
        }

        // Tracking Structures
        Map<String, Map<LocalTime, Set<Teacher>>> teacherBusyMap = new HashMap<>(); // Day -> Slot -> Busy Teachers
        Map<SubjectContext, Integer> scheduledSlotsMap = new HashMap<>(); // SubjectContext -> Slots Scheduled
        Map<String, Map<Semester, LinkedList<Subject>>> recentSubjectsMap = new HashMap<>(); // Day -> Semester -> List of last 2 scheduled Subjects

        // 2. Iteration: Day -> Time Slot -> Semester (User's requested order)
        List<Semester> sortedSemesters = semesters.stream().sorted(Comparator.comparing(Semester::getName)).collect(Collectors.toList());

        for (String day : days) {
            teacherBusyMap.put(day, new HashMap<>()); // Initialize busy map for the day
            recentSubjectsMap.put(day, new HashMap<>()); // Initialize recent map for the day

            for (LocalTime slotStart : slotStartTimes) {
                teacherBusyMap.get(day).computeIfAbsent(slotStart, k -> new HashSet<>()); // Ensure slot exists

                for (Semester semester : sortedSemesters) {
                    recentSubjectsMap.get(day).computeIfAbsent(semester, k -> new LinkedList<>()); // Init recent list for this sem/day
                    Set<Teacher> busyTeachersThisSlot = teacherBusyMap.get(day).get(slotStart);
                    LinkedList<Subject> recentSubjects = recentSubjectsMap.get(day).get(semester);

                    boolean slotFilledForThisSem = false;
                    List<Subject> subjectsToTry = new ArrayList<>(semester.getSubjects());
                    Collections.shuffle(subjectsToTry); // Try subjects in random order for variety

                    for (Subject subject : subjectsToTry) {
                        SubjectContext currentSubjectContext = new SubjectContext(subject, semester);

                        // a. Check remaining hours
                        int required = requiredSlotsMap.getOrDefault(currentSubjectContext, 0);
                        int scheduled = scheduledSlotsMap.getOrDefault(currentSubjectContext, 0);
                        if (scheduled >= required) continue; // Hours met

                        // b. Check consecutive constraint (max 2)
                        if (recentSubjects.size() >= 2 && recentSubjects.get(0).equals(subject) && recentSubjects.get(1).equals(subject)) {
                            continue; // Violates consecutive constraint
                        }

                        // c. Find potential teachers (those ASSIGNED in UI)
                        List<Teacher> potentialTeachers = subjectContextToAssignedTeachers.getOrDefault(currentSubjectContext, Collections.emptyList());
                        if (potentialTeachers.isEmpty()) continue; // No teacher assigned to this subject context

                        // d. Find available teacher among assigned ones
                        Teacher availableTeacher = null;
                        Collections.shuffle(potentialTeachers); // Try assigned teachers in random order
                        for (Teacher teacher : potentialTeachers) {
                            if (!busyTeachersThisSlot.contains(teacher)) {
                                availableTeacher = teacher;
                                break; // Found one!
                            }
                        }

                        // e. Assign if teacher found
                        if (availableTeacher != null) {
                            LocalTime slotEnd = slotStart.plusMinutes(slotMinutes);
                            if (slotEnd.isAfter(workEnd)) slotEnd = workEnd;

                            timetable.add(new TimetableEntry(day, slotStart, slotEnd, semester, subject, availableTeacher));
                            busyTeachersThisSlot.add(availableTeacher); // Mark teacher busy
                            scheduledSlotsMap.put(currentSubjectContext, scheduled + 1); // Increment count
                            recentSubjects.addLast(subject); // Update recent
                            if (recentSubjects.size() > 2) recentSubjects.removeFirst();

                            slotFilledForThisSem = true;
                            break; // Slot filled for this semester, move to next semester for this slot
                        }
                    } // End subject loop for this slot/semester

                    if (!slotFilledForThisSem) {
                        // If no subject could be placed, ensure recent subjects reflects a break
                        // Add a placeholder null or special object if needed to break consecutive chains
                        // For simplicity, we just don't add anything to recentSubjects if slot is empty
                    }

                } // End semester loop for this slot
            } // End slot loop for this day
        } // End day loop

        printUnmetNeeds(requiredSlotsMap, scheduledSlotsMap);
        return timetable;
    }

    /** Helper to print subjects that didn't meet required hours */
    private void printUnmetNeeds(Map<SubjectContext, Integer> required, Map<SubjectContext, Integer> scheduled) {
        // (Logic unchanged)
        System.out.println("--- Scheduling Needs Report ---"); boolean allMet = true; for (Map.Entry<SubjectContext, Integer> entry : required.entrySet()) { SubjectContext sc = entry.getKey(); int reqSlots = entry.getValue(); int schedSlots = scheduled.getOrDefault(sc, 0); if (schedSlots < reqSlots) { allMet = false; System.out.println(" - Subject: " + sc.toString() + " | Required Slots: " + reqSlots + " | Scheduled Slots: " + schedSlots); } } if (allMet) System.out.println("All subject hour requirements appear to be met by the schedule."); else System.out.println("Note: Some subject hour requirements were not fully met."); System.out.println("-----------------------------");
    }


    /** Calculates the start times of valid time slots */
    private List<LocalTime> calculateTimeSlots(LocalTime workStart, LocalTime workEnd, LocalTime breakStart, LocalTime breakEnd, int slotMinutes) {
        // (Calculation logic unchanged from user's code)
        List<LocalTime> slots = new ArrayList<>(); LocalTime current = workStart; while (current.isBefore(workEnd)) { LocalTime slotEnd = current.plusMinutes(slotMinutes); if (slotEnd.isAfter(workEnd)) break; boolean isDuringBreak = !current.isBefore(breakStart) && !slotEnd.isAfter(breakEnd); if (!isDuringBreak) slots.add(current); current = slotEnd; } return slots;
    }


    /** Displays the timetable in a Grid Format with Semesters as sub-rows */
    private void displayTimetableGrid(List<TimetableEntry> timetable) {
        // (Display logic unchanged from user's code)
        timetableGrid.getChildren().clear(); timetableGrid.getColumnConstraints().clear(); timetableGrid.getRowConstraints().clear(); timetableGrid.setGridLinesVisible(true);
        if (timetable.isEmpty() && !configLoadedFromFile && semesterList.isEmpty()) { generatedScrollPane.setContent(new Label("Please provide input configuration first.")); return; }
        else if (timetable.isEmpty()) { generatedScrollPane.setContent(new Label("No timetable entries generated based on input/constraints.")); return; }
        else { generatedScrollPane.setContent(timetableGrid); } // Set grid back
        Map<String, Map<Semester, Map<LocalTime, TimetableEntry>>> groupedEntries = timetable.stream().collect(Collectors.groupingBy(TimetableEntry::getDay, Collectors.groupingBy(TimetableEntry::getSemester, Collectors.toMap(TimetableEntry::getStartTime, entry -> entry, (e1, e2) -> e1))));
        List<LocalTime> slotStartTimes = timetable.stream().map(TimetableEntry::getStartTime).distinct().sorted().collect(Collectors.toList());
        List<String> timeSlotHeaders = slotStartTimes.stream().map(lt -> lt.format(timeFormatter) + "-\n" + lt.plusMinutes(slotDurationMinutes).format(timeFormatter)).collect(Collectors.toList());
        Label topLeft = createHeaderLabel("Day / Sem", 12); GridPane.setHgrow(topLeft, Priority.NEVER); timetableGrid.add(topLeft, 0, 0);
        int numTimeSlots = timeSlotHeaders.size(); for (int j = 0; j < numTimeSlots; j++) { Label timeHeader = createHeaderLabel(timeSlotHeaders.get(j), 11); GridPane.setHgrow(timeHeader, Priority.ALWAYS); timetableGrid.add(timeHeader, j + 1, 0); }
        int currentGridRow = 1; List<Semester> sortedSemesters = semesterList.stream().sorted(Comparator.comparing(Semester::getName)).collect(Collectors.toList());
        for (String day : workingDays) { Label dayLabel = createDayHeaderLabel(day); timetableGrid.add(dayLabel, 0, currentGridRow, numTimeSlots + 1, 1); currentGridRow++; Map<Semester, Map<LocalTime, TimetableEntry>> dayEntriesBySemester = groupedEntries.getOrDefault(day, Collections.emptyMap());
            if (sortedSemesters.isEmpty()) { Label noSemLabel = new Label("No Semesters Defined"); noSemLabel.setPadding(new Insets(5)); timetableGrid.add(noSemLabel, 0, currentGridRow, numTimeSlots + 1, 1); currentGridRow++; }
            else { for (Semester semester : sortedSemesters) { Label semLabel = createSemesterHeaderLabel(semester.getName()); timetableGrid.add(semLabel, 0, currentGridRow); Map<LocalTime, TimetableEntry> semesterDayEntries = dayEntriesBySemester.getOrDefault(semester, Collections.emptyMap());
                for (int j = 0; j < slotStartTimes.size(); j++) { LocalTime slotStart = slotStartTimes.get(j); TimetableEntry entry = semesterDayEntries.get(slotStart); Node cellContent;
                    if (entry != null) { VBox cellBox = new VBox(1); cellBox.setAlignment(Pos.CENTER); Label subjectLabel = new Label(entry.getSubject().getName()); subjectLabel.setFont(Font.font("System", FontWeight.BOLD, 10)); Label teacherLabel = new Label("(" + entry.getTeacher().getName() + ")"); teacherLabel.setFont(Font.font("System", Font.getDefault().getSize() * 0.85)); cellBox.getChildren().addAll(subjectLabel, teacherLabel); cellBox.setStyle("-fx-background-color: white; -fx-border-color: #cccccc; -fx-border-width: 0.5px; -fx-padding: 2px;"); cellContent = cellBox; }
                    else { Pane emptyPane = new Pane(); emptyPane.setMinHeight(35); emptyPane.setStyle("-fx-background-color: #f9f9f9; -fx-border-color: #cccccc; -fx-border-width: 0.5px;"); cellContent = emptyPane; }
                    timetableGrid.add(cellContent, j + 1, currentGridRow); GridPane.setVgrow(cellContent, Priority.SOMETIMES); GridPane.setHgrow(cellContent, Priority.ALWAYS); }
                currentGridRow++; } }
        } // End day loop
        timetableGrid.getColumnConstraints().clear(); ColumnConstraints colDaySem = new ColumnConstraints(); colDaySem.setPrefWidth(120); colDaySem.setMinWidth(100); colDaySem.setHgrow(Priority.NEVER); timetableGrid.getColumnConstraints().add(colDaySem);
        if (numTimeSlots > 0) { double percent = 100.0 / numTimeSlots; for (int j = 0; j < numTimeSlots; j++) { ColumnConstraints colJ = new ColumnConstraints(); colJ.setPercentWidth(percent); colJ.setHgrow(Priority.ALWAYS); colJ.setMinWidth(60); timetableGrid.getColumnConstraints().add(colJ); } }
        Platform.runLater(() -> generatedScrollPane.requestLayout()); // Ensure layout update
    }


    // --- Helper Methods for Header Styling (from user's code) ---
    private Label createHeaderLabel(String text, double fontSize) { Label label = new Label(text); label.setFont(Font.font("System", FontWeight.BOLD, fontSize)); label.setAlignment(Pos.CENTER); label.setMaxWidth(Double.MAX_VALUE); label.setMinWidth(50); label.setPrefHeight(40); label.setPadding(new Insets(5)); label.setStyle("-fx-background-color: #cce0ff; -fx-border-color: #aaaaaa; -fx-border-width: 0.5px;"); label.setTextAlignment(javafx.scene.text.TextAlignment.CENTER); return label; }
    private Label createDayHeaderLabel(String text) { Label label = new Label(text); label.setFont(Font.font("System", FontWeight.BOLD, 14)); label.setAlignment(Pos.CENTER_LEFT); label.setMaxWidth(Double.MAX_VALUE); label.setPadding(new Insets(6, 10, 6, 10)); label.setStyle("-fx-background-color: #aaccff; -fx-border-color: #8888aa; -fx-border-width: 1px 0 1px 0;"); return label; }
    private Label createSemesterHeaderLabel(String text) { Label label = new Label(text); label.setFont(Font.font("System", FontWeight.NORMAL, 11)); label.setAlignment(Pos.CENTER_RIGHT); label.setMaxWidth(Double.MAX_VALUE); label.setPadding(new Insets(5, 8, 5, 5)); label.setStyle("-fx-background-color: #e6f0ff; -fx-border-color: #cccccc; -fx-border-width: 0 0.5px 0.5px 0.5px;"); label.setWrapText(true); return label; }

    // --- Visibility Control Methods (from user's code) ---
    private void hideTimeConfigSection() { Node node = findNodeInParent(root, "timeConfigBox"); if(node!=null){node.setVisible(false);node.setManaged(false);}}
    private void showTimeConfigSection() { Node node = findNodeInParent(root, "timeConfigBox"); if(node!=null){node.setVisible(true); node.setManaged(true); }}
    private void hideTeacherSection() { if(mapTitle != null) mapTitle.setVisible(false); if(tCountTitle != null) tCountTitle.setVisible(false); if(teachCount != null) teachCount.setVisible(false); if(mapSubs != null) { mapSubs.setVisible(false); mapSubs.setManaged(false); /* Don't clear children here, happens in populate */ } hideGenerateButton(); }
    private void showTeacherSection() { if(mapTitle != null) mapTitle.setVisible(true); if(tCountTitle != null) tCountTitle.setVisible(true); if(teachCount != null) teachCount.setVisible(true); if(mapSubs != null) { mapSubs.setVisible(true); mapSubs.setManaged(true); } /* Generate button shown later */ }
    private void hideGenerateButton() { if(generateButton != null) { generateButton.setVisible(false); generateButton.setManaged(false); generateButton.setDisable(true); }} // Ensure disabled when hidden
    // FIX: Modify showGenerateButton to actually enable it
    private void showGenerateButton() { if(generateButton != null) { generateButton.setVisible(true); generateButton.setManaged(true); generateButton.setDisable(false); } } // Enable the button

    // --- Utility Methods ---
    private Node findNodeInParent(Pane parent, String id) { if(parent == null || id == null) return null; for (Node node : parent.getChildrenUnmodifiable()) { if (id.equals(node.getId())) return node; if (node instanceof Pane) { Node found = findNodeInParent((Pane) node, id); if (found != null) return found; } } return null; }
    private Node findNodeById(GridPane parent, String id) { return findNodeInParent(parent, id); } // Specific overload just calls generic one
    private void showErrorAlert(String title, String message) { Alert alert = new Alert(Alert.AlertType.WARNING); alert.setTitle(title); alert.setHeaderText(null); alert.setContentText(message); Stage alertStage = (Stage) alert.getDialogPane().getScene().getWindow(); loadStageIcon(alertStage); DialogPane dialogPane = alert.getDialogPane(); Platform.runLater(() -> { Label contentLabel = (Label)dialogPane.lookup(".content.label"); if(contentLabel != null) { contentLabel.setStyle("-fx-font-size: 14px;-fx-text-fill: #d32f2f;-fx-alignment: center-left;-fx-wrap-text: true;"); }}); dialogPane.setPrefWidth(400); alert.showAndWait(); }
    private void showInfoAlert(String title, String content) { Alert alert = new Alert(Alert.AlertType.INFORMATION); alert.setTitle(title); alert.setHeaderText(null); alert.setContentText(content); alert.showAndWait(); }
    private void highlightError(Node node) { if(node!=null) node.setStyle("-fx-border-color: red; -fx-border-width: 1.5px; -fx-border-radius: 3px;"); }
    private void resetHighlight(Node node) { if(node!=null) node.setStyle(""); } // Reset to default/CSS
    private void loadStageIcon(Stage stage) { if(stage == null) return; try (InputStream iconStream = getClass().getResourceAsStream("/icon.png")) { if (iconStream != null) stage.getIcons().add(new Image(iconStream)); else System.err.println("Warning: icon.png not found in classpath root."); } catch (Exception e) { System.err.println("Error loading icon: " + e.getMessage());}}
    private void setupTabPaneSelectionStyle(TabPane tabPane) { tabPane.getSelectionModel().selectedItemProperty().addListener((obs, oldTab, newTab) -> { if (oldTab != null) oldTab.setStyle(""); if (newTab != null) newTab.setStyle("-fx-border-color: #3c7fb1; -fx-border-width: 2 2 0 2; -fx-border-insets: 0 0 -2 0;"); }); Platform.runLater(() -> { Tab selected = tabPane.getSelectionModel().getSelectedItem(); if (selected != null) selected.setStyle("-fx-border-color: #3c7fb1; -fx-border-width: 2 2 0 2; -fx-border-insets: 0 0 -2 0;"); }); }
    private Node findNodeInGrid(GridPane gridPane, int row, int col) { if(gridPane == null) return null; for (Node node : gridPane.getChildrenUnmodifiable()) { Integer r = GridPane.getRowIndex(node); Integer c = GridPane.getColumnIndex(node); if (r != null && c != null && r == row && c == col) return node; } return null; }

    // --- Load/Save Placeholders (Adapted) ---
    /** Handles Load Configuration action - Reads raw JSON data and enables Generate button. */
    private void handleLoadConfiguration(Stage stage) {
        FileChooser fileChooser = new FileChooser(); fileChooser.setTitle("Load Configuration File"); fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("JSON Files", "*.json")); File file = fileChooser.showOpenDialog(stage);
        if (file != null) { try { loadedJsonData = Files.readString(Paths.get(file.getAbsolutePath())); configLoadedFromFile = true; System.out.println("Successfully loaded configuration file: " + file.getAbsolutePath()); generateButton.setDisable(false); hideTimeConfigSection(); hideTeacherSection(); showInfoAlert("Load Configuration", "Configuration data loaded.\nGenerate Timetable button is now enabled.\n(UI not updated with loaded values)"); } catch (IOException e) { System.err.println("Error reading configuration file: " + e.getMessage()); showErrorAlert("Load Error", "Error reading configuration file:\n" + e.getMessage()); resetLoadedState(); } catch (Exception e) { System.err.println("An unexpected error occurred during file load: " + e.getMessage()); showErrorAlert("Load Error", "An unexpected error occurred during file load."); resetLoadedState(); } }
    }
    /** Resets the loaded configuration state */
    private void resetLoadedState() { loadedJsonData = null; configLoadedFromFile = false; generateButton.setDisable(true); }
    /** Handles Save Configuration action - Gathers data from UI and Writes manually constructed JSON to file. */
    private void handleSaveConfiguration(Stage stage) {
        System.out.println("--- Gathering Configuration for Save ---");
        // Gather data using adapted methods
        List<SemesterConfig> semesters = gatherSemesterDataFromUI();
        List<TeacherConfig> teachers = gatherTeacherDataFromUI();
        TimeConfiguration timeConfig = gatherTimeConfigDataFromUI();

        if (semesters.isEmpty() || teachers.isEmpty() || timeConfig == null) {
            showErrorAlert("Save Error", "Cannot save - incomplete configuration data gathered from UI. Please ensure all steps are filled and validated.");
            return;
        }
        Map<String, Object> fullConfig = new LinkedHashMap<>();
        fullConfig.put("semesters", semesters.stream().map(SemesterConfig::toMap).collect(Collectors.toList()));
        fullConfig.put("teachers", teachers.stream().map(TeacherConfig::toMap).collect(Collectors.toList()));
        fullConfig.put("timeSettings", timeConfig.toMap());
        String jsonOutput = convertMapToJson(fullConfig); // Use manual JSON writer
        FileChooser fileChooser = new FileChooser(); fileChooser.setTitle("Save Configuration File"); fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("JSON Files", "*.json")); fileChooser.setInitialFileName("classmesh_config.json");
        File file = fileChooser.showSaveDialog(stage);
        if (file != null) { String filePath = file.getAbsolutePath(); if (!filePath.toLowerCase().endsWith(".json")) file = new File(filePath + ".json"); System.out.println("Save configuration to: " + file.getAbsolutePath()); try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) { writer.write(jsonOutput); showInfoAlert("Save Configuration", "Configuration saved successfully to:\n" + file.getName()); } catch (IOException e) { System.err.println("Error saving configuration file: " + e.getMessage()); showErrorAlert("Save Error","Error saving configuration file:\n" + e.getMessage()); } }
    }
    // --- Adapted Data Gathering for Save ---
    // These methods now need to work with the user's UI structure and data models
    private List<SemesterConfig> gatherSemesterDataFromUI() {
        List<SemesterConfig> semesters = new ArrayList<>();
        if (semesterList != null && !semesterList.isEmpty()) { // Prefer validated list
            for(Semester sem : semesterList) {
                List<SubjectDetail> details = sem.getSubjects().stream()
                        .map(sub -> new SubjectDetail(sub.getName(), sub.getWeeklyHours())) // Use hours from Subject
                        .collect(Collectors.toList());
                semesters.add(new SemesterConfig(sem.getName(), details));
            }
        } else { System.err.println("Save Warning: Semester list empty, cannot gather semester data for save."); }
        return semesters;
    }
    private List<TeacherConfig> gatherTeacherDataFromUI() {
        List<TeacherConfig> teachers = new ArrayList<>();
        if (teacherList != null && !teacherList.isEmpty()){ // Prefer validated list
            for(Teacher teacher : teacherList) {
                // Need to find the assigned subjects for this teacher from the UI again
                // as teacherList only stores subjects they *can* teach, not current assignment
                List<TeacherAssignment> assignments = new ArrayList<>();
                int teacherIndex = teacherList.indexOf(teacher); // Risky index lookup
                FlowPane currentTeacherSubFlow = null;
                VBox teacherInputsVBox = (VBox) findNodeInGrid(mapSubs, teacherIndex, 1); // Column 1 holds the VBox
                if(teacherInputsVBox != null) {
                    for(Node n : teacherInputsVBox.getChildren()){ if(n instanceof FlowPane) currentTeacherSubFlow = (FlowPane) n;}
                }

                if (currentTeacherSubFlow != null) {
                    for (Node subSelectNode : currentTeacherSubFlow.getChildren()) {
                        if (subSelectNode instanceof ComboBox) {
                            @SuppressWarnings("unchecked") ComboBox<SubjectContext> subjectSelectCombo = (ComboBox<SubjectContext>) subSelectNode;
                            SubjectContext selected = subjectSelectCombo.getValue();
                            if (selected != null) {
                                assignments.add(new TeacherAssignment(selected.getSubject().getName(), selected.getSemester().getName()));
                            }
                        }
                    }
                } else { System.err.println("Save Warning: Could not find subject selection UI for teacher " + teacher.getName()); }
                teachers.add(new TeacherConfig(teacher.getName(), assignments));
            }
        } else { System.err.println("Save Warning: Teacher list empty, cannot gather teacher data for save."); }
        return teachers;
    }
    private TimeConfiguration gatherTimeConfigDataFromUI() {
        String ws = workStartTimeCombo != null ? workStartTimeCombo.getValue() : null;
        String we = workEndTimeCombo != null ? workEndTimeCombo.getValue() : null;
        String bs = breakStartTimeCombo != null ? breakStartTimeCombo.getValue() : null;
        String be = breakEndTimeCombo != null ? breakEndTimeCombo.getValue() : null;
        Integer sd = slotDurationCombo != null ? slotDurationCombo.getValue() : null; // Use ComboBox value
        if (ws == null || we == null || bs == null || be == null || sd == null) return null;
        return new TimeConfiguration(ws, we, bs, be, sd);
    }

    // --- Manual JSON Conversion Helpers ---
    // (JSON helpers unchanged)
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

package com.desk.classmesh; // Make sure this matches your project structure

// Saturday, April 19, 2025 at 12:01 AM IST - Jammu, Jammu and Kashmir, India Context
// This version incorporates fixes for method signatures and includes Save/Load.

import javafx.application.Application;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;
import javafx.util.Callback;
import javafx.stage.FileChooser;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.time.Duration;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.*;
import java.util.stream.Collectors;

// Jackson Imports (Ensure library is added to project dependencies: pom.xml or build.gradle)
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;


// --- Data Model Classes (with Getters/Setters for Jackson) ---

class Subject {
    private String name;
    public Subject() {}
    public Subject(String name) { this.name = name; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    @Override public String toString() { return name; }
    @Override public boolean equals(Object o) { if (this == o) return true; if (o == null || getClass() != o.getClass()) return false; Subject s = (Subject) o; return Objects.equals(name, s.name); }
    @Override public int hashCode() { return Objects.hash(name); }
}

class Semester {
    private String name;
    private List<Subject> subjects = new ArrayList<>();
    private Map<String, Integer> subjectHoursMap = new HashMap<>(); // SubjectName -> Hours

    public Semester() {}
    public Semester(String name) { this.name = name; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public List<Subject> getSubjects() { return subjects;}
    public void setSubjects(List<Subject> subjects) { this.subjects = subjects; }
    public Map<String, Integer> getSubjectHoursMap() { return subjectHoursMap; }
    public void setSubjectHoursMap(Map<String, Integer> map) { this.subjectHoursMap = map; }

    // --- Helper methods (Not directly serialized) ---
    public void addSubject(Subject subject, int hours) {
        if (subject == null || subject.getName() == null || hours <= 0) return;
        if (this.subjects.stream().noneMatch(s -> s.equals(subject))) { this.subjects.add(subject); }
        this.subjectHoursMap.put(subject.getName(), hours);
    }
    public int getHoursForSubject(Subject subject) {
        if (subject == null || subject.getName() == null) return 0;
        return subjectHoursMap.getOrDefault(subject.getName(), 0);
    }
    public Map<Subject, Integer> getSubjectRequirements() {
        Map<Subject, Integer> reqMap = new HashMap<>();
        if (subjects != null && subjectHoursMap != null) {
            Map<String, Subject> nameToSubject = subjects.stream().filter(Objects::nonNull).collect(Collectors.toMap(Subject::getName, s -> s, (s1, s2) -> s1));
            subjectHoursMap.forEach((subjectName, hours) -> { Subject s = nameToSubject.get(subjectName); if (s != null && hours != null && hours > 0) { reqMap.put(s, hours); } });
        }
        return Collections.unmodifiableMap(reqMap);
    }
    // --- End Helper methods ---
    @Override public String toString() { return name; }
    @Override public boolean equals(Object o) { if (this == o) return true; if (o == null || getClass() != o.getClass()) return false; Semester s = (Semester) o; return Objects.equals(name, s.name); }
    @Override public int hashCode() { return Objects.hash(name); }
}

class Teacher {
    private String name;
    private List<Subject> subjectsTaught = new ArrayList<>();
    public Teacher() {}
    public Teacher(String name) { this.name = name; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public List<Subject> getSubjectsTaught() { return subjectsTaught; }
    public void setSubjectsTaught(List<Subject> subjects) { this.subjectsTaught = subjects; }
    @Override public String toString() { return name; }
    @Override public boolean equals(Object o) { if (this == o) return true; if (o == null || getClass() != o.getClass()) return false; Teacher t = (Teacher) o; return Objects.equals(name, t.name); }
    @Override public int hashCode() { return Objects.hash(name); }
}

// Wrapper class - Not serialized, rebuilt during runtime/load
class SubjectContext {
    private final Subject subject; private final Semester semester;
    public SubjectContext(Subject sub, Semester sem) { this.subject = sub; this.semester = sem; }
    public Subject getSubject() { return subject; } public Semester getSemester() { return semester; }
    @Override public String toString() { return subject.getName() + " (" + semester.getName() + ")"; }
    @Override public boolean equals(Object o) { if (this == o) return true; if (o == null || getClass() != o.getClass()) return false; SubjectContext sc = (SubjectContext) o; return Objects.equals(subject, sc.subject) && Objects.equals(semester, sc.semester); }
    @Override public int hashCode() { return Objects.hash(subject, semester); }
}

// Timetable Entry - Represents one scheduled class
class TimetableEntry {
    public String day; public LocalTime startTime; public LocalTime endTime;
    public Semester semester; public Subject subject; public Teacher teacher;
    public TimetableEntry() {}
    public TimetableEntry(String d, LocalTime st, LocalTime et, Semester sm, Subject sb, Teacher tc) { day=d; startTime=st; endTime=et; semester=sm; subject=sb; teacher=tc; }
    public String getDay() { return day; } public LocalTime getStartTime() { return startTime; } public LocalTime getEndTime() { return endTime; }
    public Semester getSemester() { return semester; } public Subject getSubject() { return subject; } public Teacher getTeacher() { return teacher; }
    public String getTimeSlotString() { DateTimeFormatter f = DateTimeFormatter.ofPattern("HH:mm"); return startTime.format(f) + "-" + endTime.format(f); }
}

// Configuration Container for Save/Load
class TimetableConfiguration {
    public List<Semester> semesters; public List<Teacher> teachers;
    public String workStartTime; public String workEndTime;
    public String breakStartTime; public String breakEndTime;
    public Integer slotDurationMinutes; public double version = 1.2;
    public TimetableConfiguration() {}
}
// --- End Data Model Classes ---


public class test extends Application {
    // --- UI Elements ---
    private Label mapTitle;
    private Label tCountTitle;
    private ComboBox<Integer> teachCount;
    private GridPane mapSubs; // Grid for teacher rows
    private Button generateButton;
    private VBox root; // Main container for input tab content
    private Tab genTimeTab;
    private ScrollPane generatedScrollPane;

    // Time Configuration UI Elements
    private ComboBox<String> workStartTimeCombo;
    private ComboBox<String> workEndTimeCombo;
    private ComboBox<String> breakStartTimeCombo;
    private ComboBox<String> breakEndTimeCombo;
    private ComboBox<Integer> slotDurationCombo;

    // --- Data Storage ---
    private final List<Semester> semesterList = new ArrayList<>();
    private final List<Teacher> teacherList = new ArrayList<>();
    private final ObservableList<SubjectContext> allSubjectsObservableList = FXCollections.observableArrayList();

    // Time Configuration Data (Validated values stored here)
    private LocalTime workStartTime;
    private LocalTime workEndTime;
    private LocalTime breakStartTime;
    private LocalTime breakEndTime;
    private int slotDurationMinutes;
    private final DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm");
    private final List<String> workingDays = Arrays.asList("Monday", "Tuesday", "Wednesday", "Thursday", "Friday");

    // Set to track assigned SubjectContexts visually
    private final Set<SubjectContext> globallySelectedSubjectContexts = new HashSet<>();

    // --- Jackson ObjectMapper ---
    private ObjectMapper objectMapper; // Initialized in start()

    @Override
    public void start(Stage stage) {
        // Initialize ObjectMapper
        objectMapper = new ObjectMapper();
        objectMapper.enable(SerializationFeature.INDENT_OUTPUT);
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);

        // --- Root Layout for Input Tab ---
        root = new VBox();
        root.setPadding(new Insets(15));
        root.setSpacing(15);
        root.setStyle("-fx-border-color:#6ca0dc;-fx-border-width: 0 1 1 1;-fx-border-radius: 0 0 4 4;");

        ScrollPane inputScrollPane = new ScrollPane();
        inputScrollPane.setContent(root);
        inputScrollPane.setFitToWidth(true);
        inputScrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        inputScrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);

        // --- UI Component Initialization ---
        // Labels
        Label titleLabel = new Label("Semester Configuration");
        titleLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");
        Label gridTitle = new Label("Semester/Class Names & Subjects:");
        gridTitle.setStyle("-fx-font-size: 14px; -fx-font-weight: bold;");

        // Semester Count ComboBox
        ComboBox<Integer> semCount = new ComboBox<>();
        semCount.setPromptText("Number of Working Semesters/Classes");
        for (int i = 1; i <= 12; i++) { semCount.getItems().add(i); }
        semCount.setMaxWidth(Double.MAX_VALUE);

        // Semester/Subject Grid (Dynamic Content)
        GridPane subGrid = new GridPane();
        subGrid.setStyle("-fx-border-color:#6ca0dc;-fx-border-thickness:1px;-fx-border-radius:4px; -fx-padding: 10;");
        subGrid.setHgap(10); subGrid.setVgap(8);

        // Time Configuration Section
        VBox timeConfigBox = createTimeConfigurationSection();

        // Teacher Configuration
        tCountTitle = new Label("Teaching Staff Configuration");
        tCountTitle.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");
        teachCount = new ComboBox<>();
        teachCount.setPromptText("Select Number of Teachers");
        for (int t = 1; t <= 30; t++) { teachCount.getItems().add(t); }
        teachCount.setMaxWidth(Double.MAX_VALUE);

        mapTitle = new Label("Teacher - Subject Mapping");
        mapTitle.setStyle("-fx-font-size: 14px; -fx-font-weight: bold;");
        mapSubs = new GridPane(); // Main Grid for Teacher Rows
        mapSubs.setStyle("-fx-border-color:#6ca0dc;-fx-border-thickness:1px;-fx-border-radius:4px;-fx-padding:10px;");
        mapSubs.setVgap(10); mapSubs.setHgap(10);
        // Column constraints for Teacher Grid layout
        ColumnConstraints teachCol1 = new ColumnConstraints(150, 200, Double.MAX_VALUE, Priority.SOMETIMES, null, true); // Name
        ColumnConstraints teachCol2 = new ColumnConstraints(100, 120, 150, Priority.NEVER, null, true); // Count
        ColumnConstraints teachCol3 = new ColumnConstraints(200, 300, Double.MAX_VALUE, Priority.ALWAYS, null, true); // Subject Selectors
        mapSubs.getColumnConstraints().addAll(teachCol1, teachCol2, teachCol3);

        // Generate Button
        HBox generateButtonBox = new HBox(generateButton = new Button("Generate Timetable"));
        generateButton.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white; -fx-font-size: 16px; -fx-padding: 10 20 10 20; -fx-background-radius: 6;");
        generateButton.setOnAction(e -> handleGenerateClick());
        generateButtonBox.setAlignment(Pos.CENTER);
        generateButtonBox.setPadding(new Insets(10, 0, 0, 0));

        // --- Event Handlers ---
        semCount.setOnAction(actionEvent -> {
            Integer selectedCount = semCount.getValue();
            configureSemesterUI(selectedCount, subGrid); // Removed timeConfigBox pass - visibility handled by workflow
        });

        // --- TabPane Setup ---
        TabPane myTabPane = new TabPane();
        myTabPane.setId("tabPane"); // ID for lookup during load if needed (though finding via scene root is ok)
        Tab inputTab = new Tab("Input Details", inputScrollPane);
        inputTab.setClosable(false);

        generatedScrollPane = new ScrollPane();
        generatedScrollPane.setFitToWidth(true); generatedScrollPane.setFitToHeight(true);
        VBox genTimeTabContent = new VBox(10);
        genTimeTabContent.setPadding(new Insets(10)); genTimeTabContent.setAlignment(Pos.TOP_CENTER);
        Label placeholderLabel = new Label("Timetable will be displayed here once generated.");
        placeholderLabel.setId("timetablePlaceholder"); placeholderLabel.setPadding(new Insets(20));
        genTimeTabContent.getChildren().addAll(placeholderLabel, generatedScrollPane);
        VBox.setVgrow(generatedScrollPane, Priority.ALWAYS);
        generatedScrollPane.setContent(placeholderLabel);
        genTimeTab = new Tab("Generated Timetable", genTimeTabContent);
        genTimeTab.setClosable(false);

        myTabPane.getTabs().addAll(inputTab, genTimeTab);
        setupTabPaneSelectionStyle(myTabPane);

        // --- Menu Bar ---
        MenuBar menuBar = new MenuBar(); /* ... (Menu Bar setup as before) ... */
        Menu fileMenu = new Menu("File"); MenuItem saveItem = new MenuItem("Save Configuration..."); MenuItem loadItem = new MenuItem("Load Configuration..."); MenuItem exitItem = new MenuItem("Exit");
        saveItem.setOnAction(e -> handleSaveConfiguration(stage));
        loadItem.setOnAction(e -> handleLoadConfiguration(stage, semCount, subGrid)); // Pass controls needed for reload
        exitItem.setOnAction(e -> Platform.exit());
        fileMenu.getItems().addAll(saveItem, loadItem, new SeparatorMenuItem(), exitItem); menuBar.getMenus().add(fileMenu);

        // --- Add components to root VBox (Input Tab Content) ---
        root.getChildren().addAll(
                titleLabel, semCount,
                gridTitle, subGrid,
                timeConfigBox,
                tCountTitle, teachCount,
                mapTitle, mapSubs,
                generateButtonBox);

        // --- Initial UI State ---
        hideTimeConfigSection(); hideTeacherSection();

        // --- Scene and Stage ---
        VBox mainLayout = new VBox(menuBar, myTabPane); VBox.setVgrow(myTabPane, Priority.ALWAYS);
        Scene scene = new Scene(mainLayout, 1250, 800);
        loadStageIcon(stage); stage.setScene(scene); stage.setTitle("ClassMesh - Timetable Management");
        stage.setMinWidth(850); stage.setMinHeight(650); stage.show();
        Platform.runLater(() -> root.requestFocus());
    }

    // --- Helper Method to Configure Semester Section UI ---
    private void configureSemesterUI(Integer selectedCount, GridPane subGrid) {
        subGrid.getChildren().clear(); hideTimeConfigSection(); hideTeacherSection();
        if (selectedCount == null || selectedCount <= 0) { return; }
        int selSemCount = selectedCount;
        for (int k = 0; k < selSemCount; k++) { /* ... (UI element creation as before) ... */
            TextField semName = new TextField(); semName.setPromptText("Sem " + (k + 1)); semName.setId("semName_" + k); semName.setPrefWidth(200);
            ComboBox<Integer> subCount = new ComboBox<>(); subCount.setPromptText("Subj Count"); for(int j=1;j<=12;j++)subCount.getItems().add(j); subCount.setId("subCount_" + k);
            FlowPane subNamePane = new FlowPane(Orientation.HORIZONTAL, 8, 5); subNamePane.setPadding(new Insets(5,0,0,0)); subNamePane.setId("subNamePane_" + k); subNamePane.setPrefWrapLength(500);
            final int semesterIndex = k;
            subCount.setOnAction(e -> configureSubjectFieldsUI(subNamePane, subCount.getValue(), semesterIndex));
            VBox subjectInputBox = new VBox(5, subCount, subNamePane); subjectInputBox.setPadding(new Insets(0,0,0,5));
            subGrid.add(semName, 0, k); subGrid.add(subjectInputBox, 1, k);
            GridPane.setHgrow(semName, Priority.SOMETIMES); GridPane.setHgrow(subjectInputBox, Priority.ALWAYS);
        }
        Button subGridNxt = new Button("Next -> Time Config"); subGridNxt.setStyle("-fx-background-color:#2196F3;-fx-text-fill:white;-fx-font-size:14px;-fx-padding:6 16;-fx-background-radius:6;");
        subGridNxt.setOnAction(e -> handleSemesterNextClick(subGrid, selSemCount)); // Simplified call
        subGrid.add(subGridNxt, 0, selSemCount, 2, 1); GridPane.setHalignment(subGridNxt, Pos.CENTER.getHpos()); GridPane.setMargin(subGridNxt, new Insets(15, 0, 5, 0));
    }

    // --- Helper Method to Configure Subject Fields within a Semester ---
    private void configureSubjectFieldsUI(FlowPane subNamePane, Integer numSubs, int semesterIndex) {
        // (Implementation remains the same as previous version)
        subNamePane.getChildren().clear(); if (numSubs == null || numSubs <= 0) return;
        for (int s = 0; s < numSubs; s++) { HBox box = new HBox(5); box.setAlignment(Pos.CENTER_LEFT);
            TextField nameFld = new TextField("Subj " + (s + 1)); nameFld.setId("subName_" + semesterIndex + "_" + s); nameFld.setPrefWidth(150);
            Spinner<Integer> hrsSpn = new Spinner<>(1, 10, 1); hrsSpn.setPrefWidth(70); hrsSpn.setId("subHours_" + semesterIndex + "_" + s); Tooltip.install(hrsSpn, new Tooltip("Weekly slots/hours"));
            box.getChildren().addAll(nameFld, new Label("Hrs:"), hrsSpn); subNamePane.getChildren().add(box); }
    }

    // --- Creates the Time Configuration Section UI ---
    private VBox createTimeConfigurationSection() {
        // (Implementation remains the same as previous version)
        VBox container=new VBox(15); container.setPadding(new Insets(15)); container.setStyle("-fx-border-color:#6ca0dc;-fx-border-width: 1px; -fx-border-radius: 4px;");
        Label timeTitle=new Label("Time Configuration"); timeTitle.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;"); timeTitle.setPadding(new Insets(0,0,5,0));
        GridPane timeGrid=new GridPane(); timeGrid.setHgap(10); timeGrid.setVgap(8); timeGrid.setAlignment(Pos.CENTER_LEFT);
        ObservableList<String> timeOptions=FXCollections.observableArrayList(); for(int h=7;h<=18;h++){timeOptions.add(String.format("%02d:00",h)); timeOptions.add(String.format("%02d:30",h));}
        workStartTimeCombo=createTimeComboBox(timeOptions,"Work Start"); workEndTimeCombo=createTimeComboBox(timeOptions,"Work End"); breakStartTimeCombo=createTimeComboBox(timeOptions,"Break Start"); breakEndTimeCombo=createTimeComboBox(timeOptions,"Break End");
        slotDurationCombo=new ComboBox<>(); slotDurationCombo.setPromptText("Slot Duration"); slotDurationCombo.getItems().addAll(30,45,50,55,60,90);
        slotDurationCombo.setConverter(new javafx.util.StringConverter<>(){ @Override public String toString(Integer o){return o==null?"":o+" min";} @Override public Integer fromString(String s){return Integer.parseInt(s.replace(" min",""));}}); slotDurationCombo.setMaxWidth(Double.MAX_VALUE);
        timeGrid.addRow(0,new Label("Working Hours:"),workStartTimeCombo,new Label("to"),workEndTimeCombo); timeGrid.addRow(1,new Label("Break Time:"),breakStartTimeCombo,new Label("to"),breakEndTimeCombo); timeGrid.addRow(2,new Label("Class Duration:"),slotDurationCombo);
        ColumnConstraints c0=new ColumnConstraints();c0.setPrefWidth(100); ColumnConstraints c1=new ColumnConstraints();c1.setHgrow(Priority.ALWAYS); ColumnConstraints c2=new ColumnConstraints();c2.setPrefWidth(25); ColumnConstraints c3=new ColumnConstraints();c3.setHgrow(Priority.ALWAYS); timeGrid.getColumnConstraints().addAll(c0,c1,c2,c3); GridPane.setColumnSpan(slotDurationCombo,3);
        Button timeNextButton=new Button("Next -> Define Teachers"); timeNextButton.setStyle("-fx-background-color:#2196F3;-fx-text-fill:white;-fx-font-size:14px;-fx-padding:6 16;-fx-background-radius:6;"); timeNextButton.setOnAction(e->handleTimeConfigNextClick()); HBox btnBox=new HBox(timeNextButton); btnBox.setAlignment(Pos.CENTER); btnBox.setPadding(new Insets(10,0,0,0));
        container.getChildren().addAll(timeTitle,timeGrid,btnBox); container.setId("timeConfigBox"); return container;
    }

    // Helper to create styled time combo boxes
    private ComboBox<String> createTimeComboBox(ObservableList<String> options, String prompt) {
        // (Implementation remains the same)
        ComboBox<String> combo = new ComboBox<>(options); combo.setPromptText(prompt); combo.setMaxWidth(Double.MAX_VALUE); combo.setVisibleRowCount(10); return combo;
    }

    // --- handleSemesterNextClick (Validates Semesters/Subjects, enables Time Config) ---
    private void handleSemesterNextClick(GridPane subGrid, int expectedSemCount) {
        // (Implementation simplified - just validates and shows next section)
        semesterList.clear(); allSubjectsObservableList.clear();
        boolean isValid = true; String errorMessage = "";
        Map<Integer, Semester> tempSemesters = new HashMap<>(); List<SubjectContext> tempSubjectContexts = new ArrayList<>();
        for (int k=0; k<expectedSemCount; k++) { /* ... (Validation Logic as before) ... */
            TextField semNameField=(TextField)findNodeInParent(subGrid,"semName_"+k); ComboBox<Integer> subCountBox=(ComboBox<Integer>)findNodeInParent(subGrid,"subCount_"+k); FlowPane subNamePane=(FlowPane)findNodeInParent(subGrid,"subNamePane_"+k);
            if(semNameField==null||subCountBox==null||subNamePane==null){isValid=false; errorMessage="UI Error Sem "+(k+1); break;} String semName=semNameField.getText().trim();
            if(semName.isEmpty()){isValid=false; errorMessage="Sem "+(k+1)+" name empty."; highlightError(semNameField); break;} else{resetHighlight(semNameField);} Integer subCount=subCountBox.getValue();
            if(subCount==null){isValid=false; errorMessage="Select subject count for '"+semName+"'."; highlightError(subCountBox); break;} else{resetHighlight(subCountBox);}
            Semester currentSemester=new Semester(semName); tempSemesters.put(k,currentSemester);
            if(subNamePane.getChildren().size()!=subCount){isValid=false; errorMessage="Subject field count mismatch for '"+semName+"'."; highlightError(subCountBox); break;}
            for (int s=0; s<subCount; s++) { Node node=subNamePane.getChildren().get(s); if(!(node instanceof HBox)){isValid=false;errorMessage="UI structure error Sem "+(k+1); break;} HBox box=(HBox)node; TextField nameFld=(TextField)findNodeByIdInPane(box,"subName_"+k+"_"+s); Spinner<Integer> hrsSpn=(Spinner<Integer>)findNodeByIdInPane(box,"subHours_"+k+"_"+s); if(nameFld==null||hrsSpn==null){isValid=false; errorMessage="Subject field missing Sem "+(k+1)+", Sub "+(s+1); break;} String subName=nameFld.getText().trim(); Integer hours=hrsSpn.getValue();
                if(subName.isEmpty()){isValid=false; errorMessage="Subj "+(s+1)+" name empty for '"+semName+"'."; highlightError(nameFld); break;} else{resetHighlight(nameFld);} if(hours<=0){isValid=false; errorMessage="Subj "+(s+1)+" hours must be positive for '"+semName+"'."; highlightError(hrsSpn); break;} else{resetHighlight(hrsSpn);}
                Subject subj=new Subject(subName); currentSemester.addSubject(subj,hours); tempSubjectContexts.add(new SubjectContext(subj,currentSemester));
            } if(!isValid)break;
        } // End semester loop
        if(isValid){ semesterList.addAll(tempSemesters.values()); tempSubjectContexts.sort(Comparator.comparing((SubjectContext sc)->sc.getSubject().getName()).thenComparing(sc->sc.getSemester().getName())); allSubjectsObservableList.setAll(tempSubjectContexts); showTimeConfigSection(); hideTeacherSection(); }
        else{ hideTimeConfigSection(); hideTeacherSection(); showErrorAlert("Input Validation Error",errorMessage); }
    }

    // --- handleTimeConfigNextClick (Validates Time, Shows Teachers) ---
    private void handleTimeConfigNextClick() {
        // (Implementation remains the same)
        if (validateAndStoreTimeConfig()) { showTeacherSection(); teachCount.setOnAction(f->populateTeacherMappingGrid(allSubjectsObservableList)); if (teachCount.getValue()!=null){populateTeacherMappingGrid(allSubjectsObservableList);} } else { hideTeacherSection(); }
    }

    // --- validateAndStoreTimeConfig (Validates Time Inputs) ---
    private boolean validateAndStoreTimeConfig() {
        // (Implementation remains the same - MUST return boolean)
        String errorMsg=""; resetHighlight(workStartTimeCombo); resetHighlight(workEndTimeCombo); resetHighlight(breakStartTimeCombo); resetHighlight(breakEndTimeCombo); resetHighlight(slotDurationCombo);
        try { String ws=workStartTimeCombo.getValue(),we=workEndTimeCombo.getValue(),bs=breakStartTimeCombo.getValue(),be=breakEndTimeCombo.getValue(); Integer duration=slotDurationCombo.getValue();
            if(ws==null||we==null||bs==null||be==null||duration==null){errorMsg="Please select all time configuration options."; if(ws==null)highlightError(workStartTimeCombo); if(we==null)highlightError(workEndTimeCombo); if(bs==null)highlightError(breakStartTimeCombo); if(be==null)highlightError(breakEndTimeCombo); if(duration==null)highlightError(slotDurationCombo);}
            else{ workStartTime=LocalTime.parse(ws,timeFormatter); workEndTime=LocalTime.parse(we,timeFormatter); breakStartTime=LocalTime.parse(bs,timeFormatter); breakEndTime=LocalTime.parse(be,timeFormatter); slotDurationMinutes=duration;
                if(workEndTime.isBefore(workStartTime)||workEndTime.equals(workStartTime)){errorMsg="Work End Time must be after Work Start Time."; highlightError(workEndTimeCombo);}
                else if(breakEndTime.isBefore(breakStartTime)||breakEndTime.equals(breakStartTime)){errorMsg="Break End Time must be after Break Start Time."; highlightError(breakEndTimeCombo);}
                else if(breakStartTime.isBefore(workStartTime)||breakEndTime.isAfter(workEndTime)||breakStartTime.equals(workEndTime)||breakEndTime.equals(workStartTime)){errorMsg="Break time must be fully within working hours."; highlightError(breakStartTimeCombo); highlightError(breakEndTimeCombo);}
                else if(Duration.between(workStartTime,workEndTime).toMinutes()<slotDurationMinutes){errorMsg="Total working duration is less than slot duration."; highlightError(slotDurationCombo);}
                else if(breakStartTime.equals(workStartTime)&&breakEndTime.equals(workEndTime)){errorMsg="Break time cannot cover entire working period."; highlightError(breakStartTimeCombo); highlightError(breakEndTimeCombo);}
            }
        }catch(DateTimeParseException e){errorMsg="Invalid time format selected.";}
        if(!errorMsg.isEmpty()){showErrorAlert("Time Configuration Error",errorMsg); return false;} // Return false on error
        return true; // Return true if valid
    }

    // --- Populates the teacher mapping grid ---
    private void populateTeacherMappingGrid(ObservableList<SubjectContext> availableSubjectContexts) {
        // (Implementation remains the same - uses FlowPane for subject selectors)
        mapSubs.getChildren().clear(); teacherList.clear(); globallySelectedSubjectContexts.clear(); hideGenerateButton(); Integer guruCount = teachCount.getValue(); if (guruCount == null || guruCount <= 0 || availableSubjectContexts.isEmpty()) { mapSubs.getChildren().clear(); return; }
        Callback<ListView<SubjectContext>, ListCell<SubjectContext>> cellFactory = createSubjectCellFactory();
        for (int v=0; v<guruCount; v++) { TextField tnf=new TextField(); tnf.setPromptText("Teacher "+(v+1)); tnf.setId("teachName_"+v);
            ComboBox<Integer> stc=new ComboBox<>(); stc.setPromptText("Subj Count"); int maxS=availableSubjectContexts.size(); for(int l=1;l<=Math.min(maxS,15);l++){stc.getItems().add(l);} stc.setId("teachSubCount_"+v); stc.setPrefWidth(150);
            FlowPane ssp=new FlowPane(Orientation.HORIZONTAL,5,5); ssp.setId("subjectSelectorsPane_"+v); ssp.setPadding(new Insets(0,0,0,5)); ssp.setPrefWrapLength(400);
            final int teacherIndex=v; stc.setOnAction(e->{ configureTeacherSubjectSelectors(ssp,stc.getValue(),teacherIndex,availableSubjectContexts,cellFactory); if(teachCount.getValue()!=null&&teachCount.getValue()>0)showGenerateButton(); });
            mapSubs.add(tnf,0,v); mapSubs.add(stc,1,v); mapSubs.add(ssp,2,v);
        } if(guruCount>0){showGenerateButton();}
    }

    // --- Helper to create Subject Selectors for a Teacher ---
    private void configureTeacherSubjectSelectors(FlowPane containerPane, Integer count, int teacherIndex, ObservableList<SubjectContext> availableSubjects, Callback<ListView<SubjectContext>, ListCell<SubjectContext>> cellFactory) {
        // (Implementation remains the same)
        containerPane.getChildren().clear(); rebuildGlobalSubjectSet(mapSubs); if(count==null||count<=0)return;
        for(int m=0; m<count; m++){ ComboBox<SubjectContext> selSub=new ComboBox<>(); selSub.setPromptText("Select Subj "+(m+1)); selSub.setItems(availableSubjects); selSub.setId("selSub_"+teacherIndex+"_"+m); selSub.setPrefWidth(200); selSub.setCellFactory(cellFactory);
            selSub.setButtonCell(new ListCell<>(){@Override protected void updateItem(SubjectContext i,boolean e){super.updateItem(i,e);setText((e||i==null)?selSub.getPromptText():i.toString());}});
            selSub.valueProperty().addListener((obs,oldV,newV)->{if(oldV!=null)globallySelectedSubjectContexts.remove(oldV); if(newV!=null)globallySelectedSubjectContexts.add(newV);});
            containerPane.getChildren().add(selSub);
        }
    }

    // --- Creates the Cell Factory for Subject ComboBoxes ---
    private Callback<ListView<SubjectContext>, ListCell<SubjectContext>> createSubjectCellFactory() {
        // (Implementation remains the same)
        return lv->new ListCell<>(){@Override protected void updateItem(SubjectContext i,boolean e){super.updateItem(i,e); if(e||i==null){setText(null);setTextFill(Color.BLACK);setStyle("");}else{String t=i.toString();boolean elsewhere=globallySelectedSubjectContexts.contains(i);SubjectContext currentSel=null;try{if(this.getListView()!=null&&this.getListView().getSelectionModel()!=null){currentSel=this.getListView().getSelectionModel().getSelectedItem();}}catch(Exception ex){} if(elsewhere&&!Objects.equals(i,currentSel)){setText(t+" [Assigned]");setTextFill(Color.GRAY);}else{setText(t);setTextFill(Color.BLACK);} setStyle("");}}};
    }

    // --- rebuildGlobalSubjectSet (Updates tracking set from UI) ---
    private void rebuildGlobalSubjectSet(GridPane mapSubsGrid) {
        // (Implementation remains the same)
        globallySelectedSubjectContexts.clear(); if(mapSubsGrid==null)return; try{int rows=mapSubsGrid.getRowCount();for(int v=0;v<rows;v++){FlowPane ssp=(FlowPane)findNodeInParent(mapSubsGrid,"subjectSelectorsPane_"+v); if(ssp!=null){for(Node n:ssp.getChildrenUnmodifiable()){if(n instanceof ComboBox){@SuppressWarnings("unchecked")ComboBox<SubjectContext> cb=(ComboBox<SubjectContext>)n; SubjectContext s=cb.getValue(); if(s!=null)globallySelectedSubjectContexts.add(s);}}}}}catch(Exception e){System.err.println("Err rebuilding global set: "+e.getMessage());}
    }


    // --- handleGenerateClick (Validates inputs, triggers generation) ---
    private void handleGenerateClick() {
        // (Implementation remains the same - performs validation, calls algorithm)
        if(!validateAndStoreTimeConfig()){showErrorAlert("Prerequisite Error","Fix Time Config"); return;}
        teacherList.clear(); boolean teachersValid=true; String validationError=""; Integer expectedTC=teachCount.getValue();
        if(expectedTC==null||expectedTC==0){showErrorAlert("Teacher Error","Select number of teachers"); return;}
        Set<Subject> allDefinedSubjects=semesterList.stream().flatMap(s->s.getSubjects().stream()).collect(Collectors.toSet()); Set<Subject> subjectsWithTeacher=new HashSet<>(); Set<SubjectContext> finalAssignedContexts=new HashSet<>();
        for(int v=0; v<expectedTC; v++){
            TextField tnf=(TextField)findNodeInParent(mapSubs,"teachName_"+v); ComboBox<Integer> scb=(ComboBox<Integer>)findNodeInParent(mapSubs,"teachSubCount_"+v); FlowPane ssp=(FlowPane)findNodeInParent(mapSubs,"subjectSelectorsPane_"+v);
            if(tnf==null||scb==null){teachersValid=false; validationError="UI Error T:"+(v+1); break;} String tn=tnf.getText().trim(); if(tn.isEmpty()){teachersValid=false; validationError="Teacher "+(v+1)+" name empty."; highlightError(tnf); break;}else{resetHighlight(tnf);} Integer stc=scb.getValue(); if(stc==null){teachersValid=false; validationError="Select subject count for '"+tn+"'."; highlightError(scb); break;}else{resetHighlight(scb);}
            Teacher currentTeacher=new Teacher(tn); Set<SubjectContext> subjectsForThisTeacher=new HashSet<>();
            if(ssp!=null&&ssp.getChildren().size()==stc){
                for(int m=0; m<stc; m++){ Node node=ssp.getChildren().get(m); if(!(node instanceof ComboBox)){teachersValid=false; validationError="UI structure error T:"+(v+1); break;} @SuppressWarnings("unchecked") ComboBox<SubjectContext> selSubBox=(ComboBox<SubjectContext>)node; SubjectContext selCtx=selSubBox.getValue();
                    if(selCtx==null){teachersValid=false; validationError="Select Subject "+(m+1)+" for '"+tn+"'."; highlightError(selSubBox); break;}else{if(!subjectsForThisTeacher.add(selCtx)){teachersValid=false; validationError="Teacher '"+tn+"' duplicate: "+selCtx; highlightError(selSubBox); break;} /*Optional Global Check*/ resetHighlight(selSubBox); currentTeacher.getSubjectsTaught().add(selCtx.getSubject()); subjectsWithTeacher.add(selCtx.getSubject());}}
            }else if(stc>0){teachersValid=false; validationError="Subject selector mismatch T:"+(v+1); break;} if(!teachersValid)break; teacherList.add(currentTeacher);
        }
        if(teachersValid){Set<Subject> unassigned=new HashSet<>(allDefinedSubjects); unassigned.removeAll(subjectsWithTeacher); if(!unassigned.isEmpty()){teachersValid=false; validationError="Unassigned Subjects: "+unassigned.stream().map(Subject::getName).collect(Collectors.joining(", "));}}
        if(teachersValid){ System.out.println("--- Input Data Ready ---"); Label genLabel=new Label("Generating... Please wait."); genLabel.setPadding(new Insets(30)); generatedScrollPane.setContent(genLabel);
            List<TimetableEntry> generatedTimetable = generateTimetableAlgorithm(List.copyOf(semesterList),List.copyOf(teacherList),allSubjectsObservableList,workStartTime,workEndTime,breakStartTime,breakEndTime,slotDurationMinutes,List.copyOf(workingDays));
            if(generatedTimetable!=null&&!generatedTimetable.isEmpty()){ displayTimetableGrid(generatedTimetable); showSuccessAlert("Generation Complete","Timetable generated."); TabPane tp=(TabPane)root.getScene().lookup("#tabPane"); if(tp!=null)tp.getSelectionModel().select(genTimeTab); else System.err.println("Cannot find TabPane.");}
            else{ boolean failed=generatedTimetable==null; showErrorAlert(failed?"Generation Failed":"Generation Result",failed?"Could not generate timetable. Check constraints/inputs.":"No classes scheduled."); Label failLabel=new Label(failed?"Generation failed.":"No classes scheduled."); failLabel.setPadding(new Insets(20)); generatedScrollPane.setContent(failLabel); }
        }else{ showErrorAlert("Input Validation Error",validationError); }
    }


    // --- generateTimetableAlgorithm (Placeholder - CORRECTED SIGNATURE & RETURN) ---
    private List<TimetableEntry> generateTimetableAlgorithm(
            List<Semester> semesters, List<Teacher> teachers,
            ObservableList<SubjectContext> subjectContexts, // Passed but maybe not used by placeholder
            LocalTime workStart, LocalTime workEnd,
            LocalTime breakStart, LocalTime breakEnd,
            int slotMinutes, List<String> days)
    {
        // !!! REPLACE THIS WITH ACTUAL BACKTRACKING OR SOLVER LOGIC !!!
        System.out.println("--- Running Placeholder Generation Algorithm ---");
        System.out.println("This algorithm does NOT enforce constraints or required hours.");
        // Example: Simple return to make it compile and indicate placeholder ran.
        List<TimetableEntry> placeholderTimetable = new ArrayList<>();
        showErrorAlert("Placeholder Active", "Timetable generation uses placeholder logic.\nPlease implement a real algorithm.");
        // MUST return a List or null to match signature
        return placeholderTimetable; // Return empty list (simulates success with no classes generated)
        // return null; // Use this to simulate generation failure
    }


    // --- calculateTimeSlots (Corrected Signature) ---
    private List<LocalTime> calculateTimeSlots(
            LocalTime workStart, LocalTime workEnd,
            LocalTime breakStart, LocalTime breakEnd,
            int slotMinutes) // Correct parameters
    {
        // (Implementation remains the same, using the passed parameters)
        List<LocalTime> slots = new ArrayList<>();
        if (workStart == null || workEnd == null || breakStart == null || breakEnd == null || slotMinutes <= 0) {
            System.err.println("Cannot calculate time slots due to invalid time configuration.");
            return slots; // Return empty list if config is invalid
        }
        LocalTime current = workStart;
        while (current.isBefore(workEnd)) {
            LocalTime slotEnd = current.plusMinutes(slotMinutes);
            if (slotEnd.isAfter(workEnd)) break;
            // Check for overlap with break [breakStart, breakEnd)
            boolean overlapsBreak = current.isBefore(breakEnd) && slotEnd.isAfter(breakStart);
            if (!overlapsBreak) {
                slots.add(current);
            }
            current = slotEnd;
        }
        return slots;
    }

    // --- displayTimetableGrid (No changes needed) ---
    private void displayTimetableGrid(List<TimetableEntry> timetable) { /* ... Same grid display logic ... */
        GridPane timetableGrid = new GridPane(); timetableGrid.setHgap(2); timetableGrid.setVgap(2); timetableGrid.setPadding(new Insets(10)); timetableGrid.setStyle("-fx-background-color: #e8e8e8;");
        Map<String, Map<Semester, Map<LocalTime, TimetableEntry>>> groupedEntries = timetable.stream().collect(Collectors.groupingBy(TimetableEntry::getDay, Collectors.groupingBy(TimetableEntry::getSemester, Collectors.toMap(TimetableEntry::getStartTime, entry -> entry, (e1, e2) -> e1))));
        List<LocalTime> slotStartTimes = calculateTimeSlots(workStartTime, workEndTime, breakStartTime, breakEndTime, slotDurationMinutes); List<String> timeSlotHeaders = slotStartTimes.stream().map(lt -> lt.format(timeFormatter) + "-\n" + lt.plusMinutes(slotDurationMinutes).format(timeFormatter)).collect(Collectors.toList());
        Label topLeft = createHeaderLabel("Day / Sem", 12); GridPane.setHgrow(topLeft, Priority.SOMETIMES); timetableGrid.add(topLeft, 0, 0); int numTimeSlots = timeSlotHeaders.size();
        for (int j = 0; j < numTimeSlots; j++) { Label timeHeader = createHeaderLabel(timeSlotHeaders.get(j), 11); GridPane.setHgrow(timeHeader, Priority.ALWAYS); timetableGrid.add(timeHeader, j + 1, 0); }
        int currentGridRow = 1; List<Semester> sortedSemesters = semesterList.stream().sorted(Comparator.comparing(Semester::getName)).collect(Collectors.toList());
        for (String day : workingDays) { Label dayLabel = createDayHeaderLabel(day); timetableGrid.add(dayLabel, 0, currentGridRow, numTimeSlots + 1, 1); currentGridRow++; Map<Semester, Map<LocalTime, TimetableEntry>> dayEntriesBySemester = groupedEntries.getOrDefault(day, Collections.emptyMap());
            if (sortedSemesters.isEmpty()) { Label noSemLabel = new Label("No Semesters Defined"); noSemLabel.setPadding(new Insets(5)); timetableGrid.add(noSemLabel, 0, currentGridRow, numTimeSlots + 1, 1); currentGridRow++; }
            else { for (Semester semester : sortedSemesters) { Label semLabel = createSemesterHeaderLabel(semester.getName()); timetableGrid.add(semLabel, 0, currentGridRow); Map<LocalTime, TimetableEntry> semesterDayEntries = dayEntriesBySemester.getOrDefault(semester, Collections.emptyMap());
                for (int j = 0; j < slotStartTimes.size(); j++) { LocalTime slotStart = slotStartTimes.get(j); TimetableEntry entry = semesterDayEntries.get(slotStart); Node cellContent;
                    if (entry != null) { Label entryLabel = new Label(entry.getSubject().getName() + "\n(" + entry.getTeacher().getName() + ")"); entryLabel.setWrapText(true); entryLabel.setFont(Font.font("System", FontWeight.NORMAL, 10)); entryLabel.setMaxWidth(Double.MAX_VALUE); entryLabel.setPadding(new Insets(3)); VBox cellBox = new VBox(entryLabel); cellBox.setAlignment(Pos.CENTER_LEFT); cellBox.setStyle("-fx-background-color: white; -fx-border-color: #cccccc; -fx-border-width: 0.5px; -fx-min-height: 35px;"); cellContent = cellBox; }
                    else { Pane emptyPane = new Pane(); emptyPane.setMinHeight(35); emptyPane.setStyle("-fx-background-color: #f9f9f9; -fx-border-color: #cccccc; -fx-border-width: 0.5px;"); cellContent = emptyPane; }
                    timetableGrid.add(cellContent, j + 1, currentGridRow); GridPane.setVgrow(cellContent, Priority.SOMETIMES); GridPane.setHgrow(cellContent, Priority.ALWAYS);
                } currentGridRow++; } } }
        ColumnConstraints col0 = new ColumnConstraints(); col0.setPrefWidth(120); col0.setMinWidth(80); col0.setHgrow(Priority.NEVER); timetableGrid.getColumnConstraints().add(col0);
        if (numTimeSlots > 0) { double percent = 100.0 / numTimeSlots; for (int j = 0; j < numTimeSlots; j++) { ColumnConstraints colJ = new ColumnConstraints(); colJ.setPercentWidth(percent); colJ.setMinWidth(60); colJ.setHgrow(Priority.ALWAYS); timetableGrid.getColumnConstraints().add(colJ); } }
        generatedScrollPane.setContent(timetableGrid); Platform.runLater(() -> generatedScrollPane.requestLayout());
    }

    // --- Header Label Helpers (No changes needed) ---
    private Label createHeaderLabel(String text, double fontSize) { /* ... Same ... */ Label l=new Label(text); l.setFont(Font.font("System",FontWeight.BOLD,fontSize)); l.setAlignment(Pos.CENTER); l.setMaxWidth(Double.MAX_VALUE); l.setMinWidth(50); l.setPrefHeight(40); l.setPadding(new Insets(5)); l.setStyle("-fx-background-color:#cce0ff;-fx-border-color:#aaaaaa;-fx-border-width:0.5px;"); l.setTextAlignment(javafx.scene.text.TextAlignment.CENTER); return l; }
    private Label createDayHeaderLabel(String text) { /* ... Same ... */ Label l=new Label(text); l.setFont(Font.font("System",FontWeight.BOLD,14)); l.setAlignment(Pos.CENTER_LEFT); l.setMaxWidth(Double.MAX_VALUE); l.setPadding(new Insets(6,10,6,10)); l.setStyle("-fx-background-color:#aaccff;-fx-border-color:#8888aa;-fx-border-width:1px 0 1px 0;"); return l; }
    private Label createSemesterHeaderLabel(String text) { /* ... Same ... */ Label l=new Label(text); l.setFont(Font.font("System",FontWeight.NORMAL,11)); l.setAlignment(Pos.CENTER_RIGHT); l.setMaxWidth(Double.MAX_VALUE); l.setPadding(new Insets(5,8,5,5)); l.setStyle("-fx-background-color:#e6f0ff;-fx-border-color:#cccccc;-fx-border-width:0 0.5px 0.5px 0.5px;-fx-min-height:35px;"); l.setWrapText(true); return l; }

    // --- Visibility Control Methods (No changes needed) ---
    private void hideTimeConfigSection() { Node n=findNodeInParent(root,"timeConfigBox"); if(n!=null){n.setVisible(false);n.setManaged(false);}}
    private void showTimeConfigSection() { Node n=findNodeInParent(root,"timeConfigBox"); if(n!=null){n.setVisible(true); n.setManaged(true); }}
    private void hideTeacherSection() { if(mapTitle!=null)mapTitle.setVisible(false); if(tCountTitle!=null)tCountTitle.setVisible(false); if(teachCount!=null)teachCount.setVisible(false); if(mapSubs!=null){mapSubs.setVisible(false); mapSubs.getChildren().clear();} hideGenerateButton(); }
    private void showTeacherSection() { if(mapTitle!=null)mapTitle.setVisible(true); if(tCountTitle!=null)tCountTitle.setVisible(true); if(teachCount!=null)teachCount.setVisible(true); if(mapSubs!=null)mapSubs.setVisible(true); }
    private void hideGenerateButton() { if(generateButton!=null){generateButton.setVisible(false); generateButton.setManaged(false);} }
    private void showGenerateButton() { if(generateButton!=null){generateButton.setVisible(true); generateButton.setManaged(true);} }

    // --- Utility Methods ---
    private Node findNodeInParent(Pane parent, String id) { if(parent==null||id==null)return null; for(Node node:parent.getChildrenUnmodifiable()){ if(id.equals(node.getId()))return node; if(node instanceof Pane){Node found=findNodeInParent((Pane)node, id); if(found!=null)return found;} } return null; }
    private Node findNodeById(GridPane parent, String id) { return findNodeInParent(parent, id); }
    private Node findNodeByIdInPane(Pane parent, String id) { return findNodeInParent(parent, id); }
    private void showSuccessAlert(String title, String message) { Alert a=new Alert(Alert.AlertType.INFORMATION); a.setTitle(title); a.setHeaderText(null); a.setContentText(message); Stage s=(Stage)a.getDialogPane().getScene().getWindow(); loadStageIcon(s); a.showAndWait(); }
    private void showErrorAlert(String title, String message) { Alert a=new Alert(Alert.AlertType.WARNING); a.setTitle(title); a.setHeaderText(null); a.setContentText(message); Stage s=(Stage)a.getDialogPane().getScene().getWindow(); loadStageIcon(s); DialogPane dp=a.getDialogPane(); dp.lookup(".content.label").setStyle("-fx-font-size: 14px;-fx-text-fill: #d32f2f;-fx-alignment: center-left;-fx-wrap-text: true;"); dp.setPrefWidth(400); a.showAndWait(); }
    private void highlightError(Node node) { if(node!=null) node.setStyle("-fx-border-color: red; -fx-border-width: 1.5px; -fx-border-radius: 3px;"); }
    private void resetHighlight(Node node) { if(node!=null) node.setStyle(node.getStyle().replaceAll("-fx-border-color:[^;]+(;\\s*)?", "").replaceAll("-fx-border-width:[^;]+(;\\s*)?", "").replaceAll("-fx-border-radius:[^;]+(;\\s*)?", "")); } // Improved reset
    private void loadStageIcon(Stage stage) { try (InputStream iconStream = getClass().getResourceAsStream("/icon.png")) { if (iconStream != null) stage.getIcons().add(new Image(iconStream)); else System.err.println("Warning: icon.png not found."); } catch (Exception e) { System.err.println("Error loading icon: " + e.getMessage());}}
    private void setupTabPaneSelectionStyle(TabPane tabPane) { tabPane.getSelectionModel().selectedItemProperty().addListener((obs, oldTab, newTab) -> { if (oldTab != null) oldTab.setStyle(""); if (newTab != null) newTab.setStyle("-fx-border-color: #3c7fb1; -fx-border-width: 2 2 0 2; -fx-border-insets: 0 0 -2 0;"); }); Platform.runLater(() -> { Tab selected = tabPane.getSelectionModel().getSelectedItem(); if (selected != null) selected.setStyle("-fx-border-color: #3c7fb1; -fx-border-width: 2 2 0 2; -fx-border-insets: 0 0 -2 0;"); }); }


    // --- Implemented Save/Load Configuration Methods ---

    private void handleSaveConfiguration(Stage ownerStage) {
        System.out.println("Attempting to save configuration...");
        // 1. Ensure essential time config is valid before allowing save
        if (!validateAndStoreTimeConfig()) { // Validate AND store latest values
            showErrorAlert("Save Error", "Cannot save. Please fix Time Configuration errors first.");
            return;
        }
        // You might add validation for semesters/teachers here too if desired

        // 2. Create Configuration Container
        TimetableConfiguration config = new TimetableConfiguration();
        // Copy data to avoid modifying original lists if needed elsewhere, though saving current state is usually fine
        config.semesters = new ArrayList<>(this.semesterList);
        config.teachers = new ArrayList<>(this.teacherList);
        // Save time/duration as strings/int using the validated member variables
        config.workStartTime = this.workStartTime != null ? this.workStartTime.format(timeFormatter) : null;
        config.workEndTime = this.workEndTime != null ? this.workEndTime.format(timeFormatter) : null;
        config.breakStartTime = this.breakStartTime != null ? this.breakStartTime.format(timeFormatter) : null;
        config.breakEndTime = this.breakEndTime != null ? this.breakEndTime.format(timeFormatter) : null;
        config.slotDurationMinutes = this.slotDurationMinutes; // Already an int

        // 3. Show File Chooser
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Save Timetable Configuration");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("JSON Files (*.json)", "*.json"));
        fileChooser.setInitialFileName("timetable_config.json");
        File file = fileChooser.showSaveDialog(ownerStage);

        // 4. Write to File
        if (file != null) {
            try {
                System.out.println("Saving configuration to: " + file.getAbsolutePath());
                objectMapper.writeValue(file, config); // Use initialized objectMapper
                showSuccessAlert("Save Successful", "Configuration saved to:\n" + file.getAbsolutePath());
            } catch (IOException e) {
                showErrorAlert("Save Failed", "Could not save configuration file.\nError: " + e.getMessage());
                e.printStackTrace();
            }
        } else {
            System.out.println("Save configuration cancelled.");
        }
    }

    private void handleLoadConfiguration(Stage ownerStage, ComboBox<Integer> semCount, GridPane subGrid) {
        System.out.println("Attempting to load configuration...");
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Load Timetable Configuration");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("JSON Files (*.json)", "*.json"));
        File file = fileChooser.showOpenDialog(ownerStage);

        if (file != null) {
            try {
                System.out.println("Loading configuration from: " + file.getAbsolutePath());
                TimetableConfiguration loadedConfig = objectMapper.readValue(file, TimetableConfiguration.class);

                if (loadedConfig == null || loadedConfig.semesters == null || loadedConfig.teachers == null) {
                    throw new IOException("Loaded configuration file is incomplete or invalid.");
                }

                clearAppState(semCount, subGrid); // Reset current state

                // Populate data models
                this.semesterList.addAll(loadedConfig.semesters);
                this.teacherList.addAll(loadedConfig.teachers);
                rebuildSubjectContextList(); // Rebuild dropdown helper list

                // Repopulate UI (Simplified approach)
                repopulateUIFromLoad(loadedConfig, semCount, subGrid);

                showSuccessAlert("Load Successful", "Configuration loaded.\nPlease click 'Next' buttons to review sections.");

            } catch (JsonProcessingException e) {
                showErrorAlert("Load Failed", "Could not parse configuration file (invalid JSON format).\nError: " + e.getMessage()); e.printStackTrace(); clearAppState(semCount, subGrid);
            } catch (IOException e) {
                showErrorAlert("Load Failed", "Could not read or process configuration file.\nError: " + e.getMessage()); e.printStackTrace(); clearAppState(semCount, subGrid);
            } catch (Exception e) {
                showErrorAlert("Load Error", "An unexpected error occurred while loading.\nError: " + e.getMessage()); e.printStackTrace(); clearAppState(semCount, subGrid);
            }
        } else {
            System.out.println("Load configuration cancelled.");
        }
    }

    // Helper to Clear State Before Loading
    private void clearAppState(ComboBox<Integer> semCount, GridPane subGrid) {
        // (Implementation remains the same)
        System.out.println("Clearing application state..."); semesterList.clear(); teacherList.clear(); allSubjectsObservableList.clear(); globallySelectedSubjectContexts.clear();
        if(semCount!=null) semCount.setValue(null); if(subGrid!=null) subGrid.getChildren().clear(); if(workStartTimeCombo!=null) workStartTimeCombo.setValue(null); if(workEndTimeCombo!=null) workEndTimeCombo.setValue(null);
        if(breakStartTimeCombo!=null) breakStartTimeCombo.setValue(null); if(breakEndTimeCombo!=null) breakEndTimeCombo.setValue(null); if(slotDurationCombo!=null) slotDurationCombo.setValue(null);
        if(teachCount!=null) teachCount.setValue(null); if(mapSubs!=null) mapSubs.getChildren().clear(); hideTimeConfigSection(); hideTeacherSection();
        if(generatedScrollPane!=null) generatedScrollPane.setContent(new Label("Load config or input details.")); System.out.println("App state cleared.");
    }

    // Helper to rebuild the observable list after loading data
    private void rebuildSubjectContextList() {
        // (Implementation remains the same)
        allSubjectsObservableList.clear(); for(Semester sem:this.semesterList){ if(sem.getSubjects()!=null){ for(Subject sub:sem.getSubjects()){ if(sub!=null){allSubjectsObservableList.add(new SubjectContext(sub,sem));}}}}
        allSubjectsObservableList.sort(Comparator.comparing((SubjectContext sc)->sc.getSubject().getName()).thenComparing(sc->sc.getSemester().getName())); System.out.println("Rebuilt SubjectContext list: "+allSubjectsObservableList.size()+" items.");
    }

    // Helper to Repopulate UI after Loading (Simplified Approach)
    private void repopulateUIFromLoad(TimetableConfiguration loadedConfig, ComboBox<Integer> semCount, GridPane subGrid) {
        System.out.println("Repopulating UI from load...");
        // 1. Set Time Config UI elements directly
        workStartTimeCombo.setValue(loadedConfig.workStartTime); workEndTimeCombo.setValue(loadedConfig.workEndTime);
        breakStartTimeCombo.setValue(loadedConfig.breakStartTime); breakEndTimeCombo.setValue(loadedConfig.breakEndTime);
        slotDurationCombo.setValue(loadedConfig.slotDurationMinutes);
        // Validate and store loaded time config values internally
        boolean timeValid = validateAndStoreTimeConfig();
        if (timeValid) { showTimeConfigSection(); } else { showTimeConfigSection(); System.err.println("Loaded time config failed validation."); }

        // 2. Set Semester Count (triggers UI creation)
        int numSemesters = loadedConfig.semesters != null ? loadedConfig.semesters.size() : 0;
        semCount.setValue(numSemesters);

        // 3. Populate Semester/Subject Fields (after UI creation)
        Platform.runLater(() -> { // Defer population until UI updates
            System.out.println("Populating semester/subject fields (runLater)...");
            try { // Add try-catch for robustness
                if (numSemesters > 0 && subGrid.getRowCount() >= numSemesters) {
                    for (int k = 0; k < numSemesters; k++) {
                        Semester loadedSemester = loadedConfig.semesters.get(k); if(loadedSemester==null) continue;
                        TextField semNameField = (TextField) findNodeInParent(subGrid, "semName_" + k);
                        ComboBox<Integer> subCountBox = (ComboBox<Integer>) findNodeInParent(subGrid, "subCount_" + k);
                        FlowPane subNamePane = (FlowPane) findNodeInParent(subGrid, "subNamePane_" + k);
                        if (semNameField != null) semNameField.setText(loadedSemester.getName());
                        int numSubjects = loadedSemester.getSubjects() != null ? loadedSemester.getSubjects().size() : 0;
                        if (subCountBox != null) { subCountBox.setValue(numSubjects); // Trigger subject field creation
                            final int semIdx = k; final int finalNumSubjects = numSubjects; final Semester finalLoadedSem = loadedSemester;
                            Platform.runLater(() -> { // Nested runLater for subject fields
                                if (subNamePane != null && subNamePane.getChildren().size() == finalNumSubjects) {
                                    for (int s = 0; s < finalNumSubjects; s++) {
                                        Subject loadedSubject = finalLoadedSem.getSubjects().get(s); if(loadedSubject==null) continue;
                                        int hours = finalLoadedSem.getHoursForSubject(loadedSubject);
                                        Node node = subNamePane.getChildren().get(s);
                                        if (node instanceof HBox) {
                                            HBox box = (HBox) node; TextField nameFld = (TextField) findNodeByIdInPane(box,"subName_"+semIdx+"_"+s); Spinner<Integer> hrsSpn = (Spinner<Integer>) findNodeByIdInPane(box,"subHours_"+semIdx+"_"+s);
                                            if (nameFld != null) nameFld.setText(loadedSubject.getName()); if (hrsSpn != null && hours > 0) hrsSpn.getValueFactory().setValue(hours);
                                        }
                                    }
                                } else { System.err.println("Subject pane children mismatch on load sem "+semIdx); }
                            }); // End nested runLater for subjects
                        } // End if subCountBox!=null
                    } // End semester loop
                } else if (numSemesters > 0) { System.err.println("Semester grid rows not ready after setting count."); }
            } catch (Exception e) { System.err.println("Error during semester UI repopulation: " + e.getMessage()); e.printStackTrace(); }

            // 4. Set Teacher Count (Actual UI population requires user interaction via Next)
            int numTeachers = loadedConfig.teachers != null ? loadedConfig.teachers.size() : 0;
            if (teachCount != null) { teachCount.setValue(numTeachers); }
            System.out.println("UI repopulation attempt finished.");
            // User needs to click "Next" through time/teacher sections to see loaded teacher details
        }); // End Platform.runLater for semesters
    }

    // --- Main Method ---
    public static void main(String[] args) {
        launch(args);
    }

} // End of ClassMesh class
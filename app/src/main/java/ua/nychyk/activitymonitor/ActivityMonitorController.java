package ua.nychyk.activitymonitor;

import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Modality;
import javafx.stage.Stage;

import ua.nychyk.activitymonitor.factory.MonitorRepositoryFactory;
import ua.nychyk.activitymonitor.monitors.*;
import ua.nychyk.activitymonitor.patterns.commands.*;
import ua.nychyk.activitymonitor.patterns.visitors.*;
import ua.nychyk.activitymonitor.report.*;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

import com.github.kwhat.jnativehook.GlobalScreen;
import com.github.kwhat.jnativehook.NativeHookException;

public class ActivityMonitorController {

    // GUI Labels
    private final Label cpuLabel = new Label("CPU Usage: Loading...");
    private final Label memoryLabel = new Label("Memory Usage: Loading...");
    private final Label windowLabel = new Label("Active Window: Loading...");
    private final Label usageLabel = new Label("Computer Usage: Loading...");
    private final Label keyboardLabel = new Label("Keyboard Activity: Loading...");
    private final Label mouseLabel = new Label("Mouse Activity: Loading...");

    // Монітори
    private final CpuMonitor cpuMonitor;
    private final MemoryMonitor memoryMonitor;
    private final WindowMonitor windowMonitor;
    private final ComputerUsageMonitor usageMonitor;
    private final KeyboardMonitor keyboardMonitor;
    private final MouseMonitor mouseMonitor;

    private boolean isMonitoring = false;

    // Звіти
    private final String dbFile;
    private final ReportService reportService;

    public ActivityMonitorController(Stage stage, String dbFile) {
        this.dbFile = dbFile;
        this.reportService = new ReportService(dbFile);

        // -----------------------------
        // 0. JNativeHook — логіка глобальних клавіш/миші
        // -----------------------------
        disableNativeHookLogging();

        try {
            GlobalScreen.registerNativeHook();
        } catch (NativeHookException e) {
            System.err.println("Failed to register native hook: " + e.getMessage());
        }

        // -----------------------------
        // 1. Репозиторії
        // -----------------------------
        MonitorRepositoryFactory repoFactory = new MonitorRepositoryFactory(dbFile);

        // -----------------------------
        // 2. Монітори
        // -----------------------------
        cpuMonitor = new CpuMonitor(cpuLabel, repoFactory.getProcessorRepository());
        memoryMonitor = new MemoryMonitor(memoryLabel, repoFactory.getMemoryRepository());
        usageMonitor = new ComputerUsageMonitor(usageLabel, repoFactory.getComputerUsageRepository());
        windowMonitor = new WindowMonitor(windowLabel, repoFactory.getWindowRepository());

        keyboardMonitor = new KeyboardMonitor(keyboardLabel);
        mouseMonitor = new MouseMonitor(mouseLabel);

        // -----------------------------
        // 3. GUI
        // -----------------------------
        VBox root = new VBox(10);
        root.setPadding(new Insets(10));

        Label title = new Label("Activity Monitor");
        title.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");

        Button reportButton = new Button("Generate Report");
        reportButton.setOnAction(e -> openReportWindow(stage));

        root.getChildren().addAll(
                title,
                cpuLabel,
                memoryLabel,
                windowLabel,
                usageLabel,
                keyboardLabel,
                mouseLabel,
                reportButton
        );

        Scene scene = new Scene(root, 500, 350);
        stage.setScene(scene);
        stage.setTitle("Activity Monitor");
        stage.setOnCloseRequest(e -> stop());
        stage.show();

        // -----------------------------
        // 4. Моніторинговий цикл
        // -----------------------------
        startMonitoring();
    }

    // Disable JNativeHook logs
    private void disableNativeHookLogging() {
        java.util.logging.Logger logger = java.util.logging.Logger.getLogger(GlobalScreen.class.getPackage().getName());
        logger.setLevel(java.util.logging.Level.OFF);
        logger.setUseParentHandlers(false);
    }

    private void startMonitoring() {
        if (isMonitoring) return;
        isMonitoring = true;

        Thread loop = new Thread(() -> {
            while (isMonitoring) {
                cpuMonitor.updateWidget();
                memoryMonitor.updateWidget();
                windowMonitor.updateWidget();

                usageMonitor.checkActivity(true);  // always active (як у твоїй Python версії)
                usageMonitor.updateWidget();

                keyboardMonitor.updateWidget();
                mouseMonitor.updateWidget();

                try {
                    Thread.sleep(1000);
                } catch (InterruptedException ignored) {}
            }
        });

        loop.setDaemon(true);
        loop.start();
    }

    public void stop() {
        isMonitoring = false;

        cpuMonitor.saveData();
        memoryMonitor.saveData();
        windowMonitor.saveData();
        usageMonitor.saveData();
    }

    // ==========================================================
    //                  REPORT WINDOW (GUI)
    // ==========================================================

    private void openReportWindow(Stage parentStage) {
        Stage dialog = new Stage();
        dialog.initOwner(parentStage);
        dialog.initModality(Modality.APPLICATION_MODAL);
        dialog.setTitle("Generate Report");

        VBox main = new VBox(10);
        main.setPadding(new Insets(10));

        // ------------------------------
        // 1. By Day / By Period
        // ------------------------------
        ToggleGroup modeGroup = new ToggleGroup();
        RadioButton byDayRadio = new RadioButton("By Day");
        RadioButton byPeriodRadio = new RadioButton("By Period");

        byDayRadio.setToggleGroup(modeGroup);
        byPeriodRadio.setToggleGroup(modeGroup);
        byDayRadio.setSelected(true);

        HBox modeBox = new HBox(10, byDayRadio, byPeriodRadio);
        main.getChildren().addAll(new Label("Choose Daily or Periodic Report:"), modeBox);

        // ------------------------------
        // 2. Date Inputs
        // ------------------------------
        HBox dayBox = new HBox(10);
        Label dayLabel = new Label("Date (YYYY-MM-DD):");
        TextField dayField = new TextField();
        dayBox.getChildren().addAll(dayLabel, dayField);

        HBox periodBox = new HBox(10);
        Label startLabel = new Label("Start (YYYY-MM-DD):");
        TextField startField = new TextField();
        Label endLabel = new Label("End (YYYY-MM-DD):");
        TextField endField = new TextField();
        periodBox.getChildren().addAll(startLabel, startField, endLabel, endField);

        main.getChildren().add(dayBox);

        byDayRadio.setOnAction(e -> {
            main.getChildren().remove(periodBox);
            if (!main.getChildren().contains(dayBox)) main.getChildren().add(2, dayBox);
        });

        byPeriodRadio.setOnAction(e -> {
            main.getChildren().remove(dayBox);
            if (!main.getChildren().contains(periodBox)) main.getChildren().add(2, periodBox);
        });

        // ------------------------------
        // 3. Report-Type
        // ------------------------------
        ToggleGroup reportTypeGroup = new ToggleGroup();
        VBox reportTypeBox = new VBox(5);

        RadioButton r1 = makeReportType("CPU Usage by Hours", 1, reportTypeGroup);
        RadioButton r2 = makeReportType("Browser Usage Percentage", 2, reportTypeGroup);
        RadioButton r3 = makeReportType("Memory Usage by Hours", 3, reportTypeGroup);
        RadioButton r4 = makeReportType("Computer Uptime by Day(s)", 4, reportTypeGroup);
        RadioButton r5 = makeReportType("Programs Used by Day(s)", 5, reportTypeGroup);
        RadioButton r6 = makeReportType("Average CPU Usage by Days", 6, reportTypeGroup);
        RadioButton r7 = makeReportType("Average Memory Usage by Days", 7, reportTypeGroup);

        reportTypeBox.getChildren().addAll(
                new Label("Choose Type of Report:"),
                r1, r2, r3, r4, r5, r6, r7
        );

        main.getChildren().add(reportTypeBox);

        // ------------------------------
        // 4. Format
        // ------------------------------
        ToggleGroup formatGroup = new ToggleGroup();
        RadioButton textFormat = new RadioButton("Text");
        RadioButton jsonFormat = new RadioButton("JSON");
        textFormat.setToggleGroup(formatGroup);
        jsonFormat.setToggleGroup(formatGroup);
        textFormat.setSelected(true);

        VBox formatBox = new VBox(5,
                new Label("Choose Format:"),
                textFormat, jsonFormat
        );
        main.getChildren().add(formatBox);

        // ------------------------------
        // 5. Error Label
        // ------------------------------
        Label errorLabel = new Label();
        errorLabel.setStyle("-fx-text-fill: red;");
        main.getChildren().add(errorLabel);

        // ------------------------------
        // 6. Submit
        // ------------------------------
        Button submitButton = new Button("Submit");
        submitButton.setOnAction(e -> {
            boolean byDay = modeGroup.getSelectedToggle() == byDayRadio;

            String day = dayField.getText().trim();
            String start = startField.getText().trim();
            String end = endField.getText().trim();

            Integer type = reportTypeGroup.getSelectedToggle() == null
                    ? null
                    : (Integer) reportTypeGroup.getSelectedToggle().getUserData();

            String fmt = textFormat.isSelected() ? "Text" : "JSON";

            String err = validateReportInput(byDay, day, start, end, type);
            if (err != null) {
                errorLabel.setText(err);
                return;
            }

            Object result = generateReport(byDay, day, start, end, type, fmt);
            showReportResult(dialog, result, fmt);
        });

        main.getChildren().add(submitButton);

        Scene dialogScene = new Scene(main, 520, 460);
        dialog.setScene(dialogScene);
        dialog.show();
    }

    private RadioButton makeReportType(String text, int type, ToggleGroup group) {
        RadioButton btn = new RadioButton(text);
        btn.setUserData(type);
        btn.setToggleGroup(group);
        return btn;
    }

    // ==========================================================
    //                     REPORT LOGIC
    // ==========================================================

    private String validateReportInput(
            boolean byDay, String day, String start, String end, Integer type
    ) {
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        LocalDate now = LocalDate.now();

        if (type == null) return "Please select a report type.";

        if (byDay) {
            if (day.isEmpty()) return "Date is required.";

            try {
                LocalDate d = LocalDate.parse(day, fmt);
                if (d.isAfter(now)) return "Date cannot be in the future.";
            } catch (DateTimeParseException ex) {
                return "Invalid date format.";
            }
        } else {
            if (start.isEmpty() || end.isEmpty()) return "Start and end dates required.";

            try {
                LocalDate s = LocalDate.parse(start, fmt);
                LocalDate e = LocalDate.parse(end, fmt);

                if (s.isAfter(e)) return "Start cannot be after end.";
                if (e.isAfter(now)) return "End cannot be in the future.";
                if (s.isEqual(e)) return "Start and end cannot be equal.";

            } catch (DateTimeParseException ex) {
                return "Invalid date format.";
            }
        }

        return null;
    }

    private Object generateReport(
            boolean byDay, String day, String start, String end, int type, String format
    ) {
        ReportInvoker invoker = new ReportInvoker();
        Command cmd;

        if (byDay)
            cmd = new GenerateDailyReportCommand(reportService, day, type);
        else
            cmd = new GeneratePeriodicReportCommand(reportService, start, end, type);

        invoker.setCommand(cmd);

        ReportVisitor visitor = format.equals("Text")
                ? new TextReportVisitor()
                : new JSONReportVisitor();

        return invoker.executeCommand(visitor);
    }

    private void showReportResult(Stage parent, Object result, String format) {
        Stage dialog = new Stage();
        dialog.initOwner(parent);
        dialog.initModality(Modality.APPLICATION_MODAL);
        dialog.setTitle("Report Result");

        VBox root = new VBox(10);
        root.setPadding(new Insets(10));

        root.getChildren().add(new Label("Report Result (" + format + "):"));

        TextArea area = new TextArea(result == null ? "No data" : result.toString());
        area.setEditable(false);
        area.setWrapText(true);

        Button close = new Button("Close");
        close.setOnAction(e -> dialog.close());

        root.getChildren().addAll(area, close);

        Scene scene = new Scene(root, 600, 400);
        dialog.setScene(scene);
        dialog.show();
    }
}

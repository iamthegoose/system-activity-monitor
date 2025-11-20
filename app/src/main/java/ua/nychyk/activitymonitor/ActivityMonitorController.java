package ua.nychyk.activitymonitor;

import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Modality;
import javafx.stage.Stage;

import ua.nychyk.activitymonitor.factory.MonitorRepositoryFactory;
import ua.nychyk.activitymonitor.monitors.ComputerUsageMonitor;
import ua.nychyk.activitymonitor.monitors.CpuMonitor;
import ua.nychyk.activitymonitor.monitors.MemoryMonitor;
import ua.nychyk.activitymonitor.monitors.WindowMonitor;
import ua.nychyk.activitymonitor.patterns.commands.Command;
import ua.nychyk.activitymonitor.patterns.commands.GenerateDailyReportCommand;
import ua.nychyk.activitymonitor.patterns.commands.GeneratePeriodicReportCommand;
import ua.nychyk.activitymonitor.patterns.visitors.JSONReportVisitor;
import ua.nychyk.activitymonitor.patterns.visitors.ReportVisitor;
import ua.nychyk.activitymonitor.patterns.visitors.TextReportVisitor;
import ua.nychyk.activitymonitor.monitors.Monitor;

import ua.nychyk.activitymonitor.report.ReportService;
import ua.nychyk.activitymonitor.report.ReportInvoker;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

public class ActivityMonitorController {

    // GUI Labels
    private final Label cpuLabel = new Label("CPU Usage: Loading...");
    private final Label memoryLabel = new Label("Memory Usage: Loading...");
    private final Label windowLabel = new Label("Active Window: Loading...");
    private final Label usageLabel = new Label("Computer Usage: Loading...");

    // Монітори
    private final CpuMonitor cpuMonitor;
    private final MemoryMonitor memoryMonitor;
    private final WindowMonitor windowMonitor;
    private final ComputerUsageMonitor usageMonitor;

    private boolean isMonitoring = false;

    // Звітна логіка
    private final String dbFile;
    private final ReportService reportService;

    public ActivityMonitorController(Stage stage, String dbFile) {
        this.dbFile = dbFile;
        this.reportService = new ReportService(dbFile);

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
                reportButton
        );

        Scene scene = new Scene(root, 500, 280);
        stage.setScene(scene);
        stage.setTitle("Activity Monitor");
        stage.setOnCloseRequest(e -> stop());
        stage.show();

        // -----------------------------
        // 4. Моніторинговий цикл
        // -----------------------------
        startMonitoring();
    }

    private void startMonitoring() {
        if (isMonitoring) return;
        isMonitoring = true;

        Thread thread = new Thread(() -> {
            while (isMonitoring) {
                // updateWidget() сам оновлює Label
                cpuMonitor.updateWidget();
                memoryMonitor.updateWidget();
                windowMonitor.updateWidget();
                usageMonitor.checkActivity(true);  // тимчасово: завжди активний
                usageMonitor.updateWidget();

                try {
                    Thread.sleep(1000);
                } catch (InterruptedException ignored) {}
            }
        });

        thread.setDaemon(true);
        thread.start();
    }

    public void stop() {
        isMonitoring = false;

        cpuMonitor.saveData();
        memoryMonitor.saveData();
        windowMonitor.saveData();
        usageMonitor.saveData();
    }

    // ==========================================================
    //            Вікно генерації звітів (GUI + логіка)
    // ==========================================================

    private void openReportWindow(Stage parentStage) {
        Stage dialog = new Stage();
        dialog.initOwner(parentStage);
        dialog.initModality(Modality.APPLICATION_MODAL);
        dialog.setTitle("Generate Report");

        VBox main = new VBox(10);
        main.setPadding(new Insets(10));

        // ------------------------------
        // 1. Вибір режиму: By Day / By Period
        // ------------------------------
        ToggleGroup modeGroup = new ToggleGroup();
        RadioButton byDayRadio = new RadioButton("By Day");
        RadioButton byPeriodRadio = new RadioButton("By Period");
        byDayRadio.setToggleGroup(modeGroup);
        byPeriodRadio.setToggleGroup(modeGroup);
        byDayRadio.setSelected(true);

        HBox modeBox = new HBox(10, byDayRadio, byPeriodRadio);
        main.getChildren().addAll(new Label("Choose daily or periodic report:"), modeBox);

        // ------------------------------
        // 2. Поля для дат
        // ------------------------------
        // By Day
        HBox dayBox = new HBox(10);
        Label dayLabel = new Label("Date (YYYY-MM-DD):");
        TextField dayField = new TextField();
        dayBox.getChildren().addAll(dayLabel, dayField);

        // By Period
        HBox periodBox = new HBox(10);
        Label startLabel = new Label("Start (YYYY-MM-DD):");
        TextField startField = new TextField();
        Label endLabel = new Label("End (YYYY-MM-DD):");
        TextField endField = new TextField();
        periodBox.getChildren().addAll(startLabel, startField, endLabel, endField);

        main.getChildren().add(dayBox); // початково показуємо dayBox

        // ------------------------------
        // 3. Вибір типу звіту (report_type)
        // ------------------------------
        ToggleGroup reportTypeGroup = new ToggleGroup();

        VBox reportTypeBox = new VBox(5);
        Label reportTypeLabel = new Label("Choose Type of Report:");

        RadioButton r1 = new RadioButton("CPU Usage by Hours");
        r1.setUserData(1);
        RadioButton r2 = new RadioButton("Browser Usage Percentage");
        r2.setUserData(2);
        RadioButton r3 = new RadioButton("Memory Usage by Hours");
        r3.setUserData(3);
        RadioButton r4 = new RadioButton("Computer Uptime by Day(s)");
        r4.setUserData(4);
        RadioButton r5 = new RadioButton("Programs Used by Day(s)");
        r5.setUserData(5);
        RadioButton r6 = new RadioButton("Average CPU Usage by Days");
        r6.setUserData(6);
        RadioButton r7 = new RadioButton("Average Memory Usage by Days");
        r7.setUserData(7);

        r1.setToggleGroup(reportTypeGroup);
        r2.setToggleGroup(reportTypeGroup);
        r3.setToggleGroup(reportTypeGroup);
        r4.setToggleGroup(reportTypeGroup);
        r5.setToggleGroup(reportTypeGroup);
        r6.setToggleGroup(reportTypeGroup);
        r7.setToggleGroup(reportTypeGroup);

        reportTypeBox.getChildren().addAll(reportTypeLabel, r1, r2, r3, r4, r5, r6, r7);
        main.getChildren().add(reportTypeBox);

        // ------------------------------
        // 4. Вибір формату (Text / JSON)
        // ------------------------------
        ToggleGroup formatGroup = new ToggleGroup();
        RadioButton textFormat = new RadioButton("Text");
        RadioButton jsonFormat = new RadioButton("JSON");
        textFormat.setToggleGroup(formatGroup);
        jsonFormat.setToggleGroup(formatGroup);
        textFormat.setSelected(true);

        VBox formatBox = new VBox(5,
                new Label("Choose Report Format:"),
                textFormat,
                jsonFormat
        );
        main.getChildren().add(formatBox);

        // ------------------------------
        // 5. Error label
        // ------------------------------
        Label errorLabel = new Label();
        errorLabel.setStyle("-fx-text-fill: red;");
        main.getChildren().add(errorLabel);

        // ------------------------------
        // 6. Перемикання режимів
        // ------------------------------
        byDayRadio.setOnAction(e -> {
            main.getChildren().remove(periodBox);
            if (!main.getChildren().contains(dayBox)) {
                main.getChildren().add(2, dayBox); // приблизно після modeBox
            }
        });

        byPeriodRadio.setOnAction(e -> {
            main.getChildren().remove(dayBox);
            if (!main.getChildren().contains(periodBox)) {
                main.getChildren().add(2, periodBox);
            }
        });

        // ------------------------------
        // 7. Кнопка Submit
        // ------------------------------
        Button submitButton = new Button("Submit");
        submitButton.setOnAction(e -> {
            boolean byDay = modeGroup.getSelectedToggle() == byDayRadio;
            String day = dayField.getText().trim();
            String start = startField.getText().trim();
            String end = endField.getText().trim();

            Integer reportType = null;
            if (reportTypeGroup.getSelectedToggle() != null) {
                reportType = (Integer) reportTypeGroup.getSelectedToggle().getUserData();
            }

            String format = formatGroup.getSelectedToggle() == textFormat ? "Text" : "JSON";

            String err = validateReportInput(byDay, day, start, end, reportType);
            if (err != null) {
                errorLabel.setText(err);
                return;
            }

            Object result = generateReport(byDay, day, start, end, reportType, format);
            showReportResult(dialog, result, format);
        });

        main.getChildren().add(submitButton);

        Scene dialogScene = new Scene(main, 520, 450);
        dialog.setScene(dialogScene);
        dialog.show();
    }

    // ==========================================================
    //                    Логіка звітів
    // ==========================================================

    private String validateReportInput(
            boolean byDay,
            String day,
            String start,
            String end,
            Integer reportType
    ) {
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        LocalDate now = LocalDate.now();

        if (reportType == null) {
            return "Please select a report type.";
        }

        if (byDay) {
            if (day.isEmpty()) return "Date is required.";
            LocalDate d;
            try {
                d = LocalDate.parse(day, fmt);
            } catch (DateTimeParseException e) {
                return "Date must be in format YYYY-MM-DD.";
            }
            if (d.isAfter(now)) return "Date cannot be in the future.";
        } else {
            if (start.isEmpty() || end.isEmpty()) {
                return "Start and end dates are required.";
            }

            LocalDate s, e;
            try {
                s = LocalDate.parse(start, fmt);
                e = LocalDate.parse(end, fmt);
            } catch (DateTimeParseException ex) {
                return "Dates must be in format YYYY-MM-DD.";
            }

            if (s.isEqual(e)) return "Start and end date cannot be the same.";
            if (s.isAfter(e)) return "Start date cannot be later than end date.";
            if (e.isAfter(now)) return "End date cannot be in the future.";
        }

        return null; // все ок
    }

    private Object generateReport(
            boolean byDay,
            String day,
            String start,
            String end,
            int reportType,
            String format
    ) {
        ReportInvoker invoker = new ReportInvoker();
        Command cmd;

        if (byDay) {
            cmd = new GenerateDailyReportCommand(reportService, day, reportType);
        } else {
            cmd = new GeneratePeriodicReportCommand(reportService, start, end, reportType);
        }

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

        Label title = new Label("Report Result (" + format + "):");
        TextArea area = new TextArea();
        area.setEditable(false);
        area.setWrapText(true);

        if (result != null) {
            area.setText(result.toString());
        } else {
            area.setText("No data or error generating report.");
        }

        Button closeBtn = new Button("Close");
        closeBtn.setOnAction(e -> dialog.close());

        root.getChildren().addAll(title, area, closeBtn);

        Scene scene = new Scene(root, 600, 400);
        dialog.setScene(scene);
        dialog.show();
    }
}

package ua.nychyk.activitymonitor;

import javafx.application.Application;
import javafx.stage.Stage;

public class Main extends Application {

    @Override
    public void start(Stage primaryStage) {
        String dbFile =  "db.sqlite";
        System.out.println("DB PATH = " + dbFile);

        new ActivityMonitorController(primaryStage, dbFile);
    }

    public static void main(String[] args) {
        launch(args);
    }
}

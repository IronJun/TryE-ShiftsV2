package com.ispw.tryeshiftsv2;

import javafx.application.Application;
import javafx.stage.Stage;
import com.ispw.tryeshiftsv2.SceneManager;

public class main extends Application {

    public void start(Stage primaryStage) throws Exception
    {
        SceneManager manager = SceneManager.getInstance();
        manager.setPrimaryStage(primaryStage);
        manager.switchScene("SignUp.fxml","E-Shifts - Sign UP", 850,550);
    }
    public static void main(String[] args) {
        launch(args);

    }
}

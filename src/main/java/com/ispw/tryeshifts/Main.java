package com.ispw.tryeshifts;

import javafx.application.Application;
import javafx.stage.Stage;

public class Main extends Application {

    public void start(Stage primaryStage) throws Exception
    {
        SceneManager manager = SceneManager.getInstance();
        manager.setPrimaryStage(primaryStage);
        manager.switchScene("SignUp.fxml","E-Shifts - Sign UP", 850,550);
    }
}

package com.ispw.tryeshifts;

import com.ispw.tryeshifts.dao.JDBC;
import com.ispw.tryeshifts.entity.UserInfo;
import javafx.application.Application;
import javafx.stage.Stage;

public class Main extends Application {

    public void start(Stage primaryStage) throws Exception
    {
        // --- TEST DB RAPIDO ---


        SceneManager manager = SceneManager.getInstance();
        manager.setPrimaryStage(primaryStage);
        manager.switchScene("SignUp.fxml","E-Shifts - Sign UP", 850,550);
    }
    public static void main(String[] args) {
        launch(args);
    }
}

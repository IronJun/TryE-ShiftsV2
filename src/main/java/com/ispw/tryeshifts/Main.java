package com.ispw.tryeshifts;

//import com.ispw.tryeshifts.dao.JDBC;
import com.ispw.tryeshifts.appcontroller.LoginAC;
import com.ispw.tryeshifts.bean.SessionContext;
import com.ispw.tryeshifts.bean.UserBean;
import com.ispw.tryeshifts.entity.UserInfo;
import com.ispw.tryeshifts.excpetion.BaseException;
import javafx.application.Application;
import javafx.stage.Stage;

public class Main extends Application {

    public void start(Stage primaryStage) throws Exception
    {
        SceneManager manager = SceneManager.getInstance();
        manager.setPrimaryStage(primaryStage);
        String savedEmail = SessionContext.getInstance().getSavedEmail();
        if(savedEmail != null){
            try{
                UserBean user = LoginAC.autoLogin(savedEmail);
                SessionContext.getInstance().setLoggeduser(user);
                manager.switchScene("Home.fxml","E-Shifts - Home", 900, 600);
                return;
            }catch(BaseException e){
                SessionContext.getInstance().clearPreferences();
            }
        }
        manager.switchScene("SignUp.fxml","E-Shifts - Sign UP", 850,550);
    }
    public static void main(String[] args) {
        launch(args);
    }
}

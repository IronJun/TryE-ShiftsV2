package com.ispw.tryeshifts.graphcontroller.javafx.utilities;

import com.ispw.tryeshifts.SceneManager;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextInputControl;

public class ErrorViewManager {

    private ErrorViewManager() {
        throw new IllegalStateException("Utility class");
    }
    public static void showError(Label errorLabel, String message) {
        if (errorLabel != null) {
            errorLabel.setText(message);
            errorLabel.setVisible(true);
            errorLabel.setManaged(true);
        }
    }
    public static void hideError(Label errorLabel) {
        if (errorLabel != null) {
            errorLabel.setVisible(false);
            errorLabel.setManaged(false);
            errorLabel.setText("");
        }
    }
    public static void setupAutoHide(Label errorLabel, TextInputControl... fields){
        for(TextInputControl field : fields){
            field.textProperty().addListener((observable, oldValue, newValue) -> hideError(errorLabel));
        }
    }
    public static void setupAutoHideCombo(Label errorLabel, ComboBox<?>... combos){
        for(ComboBox<?> combo : combos){
            combo.valueProperty().addListener((observable, oldValue, newValue) -> hideError(errorLabel));
        }
    }
    public static void screenError(String title, String message){
        SceneManager.getInstance().showErrorAlert(title, message);
    }
}

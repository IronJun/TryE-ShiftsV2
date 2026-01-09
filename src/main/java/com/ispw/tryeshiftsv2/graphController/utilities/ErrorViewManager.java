package com.ispw.tryeshiftsv2.graphController.utilities;

import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextInputControl;

public class ErrorViewManager {

    public static void showError(Label errorLabel, String message) {
        if (errorLabel != null) {
            errorLabel.setText(message);
            errorLabel.setVisible(true);
            errorLabel.setManaged(true);
        }
    }

    // Metodo per nascondere l'errore
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
}

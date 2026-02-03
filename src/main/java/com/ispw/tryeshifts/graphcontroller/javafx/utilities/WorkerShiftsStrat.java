package com.ispw.tryeshifts.graphcontroller.javafx.utilities;

import javafx.scene.control.Button;
import javafx.scene.control.Label;

public class WorkerShiftsStrat implements ShiftsUIStrat {
    @Override
    public void customizeUI(Label label, Button saveBtn, Button publicBtn) {
        label.setText("Clicca sui giorni in cui desideri dare la tua disponibilità.");
        saveBtn.setVisible(true);
        saveBtn.setManaged(true);
        publicBtn.setVisible(false);
        publicBtn.setManaged(false);
    }
}

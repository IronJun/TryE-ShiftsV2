package com.ispw.tryeshifts.graphcontroller.gui.utilities.stratgui;

import javafx.scene.control.Button;
import javafx.scene.control.Label;

public class WorkerShiftsStrat implements ShiftsUIStrat {
    public void customizeUI(Label label, Button saveBtn, Button publicBtn, String status) {
        publicBtn.setVisible(false);
        publicBtn.setManaged(false);

        if ("OPEN".equals(status)) {
            label.setText("Click on the shifts to give your availabilities.");
            saveBtn.setVisible(true);
            saveBtn.setManaged(true);
        } else {
            // Se LOCKED o PUBLISHED, il lavoratore può solo guardare
            label.setText("The shifts for this week have been locked.");
            saveBtn.setVisible(false);
            saveBtn.setManaged(false);
        }
    }
}

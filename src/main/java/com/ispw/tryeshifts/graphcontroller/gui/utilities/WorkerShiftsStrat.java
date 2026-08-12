package com.ispw.tryeshifts.graphcontroller.gui.utilities;

import javafx.scene.control.Button;
import javafx.scene.control.Label;

public class WorkerShiftsStrat implements ShiftsUIStrat {
    public void customizeUI(Label label, Button saveBtn, Button publicBtn, String status) {
        publicBtn.setVisible(false);
        publicBtn.setManaged(false);

        if ("OPEN".equals(status)) {
            label.setText("Clicca sui turni per dare la tua disponibilità.");
            saveBtn.setVisible(true);
            saveBtn.setManaged(true);
        } else {
            // Se LOCKED o PUBLISHED, il lavoratore può solo guardare
            label.setText("Le disponibilità per questa settimana sono chiuse (Sola Lettura).");
            saveBtn.setVisible(false);
            saveBtn.setManaged(false);
        }
    }
}

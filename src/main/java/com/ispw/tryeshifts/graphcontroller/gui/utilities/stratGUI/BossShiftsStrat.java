package com.ispw.tryeshifts.graphcontroller.gui.utilities.stratGUI;

import javafx.scene.control.Button;
import javafx.scene.control.Label;

public class BossShiftsStrat implements ShiftsUIStrat {

    public void customizeUI(Label label, Button saveBtn, Button publicBtn,String status) {
        label.setText("Gestione turni - Stato attuale: " + status);
        saveBtn.setVisible(false);
        saveBtn.setManaged(false);

        publicBtn.setVisible(true);
        publicBtn.setManaged(true);

        // LOGICA DINAMICA DEL TESTO
        if ("OPEN".equals(status)) {
            publicBtn.setText("Lock Shifts");
        } else if ("LOCKED".equals(status)) {
            publicBtn.setText("Public Shifts");
        } else if ("PUBLISHED".equals(status)) {
            publicBtn.setText("Turni Pubblicati");
            publicBtn.setDisable(true); // Opzionale: disabilita se già fatto
        }
    }
}

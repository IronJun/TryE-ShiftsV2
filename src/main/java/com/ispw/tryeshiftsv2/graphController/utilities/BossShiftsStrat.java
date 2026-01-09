package com.ispw.tryeshiftsv2.graphController.utilities;

import javafx.scene.control.Button;
import javafx.scene.control.Label;

public class BossShiftsStrat implements ShiftsUIStrat{

    public void customizeUI(Label label, Button saveBtn, Button publicBtn) {
        label.setText("Le disponibilità dei turni appariranno in questa tabella");
        publicBtn.setVisible(true);
        publicBtn.setManaged(true);
        saveBtn.setVisible(false);
        saveBtn.setManaged(false);
    }
}

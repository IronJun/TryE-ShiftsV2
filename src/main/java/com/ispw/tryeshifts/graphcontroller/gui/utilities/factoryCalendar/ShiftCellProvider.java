package com.ispw.tryeshifts.graphcontroller.gui.utilities.factoryCalendar;

import javafx.scene.layout.VBox;

import java.util.List;

public interface ShiftCellProvider {
    VBox createCell(String cellKey, List<String> cellContent, boolean isDayActive);
}

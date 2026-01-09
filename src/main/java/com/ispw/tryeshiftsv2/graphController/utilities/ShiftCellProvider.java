package com.ispw.tryeshiftsv2.graphController.utilities;

import javafx.scene.layout.VBox;

import java.util.List;

public interface ShiftCellProvider {
    VBox createCell(String cellKey, List<String> cellContent, boolean isDayActive);
}

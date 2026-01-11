package com.ispw.tryeshifts.graphController.utilities;

import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;

import java.util.List;
import java.util.Map;

public class WorkerCellFactory implements ShiftCellProvider{
    private final Map<String,Boolean> selectionMap;

    public WorkerCellFactory(Map<String,Boolean> selectionMap){
        this.selectionMap = selectionMap;
    }

    public VBox createCell(String cellKey, List<String> content, boolean isDayActive){
        VBox cell = new VBox(3);
        cell.setAlignment(Pos.TOP_CENTER);
        //cell.setCursor(Cursor.HAND);

        if (!isDayActive) {
            cell.setStyle("-fx-border-color: #D1CFE2; -fx-padding: 5; -fx-background-color: #f2f2f2;");
            cell.setCursor(Cursor.DEFAULT); // Niente manina

            Label status = new Label("CHIUSO");
            status.setStyle("-fx-text-fill: #bdc3c7; -fx-font-size: 10px; -fx-font-weight: bold;");
            cell.getChildren().add(status);

            // Non aggiungiamo il listener per il click, così la cella è inerte
            return cell;
        }

        cell.setCursor(Cursor.HAND);
        boolean isSelected = content.contains("SELECTED") || selectionMap.getOrDefault(cellKey,false);
        if(isSelected) selectionMap.put(cellKey,true);

        Label status = new Label(isSelected ? "Selezionato" : "Libero");
        applyStyle(cell,status,isSelected);

        cell.setOnMouseClicked(e -> {
            boolean newState = !selectionMap.getOrDefault(cellKey,false);
            selectionMap.put(cellKey,newState);
            System.out.println("DEBUG FACTORY: Cliccata cella [" + cellKey + "] -> Stato: " + newState);
            applyStyle(cell,status,newState);
        });

        cell.getChildren().add(status);

        return cell;
    }

    private void applyStyle(VBox cell, Label status, boolean sel) {
        cell.setStyle("-fx-border-color: #D1CFE2; -fx-padding: 5; -fx-background-color: " + (sel ? "#3498db;" : "white;"));
        status.setText(sel ? "Selezionato" : "Libero");
        status.setStyle("-fx-text-fill: " + (sel ? "white;" : "#cccccc;"));
    }
}

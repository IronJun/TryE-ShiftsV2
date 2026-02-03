package com.ispw.tryeshifts.graphcontroller.javafx.utilities;

import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;

import java.util.List;
import java.util.Map;

public class WorkerCellFactory implements ShiftCellProvider{
    private final Map<String,Boolean> selectionMap;
    private final boolean isLocked;

    public WorkerCellFactory(Map<String,Boolean> selectionMap, boolean locked){
        this.isLocked = locked;
        this.selectionMap = selectionMap;
    }

    public VBox createCell(String cellKey, List<String> content, boolean isDayActive){
        VBox cell = new VBox(3);
        cell.setAlignment(Pos.TOP_CENTER);

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

        if (this.isLocked) {
            // Se la settimana è bloccata, mostriamo lo stato ma impediamo modifiche
            cell.setCursor(Cursor.DEFAULT);
            cell.setOpacity(0.8); // Feedback visivo: la cella è "congelata"

            // Se è bloccata, non aggiungiamo il listener setOnMouseClicked
            // Opzionalmente aggiungiamo un tooltip o un piccolo testo
            Label lockText = new Label("Sola Lettura");
            lockText.setStyle("-fx-font-size: 8px; -fx-text-fill: gray;");
            cell.getChildren().addAll(status, lockText);
        } else {
            // 4. LOGICA DI INTERAZIONE (OPEN)
            cell.setCursor(Cursor.HAND);
            cell.setOnMouseClicked(e -> {
                boolean newState = !selectionMap.getOrDefault(cellKey, false);
                selectionMap.put(cellKey, newState);
                status.setText(newState ? "Selezionato" : "Libero");
                applyStyle(cell, status, newState);
            });
            cell.getChildren().add(status);
        }

        return cell;
    }

    private void applyStyle(VBox cell, Label status, boolean sel) {
        cell.setStyle("-fx-border-color: #D1CFE2; -fx-padding: 5; -fx-background-color: " + (sel ? "#3498db;" : "white;"));
        status.setText(sel ? "Selezionato" : "Libero");
        status.setStyle("-fx-text-fill: " + (sel ? "white;" : "#cccccc;"));
    }
}

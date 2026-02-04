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
    private static final String SELECTED = "SELECTED";
    private static final String FREE = "Free";

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
        boolean isSelectedFromDB = content.stream().anyMatch(s -> s.equalsIgnoreCase(SELECTED));
        boolean isSelectedInMap = selectionMap.getOrDefault(cellKey, false);

        boolean finalSelectedState = isSelectedFromDB || isSelectedInMap;

        if(finalSelectedState) {
            selectionMap.put(cellKey, true);
        }

        // 3. CREA LA LABEL E APPLICA LO STILE
        Label statusLabel = new Label();
        applyStyle(cell, statusLabel, finalSelectedState);

        if (this.isLocked) {
            // Se la settimana è bloccata, mostriamo lo stato ma impediamo modifiche
            cell.setCursor(Cursor.DEFAULT);
            cell.setOpacity(0.8); // Feedback visivo: la cella è "congelata"

            // Se è bloccata, non aggiungiamo il listener setOnMouseClicked
            // Opzionalmente aggiungiamo un tooltip o un piccolo testo
            Label lockText = new Label("Sola Lettura");
            lockText.setStyle("-fx-font-size: 8px; -fx-text-fill: gray;");
            cell.getChildren().addAll(statusLabel, lockText);
        } else {
            // 4. LOGICA DI INTERAZIONE (OPEN)
            cell.setCursor(Cursor.HAND);
            cell.setOnMouseClicked(e -> {
                boolean newState = !selectionMap.getOrDefault(cellKey, false);
                selectionMap.put(cellKey, newState);
                statusLabel.setText(newState ? SELECTED : FREE);
                applyStyle(cell, statusLabel, newState);
            });
            cell.getChildren().add(statusLabel);
        }

        return cell;
    }

    private void applyStyle(VBox cell, Label status, boolean sel) {
        cell.setStyle("-fx-border-color: #D1CFE2; -fx-padding: 5; -fx-background-color: " + (sel ? "#3498db;" : "white;"));
        status.setText(sel ? SELECTED : FREE);
        status.setStyle("-fx-text-fill: " + (sel ? "white;" : "#cccccc;"));
    }
}

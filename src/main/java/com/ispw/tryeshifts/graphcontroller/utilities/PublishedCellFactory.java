package com.ispw.tryeshifts.graphcontroller.utilities;

import com.ispw.tryeshifts.appcontroller.PublishShiftsAC;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;

import java.util.List;
import java.util.Map;

public class PublishedCellFactory implements ShiftCellProvider{
    private final Map<String, String> finalAssignments; // Mappa: cellKey -> Nome Lavoratore

    // Il costruttore riceve già i dati pronti dall'AC
    public PublishedCellFactory(Map<String, String> assignments) {
        this.finalAssignments = assignments;
    }
    public VBox createCell(String cellKey, List<String> content, boolean active) {
        VBox cell = new VBox(3);
        cell.setAlignment(Pos.CENTER);

        if (!active) {
            // ... stile cella chiusa ...
            return cell;
        }

        // Cerchiamo chi è stato assegnato a questa chiave (es. "2026_04_Mon_08:00")
        String workerName = finalAssignments.getOrDefault(cellKey, "Libero");

        Label nameLabel = new Label(workerName);

        if ("Libero".equals(workerName)) {
            nameLabel.setStyle("-fx-text-fill: #95a5a6; -fx-font-style: italic;");
            cell.setStyle("-fx-border-color: #D1CFE2; -fx-background-color: #ffffff;");
        } else {
            nameLabel.setStyle("-fx-font-weight: bold; -fx-text-fill: #ffffff;");
            cell.setStyle("-fx-background-color: #2ecc71; -fx-border-color: #27ae60; -fx-padding: 5;");
        }

        cell.getChildren().add(nameLabel);
        return cell;
    }
}

package com.ispw.tryeshifts.graphcontroller.gui.utilities.factoryCalendar;

import com.ispw.tryeshifts.graphcontroller.gui.utilities.ColorManager;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;

import java.util.List;
import java.util.Map;

public class PublishedCellFactory implements ShiftCellProvider {
    private final Map<String, List<String>> finalAssignments; // Mappa: cellKey -> Nome Lavoratore

    // Il costruttore riceve già i dati pronti dall'AC
    public PublishedCellFactory(Map<String, List<String>> assignments) {
        this.finalAssignments = assignments;
    }
    public VBox createCell(String cellKey, List<String> content, boolean active) {

        // STAMPA COSA C'E' NELLA MAPPA (solo per la prima cella per non intasare il log)
        VBox cell = new VBox(3);
        cell.setAlignment(Pos.TOP_CENTER);
        cell.setStyle("-fx-padding: 5; -fx-border-color: #D1CFE2; -fx-background-color: #ffffff;");

        if (!active) {
            cell.setStyle("-fx-background-color: #f2f2f2; -fx-border-color: #D1CFE2;");
            return cell;
        }

        List<String> assignedWorkers = finalAssignments.get(cellKey);

        if(assignedWorkers == null || assignedWorkers.isEmpty()){
            Label nameLable = new Label("Libero");
            nameLable.setStyle("-fx-text-fill: #95a5a6; -fx-font-style: italic;");
            cell.getChildren().add(nameLable);
        }else{
            for(String workerName : assignedWorkers){
                Label nameLabel = new Label(workerName);
                String color = ColorManager.getColorForWorker(workerName);
                nameLabel.setStyle(
                        "-fx-background-color: " + color + ";" +
                                "-fx-text-fill: white;" +
                                "-fx-font-weight: bold;" +
                                "-fx-padding: 2 5 2 5;" +
                                "-fx-background-radius: 5;" +
                                "-fx-font-size: 10px;"
                );
                nameLabel.setMaxWidth(Double.MAX_VALUE); // Allunga il badge per uniformità
                nameLabel.setAlignment(Pos.CENTER);

                cell.getChildren().add(nameLabel);
            }
        }

        return cell;

    }
}

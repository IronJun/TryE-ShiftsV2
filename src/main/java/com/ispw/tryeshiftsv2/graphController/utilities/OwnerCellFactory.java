package com.ispw.tryeshiftsv2.graphController.utilities;

import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;

import java.util.List;

public class OwnerCellFactory implements ShiftCellProvider{
    public VBox createCell(String cellKey, List<String> candidates,boolean isDayActive){
        VBox cell = new VBox(3);
        cell.setAlignment(Pos.TOP_CENTER);

        if (!isDayActive) {
            // Applichiamo uno stile grigio scuro/neutro per indicare la chiusura
            cell.setStyle("-fx-border-color: #D1CFE2; -fx-padding: 5; -fx-background-color: #f2f2f2;");

            Label closedLabel = new Label("CHIUSO");
            closedLabel.setStyle("-fx-text-fill: #bdc3c7; -fx-font-size: 10px; -fx-font-weight: bold;");
            cell.getChildren().add(closedLabel);

            return cell; // Usciamo subito: non ci interessa mostrare candidati se il giorno è chiuso
        }

        cell.setStyle("-fx-border-color: #D1CFE2; -fx-padding: 5; -fx-background-color: white;");

        if(candidates.isEmpty()){
            Label empty = new Label("-");
            empty.setStyle("-fx-text-fill: #cccccc;");
            cell.getChildren().add(empty);
        }else{
            for(String email : candidates){
                // ESTRAIAMO IL NOME (es. da asd1@mail.com a asd1)
                String name = email.contains("@") ? email.split("@")[0] : email;

                Label nameTag = new Label(name);

                // RENDIAMO IL TAG VISIBILE (Sfondo colorato per distinguerlo)
                nameTag.setStyle(
                        "-fx-background-color: #3498db; " +
                                "-fx-text-fill: white; " +
                                "-fx-padding: 2 5 2 5; " +
                                "-fx-background-radius: 3; " +
                                "-fx-font-size: 10px;"
                );
                nameTag.setMaxWidth(Double.MAX_VALUE);

                // IMPORTANTE: Aggiungi la label alla cella!
                cell.getChildren().add(nameTag);
            }
        }
        System.out.println("DEBUG BOSS: Sto processando cella " + cellKey + " - Candidati: " + candidates.size());
        return cell;

    }
}

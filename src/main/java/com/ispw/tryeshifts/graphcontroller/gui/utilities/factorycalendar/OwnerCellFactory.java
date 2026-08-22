package com.ispw.tryeshifts.graphcontroller.gui.utilities.factorycalendar;

import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

import java.util.List;
import java.util.function.BiConsumer;

import static javafx.scene.layout.Priority.ALWAYS;

public class OwnerCellFactory implements ShiftCellProvider {
    private final boolean isLocked;
    private final BiConsumer<String, String> onRemoveWorker;

    public OwnerCellFactory(boolean isLocked, BiConsumer<String,String> onRemoveWorker) {
        this.isLocked = isLocked;
        this.onRemoveWorker = onRemoveWorker;
    }
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
            for(String email : candidates) {
                // ESTRAIAMO IL NOME (es. da asd1@mail.com a asd1)
                String name = email.contains("@") ? email.split("@")[0] : email;

                Label nameTag = new Label(name);
                // Diamo alla label solo il colore del testo, lo sfondo lo daremo al contenitore
                nameTag.setStyle("-fx-text-fill: white; -fx-font-size: 10px;");

                if (this.isLocked) {
                    HBox hbox = new HBox();
                    hbox.setAlignment(Pos.CENTER_LEFT);
                    hbox.setMaxWidth(Double.MAX_VALUE); // L'HBox occupa tutta la larghezza della cella

                    // SPOSTIAMO LO SFONDO BLU SULL'INTERO HBOX
                    hbox.setStyle(
                            "-fx-background-color: #3498db; " +
                                    "-fx-padding: 2 5 2 5; " +
                                    "-fx-background-radius: 3;"
                    );

                    // La molla trasparente per spingere la X a destra
                    javafx.scene.layout.Region spacer = new javafx.scene.layout.Region();
                    javafx.scene.layout.HBox.setHgrow(spacer, javafx.scene.layout.Priority.ALWAYS);

                    // Il bottone X (ho usato un rosso leggermente più chiaro per renderlo leggibile sul blu)
                    Button btnX = new Button("X");
                    btnX.setStyle("-fx-background-color: transparent; -fx-text-fill: #ff4d4d; -fx-font-weight: bold; -fx-cursor: hand; -fx-padding: 0;");
                    btnX.setOnAction(e -> onRemoveWorker.accept(cellKey, email));

                    hbox.getChildren().addAll(nameTag, spacer, btnX);
                    cell.getChildren().add(hbox);
                } else {
                    // Se la settimana NON è bloccata (non c'è la X), rimettiamo lo sfondo direttamente sulla Label
                    nameTag.setStyle(
                            "-fx-background-color: #3498db; " +
                                    "-fx-text-fill: white; " +
                                    "-fx-padding: 2 5 2 5; " +
                                    "-fx-background-radius: 3; " +
                                    "-fx-font-size: 10px;"
                    );
                    nameTag.setMaxWidth(Double.MAX_VALUE);
                    cell.getChildren().add(nameTag);
                }
            }
        }
        return cell;
    }

}

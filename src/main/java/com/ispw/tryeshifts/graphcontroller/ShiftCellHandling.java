package com.ispw.tryeshifts.graphcontroller;

import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;

public class ShiftCellHandling extends ListCell<String> {
    private final HBox container = new HBox();
    private final Label timeLabel = new Label();
    private final Button btnDelete = new Button("X");
    private final Pane spacer = new Pane();
    public ShiftCellHandling(){
        // Configuriamo il layout una volta sola nel costruttore
        container.setSpacing(10);
        HBox.setHgrow(spacer, Priority.ALWAYS);
        btnDelete.setStyle("-fx-background-color: #ff4d4d; -fx-text-fill: white; -fx-cursor: hand;");
        container.setAlignment(Pos.CENTER_RIGHT);

        // Azione del tasto X: rimuove l'elemento dalla ListView
        btnDelete.setOnAction(e -> {
            if (getListView() != null && getItem() != null) {
                getListView().getItems().remove(getItem());
            }
        });

        container.getChildren().addAll(timeLabel, spacer, btnDelete);
    }
    protected void updateItem(String item, boolean empty) {
        super.updateItem(item, empty);
        if (empty || item == null) {
            setGraphic(null);
        } else {
            timeLabel.setText(item);
            setGraphic(container);
        }
    }
}

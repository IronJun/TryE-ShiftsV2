package com.ispw.tryeshiftsv2;


import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.stage.Stage;
import java.io.IOException;
import java.util.logging.Logger;


// Singleton initialization
public class SceneManager {
    private static final Logger LOGGER = Logger.getLogger(AppConfig.class.getName());


    private Stage primaryStage;

    //Costrutture
    private SceneManager() {
        LOGGER.info("SceneManager initialized");
    }

    private static class Holder{
        private static final SceneManager INSTANCE = new SceneManager();
    }
    // singleton method
    public static SceneManager getInstance() {
        return Holder.INSTANCE;
    }

    // Metodo per inizializzare lo Stage principale
    public void setPrimaryStage(Stage stage) {
        this.primaryStage = stage;
    }

    // Metodo per ottenere lo Stage (utile per chiudere l'applicazione o Stage multipli)
    public Stage getPrimaryStage() {
        return primaryStage;
    }

    // Metodo principale per caricare un FXML e cambiare la scena
    /**
     * Carica il file FXML specificato e imposta la radice come nuova scena
     * sullo Stage principale.
     *
     * @param fxmlFileName Il nome del file FXML (es. "signup-view.fxml")
     * @param title        Il titolo della finestra
     * @param width        Larghezza della scena (opzionale)
     * @param height       Altezza della scena (opzionale)
     */
    public void switchScene(String fxmlFileName, String title, double width, double height) {
        if (primaryStage == null) {
            throw new IllegalStateException("Primary Stage non è stato inizializzato. Chiamare setPrimaryStage() prima.");
        }

        try {
            // Ottiene la risorsa FXML
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/com/ispw/tryeshiftsv2/view/" + fxmlFileName)); // Adatta il percorso!
            Parent root = fxmlLoader.load();

            // Crea o riutilizza la scena
            Scene scene = primaryStage.getScene();
            if (scene == null) {
                scene = new Scene(root, width, height);
                primaryStage.setScene(scene);
            } else {
                scene.setRoot(root);
                // Opzionale: se le dimensioni cambiano, puoi fare:

            }
            root.setFocusTraversable(true);
            Platform.runLater(root::requestFocus);
            primaryStage.setMinWidth(1200);
            primaryStage.setMinHeight(700);
            primaryStage.setTitle(title);
            primaryStage.show();

            // Ritorna il Controller associato, che potrebbe essere utile per passare dati
            fxmlLoader.getController();

        } catch (IOException e) {
            LOGGER.info("Errore nel caricamento della finestra: " + fxmlFileName);
            e.printStackTrace();
        }
    }

    // Aggiungi questo metodo nella classe SceneManager
    public Object showModalDialog(String fxmlFileName, String title) {
        try {
            // Caricamento FXML (usa il tuo percorso standard)
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/com/ispw/tryeshiftsv2/view/" + fxmlFileName));
            Parent root = fxmlLoader.load();

            // CREIAMO UN NUOVO STAGE (NON il primaryStage)
            Stage dialogStage = new Stage();
            dialogStage.setTitle(title);

            // Impostiamo la modalità "Modal": blocca la finestra principale finchè non viene chiusa
            dialogStage.initModality(javafx.stage.Modality.APPLICATION_MODAL);
            dialogStage.initOwner(primaryStage); // La Home rimane sotto

            Scene scene = new Scene(root);
            dialogStage.setScene(scene);

            Object controller = fxmlLoader.getController();
            // Mostra e aspetta (blocca l'esecuzione del codice chiamante finchè non si chiude il popup)
            dialogStage.show();

            return controller;

        } catch (IOException e) {
            LOGGER.info("Errore nel caricamento della finestra: " + fxmlFileName);
            e.printStackTrace();
            return null;
        }
    }

    public void showInfoAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    public void showErrorAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

}

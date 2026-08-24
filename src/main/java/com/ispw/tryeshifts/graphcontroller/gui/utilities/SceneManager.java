package com.ispw.tryeshifts.graphcontroller.gui.utilities;


import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.stage.Stage;
import java.io.IOException;
import java.util.Optional;
import java.util.logging.Logger;


// Singleton initialization
public class SceneManager {
    private static final Logger LOGGER = Logger.getLogger(SceneManager.class.getName());

    private static SceneManager instance;
    private Stage primaryStage;


    //Costrutture
    private SceneManager() {
    }


    // singleton method
    public static SceneManager getInstance() {

        if(instance == null) {
            instance = new SceneManager();
        }
        return instance;
    }

    // Metodo per inizializzare lo Stage principale
    public void setPrimaryStage(Stage stage) {
        if(this.primaryStage == null){
            this.primaryStage = stage;
        }else{
            LOGGER.warning("Tentativo di sovrascrizione dello stage principale.");
        }
    }

    public void switchScene(String fxmlFileName, String title, double width, double height) {
        if (primaryStage == null) {
            throw new IllegalStateException("Primary Stage non è stato inizializzato. Chiamare setPrimaryStage() prima.");
        }

        try {
            // Ottiene la risorsa FXML
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/com/ispw/tryeshifts/view/" + fxmlFileName)); // Adatta il percorso!
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
            LOGGER.info("Errore nel caricamento della finestra: " + fxmlFileName+" "+e.getMessage());

        }
    }

    // Method for the modal Scene just for the newWorkplace in this case
    public Object showModalDialog(String fxmlFileName, String title) {
        try {
            // Caricamento FXML (usa il tuo percorso standard)
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/com/ispw/tryeshifts/view/" + fxmlFileName));
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
            LOGGER.info("Errore nel caricamento della finestra: " + fxmlFileName+" "+e.getMessage());
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

    public Boolean showConfirmationAlert(String title, String content, String message) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle(title);
        alert.setHeaderText(content);
        alert.setContentText(message);
        Optional<ButtonType> result = alert.showAndWait();
        return result.isPresent()&&result.get() == ButtonType.OK;
    }

    public boolean logoutConfirmation(){
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Logout");
        alert.setHeaderText(null); // Rimuove lo spazio grigio dell'header che può sembrare "vuoto"
        alert.setContentText("Sei sicuro di voler effettuare il logout?");

        Optional<ButtonType> result = alert.showAndWait();
        return result.isPresent() && result.get() == ButtonType.OK;
    }

}

module com.ispw.tryeshiftsv2 {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.web;

    requires org.controlsfx.controls;
    requires com.dlsc.formsfx;
    requires net.synedra.validatorfx;
    requires org.kordamp.ikonli.javafx;
    requires org.kordamp.bootstrapfx.core;
    requires eu.hansolo.tilesfx;
    requires com.almasb.fxgl.all;
    requires java.desktop;

    opens com.ispw.tryeshiftsv2 to javafx.fxml;
    opens com.ispw.tryeshiftsv2.view  to javafx.fxml;
    opens com.ispw.tryeshiftsv2.graphController to javafx.fxml;
    exports com.ispw.tryeshiftsv2;

}
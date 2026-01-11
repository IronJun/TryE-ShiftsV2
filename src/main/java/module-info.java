module com.ispw.tryeshifts {
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
    requires java.logging;

    opens com.ispw.tryeshifts to javafx.fxml;
    opens com.ispw.tryeshifts.view  to javafx.fxml;
    opens com.ispw.tryeshifts.graphController to javafx.fxml;
    opens com.ispw.tryeshifts.bean to javafx.base;

    exports com.ispw.tryeshifts;
    exports com.ispw.tryeshifts.graphController;
    exports com.ispw.tryeshifts.appController;
    exports com.ispw.tryeshifts.entity;
    exports com.ispw.tryeshifts.excpetion;
    exports com.ispw.tryeshifts.bean;
    exports com.ispw.tryeshifts.dao;

}
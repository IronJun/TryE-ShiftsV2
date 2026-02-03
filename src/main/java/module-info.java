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
    requires java.sql;
    requires mysql.connector.j;
    requires java.xml.crypto;
    requires com.fasterxml.jackson.databind;
    requires java.prefs;

    opens com.ispw.tryeshifts to javafx.fxml;
    opens com.ispw.tryeshifts.view  to javafx.fxml;
    opens com.ispw.tryeshifts.bean to javafx.base;

    exports com.ispw.tryeshifts;
    exports com.ispw.tryeshifts.graphcontroller.javafx;
    exports com.ispw.tryeshifts.appcontroller;
    exports com.ispw.tryeshifts.entity;
    exports com.ispw.tryeshifts.excpetion;
    exports com.ispw.tryeshifts.bean;
    exports com.ispw.tryeshifts.dao;
    opens com.ispw.tryeshifts.graphcontroller.javafx to javafx.fxml;

}
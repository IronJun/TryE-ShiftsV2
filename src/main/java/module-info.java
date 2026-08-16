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
    exports com.ispw.tryeshifts.graphcontroller.gui;
    exports com.ispw.tryeshifts.appcontroller;
    exports com.ispw.tryeshifts.entity;
    exports com.ispw.tryeshifts.excpetion;
    exports com.ispw.tryeshifts.bean;
    exports com.ispw.tryeshifts.dao;
    opens com.ispw.tryeshifts.graphcontroller.gui to javafx.fxml;
    exports com.ispw.tryeshifts.session;
    opens com.ispw.tryeshifts.session to javafx.base;
    exports com.ispw.tryeshifts.config;
    opens com.ispw.tryeshifts.config to javafx.fxml;
    exports com.ispw.tryeshifts.graphcontroller.gui.utilities;
    opens com.ispw.tryeshifts.graphcontroller.gui.utilities to javafx.fxml;
    exports com.ispw.tryeshifts.dao.demo;
    exports com.ispw.tryeshifts.dao.jdbc;
    exports com.ispw.tryeshifts.graphcontroller.gui.utilities.stratgui;
    opens com.ispw.tryeshifts.graphcontroller.gui.utilities.stratgui to javafx.fxml;
    exports com.ispw.tryeshifts.graphcontroller.gui.utilities.factorycalendar;
    opens com.ispw.tryeshifts.graphcontroller.gui.utilities.factorycalendar to javafx.fxml;

}
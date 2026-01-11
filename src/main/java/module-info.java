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
    requires java.logging;

    opens com.ispw.tryeshiftsv2 to javafx.fxml;
    opens com.ispw.tryeshiftsv2.view  to javafx.fxml;
    opens com.ispw.tryeshiftsv2.graphController to javafx.fxml;
    opens com.ispw.tryeshiftsv2.bean to javafx.base;

    exports com.ispw.tryeshiftsv2;
    exports com.ispw.tryeshiftsv2.graphController;
    exports com.ispw.tryeshiftsv2.appController;
    exports com.ispw.tryeshiftsv2.entity;
    exports com.ispw.tryeshiftsv2.excpetion;
    exports com.ispw.tryeshiftsv2.bean;
    exports com.ispw.tryeshiftsv2.dao;

}
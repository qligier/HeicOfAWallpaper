module ch.qligier.heicofawallpaper {
    requires java.desktop;
    requires java.logging;

    // JavaFX
    requires javafx.base;
    requires javafx.controls;
    requires javafx.fxml;
    requires org.controlsfx.controls;
    requires com.dlsc.formsfx;
    requires com.sandec.mdfx;
    requires com.dustinredmond.fxtrayicon;

    // JNA
    requires com.sun.jna;
    requires com.sun.jna.platform;

    // Other libraries
    requires dd.plist;
    requires com.github.mjeanroy.exiftool;
    requires org.checkerframework.checker.qual;
    requires com.google.gson;
    requires eventbus.java;

    exports ch.qligier.heicofawallpaper;
    exports ch.qligier.heicofawallpaper.configuration;
    exports ch.qligier.heicofawallpaper.exception;
    exports ch.qligier.heicofawallpaper.utils.heic;
    exports ch.qligier.heicofawallpaper.model;
    exports ch.qligier.heicofawallpaper.service;
    exports ch.qligier.heicofawallpaper.utils.win32;
    opens ch.qligier.heicofawallpaper.gui to javafx.fxml;
    opens ch.qligier.heicofawallpaper.gui.main to javafx.fxml, eventbus.java;
    opens ch.qligier.heicofawallpaper.model to com.google.gson;
}

module ch.qligier.heicofawallpaper {
    requires java.desktop;
    requires java.logging;

    // JavaFX
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

    exports ch.qligier.heicofawallpaper;
    exports ch.qligier.heicofawallpaper.heic;
    opens ch.qligier.heicofawallpaper.gui to javafx.fxml;
}

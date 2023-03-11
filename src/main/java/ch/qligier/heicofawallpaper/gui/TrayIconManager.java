package ch.qligier.heicofawallpaper.gui;

import ch.qligier.heicofawallpaper.HoawApplication;
import com.dustinredmond.fxtrayicon.FXTrayIcon;
import javafx.stage.Stage;

import java.util.Objects;

/**
 * The tray icon manager.
 *
 * @author Quentin Ligier
 **/
public class TrayIconManager {

    /**
     * The JavaFX tray icon.
     */
    private final FXTrayIcon icon;

    /**
     * Constructor.
     *
     * @param stage The JavaFX stage.
     * @param app   The main app.
     */
    public TrayIconManager(final Stage stage,
                           final HoawApplication app) {
        Objects.requireNonNull(stage, "stage shall not be null in TrayIcon()");
        Objects.requireNonNull(app, "app shall not be null in TrayIcon()");
        this.icon = new FXTrayIcon.Builder(stage, TrayIconManager.class.getResource("/icon/logo_color_128.png"))
            .menuItem("Settings…", e -> app.showWindow())
            .addExitMenuItem()
            .show()
            .build();
    }
}

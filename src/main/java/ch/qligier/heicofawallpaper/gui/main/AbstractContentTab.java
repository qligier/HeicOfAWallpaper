package ch.qligier.heicofawallpaper.gui.main;

import ch.qligier.heicofawallpaper.HoawApplication;
import javafx.scene.layout.AnchorPane;

/**
 * HeicOfAWallpaper
 *
 * @author Quentin Ligier
 **/
public abstract class AbstractContentTab extends AnchorPane {

    protected final HoawApplication app;
    protected final MainWindow window;

    public AbstractContentTab(final HoawApplication app,
                              final MainWindow window) {
        super();
        this.app = app;
        this.window = window;
    }

    public HoawApplication getApp() {
        return this.app;
    }

    public void close() {
    }
}

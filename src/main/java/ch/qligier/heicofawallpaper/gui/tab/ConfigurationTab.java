package ch.qligier.heicofawallpaper.gui.tab;

import ch.qligier.heicofawallpaper.HoawApplication;
import ch.qligier.heicofawallpaper.Utils;
import ch.qligier.heicofawallpaper.gui.MainWindow;
import javafx.fxml.FXMLLoader;

/**
 * The 'configuration' tab controller.
 *
 * @author Quentin Ligier
 **/
public class ConfigurationTab extends AbstractContentTab {

    public ConfigurationTab(final HoawApplication app,
                            final MainWindow window) {
        super(app, window);
        final FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/configuration.fxml"));
        loader.setController(this);
        loader.setRoot(this);
        try {
            loader.load();
        } catch (final Exception exception) {
            Utils.showException(exception);
            return;
        }
    }
}

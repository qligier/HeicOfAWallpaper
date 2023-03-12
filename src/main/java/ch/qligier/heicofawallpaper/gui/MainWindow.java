package ch.qligier.heicofawallpaper.gui;

import ch.qligier.heicofawallpaper.HoawApplication;
import ch.qligier.heicofawallpaper.gui.tab.*;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import org.checkerframework.checker.nullness.qual.MonotonicNonNull;

import java.util.function.BiFunction;

/**
 * The JavaFX controller of the main window.
 *
 * @author Quentin Ligier
 **/
public class MainWindow extends AnchorPane {

    private final HoawApplication app;
    /**
     * JavaFX markup.
     */
    @FXML
    protected BorderPane contentPane;
    @FXML
    protected VBox sidebar;
    @FXML
    protected HBox navWallpapers;
    @FXML
    protected HBox navScreens;
    @FXML
    protected HBox navConfiguration;
    @FXML
    protected HBox navAbout;
    /**
     * The page that is currently open in the application.
     */
    @MonotonicNonNull
    private Page openPage;

    /**
     * Creates an AnchorPane layout.
     */
    public MainWindow(final HoawApplication app) {
        this.app = app;
        final FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/main.fxml"));
        loader.setController(this);
        loader.setRoot(this);
        try {
            loader.load();
        } catch (final Exception e) {
            e.printStackTrace();
        }

        this.openPage(Page.WALLPAPERS);
    }

    @FXML
    protected void openPageWallpapers() {
        this.openPage(Page.WALLPAPERS);
    }

    @FXML
    protected void openPageScreens() {
        this.openPage(Page.SCREENS);
    }

    @FXML
    protected void openPageConfiguration() {
        this.openPage(Page.CONFIGURATION);
    }

    @FXML
    protected void openPageAbout() {
        this.openPage(Page.ABOUT);
    }

    protected synchronized void openPage(final Page page) {
        if (page == this.openPage) {
            return;
        }
        this.openPage = page;
        this.contentPane.setCenter(this.openPage.getSupplier().apply(this.app, this));

        // Update the menu style
        for (final Node node : this.sidebar.getChildrenUnmodifiable()) {
            if (node instanceof final HBox hBox) {
                if (page.getId().equals(hBox.getId())) {
                    hBox.getStyleClass().add("active");
                } else {
                    hBox.getStyleClass().remove("active");
                }
            }
        }
    }

    /**
     * The enumeration of pages.
     */
    protected enum Page {
        WALLPAPERS("navWallpapers", WallpapersTab::new),
        SCREENS("navScreens", ScreensTab::new),
        CONFIGURATION("navConfiguration", ConfigurationTab::new),
        ABOUT("navAbout", AboutTab::new);

        private final String id;
        private final BiFunction<HoawApplication, MainWindow, AbstractContentTab> supplier;

        Page(final String id,
             final BiFunction<HoawApplication, MainWindow, AbstractContentTab> supplier) {
            this.id = id;
            this.supplier = supplier;
        }

        public String getId() {
            return this.id;
        }

        public BiFunction<HoawApplication, MainWindow, AbstractContentTab> getSupplier() {
            return this.supplier;
        }
    }
}

package ch.qligier.heicofawallpaper.gui.tab;

import ch.qligier.heicofawallpaper.HoawApplication;
import ch.qligier.heicofawallpaper.Utils;
import ch.qligier.heicofawallpaper.configuration.RuntimeConfiguration;
import ch.qligier.heicofawallpaper.gui.MainWindow;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.ChoiceBox;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import org.checkerframework.checker.nullness.qual.MonotonicNonNull;

import java.util.Set;

/**
 * The 'screens' tab controller.
 *
 * @author Quentin Ligier
 **/
public class ScreensTab extends AbstractContentTab {

    private static final String NO_CHOICE = "None";

    @FXML
    @MonotonicNonNull
    protected VBox screenList;

    public ScreensTab(final HoawApplication app,
                      final MainWindow window) {
        super(app, window);
        final FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/screens.fxml"));
        loader.setController(this);
        loader.setRoot(this);
        try {
            loader.load();
        } catch (final Exception exception) {
            Utils.showException(exception);
        }

        this.onListRefresh();
    }

    @FXML
    protected void onListRefresh() {
        final Set<String> heicFiles = this.app.getWallpapersInFolder().keySet();

        this.screenList.getChildren().clear();
        for (final RuntimeConfiguration.Monitor monitor : this.app.getRuntimeConfiguration().monitors()) {
            final HBox hBox = new HBox();

            final VBox vBox = new VBox();
            vBox.getChildren().add(new Text(monitor.deviceName()));
            vBox.getChildren().add(new Text(monitor.width() + "x" + monitor.height()));
            hBox.getChildren().add(vBox);

            final ChoiceBox<String> choiceBox = new ChoiceBox<>();
            choiceBox.getItems().add(NO_CHOICE);
            choiceBox.setValue(NO_CHOICE);
            choiceBox.getItems().addAll(heicFiles);
            hBox.getChildren().add(choiceBox);
            choiceBox.setOnAction(event -> {
                System.out.println("Got choiceBox event");
                if (!NO_CHOICE.equals(choiceBox.getValue())) {
                    this.app.setWallpaperChoice(monitor.devicePath(), choiceBox.getValue());
                } else {
                    this.app.getUserConfiguration().getWallpaperChoices().remove(monitor.devicePath());
                }
            });

            this.screenList.getChildren().add(hBox);
        }
    }
}

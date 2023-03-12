package ch.qligier.heicofawallpaper.gui.tab;

import ch.qligier.heicofawallpaper.HoawApplication;
import ch.qligier.heicofawallpaper.Utils;
import ch.qligier.heicofawallpaper.gui.MainWindow;
import ch.qligier.heicofawallpaper.model.AppearanceDynamicWallpaper;
import ch.qligier.heicofawallpaper.model.DynamicWallpaperInterface;
import ch.qligier.heicofawallpaper.model.SolarDynamicWallpaper;
import ch.qligier.heicofawallpaper.model.TimeDynamicWallpaper;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.text.Text;
import javafx.stage.DirectoryChooser;
import org.checkerframework.checker.nullness.qual.MonotonicNonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.io.File;
import java.io.InputStream;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;

/**
 * The 'wallpapers' tab controller.
 *
 * @author Quentin Ligier
 **/
public class WallpapersTab extends AbstractContentTab {

    @FXML
    @MonotonicNonNull
    protected TextField directoryInput;

    @FXML
    @MonotonicNonNull
    protected Button selectButton;

    @FXML
    @MonotonicNonNull
    protected Button refreshButton;

    @FXML
    @MonotonicNonNull
    protected Text title;

    @FXML
    @MonotonicNonNull
    protected ChoiceBox<TypeFilter> typeChooser;

    @FXML
    @MonotonicNonNull
    protected ListView<Map.Entry<String, DynamicWallpaperInterface>> wallpaperList;

    protected TypeFilter typeFilter = TypeFilter.ALL;

    public WallpapersTab(final HoawApplication app,
                         final MainWindow window) {
        super(app, window);
        final FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/wallpapers.fxml"));
        loader.setController(this);
        loader.setRoot(this);
        try {
            loader.load();
        } catch (final Exception exception) {
            Utils.showException(exception);
            return;
        }

        this.typeChooser.getItems().addAll(TypeFilter.values());
        this.typeChooser.setValue(TypeFilter.ALL);
        this.typeChooser.setOnAction(event -> {
            this.typeFilter = this.typeChooser.getSelectionModel().getSelectedItem();
            this.onListRefresh();
        });
        this.wallpaperList.setMouseTransparent(true);
        this.wallpaperList.setFocusTraversable(false);
        this.wallpaperList.setCellFactory(list -> new WallpaperCell());
        this.directoryInput.setText(this.app.getUserConfiguration().getWallpaperFolderPath());
        this.onListRefresh();
    }

    @FXML
    protected void onSelect() {
        final DirectoryChooser chooser = new DirectoryChooser();
        chooser.setInitialDirectory(new File(this.app.getUserConfiguration().getWallpaperFolderPath()));
        chooser.setTitle("Select the wallpapers directory");
        final File selectedDirectory = chooser.showDialog(this.window.getScene().getWindow());
        if (selectedDirectory != null) {
            this.app.setWallpaperFolderPath(selectedDirectory.toString());
            this.directoryInput.setText(selectedDirectory.toString());
            this.onListRefresh();
        }
    }

    @FXML
    protected void onListRefresh() {
        final var allWallpapers = this.app.getWallpapersInFolder();
        final var filteredWallpapers = allWallpapers
            .entrySet()
            .stream()
            .filter(entry -> this.typeFilter.getFilter().apply(entry.getValue()))
            .toList();
        this.wallpaperList.setItems(FXCollections.observableList(filteredWallpapers));

        if (allWallpapers.size() == filteredWallpapers.size()) {
            this.title.setText(allWallpapers.size() + " wallpapers");
        } else {
            this.title.setText(filteredWallpapers.size() + " wallpapers (out of " + allWallpapers.size() + ")");
        }
    }

    /**
     * The enumeration of types that can be filtered.
     */
    private enum TypeFilter {
        ALL("All", wallpaper -> true),
        SOLAR("Solar", SolarDynamicWallpaper.class::isInstance),
        TIME("Time", TimeDynamicWallpaper.class::isInstance),
        APPEARANCE("Appearance", AppearanceDynamicWallpaper.class::isInstance);

        private final String display;
        private final Function<DynamicWallpaperInterface, Boolean> filter;

        TypeFilter(final String display,
                   final Function<DynamicWallpaperInterface, Boolean> filter) {
            this.display = display;
            this.filter = filter;
        }

        public String getDisplay() {
            return this.display;
        }

        public Function<DynamicWallpaperInterface, Boolean> getFilter() {
            return this.filter;
        }

        @Override
        public String toString() {
            return this.display;
        }
    }

    private static class WallpaperCell extends ListCell<Map.Entry<String, DynamicWallpaperInterface>> {
        @Override
        protected void updateItem(final Map.@Nullable Entry<String, DynamicWallpaperInterface> entry,
                                  final boolean empty) {
            super.updateItem(entry, empty);
            if (empty || entry == null) {
                setGraphic(null);
                return;
            }
            final HBox hBox = new HBox();

            final String iconName = switch (entry.getValue().type()) {
                case APPEARANCE -> "brush.png";
                case TIME -> "clock.png";
                case SOLAR -> "sun.png";
            };
            final InputStream is = WallpapersTab.class.getResourceAsStream("/icon/" + iconName);
            final ImageView imageView = new ImageView(new Image(Objects.requireNonNull(is)));
            imageView.setFitHeight(20);
            imageView.setFitWidth(20);
            hBox.getChildren().add(imageView);

            hBox.getChildren().add(new Text(entry.getKey()));

            setGraphic(hBox);
        }
    }
}

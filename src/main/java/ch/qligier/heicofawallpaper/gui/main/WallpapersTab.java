package ch.qligier.heicofawallpaper.gui.main;

import ch.qligier.heicofawallpaper.HoawApplication;
import ch.qligier.heicofawallpaper.gui.javafx.MiniatureImageView;
import ch.qligier.heicofawallpaper.gui.javafx.SvgUtils;
import ch.qligier.heicofawallpaper.gui.wallpaper_detail.WallpaperDetailWindow;
import ch.qligier.heicofawallpaper.model.DynWallDefinition;
import ch.qligier.heicofawallpaper.model.events.WallpaperDefinitionsChanged;
import ch.qligier.heicofawallpaper.service.FileSystemService;
import ch.qligier.heicofawallpaper.utils.Utils;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.text.Text;
import javafx.stage.DirectoryChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import org.checkerframework.checker.nullness.qual.MonotonicNonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.io.File;
import java.nio.file.Path;
import java.util.function.Function;
import java.util.logging.Logger;

/**
 * The 'wallpapers' tab controller.
 *
 * @author Quentin Ligier
 **/
@SuppressWarnings("java:S110")
public class WallpapersTab extends AbstractContentTab {
    private static final Logger LOG = Logger.getLogger("WallpapersTab");
    private final Image searchIcon;
    /**
     * The input field to select the wallpaper directory. Read-only display.
     */
    @FXML
    @MonotonicNonNull
    protected TextField directoryInput;
    /**
     * The button to open the wallpaper directory selection dialog.
     */
    @FXML
    @MonotonicNonNull
    protected Button selectButton;
    /**
     * The button to refresh the wallpaper definitions in the directory.
     */
    @FXML
    @MonotonicNonNull
    protected Button refreshButton;
    /**
     * The list title.
     */
    @FXML
    @MonotonicNonNull
    protected Text title;
    /**
     * The select input to filter wallpapers by their type.
     */
    @FXML
    @MonotonicNonNull
    protected ChoiceBox<TypeFilter> typeChooser;
    /**
     * The GUI list of wallpaper definitions. See {@link WallpaperCell} for the rendering of each item.
     */
    @FXML
    @MonotonicNonNull
    protected ListView<DynWallDefinition> wallpaperList;
    /**
     * The type filter currently selected.
     */
    protected TypeFilter typeFilter = TypeFilter.ALL;

    public WallpapersTab(final HoawApplication app,
                         final MainWindow window) {
        super(app, window);
        final FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/main/wallpapers.fxml"));
        loader.setController(this);
        loader.setRoot(this);
        try {
            loader.load();
        } catch (final Exception exception) {
            Utils.showException(exception);
        }

        // Register this class for some events
        EventBus.getDefault().register(this);

        this.searchIcon = SvgUtils.loadFromResource("/icon/search.svg");

        // GUI configuration
        this.typeChooser.getItems().addAll(TypeFilter.values());
        this.typeChooser.setValue(TypeFilter.ALL);
        this.typeChooser.setOnAction(event -> {
            this.typeFilter = this.typeChooser.getSelectionModel().getSelectedItem();
            this.onListRefresh();
        });
        //this.wallpaperList.setMouseTransparent(true);
        this.wallpaperList.setFocusTraversable(false);
        this.wallpaperList.setCellFactory(list -> new WallpaperCell());
        this.wallpaperList.setOnMouseClicked(event -> {
            LOG.info("onMouseClicked");
            final var selectedItem = this.wallpaperList.getSelectionModel().getSelectedItem();
            if (selectedItem != null) {
                this.openWallpaperDetailWindow(selectedItem);
            }
        });
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
        final var allWallpapers = this.app.getWallpaperDefinitions();
        final var filteredWallpapers = allWallpapers
            .stream()
            .filter(entry -> this.typeFilter.getFilter().apply(entry))
            .toList();
        this.wallpaperList.setItems(FXCollections.observableList(filteredWallpapers));

        if (allWallpapers.size() == filteredWallpapers.size()) {
            this.title.setText(allWallpapers.size() + " wallpapers");
        } else {
            this.title.setText(filteredWallpapers.size() + " wallpapers (out of " + allWallpapers.size() + ")");
        }
    }

    @Subscribe(threadMode = ThreadMode.BACKGROUND)
    public void onMessageEvent(final WallpaperDefinitionsChanged event) {
        LOG.fine("onMessageEvent WallpaperDefinitionsChanged");
        Platform.runLater(this::onListRefresh);
    }

    @Override
    public void close() {
        EventBus.getDefault().unregister(this);
    }

    protected void openWallpaperDetailWindow(final DynWallDefinition definition) {
        LOG.info("Opening wallpaper detail window");
        final var stage = new Stage();
        stage.setTitle("Details of X");
        stage.initModality(Modality.NONE);
        LOG.info("Before new WallpaperDetailWindow");
        stage.setScene(new Scene(new WallpaperDetailWindow(this.app, definition)));
        LOG.info("After new WallpaperDetailWindow");
        stage.setResizable(false);
        stage.getIcons().add(Utils.getLogo());
        stage.show();
        LOG.info("Stage shown");
    }

    /**
     * The enumeration of types that can be filtered.
     */
    private enum TypeFilter {
        ALL("All", wallpaper -> true),
        SOLAR("Solar", DynWallDefinition::isSolar),
        TIME("Time", DynWallDefinition::isTime),
        APPEARANCE("Appearance", DynWallDefinition::isAppearance);

        private final String display;
        private final Function<DynWallDefinition, Boolean> filter;

        TypeFilter(final String display,
                   final Function<DynWallDefinition, Boolean> filter) {
            this.display = display;
            this.filter = filter;
        }

        public String getDisplay() {
            return this.display;
        }

        public Function<DynWallDefinition, Boolean> getFilter() {
            return this.filter;
        }

        @Override
        public String toString() {
            return this.display;
        }
    }

    private class WallpaperCell extends ListCell<DynWallDefinition> {
        @Override
        protected void updateItem(final @Nullable DynWallDefinition entry,
                                  final boolean empty) {
            super.updateItem(entry, empty);
            if (empty || entry == null) {
                setGraphic(null);
                return;
            }
            final HBox hBox = new HBox();
            final Path cachePath = FileSystemService.getDataPath().resolve(entry.fileHash());

            // Display the preview thumbnail
            final String previewUrl = cachePath.resolve(FileSystemService.PREVIEW_FILE_NAME).toString();
            hBox.getChildren().add(new MiniatureImageView(previewUrl, 128, 72));

            // Wallpaper type
            /*final String iconName = switch (entry.type()) {
                case APPEARANCE -> "brush.png";
                case TIME -> "clock.png";
                case SOLAR -> "sun.png";
            };
            final InputStream is = WallpapersTab.class.getResourceAsStream("/icon/" + iconName);
            final ImageView imageView = new ImageView(new Image(Objects.requireNonNull(is)));
            imageView.setFitHeight(20);
            imageView.setFitWidth(20);
            hBox.getChildren().add(imageView);*/

            // Wallpaper filename
            hBox.getChildren().add(new Text(entry.filename()));

            final var searchIconView = new ImageView(SvgUtils.loadFromResource("/icon/search.svg"));
            searchIconView.setFitHeight(32);
            searchIconView.setFitWidth(32);
            hBox.getChildren().add(searchIconView);

            setGraphic(hBox);
        }
    }
}

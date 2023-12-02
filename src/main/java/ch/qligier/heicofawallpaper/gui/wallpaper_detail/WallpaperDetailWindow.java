package ch.qligier.heicofawallpaper.gui.wallpaper_detail;

import ch.qligier.heicofawallpaper.HoawApplication;
import ch.qligier.heicofawallpaper.gui.javafx.MiniatureImageView;
import ch.qligier.heicofawallpaper.model.DynWallDefinition;
import ch.qligier.heicofawallpaper.service.FileSystemService;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Bounds;
import javafx.geometry.Pos;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Tab;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import org.checkerframework.checker.nullness.qual.MonotonicNonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.nio.file.Path;
import java.util.logging.Logger;

import static java.util.Objects.requireNonNull;

/**
 * The JavaFX controller of the wallpaper detail window.
 *
 * @author Quentin Ligier
 **/
public class WallpaperDetailWindow extends AnchorPane {
    private static final Logger LOG = Logger.getLogger("WallpaperDetailWindow");

    private static final String HIGHLIGHTED_CLASS = "highlighted";

    private final HoawApplication app;

    private final DynWallDefinition definition;

    @FXML
    @MonotonicNonNull
    protected ScrollPane frameScroll;

    @FXML
    @MonotonicNonNull
    protected VBox frameList;

    @FXML
    @MonotonicNonNull
    protected VBox solarList;

    @FXML
    @MonotonicNonNull
    protected VBox timeList;

    @FXML
    @MonotonicNonNull
    protected VBox appearanceList;

    @FXML
    @MonotonicNonNull
    protected Tab solarTab;

    @FXML
    @MonotonicNonNull
    protected Tab timeTab;

    @FXML
    @MonotonicNonNull
    protected Tab appearanceTab;

    @Nullable
    private Integer highlightedFrameIndex = null;

    public WallpaperDetailWindow(final HoawApplication app,
                                 final DynWallDefinition definition) {
        LOG.info("WallpaperDetailWindow.constructor");
        this.app = requireNonNull(app);
        this.definition = requireNonNull(definition);
        final FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/wallpaper_detail/window.fxml"));
        loader.setController(this);
        loader.setRoot(this);
        try {
            loader.load();
        } catch (final Exception e) {
            LOG.severe("Cannot load the wallpaper detail window");
            e.printStackTrace();
        }
        LOG.info("Loaded");

        // Prepare the lists
        this.frameList.setFocusTraversable(false);
        for (int i = 0; i < this.definition.numberOfFrames(); i++) {
            final String frameFilename = String.format("frame-%d.jpg", i);
            final Path framePath = FileSystemService.getDataPath()
                .resolve(this.definition.fileHash())
                .resolve(frameFilename);
            this.addFrame(i, framePath.toString());
        }
        LOG.info("Frame list prepared");

        // Prepare the phases lists
        if (definition.isSolar()) {
            for (final var phase : definition.solarPhases()) {
                this.addSolarPhase();
            }
            this.solarList.setOnMouseClicked(event -> {
                LOG.info("Clicked on solar phase");
            });
        } else {
            this.solarTab.setDisable(true);
        }
        LOG.info("Solar list prepared");

        if (definition.isTime()) {
            for (final var phase : definition.timePhases()) {
                this.addTimePhase(phase.frameIndex(), phase.time().toString());
            }
            this.timeList.setOnMouseClicked(event -> {
                LOG.info("Clicked on time phase");
            });
        } else {
            this.timeTab.setDisable(true);
        }
        LOG.info("Time list prepared");

        if (definition.isAppearance()) {
            this.addAppearancePhase(definition.appearancePhase().lightFrameIndex(), "Light");
            this.addAppearancePhase(definition.appearancePhase().darkFrameIndex(), "Dark");
            this.appearanceList.setOnMouseClicked(event -> {
                LOG.info("Clicked on appearance phase");
            });
        } else {
            this.appearanceTab.setDisable(true);
        }
        LOG.info("Appearance list prepared");
    }

    private void highlightFrame(final @Nullable Integer frameIndex) {
        if (this.highlightedFrameIndex != null) {
            this.frameList.getChildren().get(this.highlightedFrameIndex).getStyleClass().remove(HIGHLIGHTED_CLASS);
        }
        this.highlightedFrameIndex = frameIndex;
        if (frameIndex != null) {
            final var node = this.frameList.getChildren().get(frameIndex);
            final Bounds viewportBounds = this.frameScroll.getViewportBounds();
            final Bounds contentBounds = node.getBoundsInLocal();
            LOG.info("Viewport bounds: %s".formatted(viewportBounds));
            LOG.info("Content bounds: %s".formatted(contentBounds));
            node.getStyleClass().add(HIGHLIGHTED_CLASS);
            //V: position.getY() / (contentBounds.getHeight() - viewportBounds.getHeight())
        }
    }

    private void addFrame(final int frameIndex,
                          final String framePath) {
        final var hBox = new HBox();
        hBox.getStyleClass().add("frameHbox");
        hBox.setAlignment(Pos.CENTER_LEFT);

        // Show the frame
        hBox.getChildren().add(new MiniatureImageView(framePath, 128, 72));

        // Show the frame index
        final var text = new Text("Frame %d".formatted(frameIndex));
        text.getStyleClass().add("frameText");
        text.setStyle("-fx-text-fill: #008000; -fx-font-size: 16px;");
        hBox.getChildren().add(text);

        this.frameList.getChildren().add(hBox);
    }

    private void addSolarPhase() {

    }

    private void addTimePhase(final int frameIndex,
                              final String time) {
        final var text = new Text("Showing frame %d at %s".formatted(frameIndex, time));
        text.setOnMouseEntered(event -> this.highlightFrame(frameIndex));
        text.setOnMouseExited(event -> this.highlightFrame(null));
        this.timeList.getChildren().add(text);
    }

    private void addAppearancePhase(final int frameIndex,
                                    final String appearanceName) {
        final var text = new Text("%s: showing frame %d".formatted(appearanceName, frameIndex));
        text.setOnMouseEntered(event -> this.highlightFrame(frameIndex));
        text.setOnMouseExited(event -> this.highlightFrame(null));
        this.appearanceList.getChildren().add(text);
    }
}

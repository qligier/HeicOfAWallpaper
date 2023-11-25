package ch.qligier.heicofawallpaper.gui.wallpaper_detail;

import ch.qligier.heicofawallpaper.HoawApplication;
import ch.qligier.heicofawallpaper.model.DynWallDefinition;
import ch.qligier.heicofawallpaper.model.PhaseSolar;
import ch.qligier.heicofawallpaper.model.PhaseTime;
import ch.qligier.heicofawallpaper.service.FileSystemService;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.ListView;
import javafx.scene.control.Tab;
import javafx.scene.layout.AnchorPane;
import org.checkerframework.checker.nullness.qual.MonotonicNonNull;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import static java.util.Objects.requireNonNull;

/**
 * The JavaFX controller of the wallpaper detail window.
 *
 * @author Quentin Ligier
 **/
public class WallpaperDetailWindow extends AnchorPane {
    private static final Logger LOG = Logger.getLogger("WallpaperDetailWindow");

    private final HoawApplication app;

    private final DynWallDefinition definition;

    @FXML
    @MonotonicNonNull
    protected ListView<FrameCell.FrameInfo> frameList;

    @FXML
    @MonotonicNonNull
    protected ListView<PhaseSolar> solarList;

    @FXML
    @MonotonicNonNull
    protected ListView<PhaseTime> timeList;

    @FXML
    @MonotonicNonNull
    protected ListView<AppearancePhaseCell.AppearancePhaseInfo> appearanceList;

    @FXML
    @MonotonicNonNull
    protected Tab solarTab;

    @FXML
    @MonotonicNonNull
    protected Tab timeTab;

    @FXML
    @MonotonicNonNull
    protected Tab appearanceTab;

    public WallpaperDetailWindow(final HoawApplication app,
                                 final DynWallDefinition definition) {
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

        // Prepare the lists
        this.frameList.setFocusTraversable(false);
        this.frameList.setCellFactory(listView -> new FrameCell());
        this.frameList.setItems(FXCollections.observableList(this.getFrameInfos()));

        // Prepare the phases lists
        if (definition.isSolar()) {
            this.solarList.setFocusTraversable(false);
            this.solarList.setCellFactory(listView -> new SolarPhaseCell());
            this.solarList.setItems(FXCollections.observableList(requireNonNull(definition.solarPhases())));
            this.solarList.setOnMouseClicked(event -> {
                final var phase = this.solarList.getSelectionModel().getSelectedItem();
                if (phase != null) {
                    this.highlightFrame(phase.frameIndex());
                }
            });
        } else {
            this.solarTab.setDisable(true);
        }

        if (definition.isTime()) {
            this.timeList.setFocusTraversable(false);
            this.timeList.setCellFactory(listView -> new TimePhaseCell());
            this.timeList.setItems(FXCollections.observableList(requireNonNull(definition.timePhases())));
            this.timeList.setOnMouseClicked(event -> {
                final var phase = this.timeList.getSelectionModel().getSelectedItem();
                if (phase != null) {
                    this.highlightFrame(phase.frameIndex());
                }
            });
        } else {
            this.timeTab.setDisable(true);
        }

        if (definition.isAppearance()) {
            this.appearanceList.setFocusTraversable(false);
            this.appearanceList.setCellFactory(listView -> new AppearancePhaseCell());
            this.appearanceList.setItems(FXCollections.observableList(requireNonNull(this.getAppearancePhaseInfos())));
            this.appearanceList.setOnMouseClicked(event -> {
                final var phase = this.appearanceList.getSelectionModel().getSelectedItem();
                if (phase != null) {
                    this.highlightFrame(phase.frameIndex());
                }
            });
        } else {
            this.appearanceTab.setDisable(true);
        }
    }

    private List<FrameCell.FrameInfo> getFrameInfos() {
        final List<FrameCell.FrameInfo> list = new ArrayList<>(this.definition.numberOfFrames());
        for (int i = 0; i < this.definition.numberOfFrames(); i++) {
            final String frameFilename = String.format("frame-%d.jpg", i);
            final Path framePath = FileSystemService.getDataPath()
                .resolve(this.definition.fileHash())
                .resolve(frameFilename);
            list.add(new FrameCell.FrameInfo(i, framePath.toString()));
        }
        return list;
    }

    private List<AppearancePhaseCell.AppearancePhaseInfo> getAppearancePhaseInfos() {
        requireNonNull(this.definition.appearancePhase());
        return List.of(new AppearancePhaseCell.AppearancePhaseInfo("Light",
                                                                   this.definition.appearancePhase().lightFrameIndex()),
                       new AppearancePhaseCell.AppearancePhaseInfo("Dark",
                                                                   this.definition.appearancePhase().darkFrameIndex()));
    }

    private void highlightFrame(final int frameIndex) {
        this.frameList.getSelectionModel().select(frameIndex);
        this.frameList.scrollTo(frameIndex);
    }
}

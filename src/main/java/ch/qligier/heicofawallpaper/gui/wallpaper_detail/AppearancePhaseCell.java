package ch.qligier.heicofawallpaper.gui.wallpaper_detail;

import javafx.scene.control.ListCell;
import javafx.scene.text.Text;
import org.checkerframework.checker.nullness.qual.Nullable;

/**
 * HeicOfAWallpaper
 *
 * @author Quentin Ligier
 **/
public class AppearancePhaseCell extends ListCell<AppearancePhaseCell.AppearancePhaseInfo> {

    @Override
    protected void updateItem(final @Nullable AppearancePhaseInfo appearancePhaseInfo, final boolean empty) {
        super.updateItem(appearancePhaseInfo, empty);
        if (empty || appearancePhaseInfo == null) {
            setGraphic(null);
            return;
        }
        final var text = new Text("%s: showing frame %d".formatted(appearancePhaseInfo.appearanceName(),
                                                                   appearancePhaseInfo.frameIndex()));
        this.setGraphic(text);
    }

    public record AppearancePhaseInfo(String appearanceName,
                                      int frameIndex) {
    }
}

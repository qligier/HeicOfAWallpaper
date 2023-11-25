package ch.qligier.heicofawallpaper.gui.wallpaper_detail;

import ch.qligier.heicofawallpaper.model.PhaseTime;
import javafx.scene.control.ListCell;
import javafx.scene.text.Text;
import org.checkerframework.checker.nullness.qual.Nullable;

/**
 * HeicOfAWallpaper
 *
 * @author Quentin Ligier
 **/
public class TimePhaseCell extends ListCell<PhaseTime> {

    @Override
    protected void updateItem(final @Nullable PhaseTime phaseTime, final boolean empty) {
        super.updateItem(phaseTime, empty);
        if (empty || phaseTime == null) {
            setGraphic(null);
            return;
        }
        final var text = new Text("Showing frame %d at %s".formatted(phaseTime.frameIndex(), phaseTime.time()));
        this.setGraphic(text);
    }
}

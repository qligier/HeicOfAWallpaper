package ch.qligier.heicofawallpaper.service;

import ch.qligier.heicofawallpaper.model.CurrentEnvironment;
import ch.qligier.heicofawallpaper.model.PhaseAppearance;
import ch.qligier.heicofawallpaper.model.PhaseSolar;
import ch.qligier.heicofawallpaper.model.PhaseTime;

import java.util.List;
import java.util.Objects;
import java.util.Random;

/**
 * An evaluator of dynamic wallpaper phases with the current environment.
 *
 * @author Quentin Ligier
 **/
public class PhaseEvaluator {

    /**
     * The current environment.
     */
    private final CurrentEnvironment currentEnvironment;

    /**
     * Constructor.
     *
     * @param currentEnvironment The current environment.
     */
    public PhaseEvaluator(final CurrentEnvironment currentEnvironment) {
        this.currentEnvironment = Objects.requireNonNull(currentEnvironment);
    }

    public short evaluateAppearanceFrame(final PhaseAppearance appearancePhase) {
        return currentEnvironment.isLightThemeEnabled() ? appearancePhase.lightFrameIndex() :
            appearancePhase.darkFrameIndex();
    }

    public short evaluateSolarFrame(final List<PhaseSolar> solarPhases) {
        return solarPhases.get(new Random().nextInt(0, solarPhases.size())).frameIndex();
    }

    public short evaluateTimeFrame(final List<PhaseTime> timePhases) {
        // Try to find the earliest phase whose changing time is now or in the past
        Short currentFrame = timePhases.stream()
            .filter(phase -> !phase.time().isAfter(this.currentEnvironment.currentTime()))
            .findFirst()
            .map(PhaseTime::frameIndex)
            .orElse(null);
        if (currentFrame != null) {
            return currentFrame;
        }

        // If there is no phase before the current time, return the last phase of the day
        return timePhases.get(timePhases.size() - 1).frameIndex();
    }
}

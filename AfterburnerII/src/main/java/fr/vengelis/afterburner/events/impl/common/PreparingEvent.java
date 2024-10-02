package fr.vengelis.afterburner.events.impl.common;

import fr.vengelis.afterburner.events.AbstractEvent;
import fr.vengelis.afterburner.events.CancellableEvent;

import java.util.ArrayList;
import java.util.List;

public class PreparingEvent extends AbstractEvent implements CancellableEvent {

    private List<SlavePreparingStep> skipStep = new ArrayList<>();
    private final Stage stage;
    private boolean cancelled = false;

    public PreparingEvent(Stage stage) {
        this.stage = stage;
    }

    public List<SlavePreparingStep> getSkipStep() {
        return skipStep;
    }

    public void setSkipStep(List<SlavePreparingStep> skipStep) {
        this.skipStep = skipStep;
    }

    public Stage getStage() {
        return stage;
    }

    @Override
    public boolean isCancelled() {
        return cancelled;
    }

    @Override
    public void setCancelled(boolean cancelled) {
        this.cancelled = cancelled;
    }

    public enum SlavePreparingStep {

        CLEANING_RENDERING_FOLDER,
        COPY_TEMPLATE,
        COPY_COMMON_FILES,
        MAP_PICKER,
        ;

    }

    public enum Stage {
        PRE,
        POST,
        CUSTOM,
        ;
    }
}

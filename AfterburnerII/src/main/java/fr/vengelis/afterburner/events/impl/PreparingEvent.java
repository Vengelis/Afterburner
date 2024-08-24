package fr.vengelis.afterburner.events.impl;

import fr.vengelis.afterburner.events.AbstractCancelableEvent;

import java.util.ArrayList;
import java.util.List;

public class PreparingEvent extends AbstractCancelableEvent {

    private List<PreparingStep> skipStep = new ArrayList<>();
    private final Stage stage;

    public PreparingEvent(Stage stage) {
        this.stage = stage;
    }

    public List<PreparingStep> getSkipStep() {
        return skipStep;
    }

    public void setSkipStep(List<PreparingStep> skipStep) {
        this.skipStep = skipStep;
    }

    public Stage getStage() {
        return stage;
    }

    public enum PreparingStep {

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

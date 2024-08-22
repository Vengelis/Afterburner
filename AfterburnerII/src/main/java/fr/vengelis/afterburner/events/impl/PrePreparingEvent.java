package fr.vengelis.afterburner.events.impl;

import fr.vengelis.afterburner.events.AbstractCancelableEvent;

import java.util.ArrayList;
import java.util.List;

/**
 * This class represents the PrePreparingEvent in the application.
 * It extends the AbstractCancelableEvent class, which means it inherits all of its methods and properties.
 * <p>
 * This event is triggered before the preparation of the minecraft server.
 * <p>
 * It has one property:
 * <ul>
 *     <li>skipStep: a List of PreparingStep that represents the steps to be skipped during preparation.</li>
 * </ul>
 * It provides one constructor:
 * <ul>
 *     <li>PrePreparingEvent(): This constructor initializes the 'skipStep' property as an empty ArrayList.</li>
 * </ul>
 * It also provides two public methods:
 * <ul>
 *     <li>getSkipStep(): This method returns the current state of the 'skipStep' property.</li>
 *     <li>setSkipStep(List<PreparingStep> skipStep): This method sets a new state for the 'skipStep' property.</li>
 * </ul>
 * It also provides an inner enum:
 * <ul>
 *     <li>PreparingStep: This enum represents the different steps that can be taken during preparation.</li>
 * </ul>
 * This event is called as many times as the server will be re-prepared.
 */
public class PrePreparingEvent extends AbstractCancelableEvent {

    private List<PreparingStep> skipStep = new ArrayList<>();

    /**
     * This method returns the current state of the 'skipStep' property.
     * @return List<PreparingStep>
     */
    public List<PreparingStep> getSkipStep() {
        return skipStep;
    }

    /**
     * This method sets a new state for the 'skipStep' property.
     * @param skipStep List of PreparingStep
     */
    public void setSkipStep(List<PreparingStep> skipStep) {
        this.skipStep = skipStep;
    }

    /**
     * This enum represents the different steps that can be taken during preparation.
     * <p>
     * It includes:
     * <ul>
     *     <li>CLEANING_RENDERING_FOLDER: Cleaning the rendering folder.</li>
     *     <li>COPY_TEMPLATE: Copying the template.</li>
     *     <li>COPY_COMMON_FILES: Copying common files.</li>
     *     <li>MAP_PICKER: Picking a map.</li>
     * </ul>
     */
    public enum PreparingStep {

        CLEANING_RENDERING_FOLDER,
        COPY_TEMPLATE,
        COPY_COMMON_FILES,
        MAP_PICKER,
        ;

    }
}

package fr.vengelis.afterburner.logs.managedprocess;

import java.util.function.Consumer;
import java.util.regex.Pattern;
/**
 * The Skipper class is used to manage and manipulate strings in the application.
 * <p>
 * The Skipper object represents a desire not to write one or more logs based on a pattern.
 * <p>
 * The skipper allows you to keep a clean console without writing unwanted lines in it without removing the fact that it is present in the final server log. The latest.log is not altered by this system
 * <p>
 * A Skipper object therefore allows you to remove one or more lines from the console visual and subsequently execute code using the consumer
 * <p>
 * It provides two constructors:
 * <ul>
 *     <li>Skipper(String words, int lineSkip, boolean cast): This constructor initializes the 'words', 'lineSkip', and 'cast' properties. The 'action' property is set to null.</li>
 *     <li>Skipper(String words, int lineSkip, boolean cast, Consumer<String> action): This constructor initializes all the properties</li>
 * </ul>
 *
 * It also provides four public methods:
 * - getPattern(): This method returns a Pattern object that represents the 'words' property.
 * - getLineSkip(): This method returns the current state of the 'lineSkip' property.
 * - isCast(): This method returns the current state of the 'cast' property.
 * - getAction(): This method returns the current state of the 'action' property.
 */
public class Skipper {

    private final String words;
    private final int lineSkip;
    private final boolean cast;
    private final Consumer<String> action;

    public Skipper(String words, int lineSkip, boolean cast) {
        this.words = words;
        this.lineSkip = lineSkip;
        this.cast = cast;
        this.action = null;
    }

    public Skipper(String words, int lineSkip, boolean cast, Consumer<String> action) {
        this.words = words;
        this.lineSkip = lineSkip;
        this.cast = cast;
        this.action = action;
    }

    public Pattern getPattern() {
        return Pattern.compile("\\b" + words + "\\b", Pattern.CASE_INSENSITIVE);
    }

    public int getLineSkip() {
        return lineSkip;
    }

    public boolean isCast() {
        return cast;
    }

    public Consumer<String> getAction() {
        return action;
    }
}

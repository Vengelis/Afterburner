package fr.vengelis.afterburner.exceptions;

public class WorldFolderEmptyException extends Exception {

    public WorldFolderEmptyException(String message) {
        super("WorldPicker folder '" + message + "' is empty. Please add some worlds to the folder.");
    }
}

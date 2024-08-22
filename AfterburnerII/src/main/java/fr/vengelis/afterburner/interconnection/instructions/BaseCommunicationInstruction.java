package fr.vengelis.afterburner.interconnection.instructions;

public abstract class BaseCommunicationInstruction<T> {

    /**
     * Méthode d'execution de l'instruction
     */
    public abstract T execute();

}

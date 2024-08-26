package fr.vengelis.afterburner.cli.command.printer;

public interface IConsolePrinter<T> {
    void print(T data);
}
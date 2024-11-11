package fr.vengelis.afterburner.cli.command.printer.impl;

import fr.vengelis.afterburner.cli.command.printer.IConsolePrinter;
import fr.vengelis.afterburner.logs.managedprocess.PrintedLog;

public class LogPrinter implements IConsolePrinter<PrintedLog> {

    @Override
    public void print(PrintedLog data) {
        data.print();
    }

}

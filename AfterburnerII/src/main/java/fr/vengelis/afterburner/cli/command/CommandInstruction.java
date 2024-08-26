package fr.vengelis.afterburner.cli.command;

public class CommandInstruction {

    private final String input;
    private final String[] args;

    public CommandInstruction(String input, String[] args) {
        this.input = input;
        this.args = args;
    }

    public String getInput() {
        return input;
    }

    public String[] getArgs() {
        return args;
    }
}

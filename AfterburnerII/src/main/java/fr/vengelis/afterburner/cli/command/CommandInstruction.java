package fr.vengelis.afterburner.cli.command;

public class CommandInstruction {

    private final String input;
    private final String[] args;
    private final AtbCommand.CommandSide side;

    public CommandInstruction(String input, String[] args, AtbCommand.CommandSide side) {
        this.input = input;
        this.args = args;
        this.side = side;
    }

    public String getInput() {
        return input;
    }

    public String[] getArgs() {
        return args;
    }

    public AtbCommand.CommandSide getSide() {
        return side;
    }
}

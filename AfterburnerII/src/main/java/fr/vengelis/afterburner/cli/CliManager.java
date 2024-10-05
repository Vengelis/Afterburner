package fr.vengelis.afterburner.cli;

import fr.vengelis.afterburner.cli.command.*;
import fr.vengelis.afterburner.handler.SuperPreInitHandler;
import fr.vengelis.afterburner.handler.HandlerRecorder;

public class CliManager implements SuperPreInitHandler {

    private final AtbCommand root;

    public CliManager() {
        this.root = new AtbCommand("root", "system", AtbCommand.State.CONTINIOUS);
        HandlerRecorder.get().register(this);
    }


    public void init() {
        AtbCommandLister commandLister = new AtbCommandLister(this.root);
        this.root.addSubCommand(new AtbCommand.AtbCommandBuilder(AtbCommand.State.FINAL)
                .setName("list-all")
                .setDescription("List all commands availables.")
                .addAlias("la", "help", "?")
                .setActionServer(commandLister)
                .setActionClient(commandLister)
                .build());

        Commands.INSTANCE_CONFIG_COMMAND.addSubCommand(Commands.INSTANCE_CONFIG_RELOAD_COMMAND);
        Commands.INSTANCE_LOG_HISTORY_COMMAND.addSubCommand(Commands.INSTANCE_LOG_HISTORY_SHOW_COMMAND);
        Commands.INSTANCE_LOG_HISTORY_COMMAND.addSubCommand(Commands.INSTANCE_LOG_HISTORY_CLEAR_COMMAND);
        Commands.INSTANCE_LOG_COMMAND.addSubCommand(Commands.INSTANCE_LOG_HISTORY_COMMAND);
        Commands.INSTANCE_LOG_COMMAND.addSubCommand(Commands.INSTANCE_LOG_DIRECT_COMMAND);
        Commands.INSTANCE_COMMAND.addSubCommand(Commands.INSTANCE_INPUT_COMMAND);
        Commands.INSTANCE_COMMAND.addSubCommand(Commands.INSTANCE_HTOP_COMMAND);
        Commands.INSTANCE_COMMAND.addSubCommand(Commands.INSTANCE_KILL_COMMAND);

        Commands.AFTERBURNER_BROADCASTER_COMMAND.addSubCommand(Commands.AFTERBURNER_BROADCASTER_UNSTUCK_COMMAND);
        Commands.AFTERBURNER_BROADCASTER_COMMAND.addSubCommand(Commands.AFTERBURNER_BROADCASTER_LOCK_COMMAND);
        Commands.AFTERBURNER_PLUGIN_COMMAND.addSubCommand(Commands.AFTERBURNER_PLUGIN_INFOS_COMMAND);
        Commands.AFTERBURNER_COMMON_FILES_COMMAND.addSubCommand(Commands.AFTERBURNER_COMMON_FILES_SHOW_COMMAND);
        Commands.AFTERBURNER_COMMON_FILES_COMMAND.addSubCommand(Commands.AFTERBURNER_COMMON_FILES_EDIT_COMMAND);
        Commands.AFTERBURNER_COMMAND.addSubCommand(Commands.AFTERBURNER_SHUTDOWN_COMMAND);
        Commands.AFTERBURNER_COMMAND.addSubCommand(Commands.AFTERBURNER_BROADCASTER_COMMAND);
        Commands.AFTERBURNER_COMMAND.addSubCommand(Commands.AFTERBURNER_PLUGIN_COMMAND);
        Commands.AFTERBURNER_COMMAND.addSubCommand(Commands.AFTERBURNER_REPREPARE_COMMAND);
        Commands.AFTERBURNER_COMMAND.addSubCommand(Commands.AFTERBURNER_COMMON_FILES_COMMAND);

        this.root.addSubCommand(Commands.INSTANCE_COMMAND);
        this.root.addSubCommand(Commands.AFTERBURNER_COMMAND);
    }

    public AtbCommand getRootCommand() {
        return root;
    }

    public CommandResult<?> execute(String input, AtbCommand.CommandSide side) {
        return this.root.execute(new CommandInstruction(input, input.split("\\s+"), side));
    }

    public CommandResult<?> execute(CommandInstruction input) {
        return this.root.execute(input);
    }
}

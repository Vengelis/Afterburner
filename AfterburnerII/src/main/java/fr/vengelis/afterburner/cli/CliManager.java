package fr.vengelis.afterburner.cli;

import fr.vengelis.afterburner.AfterburnerSlaveApp;
import fr.vengelis.afterburner.cli.command.*;
import fr.vengelis.afterburner.commonfiles.BaseCommonFile;
import fr.vengelis.afterburner.events.impl.common.PrintedLogEvent;
import fr.vengelis.afterburner.handler.PreInitHandler;
import fr.vengelis.afterburner.interconnection.instructions.impl.CleanLogHistoryInstruction;
import fr.vengelis.afterburner.interconnection.instructions.impl.GetAtbInfosInstruction;
import fr.vengelis.afterburner.interconnection.instructions.impl.ReprepareInstruction;
import fr.vengelis.afterburner.logs.PrintedLog;
import fr.vengelis.afterburner.logs.Skipper;
import fr.vengelis.afterburner.utils.ConsoleLogger;
import fr.vengelis.afterburner.handler.HandlerRecorder;

import java.util.ArrayDeque;
import java.util.LinkedList;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;

public class CliManager implements PreInitHandler {

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
        this.root.addSubCommand(new AtbCommand.AtbCommandBuilder(AtbCommand.State.CONTINIOUS)
                .setName("instance")
                .setDescription("Manage internal program instance.")
                .addAlias("inst")
                .addSubCommand(new AtbCommand.AtbCommandBuilder(AtbCommand.State.FINAL)
                        .setName("input")
                        .setDescription("Send input command to managed program console.")
                        .setActionServer(arg -> {
                            String cmd = String.join(" ", arg.getArgs());
                            boolean execRtn = AfterburnerSlaveApp.get().getManagedProcess().sendCommandToProcess(cmd);
                            return new AtbCommand.ExecutionResult<>(
                                    execRtn,
                                    "Sending input [" + (execRtn ? "successful" : "interrupted") + "] > " + cmd
                            );

                        })
                        .setActionClient(ClientCommandAction::perform)
                        .build())
                .addSubCommand(new AtbCommand.AtbCommandBuilder(AtbCommand.State.FINAL)
                        .setName("htop")
                        .setDescription("Display consommation informations.")
                        .setActionServer(arg -> {
                            new GetAtbInfosInstruction().print();

                            // TODO : A completer
                            return new AtbCommand.ExecutionResult<>(true,
                                    "");
                        })
                        .setActionClient(ClientCommandAction::perform)
                        .build())
                .addSubCommand(new AtbCommand.AtbCommandBuilder(AtbCommand.State.CONTINIOUS)
                        .setName("config")
                        .setDescription("Manage template configuration.")
                        .addAlias("conf")
                        .addSubCommand(new AtbCommand.AtbCommandBuilder(AtbCommand.State.FINAL)
                                .setName("reload")
                                .setDescription("Reload configuration.")
                                .addAlias("r")
                                .setActionServer(arg -> {
                                    AfterburnerSlaveApp.get().loadTemplateConfig();
                                    return new AtbCommand.ExecutionResult<>(
                                            true,
                                            "Template configuration was reloaded !"
                                    );
                                })
                                .setActionClient(ClientCommandAction::perform)
                                .build())
                        .build())
                .addSubCommand(new AtbCommand.AtbCommandBuilder(AtbCommand.State.CONTINIOUS)
                        .setName("log")
                        .setDescription("Log manager")
                        .addSubCommand(new AtbCommand.AtbCommandBuilder(AtbCommand.State.FINAL)
                                .setName("direct")
                                .setDescription("Enable or Disable Real-Time log viewing.")
                                .setActionServer(arg -> {
                                    boolean ns = !AfterburnerSlaveApp.get().isDisplayOutput();
                                    ConsoleLogger.printLine(Level.INFO, "Real-Time log viewing : " + ns);
                                    AfterburnerSlaveApp.get().setDisplayOutput(ns);
                                    AfterburnerSlaveApp.get().getSocketServer().sendAllClient("dli:" + AfterburnerSlaveApp.get().isDisplayOutput());
                                    AfterburnerSlaveApp.get().getSocketServer().sendAllClient(
                                            "Direct view enabled : " + ns
                                    );
                                    return new AtbCommand.ExecutionResult<>(true,
                                            "Direct view enabled : " + ns);
                                })
                                .setActionClient(ClientCommandAction::perform)
                                .build())
                        .addSubCommand(new AtbCommand.AtbCommandBuilder(AtbCommand.State.CONTINIOUS)
                                .setName("history")
                                .setDescription("Log history manager.")
                                .addAlias("h")
                                .addSubCommand(new AtbCommand.AtbCommandBuilder(AtbCommand.State.FINAL)
                                        .setName("clear")
                                        .setDescription("Clear log history")
                                        .addAlias("c", "cl")
                                        .setActionServer(arg -> {
                                            new CleanLogHistoryInstruction().execute();
                                            return new AtbCommand.ExecutionResult<>(true,
                                                    "Log history was cleared !");
                                        })
                                        .setActionClient(ClientCommandAction::perform)
                                        .build())
                                .addSubCommand(new AtbCommand.AtbCommandBuilder(AtbCommand.State.FINAL)
                                        .setName("show")
                                        .setDescription("Display current logs (add '--show-skipped-line|-ssl' for view all lines)")
                                        .setActionServer(arg -> {

                                            boolean disableSkipper = isFinalDisableSkipper(arg.getArgs());
                                            AtomicInteger skip = new AtomicInteger();

                                            ArrayDeque<String> rtn = new ArrayDeque<>();

                                            AfterburnerSlaveApp.get().getLogHistory().forEach(log -> {
                                                PrintedLogEvent event1 = new PrintedLogEvent(log, PrintedLogEvent.Handler.CLI);
                                                AfterburnerSlaveApp.get().getEventManager().call(event1);
                                                if(event1.isCancelled()) {
                                                    log.setSkip(true).save();
                                                    return;
                                                }

                                                if(!disableSkipper) {
                                                    for(Skipper value : AfterburnerSlaveApp.get().getLogSkipperManager().getSkipperList()) {
                                                        if(value.getPattern().matcher(log.getLine()).find()) {
                                                            skip.addAndGet(value.getLineSkip());
                                                        }
                                                    }
                                                }

                                                if(skip.get() == 0) {
                                                    rtn.add(log.getLine());
                                                }
                                                else {
                                                    log.setSkip(true);
                                                    skip.getAndDecrement();
                                                }
                                            });

                                            return new AtbCommand.ExecutionResult<>(true,
                                                    rtn);
                                        })
                                        .setActionClient(ClientCommandAction::perform)
                                        .build())
                                .build())
                        .build())
                .addSubCommand(new AtbCommand.AtbCommandBuilder(AtbCommand.State.FINAL)
                        .setName("kill")
                        .setDescription("Kill current process instance.")
                        .setActionServer(arg -> {
                            boolean rtn;
                            if(arg == null) {
                                rtn = AfterburnerSlaveApp.get().killTask("No reason was specified in CLI");
                                return new AtbCommand.ExecutionResult<>(rtn,
                                        "No reason was specified in CLI");
                            } else {
                                String stg = String.join(" ", arg.getArgs());
                                rtn = AfterburnerSlaveApp.get().killTask(stg);
                                return new AtbCommand.ExecutionResult<>(rtn,
                                        stg);
                            }
                        })
                        .setActionClient(ClientCommandAction::perform)
                        .build())
                .build());

        this.root.addSubCommand(new AtbCommand.AtbCommandBuilder(AtbCommand.State.CONTINIOUS)
                .setName("afterburner")
                .setDescription("Manage afterburner instance.")
                .addAlias("atb")
                .addSubCommand(new AtbCommand.AtbCommandBuilder(AtbCommand.State.CONTINIOUS)
                        .setName("commonfiles")
                        .setDescription("Manage commons files.")
                        .addAlias("cf")
                        .addSubCommand(new AtbCommand.AtbCommandBuilder(AtbCommand.State.FINAL)
                                .setName("show")
                                .setDescription("Show all commons files")
                                .addAlias("list", "s")
                                .setActionServer(arg -> {
                                    LinkedList<String> rtn = new LinkedList<>();
                                    AfterburnerSlaveApp.get().getActualCommonFilesLoaded().forEach((cf, cflist) -> {
                                        if(cflist.isEmpty()) {
                                            rtn.add("Common file list of type '" + cf.getSimpleName() + "' is empty");
                                        } else {
                                            rtn.add("Common file list of type '" + cf.getSimpleName() + "' :");
                                            for (Object cfa : cflist) {
                                                BaseCommonFile icfa = (BaseCommonFile) cfa;
                                                rtn.add("   - " + icfa.getName() + " (Enabled : " + icfa.isEnabled() + ")");
                                            }
                                        }
                                    });
                                    return new AtbCommand.ExecutionResult<>(true,
                                            rtn);
                                })
                                .setActionClient(ClientCommandAction::perform)
                                .build())
                        .addSubCommand(new AtbCommand.AtbCommandBuilder(AtbCommand.State.FINAL)
                                .setName("edit")
                                .setDescription("Enable or disable common file - Command : atb cf edit <common file name> <setting> <true/false>")
                                .requiresArgument("Error: Command : atb cf edit <common file name> <setting> <true/false>")
                                .setActionServer(arg -> {
                                    if(arg.getArgs().length < 3)
                                        return new AtbCommand.ExecutionResult<>(false,
                                                "Missing common file name and/or settings and/or data of setting");
                                    else {
                                        String cfn = arg.getArgs()[0];
                                        String setting = arg.getArgs()[1];
                                        String settingdata = arg.getArgs()[2];
                                        Optional<BaseCommonFile> cfnr = AfterburnerSlaveApp.get().computeCommonFileWithName(cfn);
                                        if(cfnr.isPresent()) {
                                            if(setting.equalsIgnoreCase("enabled")) {
                                                boolean v = Boolean.parseBoolean(settingdata);
                                                cfnr.get().setEnabled(v);
                                                return new AtbCommand.ExecutionResult<>(true,
                                                        "Setting '" + setting + "' for common file '" + cfn + "' changed to '" + v + "'");
                                            } else {
                                                return new AtbCommand.ExecutionResult<>(false,
                                                        "setting " + setting + " not available/found !");
                                            }
                                        } else {
                                            return new AtbCommand.ExecutionResult<>(false,
                                                    "Unknown common file '" + cfn + "'");
                                        }
                                    }
                                })
                                .setActionClient(ClientCommandAction::perform)
                                .build())
                        .build())
                .addSubCommand(new AtbCommand.AtbCommandBuilder(AtbCommand.State.FINAL)
                        .setName("reprepare")
                        .setDescription("Resumes the process after the previous one ends.")
                        .addAlias("rep")
                        .requiresArgument("Error: This command requires a reason message.")
                        .setActionServer(arg -> {
                            boolean rtn = new ReprepareInstruction(String.join(" ", arg.getArgs())).execute();
                            return new AtbCommand.ExecutionResult<>(rtn,
                                    rtn);
                        })
                        .setActionClient(ClientCommandAction::perform)
                        .build())
                .addSubCommand(new AtbCommand.AtbCommandBuilder(AtbCommand.State.CONTINIOUS)
                        .setName("plugin")
                        .setDescription("")
                        .addAlias("pl")
                        .addSubCommand(new AtbCommand.AtbCommandBuilder(AtbCommand.State.FINAL)
                                .setName("infos")
                                .setDescription("List plugins informations")
                                .addAlias("is")
                                .setActionServer(args -> {
                                    LinkedList<String> rtn = new LinkedList<>();
                                    rtn.add("Plugins : ");
                                    AfterburnerSlaveApp.get().getPluginManager().getPlugins().forEach((pn, p) -> {
                                        rtn.add(" - " + pn);
                                    });
                                    return new AtbCommand.ExecutionResult<>(true,
                                            rtn);
                                })
                                .setActionClient(ClientCommandAction::perform)
                                .build())
                        .build())
                .addSubCommand(new AtbCommand.AtbCommandBuilder(AtbCommand.State.CONTINIOUS)
                        .setName("broadcaster")
                        .setDescription("")
                        .addAlias("bct")
                        .addSubCommand(new AtbCommand.AtbCommandBuilder(AtbCommand.State.FINAL)
                                .setName("unstuck")
                                .setDescription("Unstuck broadcaster call if is automatically locked after max time out request reached")
                                .addAlias("us")
                                .setActionServer(args -> {
                                    AfterburnerSlaveApp.get().getBroadcasterWebApiHandler().resetLocker();
                                    return new AtbCommand.ExecutionResult<>(true,
                                            "Broadcaster unlocked");
                                })
                                .setActionClient(ClientCommandAction::perform)
                                .build())
                        .addSubCommand(new AtbCommand.AtbCommandBuilder(AtbCommand.State.FINAL)
                                .setName("stop")
                                .setDescription("Lock broadcaster calls")
                                .addAlias("s")
                                .setActionServer(args -> {
                                    AfterburnerSlaveApp.get().getBroadcasterWebApiHandler().stop();
                                    return new AtbCommand.ExecutionResult<>(true,
                                            "Broadcaster locked");
                                })
                                .setActionClient(ClientCommandAction::perform)
                                .build())
                        .build())
                .addSubCommand(new AtbCommand.AtbCommandBuilder(AtbCommand.State.FINAL)
                        .setName("shutdown")
                        .setDescription("Shutdown afterburner immediately.")
                        .addAlias("sh")
                        .setActionServer(arg -> {
                            String reason = String.join(" ", arg.getArgs());
                            boolean wp = true;
                            if(reason.isEmpty()) reason = "Ordered by CLI without reason";
                            else if(arg.getArgs().length > 0) {
                                String a = arg.getArgs()[0];
                                if(a.equalsIgnoreCase("--skip-end-process") ||
                                        a.equalsIgnoreCase("-sep") ||
                                        a.equalsIgnoreCase("-s"))
                                    wp = false;
                            }
                            AfterburnerSlaveApp.get().killTask(reason, wp);
                            AfterburnerSlaveApp.get().setReprepareEnabled(false);
                            return new AtbCommand.ExecutionResult<>(true,
                                    "Good by :D");
                        })
                        .setActionClient(ClientCommandAction::perform)
                        .build())
                .build());

    }

    private static boolean isFinalDisableSkipper(String[] arg) {
        boolean disableSkipper = false;
        for (String a : arg) {
            if (a.equalsIgnoreCase("--show-skipped-line") || a.equalsIgnoreCase("-ssl")) {
                disableSkipper = true;
                break;
            }
        }
        return disableSkipper;
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

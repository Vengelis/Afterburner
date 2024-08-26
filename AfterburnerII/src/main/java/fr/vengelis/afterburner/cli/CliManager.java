package fr.vengelis.afterburner.cli;

import fr.vengelis.afterburner.AfterburnerApp;
import fr.vengelis.afterburner.cli.command.AtbCommand;
import fr.vengelis.afterburner.cli.command.AtbCommandLister;
import fr.vengelis.afterburner.cli.command.CommandInstruction;
import fr.vengelis.afterburner.commonfiles.BaseCommonFile;
import fr.vengelis.afterburner.events.impl.PrintedLogEvent;
import fr.vengelis.afterburner.handler.PreInitHandler;
import fr.vengelis.afterburner.interconnection.instructions.impl.CleanLogHistoryInstruction;
import fr.vengelis.afterburner.interconnection.instructions.impl.GetAtbInfosInstruction;
import fr.vengelis.afterburner.interconnection.instructions.impl.ReprepareInstruction;
import fr.vengelis.afterburner.logs.Skipper;
import fr.vengelis.afterburner.utils.ConsoleLogger;
import fr.vengelis.afterburner.handler.HandlerRecorder;

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
                .setAction(commandLister)
                .build());

        this.root.addSubCommand(new AtbCommand.AtbCommandBuilder(AtbCommand.State.CONTINIOUS)
                .setName("instance")
                .setDescription("Manage internal program instance.")
                .addAlias("inst")
                .addSubCommand(new AtbCommand.AtbCommandBuilder(AtbCommand.State.FINAL)
                        .setName("input")
                        .setDescription("Send input command to managed program console.")
                        .setAction(arg -> {
                            String cmd = String.join(" ", arg.getArgs());
                            boolean execRtn = AfterburnerApp.get().getManagedProcess().sendCommandToProcess(cmd);
                            return new AtbCommand.ExecutionResult<>(
                                    execRtn,
                                    "Sending input [" + (execRtn ? "successful" : "interrupted") + "] > " + cmd
                            );

                        })
                        .build())
                .addSubCommand(new AtbCommand.AtbCommandBuilder(AtbCommand.State.FINAL)
                        .setName("htop")
                        .setDescription("Display consommation informations.")
                        .setAction(arg -> {
                            new GetAtbInfosInstruction().print();

                            // TODO : A completer
                            return new AtbCommand.ExecutionResult<>(true,
                                    "");
                        })
                        .build())
                .addSubCommand(new AtbCommand.AtbCommandBuilder(AtbCommand.State.CONTINIOUS)
                        .setName("config")
                        .setDescription("Manage template configuration.")
                        .addAlias("conf")
                        .addSubCommand(new AtbCommand.AtbCommandBuilder(AtbCommand.State.FINAL)
                                .setName("reload")
                                .setDescription("Reload configuration.")
                                .addAlias("r")
                                .setAction(arg -> {
                                    AfterburnerApp.get().loadTemplateConfig();
                                    return new AtbCommand.ExecutionResult<>(
                                            true,
                                            "Template configuration was reloaded !"
                                    );
                                })
                                .build())
                        .build())
                .addSubCommand(new AtbCommand.AtbCommandBuilder(AtbCommand.State.CONTINIOUS)
                        .setName("log")
                        .setDescription("Log manager")
                        .addSubCommand(new AtbCommand.AtbCommandBuilder(AtbCommand.State.FINAL)
                                .setName("direct")
                                .setDescription("Enable or Disable Real-Time log viewing.")
                                .setAction(arg -> {
                                    boolean ns = !AfterburnerApp.get().isDisplayOutput();
                                    ConsoleLogger.printLine(Level.INFO, "Real-Time log viewing : " + ns);
                                    AfterburnerApp.get().setDisplayOutput(ns);

                                    // TODO : A completer
                                    return new AtbCommand.ExecutionResult<>(true,
                                            "");
                                })
                                .build())
                        .addSubCommand(new AtbCommand.AtbCommandBuilder(AtbCommand.State.CONTINIOUS)
                                .setName("history")
                                .setDescription("Log history manager.")
                                .addAlias("h")
                                .addSubCommand(new AtbCommand.AtbCommandBuilder(AtbCommand.State.FINAL)
                                        .setName("clear")
                                        .setDescription("Clear log history")
                                        .addAlias("c", "cl")
                                        .setAction(arg -> {
                                            new CleanLogHistoryInstruction().execute();
                                            return new AtbCommand.ExecutionResult<>(true,
                                                    "Log history was cleared !");
                                        })
                                        .build())
                                .addSubCommand(new AtbCommand.AtbCommandBuilder(AtbCommand.State.FINAL)
                                        .setName("show")
                                        .setDescription("Display current logs (add '--show-skipped-line|-ssl' for view all lines)")
                                        .setAction(arg -> {

                                            boolean disableSkipper = isFinalDisableSkipper(arg.getArgs());
                                            AtomicInteger skip = new AtomicInteger();

                                            AfterburnerApp.get().getLogHistory().forEach(log -> {
                                                PrintedLogEvent event1 = new PrintedLogEvent(log, PrintedLogEvent.Handler.CLI);
                                                AfterburnerApp.get().getEventManager().call(event1);
                                                if(event1.isCancelled()) {
                                                    log.setSkip(true).save();
                                                    return;
                                                }

                                                if(!disableSkipper) {
                                                    for(Skipper value : AfterburnerApp.get().getLogSkipperManager().getSkipperList()) {
                                                        if(value.getPattern().matcher(log.getLine()).find()) {
                                                            if(value.isCast()) ConsoleLogger.printLine(Level.INFO, "LH : Skipper trigger : " + log.getLine() + " (" + value.getLineSkip() + " lines ignored)");
                                                            if(value.getAction() != null) ConsoleLogger.printLine(Level.INFO, String.format("LH : Action of line >%s< is volontary skipped by log history !", log.getLine()));
                                                            skip.addAndGet(value.getLineSkip());
                                                        }
                                                    }
                                                }

                                                if(skip.get() == 0) ConsoleLogger.printLine(Level.INFO, "LH : " + log.getLine());
                                                else {
                                                    log.setSkip(true);
                                                    skip.getAndDecrement();
                                                }
                                            });

                                            // TODO : A completer
                                            return new AtbCommand.ExecutionResult<>(true,
                                                    "");
                                        })
                                        .build())
                                .build())
                        .build())
                .addSubCommand(new AtbCommand.AtbCommandBuilder(AtbCommand.State.FINAL)
                        .setName("kill")
                        .setDescription("Kill current process instance.")
                        .setAction(arg -> {
                            boolean rtn;
                            if(arg == null) {
                                rtn = AfterburnerApp.get().killTask("No reason was specified in CLI");
                                return new AtbCommand.ExecutionResult<>(rtn,
                                        "No reason was specified in CLI");
                            } else {
                                String stg = String.join(" ", arg.getArgs());
                                rtn = AfterburnerApp.get().killTask(stg);
                                return new AtbCommand.ExecutionResult<>(rtn,
                                        stg);
                            }
                        })
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
                                .setAction(arg -> {
                                    LinkedList<String> rtn = new LinkedList<>();
                                    AfterburnerApp.get().getActualCommonFilesLoaded().forEach((cf, cflist) -> {
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
                                .build())
                        .addSubCommand(new AtbCommand.AtbCommandBuilder(AtbCommand.State.FINAL)
                                .setName("edit")
                                .setDescription("Enable or disable common file - Command : atb cf edit <common file name> <setting> <true/false>")
                                .requiresArgument()
                                .setAction(arg -> {
                                    if(arg.getArgs().length < 3)
                                        return new AtbCommand.ExecutionResult<>(false,
                                                "Missing common file name and/or settings and/or data of setting");
                                    else {
                                        String cfn = arg.getArgs()[0];
                                        String setting = arg.getArgs()[1];
                                        String settingdata = arg.getArgs()[2];
                                        Optional<BaseCommonFile> cfnr = AfterburnerApp.get().computeCommonFileWithName(cfn);
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
                                .build())
                        .build())
                .addSubCommand(new AtbCommand.AtbCommandBuilder(AtbCommand.State.FINAL)
                        .setName("reprepare")
                        .setDescription("Resumes the process after the previous one ends.")
                        .addAlias("rep")
                        .requiresArgument()
                        .setAction(arg -> {
                            boolean rtn = new ReprepareInstruction(String.join(" ", arg.getArgs())).execute();
                            return new AtbCommand.ExecutionResult<>(rtn,
                                    rtn);
                        })
                        .build())
                .addSubCommand(new AtbCommand.AtbCommandBuilder(AtbCommand.State.CONTINIOUS)
                        .setName("plugin")
                        .setDescription("")
                        .addAlias("pl")
                        .addSubCommand(new AtbCommand.AtbCommandBuilder(AtbCommand.State.FINAL)
                                .setName("infos")
                                .setDescription("List plugins informations")
                                .addAlias("is")
                                .setAction(args -> {
                                    LinkedList<String> rtn = new LinkedList<>();
                                    rtn.add("Plugins : ");
                                    AfterburnerApp.get().getPluginManager().getPlugins().forEach((pn, p) -> {
                                        rtn.add(" - " + pn);
                                    });
                                    return new AtbCommand.ExecutionResult<>(true,
                                            rtn);
                                })
                                .build())
                        .build())
                .addSubCommand(new AtbCommand.AtbCommandBuilder(AtbCommand.State.FINAL)
                        .setName("shutdown")
                        .setDescription("Shutdown afterburner immediately.")
                        .addAlias("sh")
                        .setAction(arg -> {
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
                            AfterburnerApp.get().killTask(reason, wp);
                            AfterburnerApp.get().setReprepareEnabled(false);
                            return new AtbCommand.ExecutionResult<>(true,
                                    "Good by :D");
                        })
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

    public void execute(String input) {
        CommandInstruction instruction = new CommandInstruction(input, input.split("\\s+"));
        this.root.execute(instruction);
    }
}

package fr.vengelis.afterburner.cli;

import fr.vengelis.afterburner.AfterburnerApp;
import fr.vengelis.afterburner.cli.consumers.AtbCommandLister;
import fr.vengelis.afterburner.commonfiles.BaseCommonFile;
import fr.vengelis.afterburner.events.impl.PrintedLogEvent;
import fr.vengelis.afterburner.interconnection.instructions.impl.CleanLogHistoryInstruction;
import fr.vengelis.afterburner.interconnection.instructions.impl.GetAtbInfosInstruction;
import fr.vengelis.afterburner.interconnection.instructions.impl.ReprepareInstruction;
import fr.vengelis.afterburner.logs.Skipper;
import fr.vengelis.afterburner.utils.ConsoleLogger;

import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;

public class CliManager {

    private final AtbCommand root;

    public CliManager() {
        this.root = new AtbCommand("root", "system", AtbCommand.State.CONTINIOUS);
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
                            String cmd = String.join(" ", arg);
                            ConsoleLogger.printLine(Level.INFO, "Command sended > " + cmd);
                            AfterburnerApp.get().sendCommandToProcess(cmd);
                        })
                        .build())
                .addSubCommand(new AtbCommand.AtbCommandBuilder(AtbCommand.State.FINAL)
                        .setName("htop")
                        .setDescription("Display consommation informations.")
                        .setAction(arg -> {
                            new GetAtbInfosInstruction().print();
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
                                    ConsoleLogger.printLine(Level.INFO, "Template configuration was reloaded !");
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
                                            ConsoleLogger.printLine(Level.INFO, "Log history was cleared !");
                                        })
                                        .build())
                                .addSubCommand(new AtbCommand.AtbCommandBuilder(AtbCommand.State.FINAL)
                                        .setName("show")
                                        .setDescription("Display current logs (add '--show-skipped-line|-ssl' for view all lines)")
                                        .setAction(arg -> {

                                            boolean disableSkipper = isFinalDisableSkipper(arg);
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
                                        })
                                        .build())
                                .build())
                        .build())
                .addSubCommand(new AtbCommand.AtbCommandBuilder(AtbCommand.State.FINAL)
                        .setName("kill")
                        .setDescription("Kill current process instance.")
                        .setAction(arg -> {
                            if(arg == null) {
                                AfterburnerApp.get().killTask("No reason was specified in CLI");
                            } else {
                                AfterburnerApp.get().killTask(String.join(" ", arg));
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
                                    AfterburnerApp.get().getActualCommonFilesLoaded().forEach((cf, cflist) -> {
                                        if(cflist.isEmpty()) ConsoleLogger.printLine(Level.INFO, "Common file list of type '" + cf.getSimpleName() + "' is empty");
                                        else {
                                            ConsoleLogger.printLine(Level.INFO, "Common file list of type '" + cf.getSimpleName() + "' :");
                                            for (Object cfa : cflist) {
                                                BaseCommonFile icfa = (BaseCommonFile) cfa;
                                                ConsoleLogger.printLine(Level.INFO, "   - " + icfa.getName() + " (Enabled : " + icfa.isEnabled() + ")");
                                            }
                                        }
                                    });
                                })
                                .build())
                        .addSubCommand(new AtbCommand.AtbCommandBuilder(AtbCommand.State.FINAL)
                                .setName("edit")
                                .setDescription("Enable or disable common file - Command : atb cf edit <common file name> <setting> <true/false>")
                                .requiresArgument()
                                .setAction(arg -> {
                                    if(arg.length < 3) ConsoleLogger.printLine(Level.SEVERE, "Missing common file name and/or settings and/or data of setting");
                                    else {
                                        String cfn = arg[0];
                                        String setting = arg[1];
                                        String settingdata = arg[2];
                                        Optional<BaseCommonFile> cfnr = AfterburnerApp.get().computeCommonFileWithName(cfn);
                                        if(cfnr.isPresent()) {
                                            if(setting.equalsIgnoreCase("enabled")) {
                                                boolean v = Boolean.parseBoolean(settingdata);
                                                cfnr.get().setEnabled(v);
                                                ConsoleLogger.printLine(Level.INFO, "Setting '" + setting + "' for common file '" + cfn + "' changed to '" + v + "'");
                                            }
                                        } else {
                                            ConsoleLogger.printLine(Level.SEVERE, "Unknown common file '" + cfn + "'");
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
                            new ReprepareInstruction(String.join(" ", arg)).execute();
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
                                    ConsoleLogger.printLine(Level.INFO, "Plugins : ");
                                    AfterburnerApp.get().getPluginManager().getPlugins().forEach((pn, p) -> {
                                        ConsoleLogger.printLine(Level.INFO, " - " + pn);
                                    });
                                })
                                .build())
                        .build())
                .addSubCommand(new AtbCommand.AtbCommandBuilder(AtbCommand.State.FINAL)
                        .setName("shutdown")
                        .setDescription("Shutdown afterburner immediatly.")
                        .addAlias("sh")
                        .setAction(arg -> {
                            String reason = String.join(" ", arg);
                            if(reason.isEmpty()) reason = "Ordered by CLI without reason";
                            AfterburnerApp.get().killTask(reason);
                            ConsoleLogger.printLine(Level.INFO, "Good by :D");
                            System.exit(0);
                        })
                        .build())
                .build());

    }

    private static boolean isFinalDisableSkipper(String[] arg) {
        boolean disableSkipper = false;
        if(arg.length != 0) {
            for (String a : arg) {
                if(a.equalsIgnoreCase("--show-skipped-line") || a.equalsIgnoreCase("-ssl"))
                    disableSkipper = true;
            }
        }
        return disableSkipper;
    }

    public AtbCommand getRootCommand() {
        return root;
    }

    public void execute(String input) {
        this.root.execute(input.split(" "));
    }
}

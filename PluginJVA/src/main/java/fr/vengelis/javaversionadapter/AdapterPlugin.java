package fr.vengelis.javaversionadapter;

import fr.vengelis.afterburner.Afterburner;
import fr.vengelis.afterburner.AfterburnerSlaveApp;
import fr.vengelis.afterburner.cli.command.AtbCommand;
import fr.vengelis.afterburner.cli.command.ClientCommandAction;
import fr.vengelis.afterburner.configurations.AsConfig;
import fr.vengelis.afterburner.plugins.ATBPlugin;
import fr.vengelis.afterburner.plugins.AbstractATBPlugin;
import fr.vengelis.javaversionadapter.adapter.Adapter;
import fr.vengelis.javaversionadapter.adapter.AdapterManager;
import fr.vengelis.javaversionadapter.listeners.Listeners;
import org.yaml.snakeyaml.Yaml;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;

@ATBPlugin(name = "JavaVersionAdapter", launchType = Afterburner.LaunchType.SLAVE)
public class AdapterPlugin extends AbstractATBPlugin implements AsConfig {

    private static AdapterPlugin instance;

    private final AdapterManager adapterManager = new AdapterManager();

    public Adapter lastApplied = null;

    @Override
    public void onLoad() {
        instance = this;
        AfterburnerSlaveApp.get().getEventManager().register(new Listeners());
        AfterburnerSlaveApp.get().getCliManager().getRootCommand().addSubCommand(new AtbCommand.AtbCommandBuilder(AtbCommand.State.CONTINIOUS)
                .setName("jva")
                .setDescription("")
                .addSubCommand(new AtbCommand.AtbCommandBuilder(AtbCommand.State.FINAL)
                        .setName("last")
                        .setDescription("Get last adapter applied to instance")
                        .addAlias("l", "get")
                        .setActionServer(arg -> {
                            Adapter rtn = lastApplied;
                            if(rtn == null) rtn = new Adapter("null", null, null);
                            return new AtbCommand.ExecutionResult<>(true, "Last jva adapter applied : " + rtn.getName());
                        })
                        .setActionClient(ClientCommandAction::perform)
                        .build())
                .build());
    }

    @Override
    public void loadConfig() throws IOException {
        Path configPath = Paths.get(getWorkingDirectory(), "PluginJVA", "config.yml");
        Map<String, Object> data = new Yaml().load(Files.newInputStream(configPath));
        List<Object> regexlist = (List<Object>) data.get("adapter");
        for(Object regex : regexlist) {
            String[] s = regex.toString()
                    .replace("{", "")
                    .replace("}", "")
                    .split(", ")
                    ;
            String name = s[0].replace("name=", "");
            String trigger = s[1].replace("trigger-name=", "");
            String execute = s[2].replace("java-exec-command=", "");
            adapterManager.register(new Adapter(name, trigger, execute));
        }
    }

    public static AdapterPlugin get() {
        return instance;
    }

    public AdapterManager getAdapterManager() {
        return adapterManager;
    }
}

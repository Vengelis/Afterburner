package fr.vengelis.afterburner;

import fr.vengelis.afterburner.cli.CliManager;
import fr.vengelis.afterburner.configurations.ConfigGeneral;
import fr.vengelis.afterburner.events.EventManager;
import fr.vengelis.afterburner.events.impl.EndEvent;
import fr.vengelis.afterburner.events.impl.ExecutableEvent;
import fr.vengelis.afterburner.events.impl.InitializeEvent;
import fr.vengelis.afterburner.events.impl.PreparingEvent;
import fr.vengelis.afterburner.interconnection.socket.system.SocketEmbarkedClient;
import fr.vengelis.afterburner.plugins.PluginManager;
import fr.vengelis.afterburner.providers.ProviderManager;
import fr.vengelis.afterburner.runnables.RunnableManager;
import fr.vengelis.afterburner.utils.ConsoleLogger;
import fr.vengelis.afterburner.utils.ResourceExporter;

import java.util.logging.Level;

public class AfterburnerClientApp implements AApp{

    private static AfterburnerClientApp instance;

    private final ResourceExporter exporter = new ResourceExporter();

    private final EventManager eventManager = new EventManager();
    private final ProviderManager providerManager = new ProviderManager();
    private final PluginManager pluginManager = new PluginManager();
    private final RunnableManager runnableManager = new RunnableManager();
    private final CliManager cliManager = new CliManager();

    private boolean alreadyInit = false;
    private SocketEmbarkedClient client;

    public AfterburnerClientApp() {
        instance = this;
    }

    public static AfterburnerClientApp get() {
        return instance;
    }

    @Override
    public void exportRessources() {
        ConsoleLogger.printLine(Level.INFO, "Exporting configurations ...");
        AfterburnerAppCommon.exportRessources(this);
    }

    @Override
    public void loadPluginsAndProviders() {
        AfterburnerAppCommon.loadProviderAndPlugin(this);
    }

    @Override
    public void loadGeneralConfigs() {
        AfterburnerAppCommon.loadGeneralConfig(this);
    }

    @Override
    public void initialize() {
        if(alreadyInit) return;
        alreadyInit = true;
        ConsoleLogger.printLine(Level.INFO, "Initializing");

        client = new SocketEmbarkedClient(ConfigGeneral.QUERY_HOST.getData().toString(), (Integer) ConfigGeneral.QUERY_PORT.getData());

        eventManager.call(new InitializeEvent());
    }

    @Override
    public void preparing() {
        eventManager.call(new PreparingEvent(PreparingEvent.Stage.PRE));
        eventManager.call(new PreparingEvent(PreparingEvent.Stage.POST));
    }

    @Override
    public void execute() {
        try {
            client.start();
        } catch (Exception ignored) {
        }
        eventManager.call(new ExecutableEvent(new StringBuilder()));
    }

    @Override
    public void ending() {
        eventManager.call(new EndEvent());
    }

    @Override
    public ResourceExporter getExporter() {
        return exporter;
    }

    @Override
    public EventManager getEventManager() {
        return eventManager;
    }

    @Override
    public ProviderManager getProviderManager() {
        return providerManager;
    }

    @Override
    public PluginManager getPluginManager() {
        return pluginManager;
    }

    @Override
    public RunnableManager getRunnableManager() {
        return runnableManager;
    }

    public SocketEmbarkedClient getClient() {
        return client;
    }

    @Override
    public CliManager getCliManager() {
        return cliManager;
    }
}

package fr.vengelis.afterburner;

import fr.vengelis.afterburner.cli.CliManager;
import fr.vengelis.afterburner.configurations.ConfigGeneral;
import fr.vengelis.afterburner.events.EventManager;
import fr.vengelis.afterburner.events.impl.common.EndEvent;
import fr.vengelis.afterburner.events.impl.common.ExecutableEvent;
import fr.vengelis.afterburner.events.impl.common.InitializeEvent;
import fr.vengelis.afterburner.events.impl.common.PreparingEvent;
import fr.vengelis.afterburner.handler.HandlerRecorder;
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

    private final String host;
    private final Integer port;
    private final String password;

    public AfterburnerClientApp() {
        instance = this;
        this.host = null;
        this.port = null;
        this.password = null;
    }

    public AfterburnerClientApp(String host, Integer port, String password) {
        instance = this;
        this.host = host;
        this.port = port;
        this.password = password;
    }

    public static AfterburnerClientApp get() {
        return instance;
    }

    @Override
    public void boot(HandlerRecorder handlerRecorder) {
        AfterburnerClientApp finalApp = instance;
        new Thread(() -> {
            finalApp.exportRessources();
            handlerRecorder.executeSuperPreInit();
            finalApp.loadPluginsAndProviders();
            finalApp.loadGeneralConfigs();
            handlerRecorder.executePreInit(finalApp);
            finalApp.initialize();
            finalApp.preparing();
            finalApp.execute();
            finalApp.ending();
        }).start();
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

        if(host == null && port == null && password == null)
            client = new SocketEmbarkedClient(
                    ConfigGeneral.QUERY_HOST.getData().toString(),
                    (Integer) ConfigGeneral.QUERY_PORT.getData(),
                    ConfigGeneral.QUERY_PASSWORD.getData().toString());
        else
            client = new SocketEmbarkedClient(host,port,password);
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
            ConsoleLogger.printStacktrace(ignored);
        }
        eventManager.call(new ExecutableEvent(null, new StringBuilder()));
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

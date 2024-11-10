package fr.vengelis.afterburner;

import fr.vengelis.afterburner.cli.CliManager;
import fr.vengelis.afterburner.configurations.ConfigBroadcaster;
import fr.vengelis.afterburner.events.EventManager;
import fr.vengelis.afterburner.events.impl.common.ExecutableEvent;
import fr.vengelis.afterburner.events.impl.common.InitializeEvent;
import fr.vengelis.afterburner.events.impl.common.PreparingEvent;
import fr.vengelis.afterburner.exceptions.BrokenConfigException;
import fr.vengelis.afterburner.handler.HandlerRecorder;
import fr.vengelis.afterburner.interconnection.socket.broadcaster.SlaveBroadcast;
import fr.vengelis.afterburner.language.LanguageManager;
import fr.vengelis.afterburner.plugins.PluginManager;
import fr.vengelis.afterburner.providers.ProviderManager;
import fr.vengelis.afterburner.runnables.RunnableManager;
import fr.vengelis.afterburner.utils.ConsoleLogger;
import fr.vengelis.afterburner.utils.ResourceExporter;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.yaml.snakeyaml.Yaml;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;

@SpringBootApplication
public class AfterburnerBroadcasterApp implements AApp{

    private static AfterburnerBroadcasterApp instance;

    private final ResourceExporter exporter = new ResourceExporter();

    private final EventManager eventManager = new EventManager();
    private final ProviderManager providerManager = new ProviderManager();
    private final PluginManager pluginManager = new PluginManager();
    private final RunnableManager runnableManager = new RunnableManager();
    private final CliManager cliManager = new CliManager();

    private boolean alreadyInit = false;
    private final List<SlaveBroadcast> slaves = new ArrayList<>();

    public AfterburnerBroadcasterApp() {
        instance = this;
    }

    @Override
    public void boot(HandlerRecorder handlerRecorder) {
        AfterburnerBroadcasterApp finalApp = instance;
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
        try {
            exporter.saveResource(new File(Afterburner.WORKING_AREA), "/broadcaster.yml", false);
            exporter.createFolder(Afterburner.WORKING_AREA + File.separator + "plugins");
            exporter.createFolder(Afterburner.WORKING_AREA + File.separator + "providers");
        } catch (IOException e) {
            ConsoleLogger.printStacktrace(e);
            System.exit(1);
        }

    }

    @Override
    public void loadPluginsAndProviders() {
        AfterburnerAppCommon.loadProviderAndPlugin(this);
    }

    @Override
    public void loadGeneralConfigs() {
        try {
            File config = new File(Afterburner.WORKING_AREA + File.separator + "broadcaster.yml");
            Yaml yaml = new Yaml();
            InputStream stm = Files.newInputStream(config.toPath());

            Map<String, Object> data = yaml.load(stm);

            Map<String, Object> api = (Map<String, Object>) data.get("api");
            ConfigBroadcaster.API_HOST.setData(api.get("host"));
            ConfigBroadcaster.API_PORT.setData(api.get("port"));
            ConfigBroadcaster.API_TOKEN.setData(api.get("token"));

        } catch (IOException e) {
            ConsoleLogger.printStacktrace(new BrokenConfigException(e));
            System.exit(1);
        }
    }

    @Override
    public void initialize() {
        if(alreadyInit) return;
        alreadyInit = true;
        ConsoleLogger.printLine(Level.INFO, LanguageManager.translate("atb-initializing"));

        new Thread(() -> {
            while (true) {
                List<SlaveBroadcast> sl = new ArrayList<>(AfterburnerBroadcasterApp.get().getSlaves());
                sl.forEach(SlaveBroadcast::actualise);
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
        }).start();
        eventManager.call(new InitializeEvent());
    }

    @Override
    public void preparing() {
        eventManager.call(new PreparingEvent(PreparingEvent.Stage.PRE));
        eventManager.call(new PreparingEvent(PreparingEvent.Stage.POST));
    }

    @Override
    public void execute() {
        eventManager.call(new ExecutableEvent(null, new StringBuilder()));
        SpringApplication spa = new SpringApplication(AfterburnerBroadcasterApp.class);
        spa.setDefaultProperties(Collections.singletonMap("server.port", ConfigBroadcaster.API_PORT.getData()));
        spa.run();
    }

    @Override
    public void ending() {

    }

    @Override
    public ResourceExporter getExporter() {
        return exporter;
    }

    @Override
    public CliManager getCliManager() {
        return cliManager;
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

    public List<SlaveBroadcast> getSlaves() {
        return slaves;
    }

    public static AfterburnerBroadcasterApp get() {
        return instance;
    }
}

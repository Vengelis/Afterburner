package fr.vengelis.afterburner;

import fr.vengelis.afterburner.cli.CliManager;
import fr.vengelis.afterburner.events.EventManager;
import fr.vengelis.afterburner.handler.HandlerRecorder;
import fr.vengelis.afterburner.interconnection.redis.task.RedisTaskManager;
import fr.vengelis.afterburner.plugins.PluginManager;
import fr.vengelis.afterburner.providers.ProviderManager;
import fr.vengelis.afterburner.runnables.RunnableManager;
import fr.vengelis.afterburner.utils.ResourceExporter;

public interface AApp {

    void boot(HandlerRecorder handlerRecorder);

    void exportRessources();
    void loadPluginsAndProviders();
    void loadGeneralConfigs();
    void initialize();
    void preparing();
    void execute();
    void ending();

    ResourceExporter getExporter();
    CliManager getCliManager();
    EventManager getEventManager();
    ProviderManager getProviderManager();
    PluginManager getPluginManager();
    RunnableManager getRunnableManager();

}

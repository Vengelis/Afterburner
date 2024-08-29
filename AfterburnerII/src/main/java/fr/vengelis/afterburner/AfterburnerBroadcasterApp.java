package fr.vengelis.afterburner;

import fr.vengelis.afterburner.cli.CliManager;
import fr.vengelis.afterburner.events.EventManager;
import fr.vengelis.afterburner.plugins.PluginManager;
import fr.vengelis.afterburner.providers.ProviderManager;
import fr.vengelis.afterburner.runnables.RunnableManager;
import fr.vengelis.afterburner.utils.ConsoleLogger;
import fr.vengelis.afterburner.utils.ResourceExporter;

import java.io.File;
import java.io.IOException;

public class AfterburnerBroadcasterApp implements AApp{

    private final ResourceExporter exporter = new ResourceExporter();

    private final EventManager eventManager = new EventManager();
    private final ProviderManager providerManager = new ProviderManager();
    private final PluginManager pluginManager = new PluginManager();
    private final RunnableManager runnableManager = new RunnableManager();
    private final CliManager cliManager = new CliManager();

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

    }

    @Override
    public void initialize() {

    }

    @Override
    public void preparing() {

    }

    @Override
    public void execute() {

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
}

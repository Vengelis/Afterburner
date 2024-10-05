package fr.vengelis.afterburner;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import fr.vengelis.afterburner.configurations.AsConfig;
import fr.vengelis.afterburner.configurations.ConfigGeneral;
import fr.vengelis.afterburner.events.impl.common.LoadEvent;
import fr.vengelis.afterburner.exceptions.BrokenConfigException;
import fr.vengelis.afterburner.exceptions.ProviderUnknownInstructionException;
import fr.vengelis.afterburner.exceptions.UnknownProviderException;
import fr.vengelis.afterburner.providers.IAfterburnerProvider;
import fr.vengelis.afterburner.providers.ProviderInstructions;
import fr.vengelis.afterburner.utils.ConsoleLogger;
import org.yaml.snakeyaml.Yaml;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;

public class AfterburnerAppCommon {

    public static void exportRessources(AApp app) {
        try {
            app.getExporter().saveResource(new File(Afterburner.WORKING_AREA), "/config.yml", false);

            app.getExporter().createFolder(Afterburner.WORKING_AREA + File.separator + "commonfiles");
            app.getExporter().saveResource(new File(Afterburner.WORKING_AREA), "/commonfiles/mcplugins.yml", false);
            app.getExporter().saveResource(new File(Afterburner.WORKING_AREA), "/commonfiles/mcworlds.yml", false);
            app.getExporter().saveResource(new File(Afterburner.WORKING_AREA), "/commonfiles/serverfiles.yml", false);

            app.getExporter().createFolder(Afterburner.WORKING_AREA + File.separator + "plugins");

            app.getExporter().createFolder(Afterburner.WORKING_AREA + File.separator + "providers");
        } catch (IOException e) {
            ConsoleLogger.printStacktrace(e);
            System.exit(1);
        }

    }

    public static void loadProviderAndPlugin(AApp app) {
        app.getProviderManager().loadProviders(Afterburner.WORKING_AREA + File.separator + "providers");
        app.getProviderManager().getProviders().forEach((n, p) -> {
            if(p instanceof AsConfig) {
                try {
                    ConsoleLogger.printLine(Level.INFO, "Loading '" + n + "' provider configuration");
                    ((AsConfig) p).loadConfig();
                } catch (Exception e) {
                    ConsoleLogger.printStacktrace(e);
                }
            }
        });

        app.getPluginManager().loadPlugins(Afterburner.WORKING_AREA + File.separator + "plugins");
        app.getPluginManager().getCompatiblePlugins().forEach((n, p) -> {
            ConsoleLogger.printLine(Level.INFO, "Loading plugin '" + n + "'");
            try {
                p.onLoad();
            } catch (Exception e) {
                ConsoleLogger.printStacktrace(e);
            }
            if(p instanceof AsConfig) {
                try {
                    ConsoleLogger.printLine(Level.INFO, "Loading '" + n + "' plugin configuration");
                    ((AsConfig) p).loadConfig();
                } catch (Exception e) {
                    ConsoleLogger.printStacktrace(e);
                }
            }
        });
        app.getPluginManager().getAllPlugins().forEach((n, p) -> {
            try {
                p.registerCommands(app.getCliManager().getRootCommand());
            } catch (AbstractMethodError e) {
                ConsoleLogger.printStacktrace(e, "Plugin " + p.getClass().getSimpleName() + " is broken.");
            }
        });
        app.getEventManager().call(new LoadEvent());
    }

    public static void loadGeneralConfig(AApp app) {
        try {
            ConsoleLogger.printLine(Level.INFO, "Loading generals configurations");

            // General Config
            File config = new File(Afterburner.WORKING_AREA + File.separator + "config.yml");
            Yaml yaml = new Yaml();
            InputStream stm = new FileInputStream(config);

            Map<String, Object> data = yaml.load(stm);
            ConfigGeneral.CONFIG_VERSION.setData(data.get("config-version"));
            if(ConfigGeneral.CONFIG_VERSION.isDeprecated((int) ConfigGeneral.CONFIG_VERSION.getData())) {
                ConsoleLogger.printLineBox(Level.SEVERE, "config.yml was not updated. Please update your config version !");
                System.exit(1);
            }

            ConfigGeneral.READY.setData(data.get("ready"));

            Map<String, Object> paths = (Map<String, Object>) data.get("paths");
            ConfigGeneral.PATH_RENDERING_DIRECTORY.setData(paths.get("rendering-directory").toString().replace("<space>", " "));
            ConfigGeneral.PATH_TEMPLATE.setData(paths.get("templates").toString().replace("<space>", " "));
            ConfigGeneral.PATH_WORLDS_BATCHED.setData(paths.get("worlds-batched").toString().replace("<space>", " "));
            ConfigGeneral.PATH_COMMON_FILES.setData(paths.get("common-files").toString().replace("<space>", " "));

            Map<String, Object> query = (Map<String, Object>) data.get("query");
            ConfigGeneral.QUERY_AUTO_BIND.setData(query.get("host-auto-bind"));
            ConfigGeneral.QUERY_HOST.setData(query.get("host"));
            ConfigGeneral.QUERY_PORT.setData(query.get("port"));
            ConfigGeneral.QUERY_PASSWORD.setData(query.get("password"));

            Map<String, Object> queryBroadcaster = (Map<String, Object>) data.get("query-broadcaster");
            ConfigGeneral.QUERY_BROADCASTER_ENABLED.setData(queryBroadcaster.get("enabled"));
            ConfigGeneral.QUERY_BROADCASTER_HOST.setData(queryBroadcaster.get("host"));
            ConfigGeneral.QUERY_BROADCASTER_PORT.setData(queryBroadcaster.get("port"));
            ConfigGeneral.QUERY_BROADCASTER_TOKEN.setData(queryBroadcaster.get("token"));
            ConfigGeneral.QUERY_BROADCASTER_HTTPS.setData(queryBroadcaster.get("is-https"));

            Map<String, Object> redis = (Map<String, Object>) data.get("redis");
            ConfigGeneral.REDIS_ENABLED.setData(redis.get("enabled"));
            ConfigGeneral.REDIS_HOST.setData(redis.get("host"));
            ConfigGeneral.REDIS_PORT.setData(redis.get("port"));
            ConfigGeneral.REDIS_USER.setData(redis.get("user"));
            ConfigGeneral.REDIS_PASSWORD.setData(redis.get("password"));
            ConfigGeneral.REDIS_DATABASE.setData(redis.get("database"));

            List<Object> listProviders = (List<Object>) data.get("provider");
            ((HashMap<ProviderInstructions, IAfterburnerProvider>) ConfigGeneral.PROVIDERS.getData()).clear();
            for(Object provider : listProviders) {
                JsonObject jo = new Gson().fromJson(provider.toString(), JsonObject.class);
                ProviderInstructions providerInstructions = null;
                try {
                    providerInstructions = ProviderInstructions.valueOf(jo.get("instruction").getAsString().toUpperCase());
                } catch (IllegalArgumentException e) {
                    ConsoleLogger.printStacktrace(new ProviderUnknownInstructionException(e));
                    System.exit(1);
                }

                IAfterburnerProvider provider1 = app.getProviderManager().getProvider(jo.get("system").getAsString().toUpperCase());
                if(provider1 == null) {
                    ConsoleLogger.printStacktrace(new UnknownProviderException(jo.get("system").getAsString().toUpperCase()));
                    System.exit(1);
                }
                ((HashMap<ProviderInstructions, IAfterburnerProvider>) ConfigGeneral.PROVIDERS.getData()).put(providerInstructions, provider1);
            }

            if(Afterburner.VERBOSE) {
                for (ProviderInstructions providerInstructions : ProviderInstructions.values()) {
                    ConsoleLogger.printLine(Level.CONFIG, "Verbose provider result : instruction : " + providerInstructions.name() + " - result : " + app.getProviderManager().getResultInstruction(providerInstructions));
                }
            }

            if(!((boolean) ConfigGeneral.READY.getData())) {
                ConsoleLogger.printLineBox(Level.CONFIG, "Afterburner marked not ready. Stopping load process.");
                System.exit(0);
            }

        } catch (IOException e) {
            ConsoleLogger.printStacktrace(new BrokenConfigException(e));
            System.exit(1);
        }
    }

}

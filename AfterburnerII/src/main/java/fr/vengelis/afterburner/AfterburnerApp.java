package fr.vengelis.afterburner;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import fr.vengelis.afterburner.cli.CliManager;
import fr.vengelis.afterburner.commonfiles.BaseCommonFile;
import fr.vengelis.afterburner.commonfiles.CommonFilesTypeManager;
import fr.vengelis.afterburner.configurations.ConfigGeneral;
import fr.vengelis.afterburner.configurations.ConfigTemplate;
import fr.vengelis.afterburner.events.EventManager;
import fr.vengelis.afterburner.events.impl.*;
import fr.vengelis.afterburner.exceptions.BrokenConfigException;
import fr.vengelis.afterburner.exceptions.ProviderUnknownInstructionException;
import fr.vengelis.afterburner.exceptions.UnknownProviderException;
import fr.vengelis.afterburner.exceptions.WorldFolderEmptyException;
import fr.vengelis.afterburner.interconnection.socket.SocketClient;
import fr.vengelis.afterburner.interconnection.socket.SocketServer;
import fr.vengelis.afterburner.logs.LogSkipperManager;
import fr.vengelis.afterburner.logs.PrintedLog;
import fr.vengelis.afterburner.mprocess.ManagedProcess;
import fr.vengelis.afterburner.mprocess.argwrapper.ArgumentWrapperManager;
import fr.vengelis.afterburner.plugins.PluginManager;
import fr.vengelis.afterburner.configurations.AsConfig;
import fr.vengelis.afterburner.providers.IAfterburnerProvider;
import fr.vengelis.afterburner.providers.ProviderInstructions;
import fr.vengelis.afterburner.providers.ProviderManager;
import fr.vengelis.afterburner.interconnection.redis.PubSubAPI;
import fr.vengelis.afterburner.interconnection.redis.RedisConnection;
import fr.vengelis.afterburner.interconnection.redis.task.RedisTaskManager;
import fr.vengelis.afterburner.interconnection.redis.task.impl.RedisAfterburnerInfoTask;
import fr.vengelis.afterburner.interconnection.redis.task.impl.RedisCleanLogHistory;
import fr.vengelis.afterburner.interconnection.redis.task.impl.RedisKillTask;
import fr.vengelis.afterburner.interconnection.redis.task.impl.RedisReprepareTask;
import fr.vengelis.afterburner.runnables.RunnableManager;
import fr.vengelis.afterburner.utils.ConsoleLogger;
import fr.vengelis.afterburner.utils.ResourceExporter;
import org.apache.commons.io.FileUtils;
import org.yaml.snakeyaml.Yaml;

import java.io.*;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;
import java.util.logging.Level;

public class AfterburnerApp {

    private static AfterburnerApp instance;
    private final String MACHINE_NAME;
    private final String TEMPLATE;

    private final ResourceExporter exporter = new ResourceExporter();

    private final EventManager eventManager = new EventManager();
    private final ProviderManager providerManager = new ProviderManager();
    private final PluginManager pluginManager = new PluginManager();
    private final PubSubAPI pubSubAPI = new PubSubAPI();
    private final LogSkipperManager logSkipperManager = new LogSkipperManager();
    private final CommonFilesTypeManager commonFilesTypeManager = new CommonFilesTypeManager();
    private final RedisTaskManager redisTaskManager = new RedisTaskManager();
    private final RunnableManager runnableManager = new RunnableManager();
    private final CliManager cliManager = new CliManager();
    private final ArgumentWrapperManager argumentWrapperManager = new ArgumentWrapperManager();

    private boolean alreadyInit = false;

    private ManagedProcess managedProcess;
    private final UUID uniqueId = UUID.randomUUID();
    private final Map<Class<? extends BaseCommonFile>, List<Object>> commonFilesGeneral = new HashMap<>();
    private int totalTimeRunning = 0;
    private boolean reprepareEnabled = false;
    private int repreparedCount = 0;
    private final LinkedList<PrintedLog> logHistory = new LinkedList<>();
    private boolean displayOutput;
    private SocketServer socketServer;
    private SocketClient socketClient;

    public AfterburnerApp(String machineName, String templateName, boolean defaultDisplayProgramOutput) {
        instance = this;
        MACHINE_NAME = machineName;
        TEMPLATE = templateName;
        displayOutput = defaultDisplayProgramOutput;
    }

    public void exportRessources() {
        ConsoleLogger.printLine(Level.INFO, "Exporting configurations ...");
        try {
            exporter.saveResource(new File(Afterburner.WORKING_AREA), "/config.yml", false);

            exporter.createFolder(Afterburner.WORKING_AREA + File.separator + "commonfiles");
            exporter.saveResource(new File(Afterburner.WORKING_AREA), "/commonfiles/mcplugins.yml", false);
            exporter.saveResource(new File(Afterburner.WORKING_AREA), "/commonfiles/mcworlds.yml", false);
            exporter.saveResource(new File(Afterburner.WORKING_AREA), "/commonfiles/serverfiles.yml", false);

            exporter.createFolder(Afterburner.WORKING_AREA + File.separator + "templates");
            if(!Afterburner.DISABLE_TEST_TEMPLATE) exporter.saveResource(new File(Afterburner.WORKING_AREA), "/templates/example.yml", false);

            exporter.createFolder(Afterburner.WORKING_AREA + File.separator + "plugins");

            exporter.createFolder(Afterburner.WORKING_AREA + File.separator + "providers");
        } catch (IOException e) {
            ConsoleLogger.printStacktrace(e);
        }
    }

    public void loadPluginsAndProviders() {
        providerManager.loadProviders(Afterburner.WORKING_AREA + File.separator + "providers");
        providerManager.getProviders().forEach((n, p) -> {
            if(p instanceof AsConfig) {
                try {
                    ConsoleLogger.printLine(Level.INFO, "Loading '" + n + "' provider configuration");
                    ((AsConfig) p).loadConfig();
                } catch (Exception e) {
                    ConsoleLogger.printStacktrace(e);
                }
            }
        });

        pluginManager.loadPlugins(Afterburner.WORKING_AREA + File.separator + "plugins");
        pluginManager.getPlugins().forEach((n, p) -> {
            ConsoleLogger.printLine(Level.INFO, "Loading plugin '" + n + "'");
            try {
                p.onLoad();
            } catch (Exception e) {
                ConsoleLogger.printStacktrace(e);
            }
        });
    }

    public void loadGeneralConfigs() {
        try {
            ConsoleLogger.printLine(Level.INFO, "Loading configurations");

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
            ConfigGeneral.PATH_JAVA.setData(paths.get("java").toString().replace("<space>", " "));

            Map<String, Object> query = (Map<String, Object>) data.get("query");
            ConfigGeneral.QUERY_PORT.setData(query.get("port"));
            ConfigGeneral.QUERY_PASSWORD.setData(query.get("password"));

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

                IAfterburnerProvider provider1 = providerManager.getProvider(jo.get("system").getAsString().toUpperCase());
                if(provider1 == null) {
                    ConsoleLogger.printStacktrace(new UnknownProviderException(jo.get("system").getAsString().toUpperCase()));
                    System.exit(1);
                }
                ((HashMap<ProviderInstructions, IAfterburnerProvider>) ConfigGeneral.PROVIDERS.getData()).put(providerInstructions, provider1);
            }


            // Common Files
            ConsoleLogger.printLine(Level.INFO, "Checking common files");
            commonFilesGeneral.clear();
            for (Class<? extends BaseCommonFile> fileTypes : this.commonFilesTypeManager.get()) {
                ConsoleLogger.printLine(Level.INFO, " - CFM : " + fileTypes.getSimpleName());
                config = new File(Afterburner.WORKING_AREA + File.separator + "commonfiles" + File.separator + fileTypes.getSimpleName().toLowerCase() + ".yml");
                stm = new FileInputStream(config);
                data.clear();
                data = yaml.load(stm);

                List<Object> cfl = new ArrayList();
                List<Object> filesType = (List<Object>) data.get(fileTypes.getSimpleName().toLowerCase());
                for(Object file : filesType) {
                    JsonObject jo = new Gson().fromJson(file.toString(), JsonObject.class);
                    try {
                        cfl.add(fileTypes.getConstructor(JsonObject.class).newInstance(jo));
                    } catch (Exception e) {
                        ConsoleLogger.printStacktrace(new BrokenConfigException(e));
                        System.exit(1);
                    }
                }
                commonFilesGeneral.put(fileTypes, cfl);
            }

            loadTemplateConfig();

            if(Afterburner.VERBOSE_PROVIDERS) {
                for (ProviderInstructions providerInstructions : ProviderInstructions.values()) {
                    ConsoleLogger.printLine(Level.CONFIG, "Verbose provider result : instruction : " + providerInstructions.name() + " - result : " + providerManager.getResultInstruction(providerInstructions));
                }
            }

            if(!((boolean) ConfigGeneral.READY.getData())) {
                ConsoleLogger.printLineBox(Level.CONFIG, "Afterburner marked not ready. Stopping load process.");
                System.exit(0);
            }

            eventManager.call(new LoadEvent());
        } catch (IOException e) {
            ConsoleLogger.printStacktrace(new BrokenConfigException(e));
            System.exit(1);
        }
    }

    public void loadTemplateConfig() {
        // Template Config
        try {
            ConsoleLogger.printLine(Level.INFO, "Loading template configuration '" + getTemplateName() + "'");
            File config = new File(Afterburner.WORKING_AREA + File.separator + "templates" + File.separator + getTemplateName());
            InputStream stm = new FileInputStream(config);
            Yaml yaml = new Yaml();
            Map<String, Object> data = yaml.load(stm);

            ConfigTemplate.CONFIG_VERSION.setData(data.get("config-version"));
            if(ConfigTemplate.CONFIG_VERSION.isDeprecated((int) ConfigTemplate.CONFIG_VERSION.getData())) {
                ConsoleLogger.printLineBox(Level.SEVERE, getTemplateName() + ".yml was not updated. Please update your config version !");
                System.exit(1);
            }

            Map<String, Object> pattern = (Map<String, Object>) data.get("pattern");
            ConfigTemplate.PATTERN_NAME.setData(pattern.get("name"));

            List<Object> listMapPicker = (List<Object>) data.get("map-picker");
            ((ArrayList<ConfigTemplate.MapPicker>)ConfigTemplate.MAP_PICKER.getData()).clear();
            if(!listMapPicker.isEmpty()) {
                for(Object mapPicker : listMapPicker) {
                    JsonObject jo = new Gson().fromJson(mapPicker.toString(), JsonObject.class);
                    try {
                        boolean enabled = jo.get("enabled").getAsBoolean();
                        String library = jo.get("library").getAsString();
                        String renameto = jo.get("rename-to").getAsString();
                        ((ArrayList<ConfigTemplate.MapPicker>)ConfigTemplate.MAP_PICKER.getData())
                                .add(new ConfigTemplate.MapPicker(enabled, library, renameto));
                    } catch (Exception e) {
                        ConsoleLogger.printStacktrace(new BrokenConfigException(e));
                        System.exit(1);
                    }
                }
            }

            List<Object> listCommonFiles = (List<Object>) data.get("common-files");
            ((Map<Class<? extends BaseCommonFile>, List<Object>>)ConfigTemplate.COMMON_FILES.getData()).clear();
            if(!listCommonFiles.isEmpty()) {
                for(Object commonFileNode : listCommonFiles) {
                    JsonObject jo = new Gson().fromJson(commonFileNode.toString(), JsonObject.class);
                    try {
                        Class<? extends BaseCommonFile> clazz = commonFilesTypeManager.get(jo.get("name").getAsString());
                        List<Object> cfla = new ArrayList<>();
                        for(Object cfs : new Gson().fromJson(jo.get("list"), List.class)) {
                            boolean finded = false;
                            for (Object o : commonFilesGeneral.get(clazz)) {
                                if(((BaseCommonFile) o).getName().equalsIgnoreCase(cfs.toString())) {
                                    cfla.add(o);
                                    finded = true;
                                    ConsoleLogger.printLine(Level.INFO, " - Requested common file of type '" + clazz.getSimpleName() + "' with name '" + cfs + "' added.");
                                }
                            }
                            if(!finded) ConsoleLogger.printLine(Level.WARNING, " - Requested common file of type '" + clazz.getSimpleName() + "' with name '" + cfs + "' doesn't exist.");
                        }
                        ((Map<Class<? extends BaseCommonFile>, List<Object>>)ConfigTemplate.COMMON_FILES.getData()).put(clazz, cfla);
                    } catch (Exception e) {
                        ConsoleLogger.printStacktrace(new BrokenConfigException(e));
                        System.exit(1);
                    }
                }
            }

            Map<String, Object> exec = (Map<String, Object>) data.get("executable");
            ConfigTemplate.EXECUTABLE_TYPE.setData(exec.get("type"));
            ConfigTemplate.EXECUTABLE_MIN_RAM.setData(exec.get("min-ram"));
            ConfigTemplate.EXECUTABLE_MAX_RAM.setData(exec.get("max-ram"));
            ConfigTemplate.EXECUTABLE_NAME.setData(exec.get("exec"));
            List<String> moreargs = (List<String>) exec.get("more-args");
            ((List<String>)ConfigTemplate.EXECUTABLE_MORE_ARGS.getData()).clear();
            for (String s : moreargs) {
                ((List<String>)ConfigTemplate.EXECUTABLE_MORE_ARGS.getData()).add(s);
            }

            Map<String, Object> save = (Map<String, Object>) data.get("save");
            ConfigTemplate.SAVE_ENABLED.setData(save.get("enabled"));
            List<Object> listSaveWorld = (List<Object>) save.get("maps-to-save");
            ((HashMap<String, String>) ConfigTemplate.SAVE_WORLDS.getData()).clear();
            if(!listSaveWorld.isEmpty()) {
                for(Object o : listSaveWorld) {
                    JsonObject jsonObject = new Gson().fromJson(o.toString(), JsonObject.class);
                    ((HashMap<String, String>) ConfigTemplate.SAVE_WORLDS.getData())
                            .put(jsonObject.get("world-name").getAsString(), jsonObject.get("to-map-picker").getAsString());
                }
            }
        } catch (IOException e) {
            ConsoleLogger.printStacktrace(new BrokenConfigException(e));
            System.exit(1);
        }
    }

    public void initialize() {
        if(alreadyInit) return;
        alreadyInit = true;
        ConsoleLogger.printLine(Level.INFO, "Initializing");

//        socketClient = new SocketClient("localhost", (Integer) ConfigGeneral.QUERY_PORT.getData());
//        try {
//            socketClient.start();
//        } catch (IOException e) {
//            if(socketServer != null) socketServer.stop();
//            ConsoleLogger.printStacktrace(e, "Query server console was not initialized. The service has therefore been stopped.");
//        }

        if((boolean) ConfigGeneral.REDIS_ENABLED.getData()) {
            RedisConnection.create();
            pubSubAPI.tryHelloWorld();

            RegisterRedisTaskEvent event = new RegisterRedisTaskEvent();
            eventManager.call(event);

            cliManager.init();

            redisTaskManager.register(
                    new RedisKillTask(),
                    new RedisAfterburnerInfoTask(),
                    new RedisReprepareTask(),
                    new RedisCleanLogHistory());

            event.getRegisterTasks().forEach(redisTaskManager::register);

            pubSubAPI.psubscribe("AFTERBURNER-*", ((pattern, channel, message) -> {
                getRedisTaskManager().getRedisTasks().stream()
                        .filter(redisTask -> redisTask.getChannel().equalsIgnoreCase(channel))
                        .forEach(redisTask -> redisTask.run(message));
            }));
        }

        eventManager.call(new InitializeEvent());
    }

    public void preparing() {
        ConsoleLogger.printLine(Level.INFO, "Preparing");
        PreparingEvent event = new PreparingEvent(PreparingEvent.Stage.PRE);
        eventManager.call(event);
        if(!event.isCancelled()) {
            if(!event.getSkipStep().contains(PreparingEvent.PreparingStep.CLEANING_RENDERING_FOLDER)) {
                ConsoleLogger.printLine(Level.INFO, "Cleaning rendering directory");
                try {
                    FileUtils.cleanDirectory(new File(ConfigGeneral.PATH_RENDERING_DIRECTORY.getData().toString()));
                } catch (IOException e) {
                    ConsoleLogger.printStacktrace(e, "There was a problem cleaning the render folder");
                }
            }
            if(!event.getSkipStep().contains(PreparingEvent.PreparingStep.COPY_TEMPLATE)) {
                ConsoleLogger.printLine(Level.INFO, "Copying template into rendering directory");
                try {
                    FileUtils.copyDirectory(
                            new File(ConfigGeneral.PATH_TEMPLATE.getData() + File.separator + ConfigTemplate.PATTERN_NAME.getData()),
                            new File((String) ConfigGeneral.PATH_RENDERING_DIRECTORY.getData()));
                } catch (IOException e) {
                    ConsoleLogger.printStacktrace(e, "There was a problem copying template to render folder");
                }
            }
            if(!event.getSkipStep().contains(PreparingEvent.PreparingStep.COPY_COMMON_FILES)) {
                ConsoleLogger.printLine(Level.INFO, "Copying commons files into rendering directory");
                Map<Class<? extends BaseCommonFile>, List<Object>> commonFilesData = (HashMap<Class<? extends BaseCommonFile>, List<Object>>) ConfigTemplate.COMMON_FILES.getData();
                for(Class<? extends BaseCommonFile> type : commonFilesData.keySet()) {
                    ConsoleLogger.printLine(Level.INFO, " - Copying type '" + type.getSimpleName() + "'");
                    for(Object cf : commonFilesData.get(type)) {
                        BaseCommonFile bcf = (BaseCommonFile) cf;
                        if(bcf.isEnabled()) {
                            ConsoleLogger.printLine(Level.INFO, "    | " + bcf.getName() + " copying");
                            try {
                                bcf.copy();
                            } catch (IOException e) {
                                ConsoleLogger.printStacktrace(e);
                            }
                        } else {
                            ConsoleLogger.printLine(Level.INFO, "    | " + bcf.getName() + " skipped");
                        }
                    }
                }
            }

            if(!event.getSkipStep().contains(PreparingEvent.PreparingStep.MAP_PICKER)) {
                ConsoleLogger.printLine(Level.INFO, "Checking map picker");
                for (ConfigTemplate.MapPicker picker : ((ArrayList<ConfigTemplate.MapPicker>) ConfigTemplate.MAP_PICKER.getData())) {
                    if(picker.isEnabled()) {
                        Random rand = new Random();
                        File[] worldLib = new File(ConfigGeneral.PATH_WORLDS_BATCHED.getData() + File.separator + picker.getLibrary()).listFiles();
                        if(worldLib == null || worldLib.length == 0) {
                            ConsoleLogger.printStacktrace(new WorldFolderEmptyException(picker.getLibrary()));
                            continue;
                        }
                        File selectedWorld = worldLib[rand.nextInt(worldLib.length)];
                        PickWorldEvent event1 = new PickWorldEvent(selectedWorld);
                        eventManager.call(event1);
                        if(!event1.isCancelled()) {
                            try {
                                FileUtils.copyDirectory(event1.getPickedWorld(), new File(ConfigGeneral.PATH_RENDERING_DIRECTORY.getData().toString() + File.separator + picker.getRenameTo()));
                                ConsoleLogger.printLine(Level.INFO, " - Adding " + event1.getPickedWorld().getName() + "(final name : " + picker.getRenameTo() + ")");
                            } catch (IOException e) {
                                ConsoleLogger.printStacktrace(e);
                            }
                        }
                    }
                }
            }
        }
        eventManager.call(new PreparingEvent(PreparingEvent.Stage.POST));
    }

    public void execute() {
        ConsoleLogger.printLine(Level.INFO, "Preparing executable");

        managedProcess = new ManagedProcess(uniqueId);
        if(!managedProcess.execute()) {
            ConsoleLogger.printLine(Level.WARNING, "A problem occurred before or during program execution. Afterburner forced to stop.");
            System.exit(1);
        }

        eventManager.call(new ExecutableEvent());
    }

    public void ending() {
        eventManager.call(new EndEvent());
        if((boolean) ConfigTemplate.SAVE_ENABLED.getData()) {
            ConsoleLogger.printLine(Level.INFO, "Map saver enabled, saving maps");
            ((Map<String, String>) ConfigTemplate.SAVE_WORLDS.getData()).forEach((map, lib) -> {
                File rendering = new File(ConfigGeneral.PATH_RENDERING_DIRECTORY.getData().toString());
                String mapDestName = "saved_" + System.currentTimeMillis();
                SavingMapEvent event = new SavingMapEvent(map, lib, mapDestName);
                eventManager.call(event);
                if(!event.isCancelled()) {
                    try {
                        String dest = ConfigGeneral.PATH_WORLDS_BATCHED.getData() + File.separator + event.getDestination();
                        FileUtils.copyDirectory(rendering.toPath().resolve(event.getInitialName()).toFile(),
                                new File(dest + File.separator + event.getModifiedName()));
                        ConsoleLogger.printLine(Level.INFO, "Saving generated map into " + event.getDestination() + " with name " + event.getModifiedName());
                    } catch (IOException e) {
                        ConsoleLogger.printStacktrace(e);
                    }
                }
            });
        } else {
            ConsoleLogger.printLine(Level.INFO, "Map saver disabled");
        }
        ConsoleLogger.printLine(Level.INFO, "Job ended, goodby world :D");
    }

    public boolean killTask() {
        return this.killTask("Reason not specified");
    }

    public boolean killTask(String message) {
        return this.killTask(message, true);
    }

    public boolean killTask(String message, Boolean waitingProcess) {
        JsonObject msg = new JsonObject();
        msg.addProperty("message", message);
        KillTaskEvent event = new KillTaskEvent(msg);
        eventManager.call(event);
        if(!event.isCancelled()) {
            if(managedProcess.getProcess().isPresent()) {

                if(waitingProcess) {
                    ConsoleLogger.printLine(Level.INFO, "Shutting down with wainting process");
                    managedProcess.sendCommandToProcess("stop");
                } else {
                    managedProcess.getProcess().get().destroyForcibly();
                }
                ConsoleLogger.printLine(Level.WARNING, "Kill task received, shutting down managed process (Reason : " + message + ").");
                if(event.isShutdownAfterburner()) {
                    ConsoleLogger.printLine(Level.WARNING, "Shutting down afterburner.");
                    System.exit(0);
                }
                return true;
            } else {
                ConsoleLogger.printLine(Level.SEVERE, "Process is not started !");
            }
        }
        return false;
    }

    public static AfterburnerApp get() {
        return instance;
    }

    public String getMachineName() {
        return MACHINE_NAME;
    }

    public String getTemplateName() {
        return TEMPLATE;
    }

    public EventManager getEventManager() {
        return eventManager;
    }

    public ProviderManager getProviderManager() {
        return providerManager;
    }

    public PluginManager getPluginManager() {
        return pluginManager;
    }

    public ResourceExporter getExporter() {
        return exporter;
    }

    public PubSubAPI getPubSubAPI() {
        return pubSubAPI;
    }

    public LogSkipperManager getLogSkipperManager() {
        return logSkipperManager;
    }

    public ManagedProcess getManagedProcess() {
        return managedProcess;
    }

    public int getTotalTimeRunning() {
        return totalTimeRunning;
    }

    public void setTotalTimeRunning(int totalTimeRunning) {
        this.totalTimeRunning = totalTimeRunning;
    }

    public boolean isReprepareEnabled() {
        return reprepareEnabled;
    }

    public void setReprepareEnabled(boolean reprepareEnabled) {
        this.reprepareEnabled = reprepareEnabled;
    }

    public int getRepreparedCount() {
        return repreparedCount;
    }

    public void setRepreparedCount(int repreparedCount) {
        this.repreparedCount = repreparedCount;
    }

    public LinkedList<PrintedLog> getLogHistory() {
        return logHistory;
    }

    public CommonFilesTypeManager getCommonFilesTypeManager() {
        return commonFilesTypeManager;
    }

    public Map<Class<? extends BaseCommonFile>, List<Object>> getActualCommonFilesLoaded() {
        return new HashMap<>(this.commonFilesGeneral);
    }

    public Optional<BaseCommonFile> computeCommonFileWithName(String name) {
        AtomicReference<BaseCommonFile> bcf = new AtomicReference<>(null);
        AfterburnerApp.get().getActualCommonFilesLoaded().values().forEach(lo -> lo.forEach(cf -> {
            if(((BaseCommonFile) cf).getName().equalsIgnoreCase(name)) bcf.set((BaseCommonFile) cf);
        }));
        return Optional.ofNullable(bcf.get());
    }

    public RunnableManager getRunnableManager() {
        return runnableManager;
    }

    public RedisTaskManager getRedisTaskManager() {
        return redisTaskManager;
    }

    public CliManager getCliManager() {
        return cliManager;
    }

    public UUID getUniqueId() {
        return uniqueId;
    }

    public boolean isDisplayOutput() {
        return displayOutput;
    }

    public void setDisplayOutput(boolean displayOutput) {
        this.displayOutput = displayOutput;
    }

    public ArgumentWrapperManager getArgumentWrapperManager() {
        return argumentWrapperManager;
    }

    public void setSocketServer(SocketServer s) {
        if(socketServer == null) socketServer = s;
    }

    public SocketServer getSocketServer() {
        return socketServer;
    }

    public SocketClient getSocketClient() {
        return socketClient;
    }
}

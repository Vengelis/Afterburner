package fr.vengelis.afterburner;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import fr.vengelis.afterburner.cli.CliManager;
import fr.vengelis.afterburner.commonfiles.BaseCommonFile;
import fr.vengelis.afterburner.commonfiles.CommonFilesTypeManager;
import fr.vengelis.afterburner.configurations.ConfigGeneral;
import fr.vengelis.afterburner.configurations.ConfigTemplate;
import fr.vengelis.afterburner.events.EventManager;
import fr.vengelis.afterburner.events.impl.common.EndEvent;
import fr.vengelis.afterburner.events.impl.common.ExecutableEvent;
import fr.vengelis.afterburner.events.impl.common.InitializeEvent;
import fr.vengelis.afterburner.events.impl.common.PreparingEvent;
import fr.vengelis.afterburner.events.impl.slave.*;
import fr.vengelis.afterburner.exceptions.BrokenConfigException;
import fr.vengelis.afterburner.exceptions.WorldFolderEmptyException;
import fr.vengelis.afterburner.interconnection.socket.broadcaster.SlaveBroadcast;
import fr.vengelis.afterburner.interconnection.socket.broadcaster.BroadcasterWebApiHandler;
import fr.vengelis.afterburner.interconnection.socket.system.SocketServer;
import fr.vengelis.afterburner.logs.LogSkipperManager;
import fr.vengelis.afterburner.logs.PrintedLog;
import fr.vengelis.afterburner.mprocess.ManagedProcess;
import fr.vengelis.afterburner.mprocess.argwrapper.ArgumentWrapperManager;
import fr.vengelis.afterburner.plugins.PluginManager;
import fr.vengelis.afterburner.providers.ProviderManager;
import fr.vengelis.afterburner.interconnection.redis.PubSubAPI;
import fr.vengelis.afterburner.interconnection.redis.RedisConnection;
import fr.vengelis.afterburner.interconnection.redis.task.RedisTaskManager;
import fr.vengelis.afterburner.interconnection.redis.task.impl.RedisAfterburnerInfoTask;
import fr.vengelis.afterburner.interconnection.redis.task.impl.RedisCleanLogHistory;
import fr.vengelis.afterburner.interconnection.redis.task.impl.RedisKillTask;
import fr.vengelis.afterburner.interconnection.redis.task.impl.RedisReprepareTask;
import fr.vengelis.afterburner.runnables.RunnableManager;
import fr.vengelis.afterburner.runnables.impl.slave.SBRunnable;
import fr.vengelis.afterburner.utils.ConsoleLogger;
import fr.vengelis.afterburner.utils.ResourceExporter;
import org.apache.commons.io.FileUtils;
import org.springframework.http.HttpMethod;
import org.yaml.snakeyaml.Yaml;

import java.io.*;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import java.util.logging.Level;

public class AfterburnerSlaveApp implements AApp {

    private static AfterburnerSlaveApp instance;
    private final String MACHINE_NAME;
    private final String TEMPLATE;

    private AfterburnerState state = AfterburnerState.NOT_STARTED;

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
    private final SocketServer socketServer = new SocketServer();

    private boolean alreadyInit = false;

    private ManagedProcess managedProcess;
    private final UUID uniqueId = UUID.randomUUID();
    private final Map<Class<? extends BaseCommonFile>, List<Object>> commonFilesGeneral = new HashMap<>();
    private int totalTimeRunning = 0;
    private boolean reprepareEnabled = false;
    private int repreparedCount = 0;
    private final LinkedList<PrintedLog> logHistory = new LinkedList<>();
    private boolean displayOutput;
    private SlaveBroadcast slaveBroadcast;
    private BroadcasterWebApiHandler broadcasterWebApiHandler;

    public AfterburnerSlaveApp(String machineName, String templateName, boolean defaultDisplayProgramOutput) {
        instance = this;
        MACHINE_NAME = machineName;
        TEMPLATE = templateName;
        displayOutput = defaultDisplayProgramOutput;
    }

    @Override
    public void exportRessources() {
        state = AfterburnerState.EXPORTING;
        ConsoleLogger.printLine(Level.INFO, "Exporting configurations ...");
        try {
            AfterburnerAppCommon.exportRessources(this);
            exporter.createFolder(Afterburner.WORKING_AREA + File.separator + "templates");
            if(!Afterburner.DISABLE_TEST_TEMPLATE) exporter.saveResource(new File(Afterburner.WORKING_AREA), "/templates/example.yml", false);
        } catch (IOException e) {
            ConsoleLogger.printStacktrace(e);
            System.exit(1);
        }
    }

    @Override
    public void loadPluginsAndProviders() {
        state = AfterburnerState.LOADING;
        AfterburnerAppCommon.loadProviderAndPlugin(this);
    }

    @Override
    public void loadGeneralConfigs() {
        ConsoleLogger.printLine(Level.INFO, "Loading configurations");

        AfterburnerAppCommon.loadGeneralConfig(this);

        try {
            // Common Files
            ConsoleLogger.printLine(Level.INFO, "Checking common files");
            commonFilesGeneral.clear();
            for (Class<? extends BaseCommonFile> fileTypes : this.commonFilesTypeManager.get()) {
                ConsoleLogger.printLine(Level.INFO, " - CFM : " + fileTypes.getSimpleName());

                File config = new File(Afterburner.WORKING_AREA + File.separator + "commonfiles" + File.separator + fileTypes.getSimpleName().toLowerCase() + ".yml");
                InputStream stm = new FileInputStream(config);
                Yaml yaml = new Yaml();
                Map<String, Object> data = yaml.load(stm);

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
        } catch (IOException e) {
            ConsoleLogger.printStacktrace(new BrokenConfigException(e));
            System.exit(1);
        }
        loadTemplateConfig();
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

    @Override
    public void initialize() {
        if(alreadyInit) return;
        alreadyInit = true;

        state = AfterburnerState.INITIALIZING;
        ConsoleLogger.printLine(Level.INFO, "Initializing");

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

        if((boolean) ConfigGeneral.QUERY_BROADCASTER_ENABLED.getData()) {
            slaveBroadcast = new SlaveBroadcast(
                    uniqueId,
                    MACHINE_NAME,
                    ConfigGeneral.QUERY_BROADCASTER_HOST.getData().toString(),
                    (Integer) ConfigGeneral.QUERY_BROADCASTER_PORT.getData()
            );
            slaveBroadcast.setAvailable(false);
            slaveBroadcast.setLastContact(Instant.now().getEpochSecond());

            broadcasterWebApiHandler = new BroadcasterWebApiHandler(
                    ((boolean) ConfigGeneral.QUERY_BROADCASTER_HTTPS.getData() ? "https" : "http") +
                            "://" +
                            ConfigGeneral.QUERY_BROADCASTER_HOST.getData().toString() + ":" +
                            ConfigGeneral.QUERY_BROADCASTER_PORT.getData().toString(),
                    ConfigGeneral.QUERY_BROADCASTER_TOKEN.getData().toString(),
                    (short) 1000
            );

            broadcasterWebApiHandler.sendRequest(slaveBroadcast, BroadcasterWebApiHandler.Action.ADD, HttpMethod.POST);
            runnableManager.runTaskTimer(new SBRunnable(), 0L, 1L, TimeUnit.SECONDS);
        }

        eventManager.call(new InitializeEvent());
    }

    @Override
    public void preparing() {
        state = AfterburnerState.PREPARING;
        ConsoleLogger.printLine(Level.INFO, "Preparing");
        PreparingEvent event = new PreparingEvent(PreparingEvent.Stage.PRE);
        eventManager.call(event);
        if(!event.isCancelled()) {
            if(!event.getSkipStep().contains(PreparingEvent.SlavePreparingStep.CLEANING_RENDERING_FOLDER)) {
                ConsoleLogger.printLine(Level.INFO, "Cleaning rendering directory");
                try {
                    FileUtils.cleanDirectory(new File(ConfigGeneral.PATH_RENDERING_DIRECTORY.getData().toString()));
                } catch (IOException e) {
                    ConsoleLogger.printStacktrace(e, "There was a problem cleaning the render folder");
                }
            }
            if(!event.getSkipStep().contains(PreparingEvent.SlavePreparingStep.COPY_TEMPLATE)) {
                ConsoleLogger.printLine(Level.INFO, "Copying template into rendering directory");
                try {
                    FileUtils.copyDirectory(
                            new File(ConfigGeneral.PATH_TEMPLATE.getData() + File.separator + ConfigTemplate.PATTERN_NAME.getData()),
                            new File((String) ConfigGeneral.PATH_RENDERING_DIRECTORY.getData()));
                } catch (IOException e) {
                    ConsoleLogger.printStacktrace(e, "There was a problem copying template to render folder");
                }
            }
            if(!event.getSkipStep().contains(PreparingEvent.SlavePreparingStep.COPY_COMMON_FILES)) {
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

            if(!event.getSkipStep().contains(PreparingEvent.SlavePreparingStep.MAP_PICKER)) {
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

    @Override
    public void execute() {
        state = AfterburnerState.EXECUTING;
        ConsoleLogger.printLine(Level.INFO, "Preparing executable");

        managedProcess = new ManagedProcess(uniqueId);
        if(!managedProcess.execute()) {
            ConsoleLogger.printLine(Level.WARNING, "A problem occurred before or during program execution. Afterburner forced to stop.");
            System.exit(1);
        }

        eventManager.call(new ExecutableEvent());
    }

    @Override
    public void ending() {
        state = AfterburnerState.ENDING;
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
        shutdown();
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
                    shutdown();
                }
                return true;
            } else {
                ConsoleLogger.printLine(Level.SEVERE, "Process is not started !");
            }
        }
        return false;
    }

    private void shutdown() {
        eventManager.call(new ShutdownEvent());
        socketServer.stop();
        System.exit(0);
    }

    public static AfterburnerSlaveApp get() {
        return instance;
    }

    public String getMachineName() {
        return MACHINE_NAME;
    }

    public String getTemplateName() {
        return TEMPLATE;
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
        AfterburnerSlaveApp.get().getActualCommonFilesLoaded().values().forEach(lo -> lo.forEach(cf -> {
            if(((BaseCommonFile) cf).getName().equalsIgnoreCase(name)) bcf.set((BaseCommonFile) cf);
        }));
        return Optional.ofNullable(bcf.get());
    }

    @Override
    public RunnableManager getRunnableManager() {
        return runnableManager;
    }

    public RedisTaskManager getRedisTaskManager() {
        return redisTaskManager;
    }

    @Override
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

    public SocketServer getSocketServer() {
        return socketServer;
    }

    public SlaveBroadcast getSlaveBroadcast() {
        return slaveBroadcast;
    }

    public BroadcasterWebApiHandler getBroadcasterWebApiHandler() {
        return broadcasterWebApiHandler;
    }

    public AfterburnerState getState() {
        return state;
    }
}

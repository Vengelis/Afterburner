package fr.vengelis.afterburner.interconnection.redis.task;

import fr.vengelis.afterburner.utils.ConsoleLogger;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

public class RedisTaskManager {

    private final List<AbstractRedisTask> redisTasks = new ArrayList<>();

    public List<AbstractRedisTask> getRedisTasks() {
        return redisTasks;
    }

    public void register(AbstractRedisTask task) {
        redisTasks.add(task);
        ConsoleLogger.printVerbose(Level.INFO, " - Registered redis instruction task " + task.getClass().getSimpleName() + " (channel : " + task.getChannel() + ")");
    }

    public void register(AbstractRedisTask... task) {
        for (AbstractRedisTask t : task) {
            register(t);
        }
    }

}

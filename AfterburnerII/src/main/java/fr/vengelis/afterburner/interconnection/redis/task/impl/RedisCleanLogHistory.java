package fr.vengelis.afterburner.interconnection.redis.task.impl;

import fr.vengelis.afterburner.interconnection.instructions.impl.CleanLogHistoryInstruction;
import fr.vengelis.afterburner.interconnection.redis.task.AbstractRedisTask;

public class RedisCleanLogHistory extends AbstractRedisTask {

    public RedisCleanLogHistory() {
        super("AFTERBURNER-CLEAN-LOGHISTORY");
    }

    @Override
    public void run(String message) {
        new CleanLogHistoryInstruction().execute();
    }
}

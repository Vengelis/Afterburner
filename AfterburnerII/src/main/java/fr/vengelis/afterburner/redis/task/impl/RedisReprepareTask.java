package fr.vengelis.afterburner.redis.task.impl;

import fr.vengelis.afterburner.interconnection.instructions.impl.ReprepareInstruction;
import fr.vengelis.afterburner.redis.task.AbstractRedisTask;

public class RedisReprepareTask extends AbstractRedisTask {

    public RedisReprepareTask() {
        super("AFTERBURNER-REPREPARETASK");
    }

    @Override
    public void run(String message) {
        new ReprepareInstruction(message).execute();
    }
}

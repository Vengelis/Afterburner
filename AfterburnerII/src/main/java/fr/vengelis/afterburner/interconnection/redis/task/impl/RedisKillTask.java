package fr.vengelis.afterburner.interconnection.redis.task.impl;

import fr.vengelis.afterburner.interconnection.instructions.impl.KillTaskIntruction;
import fr.vengelis.afterburner.interconnection.redis.task.AbstractRedisTask;

public class RedisKillTask extends AbstractRedisTask {

    public RedisKillTask() {
        super("AFTERBURNER-KILLTASK");
    }

    @Override
    public void run(String message) {
        new KillTaskIntruction(
                message,
                KillTaskIntruction.InputType.JSON_MACHINE_NAME_STRING,
                "lambda redis instruction")
                .execute();
    }
}

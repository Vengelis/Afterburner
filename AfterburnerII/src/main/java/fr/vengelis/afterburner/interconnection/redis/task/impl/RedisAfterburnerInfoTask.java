package fr.vengelis.afterburner.interconnection.redis.task.impl;

import com.google.gson.Gson;
import fr.vengelis.afterburner.AfterburnerSlaveApp;
import fr.vengelis.afterburner.interconnection.instructions.impl.GetAtbInfosInstruction;
import fr.vengelis.afterburner.interconnection.redis.task.AbstractRedisTask;

public class RedisAfterburnerInfoTask extends AbstractRedisTask {

    public RedisAfterburnerInfoTask() {
        super("AFTERBURNER-GETINFO");
    }

    @Override
    public void run(String message) {
        AfterburnerSlaveApp.get().getPubSubAPI().publish("AFTERBURNER-RETURNINFO", new Gson().toJson(new GetAtbInfosInstruction().execute()));
    }
}

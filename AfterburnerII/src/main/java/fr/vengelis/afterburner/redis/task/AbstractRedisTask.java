package fr.vengelis.afterburner.redis.task;

public abstract class AbstractRedisTask {

    private final String channel;

    public AbstractRedisTask(String channel) {
        this.channel = channel;
    }

    public String getChannel() {
        return channel;
    }

    public abstract void run(String message);

}

package fr.vengelis.afterburner.runnables;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.*;

public class RunnableManager {

    private final ScheduledExecutorService executor = Executors.newScheduledThreadPool(10);
    private final Map<UUID, ScheduledFuture<?>> tasks = new HashMap<>();

    public void runTask(Runnable runnable) {
        executor.submit(runnable);
    }

    public void runTaskTimer(Runnable runnable, long delay, long period, TimeUnit unit) {
        ScheduledFuture<?> sf = executor.scheduleAtFixedRate(runnable, delay, period, unit);
        tasks.put(UUID.randomUUID(), sf);
    }

    public void runTaskLater(Runnable runnable, long delay, TimeUnit unit) {
        ScheduledFuture<?> sf = executor.schedule(runnable, delay, unit);
        tasks.put(UUID.randomUUID(), sf);
    }

    public void cancelTask(UUID id) {
        ScheduledFuture<?> task = tasks.get(id);
        if(task != null) {
            task.cancel(true);
            tasks.remove(id);
        }
    }

    public void cancelAllTasks() {
        tasks.forEach((id, task) -> {
            task.cancel(true);
        });
        tasks.clear();
    }

    public void shutdown() {
        executor.shutdown();
    }

}

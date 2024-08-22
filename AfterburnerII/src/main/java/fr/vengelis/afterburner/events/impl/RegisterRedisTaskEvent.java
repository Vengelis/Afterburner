package fr.vengelis.afterburner.events.impl;

import fr.vengelis.afterburner.events.AbstractEvent;
import fr.vengelis.afterburner.redis.task.AbstractRedisTask;

import java.util.ArrayList;
import java.util.List;

/**
 * This class represents the RegisterRedisTaskEvent in the application.
 * It extends the AbstractEvent class, which means it inherits all of its methods and properties.
 * <p>
 * The RegisterRedisTaskEvent class is used to register tasks in the RedisManager.
 * <p>
 * It has one property:
 * <ul>
 *     <li>registerTasks: a List of AbstractRedisTask that represents the tasks to be registered.</li>
 * </ul>
 * It provides one constructor:
 * <ul>
 *     <li>RegisterRedisTaskEvent(): This constructor initializes the 'registerTasks' property as an empty ArrayList.</li>
 * </ul>
 * It also provides one public method:
 * <ul>
 *     <li>getRegisterTasks(): This method returns the current state of the 'registerTasks' property.</li>
 * </ul>
 */
public class RegisterRedisTaskEvent extends AbstractEvent {

    private final List<AbstractRedisTask> registerTasks = new ArrayList<>();

    /**
     * This method returns the current state of the 'registerTasks' property.
     * @return List of AbstractRedisTask
     */
    public List<AbstractRedisTask> getRegisterTasks() {
        return registerTasks;
    }
}

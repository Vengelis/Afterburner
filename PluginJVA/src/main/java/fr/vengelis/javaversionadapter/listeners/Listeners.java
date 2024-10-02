package fr.vengelis.javaversionadapter.listeners;

import fr.vengelis.afterburner.events.EventHandler;
import fr.vengelis.afterburner.events.Listener;
import fr.vengelis.afterburner.events.impl.common.ExecutableEvent;
import fr.vengelis.afterburner.mprocess.argwrapper.IArgWrapper;
import fr.vengelis.afterburner.mprocess.argwrapper.impl.JavaArguments;
import fr.vengelis.afterburner.utils.ConsoleLogger;
import fr.vengelis.javaversionadapter.AdapterPlugin;
import fr.vengelis.javaversionadapter.adapter.Adapter;

import java.util.logging.Level;

public class Listeners implements Listener {

    @EventHandler
    public void onExecEvent(ExecutableEvent event) {
        if(event.getStage().equals(ExecutableEvent.Stage.POST))
            return;
        IArgWrapper wrapper = event.getWrapper();
        if(!(wrapper instanceof JavaArguments))
            return;

        String[] line = event.getCmdline().toString().split(" ");
        for (Adapter adapter : AdapterPlugin.get().getAdapterManager().getAdapters().values()) {
            if(adapter.matchName()) {
                line[0] = adapter.getExec();
                ConsoleLogger.printLine(Level.INFO, "JVA : applying adapter " + adapter.getName());
            }
        }
        event.setCmdline(new StringBuilder(String.join(" ", line)));
    }

}

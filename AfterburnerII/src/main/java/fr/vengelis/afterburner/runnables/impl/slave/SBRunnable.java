package fr.vengelis.afterburner.runnables.impl.slave;

import fr.vengelis.afterburner.AfterburnerSlaveApp;
import fr.vengelis.afterburner.AfterburnerState;
import fr.vengelis.afterburner.interconnection.socket.broadcaster.BroadcasterWebApiHandler;
import fr.vengelis.afterburner.interconnection.socket.broadcaster.SlaveBroadcast;
import org.springframework.http.HttpMethod;

import java.time.Instant;

public class SBRunnable implements Runnable{

    boolean shutdown = false;

    @Override
    public void run() {
        AfterburnerState s = AfterburnerSlaveApp.get().getState();
        if(s.equals(AfterburnerState.ENDING) && !shutdown) {
            shutdown = true;
            SlaveBroadcast sb = AfterburnerSlaveApp.get().getSlaveBroadcast();
            sb.setLastContact(Instant.now().getEpochSecond());
            sb.setAvailable(false);
            AfterburnerSlaveApp.get().getBroadcasterWebApiHandler().sendRequest(
                    sb,
                    BroadcasterWebApiHandler.Action.REMOVE,
                    HttpMethod.DELETE
            );
        } else if(!s.equals(AfterburnerState.ENDING) && !shutdown) {
            SlaveBroadcast sb = AfterburnerSlaveApp.get().getSlaveBroadcast();
            sb.setLastContact(Instant.now().getEpochSecond());
            AfterburnerSlaveApp.get().getBroadcasterWebApiHandler().sendRequest(
                    sb,
                    BroadcasterWebApiHandler.Action.UPDATE,
                    HttpMethod.POST
            );
        }
    }

}

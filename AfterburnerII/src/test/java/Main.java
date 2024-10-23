import fr.vengelis.afterburner.interconnection.socket.broadcaster.BroadcasterWebApiHandler;
import fr.vengelis.afterburner.interconnection.socket.broadcaster.SlaveBroadcast;
import org.springframework.http.HttpMethod;

import java.time.Instant;
import java.util.UUID;

public class Main {
    public static void main(String[] args) {

        System.out.println(System.getProperty("os.name"));

//        BroadcasterWebApiHandler h = new BroadcasterWebApiHandler(
//                "http://localhost:46799",
//                "bearer-token",
//                (short) 1000
//        );
//        SlaveBroadcast sb = new SlaveBroadcast(
//                UUID.randomUUID(),
//                "Test",
//                "localhost",
//                46798
//        );
//        sb.setLastContact(Instant.now().getEpochSecond());
//        sb.setAvailable(true);
////        h.sendRequest(sb, Action.ADD, HttpMethod.POST);
//        h.sendRequest(sb, BroadcasterWebApiHandler.Action.LIST, HttpMethod.GET);
    }
}

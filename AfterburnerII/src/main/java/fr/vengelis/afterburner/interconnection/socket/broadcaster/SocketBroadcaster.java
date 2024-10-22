package fr.vengelis.afterburner.interconnection.socket.broadcaster;

import com.google.gson.JsonSyntaxException;
import fr.vengelis.afterburner.AfterburnerBroadcasterApp;
import fr.vengelis.afterburner.configurations.ConfigBroadcaster;
import fr.vengelis.afterburner.events.impl.broadcaster.PerformActionEvent;
import fr.vengelis.afterburner.utils.ConsoleLogger;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.logging.Level;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/client")
public class SocketBroadcaster {

    @GetMapping("/all")
    public ResponseEntity<List<String>> getAllClients(@RequestHeader("Authorization") String token) {
        if (!isValidToken(token)) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }

        List<String> jsonSlaves = AfterburnerBroadcasterApp.get().getSlaves().stream()
                .map(SlaveBroadcast::toJson)
                .collect(Collectors.toList());
        ConsoleLogger.printLine(Level.INFO, jsonSlaves.toString());
        return new ResponseEntity<>(jsonSlaves, HttpStatus.OK);
    }

    @GetMapping("/get")
    public ResponseEntity<String> getClientByName(@RequestBody String uuid, @RequestHeader("Authorization") String token) {
        if (!isValidToken(token)) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }
        return AfterburnerBroadcasterApp.get().getSlaves().stream()
                .filter(sb -> sb.getUuid().equals(UUID.fromString(uuid)))
                .findAny()
                .map(slaveBroadcast -> new ResponseEntity<>(slaveBroadcast.toJson(), HttpStatus.OK))
                .orElseGet(() -> new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    @PostMapping("/add")
    public ResponseEntity<String> addClient(@RequestBody String slaveJsonized, @RequestHeader("Authorization") String token) {
        if (!isValidToken(token)) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }

        SlaveBroadcast sb;
        try {
            sb = SlaveBroadcast.fromJson(slaveJsonized);
        } catch (JsonSyntaxException e) {
            return new ResponseEntity<>("Wrong informations in body", HttpStatus.BAD_REQUEST);
        }

        ConsoleLogger.printLine(Level.INFO, "New client : " + sb.getUuid().toString() + " (Name : " + sb.getName() + ")");

        for (SlaveBroadcast slave : AfterburnerBroadcasterApp.get().getSlaves()) {
            if(slave.getUuid().equals(sb.getUuid()))
                return new ResponseEntity<>("Client already exists", HttpStatus.CONFLICT);
        }

        sb.setLastContact(Instant.now().getEpochSecond());
        AfterburnerBroadcasterApp.get().getSlaves().add(sb);

        AfterburnerBroadcasterApp.get().getEventManager().call(new PerformActionEvent(sb, BroadcasterWebApiHandler.Action.ADD));

        return new ResponseEntity<>("Client added", HttpStatus.OK);
    }

    @PostMapping("/update")
    public ResponseEntity<String> updateClient(@RequestBody String slaveJsonized, @RequestHeader("Authorization") String token) {
        if (!isValidToken(token)) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }

        SlaveBroadcast sb;
        try {
            sb = SlaveBroadcast.fromJson(slaveJsonized);
        } catch (JsonSyntaxException e) {
            return new ResponseEntity<>("Wrong informations in body", HttpStatus.BAD_REQUEST);
        }

        boolean finded = false;
        for (SlaveBroadcast slave : AfterburnerBroadcasterApp.get().getSlaves()) {
            if(slave.getUuid().equals(sb.getUuid())) {
                finded = true;
                break;
            }
        }
        if(!finded) {
            AfterburnerBroadcasterApp.get().getSlaves().add(sb);
            ConsoleLogger.printLine(Level.INFO, "New client : " + sb.getUuid().toString() + " (Name : " + sb.getName() + ")");
            return new ResponseEntity<>("Client updated", HttpStatus.OK);
        } else {
            SlaveBroadcast inded = AfterburnerBroadcasterApp.get().getSlaves().stream()
                    .filter(s -> s.getUuid().equals(sb.getUuid()))
                    .findFirst().get();
            inded.setAvailable(sb.isAvailable());
            inded.setLastContact(sb.getLastContact());
            inded.setState(sb.getState());

            AfterburnerBroadcasterApp.get().getEventManager().call(new PerformActionEvent(sb, BroadcasterWebApiHandler.Action.UPDATE));

            return new ResponseEntity<>("Client updated", HttpStatus.OK);
        }
    }

    @DeleteMapping("/delete")
    public ResponseEntity<String> deleteClient(@RequestBody String slaveJsonized, @RequestHeader("Authorization") String token) {
        if (!isValidToken(token)) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }

        SlaveBroadcast sb;
        try {
            sb = SlaveBroadcast.fromJson(slaveJsonized);
        } catch (JsonSyntaxException e) {
            return new ResponseEntity<>("Wrong informations in body", HttpStatus.BAD_REQUEST);
        }

        for (SlaveBroadcast slave : new ArrayList<>(AfterburnerBroadcasterApp.get().getSlaves())) {
            if(slave.getUuid().equals(sb.getUuid())) {
                AfterburnerBroadcasterApp.get().getEventManager().call(new PerformActionEvent(sb, BroadcasterWebApiHandler.Action.REMOVE));
                AfterburnerBroadcasterApp.get().getSlaves().remove(slave);
                return new ResponseEntity<>("Client deleted", HttpStatus.OK);
            }
        }

        return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }

    private boolean isValidToken(String token) {
        return token != null && token.equals("Bearer " + ConfigBroadcaster.API_TOKEN.getData());
    }

}

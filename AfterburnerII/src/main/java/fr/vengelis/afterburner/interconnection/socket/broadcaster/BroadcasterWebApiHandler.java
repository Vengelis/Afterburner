package fr.vengelis.afterburner.interconnection.socket.broadcaster;

import fr.vengelis.afterburner.AfterburnerSlaveApp;
import fr.vengelis.afterburner.configurations.ConfigGeneral;
import fr.vengelis.afterburner.events.impl.slave.SendUpdateBroadcasterEvent;
import fr.vengelis.afterburner.language.LanguageManager;
import fr.vengelis.afterburner.utils.ConsoleLogger;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import java.util.logging.Level;

public class BroadcasterWebApiHandler {

    private final String url;
    private final String token;
    private final Short timeout;
    private final SimpleClientHttpRequestFactory requestFactory;

    private short attemptCallBroadcasterCount = 0;
    private boolean attemptCallBroadcasterFailed = false;

    public enum Action {
        ADD("/client/add"),
        UPDATE("/client/update"),
        REMOVE("/client/delete"),
        LIST("/client/all")
        ;

        private final String route;

        Action(String route) {
            this.route = route;
        }

        public String getRoute() {
            return route;
        }
    }

    public BroadcasterWebApiHandler(String url, String token, Short timeout) {
        this.url = url;
        this.token = token;
        this.timeout = timeout;
        requestFactory = new SimpleClientHttpRequestFactory();
        requestFactory.setConnectTimeout(timeout);
        requestFactory.setReadTimeout(timeout);
    }

    public void sendRequest(SlaveBroadcast slaveBroadcast, Action action, HttpMethod method) {

        try {
            if(!(boolean) ConfigGeneral.QUERY_BROADCASTER_ENABLED.getData())
                return;
        } catch (Exception ignored) {

        }

        if(attemptCallBroadcasterFailed) return;

        RestTemplate restTemplate = new RestTemplate();
        restTemplate.setRequestFactory(requestFactory);

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + token);
        headers.set("Content-Type", "application/json");

        HttpEntity<String> request = new HttpEntity<>(slaveBroadcast.toJson(), headers);

        try {
            AfterburnerSlaveApp.get().getEventManager().call(new SendUpdateBroadcasterEvent(slaveBroadcast));
            ResponseEntity<String> response = restTemplate.exchange(url + action.getRoute(), method, request, String.class);

            if (response.getStatusCode().is2xxSuccessful()) {
                ConsoleLogger.printLine(Level.INFO, String.format(LanguageManager.translate("broadcaster-success-perform"), action.name()));
                ConsoleLogger.printVerbose(Level.INFO, response.getBody());
                attemptCallBroadcasterCount = 0;
            } else {
                ConsoleLogger.printLine(Level.INFO, String.format(LanguageManager.translate("broadcaster-error-perform"), action.name(), response.getStatusCode()));
            }
        } catch (ResourceAccessException e) {
            attemptCallBroadcasterCount += 1;
            ConsoleLogger.printLine(Level.WARNING, LanguageManager.translate("broadcaster-timeout"));
            if(attemptCallBroadcasterCount == 10) {
                attemptCallBroadcasterFailed = true;
                ConsoleLogger.printLineBox(Level.SEVERE, "[Broadcaster] > Query broadcaster requests was definitely blocked because all requests timed out");
            }
        } catch (RestClientException e) {
            ConsoleLogger.printStacktrace(e);
        }
    }

    public void stop() {
        attemptCallBroadcasterCount = 0;
        attemptCallBroadcasterFailed = true;
    }

    public void resetLocker() {
        attemptCallBroadcasterCount = 0;
        attemptCallBroadcasterFailed = false;
    }

}

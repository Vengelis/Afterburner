package fr.vengelis.afterburner.redis;

public interface IPacketsReceiver {
    void receive(String channel, String message) throws Exception;
}

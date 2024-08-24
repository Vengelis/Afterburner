package fr.vengelis.afterburner.interconnection.redis;

public interface IPacketsReceiver {
    void receive(String channel, String message) throws Exception;
}

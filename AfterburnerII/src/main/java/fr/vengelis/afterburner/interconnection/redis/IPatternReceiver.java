package fr.vengelis.afterburner.interconnection.redis;

public interface IPatternReceiver {
    void receive(String pattern, String channel, String message);
}

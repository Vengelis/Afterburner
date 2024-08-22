package fr.vengelis.afterburner.redis;

public interface IPatternReceiver {
    void receive(String pattern, String channel, String message);
}

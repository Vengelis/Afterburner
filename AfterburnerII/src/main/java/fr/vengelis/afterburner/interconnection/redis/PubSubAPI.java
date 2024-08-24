package fr.vengelis.afterburner.interconnection.redis;

import fr.vengelis.afterburner.utils.ConsoleLogger;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPubSub;

import java.util.logging.Level;

public class PubSubAPI {

    public void tryHelloWorld() {
        try (Jedis jedis = RedisConnection.getJedis()) {
            jedis.publish("AFTERBURNER-HELLOWORLD", "Hi guy's :D");
        } catch (Exception e) {
            ConsoleLogger.printStacktrace(e);
            ConsoleLogger.printLinesBox(Level.SEVERE, new String[]{
                    "There was a problem with Redis",
                    "Shutting down process with code 7"
            });
            System.exit(7);
        }
    }

    public void publish(String channel, String message) {
        try (Jedis jedis = RedisConnection.getJedis()) {
            jedis.publish(channel, message);
        } catch (Exception e) {
            ConsoleLogger.printStacktrace(e);
        }
    }

    public void subscribe(String channel, IPacketsReceiver receiver) {
        Thread thread = new Thread(() -> {
            try (Jedis jedis = RedisConnection.getJedis()) {
                jedis.subscribe(new JedisPubSub() {

                    @Override
                    public void onMessage(String channel, String message) {
                        try {
                            receiver.receive(channel, message);
                        } catch (Exception e) {
                            throw new RuntimeException(e);
                        }
                    }

                }, channel);
            } catch (Exception e) {
                ConsoleLogger.printStacktrace(e);
            }
        }, "sub (c = " + channel + ")");
        thread.start();
    }

    public void psubscribe(String pattern, IPatternReceiver receiver) {
        Thread thread = new Thread(() -> {
            try (Jedis jedis = RedisConnection.getJedis()) {
                jedis.psubscribe(new JedisPubSub() {

                    @Override
                    public void onPMessage(String pattern, String channel, String message) {
                        receiver.receive(pattern, channel, message);
                    }

                }, pattern);
            } catch (Exception e) {
                ConsoleLogger.printStacktrace(e);
            }
        }, "sub (p = " + pattern + ")");
        thread.start();
    }
}

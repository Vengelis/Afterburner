package fr.vengelis.afterburner.redis;

import fr.vengelis.afterburner.configurations.ConfigGeneral;
import fr.vengelis.afterburner.utils.ConsoleLogger;
import org.apache.commons.pool2.impl.GenericObjectPoolConfig;
import org.yaml.snakeyaml.Yaml;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;
import redis.clients.jedis.params.ScanParams;
import redis.clients.jedis.resps.ScanResult;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.time.Duration;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;

public class RedisConnection {

    private static JedisPool pool;

    public static void create() {
        String host = (String) ConfigGeneral.REDIS_HOST.getData();
        int port = (int) ConfigGeneral.REDIS_PORT.getData();
        String user = (String) ConfigGeneral.REDIS_USER.getData();
        String password = (String) ConfigGeneral.REDIS_PASSWORD.getData();
        int database = (int) ConfigGeneral.REDIS_DATABASE.getData();
        create(host, user, password, port, database);
    }

    public static void create(String host, String user, String password, int port, int database) {
        ClassLoader previous = Thread.currentThread().getContextClassLoader();
        Thread.currentThread().setContextClassLoader(Jedis.class.getClassLoader());


        RedisConnection.pool = new JedisPool(new JedisPoolConfig(), host, port, 2000, password, database);
        pool.setMaxTotal(30);
        pool.setMaxIdle(30);
        pool.setTimeBetweenEvictionRuns(Duration.ofMillis(2000));
        Thread.currentThread().setContextClassLoader(previous);
    }

    public static Jedis getJedis() {
        return RedisConnection.pool.getResource();
    }

    public static void set(String key, String value) {
        try(Jedis jedis = RedisConnection.getJedis()) {
            jedis.set(key, value);
        }
    }

    public static void set(String key, String value, int seconds) {
        try(Jedis jedis = RedisConnection.getJedis()) {
            jedis.setex(key, seconds, value);
        }
    }

    public static void del(String key) {
        try(Jedis jedis = RedisConnection.getJedis()) {
            jedis.del(key);
        }
    }

    public static String get(String key) {
        try(Jedis jedis = RedisConnection.getJedis()) {
            return jedis.get(key);
        }
    }

    public static void expire(String key, Integer time) {
        try(Jedis jedis = RedisConnection.getJedis()) {
            jedis.expire(key, time);
        }
    }

    public static Set<String> getKeys(String pattern) {
        try (Jedis jedis = RedisConnection.getJedis()) {
            Set<String> keys = new HashSet<>();

            String cursor = ScanParams.SCAN_POINTER_START;
            ScanParams scanParams = new ScanParams().match(pattern);
            do {
                ScanResult<String> scanResult = jedis.scan(cursor, scanParams);
                keys.addAll(scanResult.getResult());
                cursor = scanResult.getCursor();
            } while (!cursor.equals(ScanParams.SCAN_POINTER_START));

            return keys;
        }
    }

}
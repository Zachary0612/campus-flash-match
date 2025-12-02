package com.campus.util;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

import org.springframework.data.redis.connection.DataType;
import org.springframework.data.redis.connection.ReturnType;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.SessionCallback;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.data.redis.core.RedisOperations;
import org.springframework.data.geo.Circle;
import org.springframework.data.geo.Distance;
import org.springframework.data.geo.GeoResults;
import org.springframework.data.geo.Point;
import org.springframework.data.redis.connection.RedisGeoCommands;
import org.springframework.stereotype.Component;

/**
 * Redis工具类，封装Redis常用操作
 */
@Component
public class RedisUtil {

    private final RedisTemplate<String, Object> redisTemplate;

    public RedisUtil(RedisTemplate<String, Object> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    // 字符串操作
    public void set(String key, Object value) {
        redisTemplate.opsForValue().set(key, value);
    }

    public void set(String key, Object value, long timeout, TimeUnit unit) {
        redisTemplate.opsForValue().set(key, value, timeout, unit);
    }

    public Object get(String key) {
        return redisTemplate.opsForValue().get(key);
    }

    public Boolean setIfAbsent(String key, Object value) {
        return redisTemplate.opsForValue().setIfAbsent(key, value);
    }

    public Boolean setEx(String key, Object value, long timeout, TimeUnit unit) {
        try {
            redisTemplate.opsForValue().set(key, value, timeout, unit);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    // Hash 操作
    public void hSet(String key, String hashKey, Object value) {
        redisTemplate.opsForHash().put(key, hashKey, value);
    }

    public Object hGet(String key, String hashKey) {
        try {
            return redisTemplate.opsForHash().get(key, hashKey);
        } catch (Exception e) {
            System.err.println("Redis hGet操作失败，key: " + key + "，hashKey: " + hashKey + "，错误信息: " + e.getMessage());
            return null;
        }
    }
    
    public Map<Object, Object> hGetAll(String key) {
        try {
            return redisTemplate.opsForHash().entries(key);
        } catch (Exception e) {
            System.err.println("Redis hGetAll操作失败，key: " + key + "，错误信息: " + e.getMessage());
            return new HashMap<>();
        }
    }
    
    public void hPutAll(String key, Map<String, Object> map) {
        redisTemplate.opsForHash().putAll(key, map);
    }

    public Long hIncrement(String key, String hashKey, long delta) {
        return redisTemplate.opsForHash().increment(key, hashKey, delta);
    }

    // Set 操作
    public void addToSet(String key, Object value) {
        redisTemplate.opsForSet().add(key, value);
    }

    public void sAdd(String key, Object... values) {
        redisTemplate.opsForSet().add(key, values);
    }

    public Boolean isMember(String key, Object value) {
        return redisTemplate.opsForSet().isMember(key, value);
    }

    public Boolean sIsMember(String key, Object value) {
        return redisTemplate.opsForSet().isMember(key, value);
    }

    public Set<Object> sMembers(String key) {
        return redisTemplate.opsForSet().members(key);
    }

    public void removeFromSet(String key, Object value) {
        redisTemplate.opsForSet().remove(key, value);
    }

    public Long sRemove(String key, Object... values) {
        return redisTemplate.opsForSet().remove(key, values);
    }
    
    public Long sSize(String key) {
        return redisTemplate.opsForSet().size(key);
    }

    // Key 操作
    public DataType type(String key) {
        try {
            return redisTemplate.type(key);
        } catch (Exception e) {
            System.err.println("Redis type操作失败，key: " + key + "，错误信息: " + e.getMessage());
            return DataType.NONE;
        }
    }
    
    public Boolean hasKey(String key) {
        try {
            Boolean result = redisTemplate.hasKey(key);
            return result != null ? result : Boolean.FALSE;
        } catch (Exception e) {
            System.err.println("Redis hasKey操作失败，key: " + key + "，错误信息: " + e.getMessage());
            return Boolean.FALSE;
        }
    }

    public void delete(String key) {
        redisTemplate.delete(key);
    }

    public Boolean delete(Set<String> keys) {
        return redisTemplate.delete(keys) > 0;
    }

    public Boolean expire(String key, long timeout, TimeUnit unit) {
        return redisTemplate.expire(key, timeout, unit);
    }

    public Long getExpire(String key) {
        return redisTemplate.getExpire(key);
    }

    // Geo 操作
    public Boolean geoAdd(String key, double x, double y, String member) {
        try {
            redisTemplate.opsForGeo().add(key, new Point(x, y), member);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public Long geoRemove(String key, String... members) {
        return redisTemplate.opsForGeo().remove(key, members);
    }

    public List<Point> geoPosition(String key, String... members) {
        return redisTemplate.opsForGeo().position(key, members);
    }

    public GeoResults<RedisGeoCommands.GeoLocation<Object>> geoRadius(String key, double x, double y, double radius, RedisGeoCommands.DistanceUnit unit) {
        try {
            Circle circle = new Circle(new Point(x, y), new Distance(radius, unit));
            RedisGeoCommands.GeoRadiusCommandArgs args = RedisGeoCommands.GeoRadiusCommandArgs.newGeoRadiusArgs()
                    .includeDistance()
                    .includeCoordinates()
                    .sortAscending();
            return redisTemplate.opsForGeo().radius(key, circle, args);
        } catch (Exception e) {
            System.err.println("Redis geoRadius操作失败，key: " + key + "，错误信息: " + e.getMessage());
            // 返回空的结果集而不是抛出异常
            return new GeoResults<>(new ArrayList<>(), new Distance(0));
        }
    }

    public GeoResults<RedisGeoCommands.GeoLocation<Object>> geoRadiusByMember(String key, String member, double radius, RedisGeoCommands.DistanceUnit unit) {
        Distance distance = new Distance(radius, unit);
        RedisGeoCommands.GeoRadiusCommandArgs args = RedisGeoCommands.GeoRadiusCommandArgs.newGeoRadiusArgs()
                .includeDistance()
                .includeCoordinates()
                .sortAscending();
        return redisTemplate.opsForGeo().radius(key, member, distance, args);
    }
    // RedisUtil.java
    public Set<Object> zRange(String key, long start, long end) {
        try {
            return redisTemplate.opsForZSet().range(key, start, end);
        } catch (Exception e) {
            System.err.println("Redis zRange操作失败，key: " + key + "，错误信息: " + e.getMessage());
            return Collections.emptySet();
        }
    }
    // 事务操作
    public <T> T execute(SessionCallback<T> sessionCallback) {
        return redisTemplate.execute(sessionCallback);
    }

    // 分布式锁简易实现
    public boolean tryLock(String lockKey, String requestId, long expireTime, TimeUnit unit) {
        return redisTemplate.opsForValue().setIfAbsent(lockKey, requestId, expireTime, unit);
    }

    public boolean releaseLock(String lockKey, String requestId) {
        String script = "if redis.call('get', KEYS[1]) == ARGV[1] then return redis.call('del', KEYS[1]) else return 0 end";
        return Boolean.TRUE.equals(redisTemplate.execute((RedisCallback<Object>) connection -> 
                connection.eval(script.getBytes(), ReturnType.INTEGER, 1, lockKey.getBytes(), requestId.getBytes())));
    }

    // 通用操作，带异常处理
    public <T> T executeWithRetry(Supplier<T> action, int maxRetries) {
        int retryCount = 0;
        while (true) {
            try {
                return action.get();
            } catch (Exception e) {
                retryCount++;
                if (retryCount >= maxRetries) {
                    throw e;
                }
                try {
                    Thread.sleep(100 * retryCount); // 指数退避
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    throw new RuntimeException(ie);
                }
            }
        }
    }
}
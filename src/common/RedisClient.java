package common;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

import java.util.HashMap;

public class RedisClient {

	private static HashMap<String, JedisPool> shardedJedisPool = new HashMap<String, JedisPool>();
	private static HashMap<String, Integer> shardedWhich = new HashMap<String, Integer>();

	private RedisClient() {
	}

	/**
	 * 初始化非切片池
	 */
	public static void initialPool(String ip, Integer port, String dbName, int which) {
		// 池基本配置
		JedisPoolConfig config = new JedisPoolConfig();
		config.setMaxTotal(20);
		config.setMaxIdle(5);
		config.setMaxWaitMillis(1000l);
		config.setTestOnBorrow(false);

		JedisPool jedisPool = new JedisPool(config, ip, port);
		Jedis sj = jedisPool.getResource();
		sj.select(which);
		shardedJedisPool.put(dbName, jedisPool);
		shardedWhich.put(dbName, which);
	}


	public static void hset(String dbName, String key, String field, String value, Integer seconds) {
		try (Jedis jedis = shardedJedisPool.get(dbName).getResource()) {
			jedis.select(shardedWhich.get(dbName));
			jedis.hset(key, field, value);
			jedis.expire(key, seconds);
		}
	}

	public static String hget(String dbName, String key, String field) {
		try (Jedis jedis = shardedJedisPool.get(dbName).getResource()) {
			jedis.select(shardedWhich.get(dbName));
			return jedis.hget(key, field);
		}
	}

	public static Long hdel(String dbName, String key) {
		try (Jedis jedis = shardedJedisPool.get(dbName).getResource()) {
			jedis.select(shardedWhich.get(dbName));
			return jedis.del(key);
		}
	}

	public static Long tll(String dbName, String key) {
		try (Jedis jedis = shardedJedisPool.get(dbName).getResource()) {
			jedis.select(shardedWhich.get(dbName));
			return jedis.ttl(key);
		}
	}
}

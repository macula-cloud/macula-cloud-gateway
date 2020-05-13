package org.macula.cloud.gateway.repository;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.macula.cloud.gateway.util.Constants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.gateway.route.RouteDefinition;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import com.alibaba.fastjson.JSON;

/**
 * @Auther: Aaron
 * @Date: 2018/8/29 18:35
 * @Description:
 */
@Component
public class RedisRepository {

	@Autowired
	private StringRedisTemplate redisTemplate;

	/**
	 * key value 形式
	 *
	 * @param key
	 * @param value
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public void set(final String key, final String value) {
		redisTemplate.execute(new RedisCallback() {
			@Override
			public Long doInRedis(RedisConnection connection) throws DataAccessException {
				connection.set(key.getBytes(), value.getBytes());
				return 1L;
			}
		});
	}

	/**
	 * set 一个有时间的key 到时间后自动过期
	 *
	 * @param key
	 * @param value
	 * @param liveTime 存活时间/秒
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public void set(final String key, final String value, final long liveTime) {
		redisTemplate.execute(new RedisCallback() {
			@Override
			public Long doInRedis(RedisConnection connection) throws DataAccessException {
				connection.setEx(key.getBytes(), liveTime, value.getBytes());
				return 1L;
			}
		});
	}

	/**
	 * 根据key 获取value
	 *
	 * @param key
	 * @return
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public String get(final String key) {
		return (String) redisTemplate.execute(new RedisCallback() {
			@Override
			public Object doInRedis(RedisConnection connection) throws DataAccessException {
				try {
					return new String(connection.get(key.getBytes()));
				} catch (Exception e) {
				}
				return null;
			}
		});
	}

	/**
	 * 自增 key不存在则为1 key存在则自增
	 *
	 * @param key
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public void incrby(final String key) {
		redisTemplate.execute(new RedisCallback() {
			@Override
			public Long doInRedis(RedisConnection connection) throws DataAccessException {
				Long incr = connection.incr(key.getBytes());
				return incr;
			}
		});
	}

	/**
	 * 自增不存在数值会默认初始化0然后加1，传入时间后自动销毁
	 *
	 * @param key
	 * @param liveTime
	 */
	public void incrby(final String key, final long liveTime) {
		redisTemplate.execute(new RedisCallback<Object>() {
			@Override
			public Long doInRedis(RedisConnection connection) throws DataAccessException {
				Long incr = connection.incr(key.getBytes());
				connection.expire(key.getBytes(), liveTime);
				return incr;
			}
		});
	}

	/**
	 * 自减
	 *
	 * @param key
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public void decrby(final String key) {
		redisTemplate.execute(new RedisCallback() {
			@Override
			public Long doInRedis(RedisConnection connection) throws DataAccessException {
				Long incr = connection.decr(key.getBytes());
				return incr;
			}
		});
	}

	/**
	 * 删除key的value
	 *
	 * @param key
	 */
	public void del(final String key) {
		redisTemplate.execute(new RedisCallback<Object>() {
			@Override
			public Long doInRedis(RedisConnection connection) throws DataAccessException {
				return connection.del(key.getBytes());
			}
		});
	}

	/**
	 * 根据key查看是否存在 刷新
	 *
	 * @param key
	 * @return
	 */
	public boolean hasKey(String key) {
		return redisTemplate.hasKey(key);
	}

	/**
	 * 根据key刷新过期时间
	 *
	 * @param key
	 * @param timeOut
	 * @return
	 */
	public boolean refreshLiveTime(String key, long timeOut) {
		return redisTemplate.expire(key, timeOut, TimeUnit.SECONDS);
	}

	/**
	 * 使用的hash取出网关
	 *
	 * @return
	 */
	public List<RouteDefinition> getRouteDefinitions() {
		List<RouteDefinition> routeDefinitions = new ArrayList<>();
		redisTemplate.opsForHash().values(Constants.GATEWAY_ROUTES).stream().forEach(routeDefinition -> {
			routeDefinitions.add(JSON.parseObject(routeDefinition.toString(), RouteDefinition.class));
		});
		return routeDefinitions;
	}

}

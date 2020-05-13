package org.macula.cloud.gateway.repository;

import java.util.ArrayList;
import java.util.List;

import org.macula.cloud.gateway.util.Constants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.gateway.route.RouteDefinition;
import org.springframework.cloud.gateway.route.RouteDefinitionRepository;
import org.springframework.cloud.gateway.support.NotFoundException;
import org.springframework.data.redis.core.StringRedisTemplate;

import com.alibaba.fastjson.JSON;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * 使用Redis保存自定义路由配置（代替默认的InMemoryRouteDefinitionRepository）
 * <p/>
 * 存在问题：每次请求都会调用getRouteDefinitions，当网关较多时，会影响请求速度，考虑放到本地Map中，使用消息通知Map更新。
 *
 * @linqina 2018年8月20日 下午3:40:32
 */
//@Component
public class RedisRouteDefinitionRepository implements RouteDefinitionRepository {
	@Autowired
	private StringRedisTemplate redisTemplate;

	@Override
	public Flux<RouteDefinition> getRouteDefinitions() {
		List<RouteDefinition> routeDefinitions = new ArrayList<>();
		redisTemplate.opsForHash().values(Constants.GATEWAY_ROUTES).stream().forEach(routeDefinition -> {
			routeDefinitions.add(JSON.parseObject(routeDefinition.toString(), RouteDefinition.class));
		});
		return Flux.fromIterable(routeDefinitions);
	}

	@Override
	public Mono<Void> save(Mono<RouteDefinition> route) {
		return route.flatMap(routeDefinition -> {
			redisTemplate.opsForHash().put(Constants.GATEWAY_ROUTES, routeDefinition.getId(),
					JSON.toJSONString(routeDefinition));
			return Mono.empty();
		});
	}

	@Override
	public Mono<Void> delete(Mono<String> routeId) {
		return routeId.flatMap(id -> {
			if (redisTemplate.opsForHash().hasKey(Constants.GATEWAY_ROUTES, id)) {
				redisTemplate.opsForHash().delete(Constants.GATEWAY_ROUTES, id);
				return Mono.empty();
			}
			return Mono.defer(() -> Mono.error(new NotFoundException("RouteDefinition not found: " + routeId)));
		});
	}

}

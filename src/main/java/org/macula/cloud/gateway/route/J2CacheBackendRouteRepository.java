package org.macula.cloud.gateway.route;

import java.util.HashMap;
import java.util.Map;

import org.macula.cloud.gateway.util.Constants;
import org.macula.cloud.core.utils.J2CacheUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.gateway.event.RefreshRoutesEvent;
import org.springframework.cloud.gateway.route.RouteDefinition;
import org.springframework.cloud.gateway.route.RouteDefinitionRepository;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.ApplicationEventPublisherAware;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Component;

import com.alibaba.fastjson.JSON;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Component
public class J2CacheBackendRouteRepository implements RouteDefinitionRepository, ApplicationEventPublisherAware {

	@Autowired
	private ThreadPoolTaskExecutor executor;

	private ApplicationEventPublisher applicationEventPublisher;

	@Override
	public Flux<RouteDefinition> getRouteDefinitions() {
		try {
			Map<String, String> routes = J2CacheUtils.get(J2CacheUtils.CACHE_REGION, Constants.GATEWAY_ROUTES);
			return Flux.fromIterable(routes.entrySet())
					.map(define -> JSON.parseObject(define.getValue(), RouteDefinition.class));
		} catch (Exception ex) {
			return Flux.empty();
		}
	}

	@Override
	public Mono<Void> save(Mono<RouteDefinition> route) {
		try {
			Map<String, String> cacheRoutes = J2CacheUtils.get(J2CacheUtils.CACHE_REGION, Constants.GATEWAY_ROUTES);
			if (cacheRoutes == null) {
				cacheRoutes = new HashMap<String, String>();
			}
			Map<String, String> routes = cacheRoutes;
			route.subscribe(routeDefinition -> {
				routes.put(routeDefinition.getId(), JSON.toJSONString(routeDefinition));
				J2CacheUtils.set(J2CacheUtils.CACHE_REGION, Constants.GATEWAY_ROUTES, routes);
			});
		} catch (Exception ex) {
			// IGNORE
		}
		return refreshRoutes();
	}

	@Override
	public Mono<Void> delete(Mono<String> routeId) {
		try {
			Map<String, String> routes = J2CacheUtils.get(J2CacheUtils.CACHE_REGION, Constants.GATEWAY_ROUTES);
			routeId.subscribe(id -> {
				if (routes != null) {
					routes.remove(id);
					J2CacheUtils.set(J2CacheUtils.CACHE_REGION, Constants.GATEWAY_ROUTES, routes);
				}
			});
		} catch (Exception ex) {
			// IGNORE
		}
		return refreshRoutes();
	}

	protected Mono<Void> refreshRoutes() {
		executor.execute(() -> this.applicationEventPublisher.publishEvent(new RefreshRoutesEvent(this)));
		return Mono.empty();
	}

	@Override
	public void setApplicationEventPublisher(ApplicationEventPublisher applicationEventPublisher) {
		this.applicationEventPublisher = applicationEventPublisher;
	}

}

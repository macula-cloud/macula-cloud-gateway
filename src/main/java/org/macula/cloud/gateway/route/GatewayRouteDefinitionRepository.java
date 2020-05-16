package org.macula.cloud.gateway.route;

import java.util.List;

import org.macula.cloud.gateway.domain.GatewayRoute;
import org.macula.cloud.gateway.service.GatewayRouteService;
import org.macula.cloud.gateway.util.GatewayRouteUtils;
import org.springframework.cloud.gateway.route.RouteDefinition;
import org.springframework.cloud.gateway.route.RouteDefinitionRepository;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public class GatewayRouteDefinitionRepository implements RouteDefinitionRepository {

	private GatewayRouteService service;

	public GatewayRouteDefinitionRepository(GatewayRouteService service) {
		this.service = service;
	}

	@Override
	public Flux<RouteDefinition> getRouteDefinitions() {
		List<GatewayRoute> routes = service.loadGatewayRoutes();
		return Flux.fromIterable(routes).map(GatewayRouteUtils::cast);
	}

	@Override
	public Mono<Void> save(Mono<RouteDefinition> route) {
		return route.doOnNext(define -> {
			GatewayRoute gatewayRoute = new GatewayRoute();
			gatewayRoute.clone(GatewayRouteUtils.cast(define));
			service.update(gatewayRoute);
		}).then();
	}

	@Override
	public Mono<Void> delete(Mono<String> routeId) {
		return routeId.doOnNext(id -> {
			service.delete(id);
		}).then();
	}
}

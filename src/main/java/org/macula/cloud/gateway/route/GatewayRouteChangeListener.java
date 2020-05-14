package org.macula.cloud.gateway.route;

import org.macula.cloud.gateway.util.GatewayRouteUtils;
import org.macula.cloud.core.domain.GatewayRoute;
import org.macula.cloud.core.event.GatewayRouteChangeEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

@Slf4j
@Component
public class GatewayRouteChangeListener implements ApplicationListener<GatewayRouteChangeEvent> {

	@Autowired
	private J2CacheBackendRouteRepository routeRepository;

	public GatewayRouteChangeListener(J2CacheBackendRouteRepository routeRepository) {
		this.routeRepository = routeRepository;
	}

	@Override
	public void onApplicationEvent(GatewayRouteChangeEvent event) {
		log.info("Handle GatewayRouteChangeEvent ...");
		GatewayRoute route = event.getSource();
		if (route.isDeleted()) {
			routeRepository.delete(Mono.just(String.valueOf(route.getId())));
		} else {
			routeRepository.save(Mono.just(GatewayRouteUtils.cast(route)));
		}
	}

}

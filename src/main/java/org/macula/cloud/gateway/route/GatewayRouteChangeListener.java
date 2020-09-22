package org.macula.cloud.gateway.route;

import org.macula.cloud.core.domain.GatewayRoute;
import org.macula.cloud.gateway.event.GatewayRouteChangeEvent;
import org.macula.cloud.gateway.util.GatewayRouteUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

@Slf4j
@Component
public class GatewayRouteChangeListener implements ApplicationListener<GatewayRouteChangeEvent> {

	@Autowired
	private GatewayRouteDefinitionRepository routeRepository;

	public GatewayRouteChangeListener(GatewayRouteDefinitionRepository routeRepository) {
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

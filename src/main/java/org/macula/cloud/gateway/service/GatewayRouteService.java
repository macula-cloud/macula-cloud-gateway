package org.macula.cloud.gateway.service;

import java.util.List;

import org.macula.cloud.gateway.domain.GatewayRoute;
import org.macula.cloud.gateway.repository.GatewayRouteRepository;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Order;
import org.springframework.stereotype.Service;

@Service
public class GatewayRouteService {

	private GatewayRouteRepository repository;

	public GatewayRouteService(GatewayRouteRepository repository) {
		this.repository = repository;
	}

	@Cacheable(cacheNames = "routes", key = "GatewayRoutes")
	public List<GatewayRoute> loadGatewayRoutes() {
		return repository.findAll(Sort.by(Order.asc("ordered")));
	}

	public void update(GatewayRoute gatewayRoute) {
		repository.save(gatewayRoute);
	}

	public void delete(String id) {
		try {
			long key = Long.parseLong(id);
			if (key > 0) {
				repository.deleteById(key);
			}
		} catch (Exception ex) {
			// IGNORE
		}
	}

}

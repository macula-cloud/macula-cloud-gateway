package org.macula.cloud.gateway.util;

import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.macula.cloud.core.domain.GatewayRoute;
import org.springframework.cloud.gateway.filter.FilterDefinition;
import org.springframework.cloud.gateway.handler.predicate.PredicateDefinition;
import org.springframework.cloud.gateway.route.RouteDefinition;

public class GatewayRouteUtils {

	public static RouteDefinition cast(GatewayRoute route) {
		RouteDefinition routeDefinition = new RouteDefinition();
		if (route.getId() != null) {
			routeDefinition.setId(route.getId().toString());
		}
		routeDefinition.setUri(URI.create(route.getUri()));
		List<PredicateDefinition> predicateDefinitions = new ArrayList<>();
		List<FilterDefinition> filterDefinitions = new ArrayList<>();
		if (StringUtils.isNotBlank(route.getPath())) {
			PredicateDefinition predicateDefinition = new PredicateDefinition();
			predicateDefinition.setName("Path");
			Map<String, String> map = new HashMap<>();
			map.put("_genkey_0", route.getPath());
			predicateDefinition.setArgs(map);
			predicateDefinitions.add(predicateDefinition);
		}
		if (StringUtils.isNotBlank(route.getGroup()) && StringUtils.isNotBlank(route.getWeight())) {
			PredicateDefinition predicateDefinition = new PredicateDefinition();
			predicateDefinition.setName("Weight");
			Map<String, String> map = new HashMap<>();
			map.put("_genkey_0", route.getGroup());
			map.put("_genkey_1", route.getWeight());
			predicateDefinition.setArgs(map);
			predicateDefinitions.add(predicateDefinition);
		}
		if (route.getMethod() != null) {
			PredicateDefinition predicateDefinition = new PredicateDefinition();
			String[] methods = StringUtils.split(route.getMethod(), ",");
			predicateDefinition.setName("Method");
			Map<String, String> map = new HashMap<>();
			for (int i = 0; i < methods.length; i++) {
				map.put("_genkey_" + i, methods[i]);
			}
			predicateDefinition.setArgs(map);
			predicateDefinitions.add(predicateDefinition);
		}
		if (route.getStripPrefix() != null && route.getStripPrefix() > 0) {
			FilterDefinition filterDefinition = new FilterDefinition();
			filterDefinition.setName("StripPrefix");
			Map<String, String> map = new HashMap<>();
			map.put("_genkey_0", String.valueOf(route.getStripPrefix()));
			filterDefinition.setArgs(map);
			filterDefinitions.add(filterDefinition);
		}
		routeDefinition.setOrder(route.getOrdered());

		routeDefinition.setPredicates(predicateDefinitions);
		routeDefinition.setFilters(filterDefinitions);
		return routeDefinition;
	}

	public static GatewayRoute cast(RouteDefinition routeDefinition) {
		GatewayRoute route = new GatewayRoute();
		route.setUri(routeDefinition.getUri().toString());
		route.setOrdered(routeDefinition.getOrder());
		List<PredicateDefinition> predicateDefinitions = routeDefinition.getPredicates();
		List<FilterDefinition> filterDefinitions = routeDefinition.getFilters();
		for (int i = 0; i < predicateDefinitions.size(); i++) {
			PredicateDefinition predicate = predicateDefinitions.get(i);
			String pName = predicate.getName();
			Map<String, String> pArgs = predicate.getArgs();
			switch (pName) {
			case "Path":
				route.setPath(pArgs.get("_genkey_0"));
				break;
			case "Method":
				route.setMethod(StringUtils.join(pArgs.values(), ","));
				break;
			case "Weight":
				route.setGroup(pArgs.get("_genkey_0"));
				route.setWeight(pArgs.get("_genkey_1"));
				break;
			}
		}
		for (int i = 0; i < filterDefinitions.size(); i++) {
			FilterDefinition filterDefinition = filterDefinitions.get(i);
			String fName = filterDefinition.getName();
			if ("StripPrefix".equals(fName)) {
				route.setStripPrefix(Integer.parseInt(filterDefinition.getArgs().get("_genkey_0")));
			}
		}
		return route;
	}
}

package org.macula.cloud.gateway.service;

import java.net.URI;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.macula.cloud.gateway.domain.RouteAndAclVO;
import org.macula.cloud.gateway.repository.RedisRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.gateway.event.RefreshRoutesEvent;
import org.springframework.cloud.gateway.filter.FilterDefinition;
import org.springframework.cloud.gateway.handler.predicate.PredicateDefinition;
import org.springframework.cloud.gateway.route.Route;
import org.springframework.cloud.gateway.route.RouteDefinition;
import org.springframework.cloud.gateway.route.RouteDefinitionLocator;
import org.springframework.cloud.gateway.route.RouteDefinitionWriter;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.support.NotFoundException;
import org.springframework.cloud.gateway.support.ServerWebExchangeUtils;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.ApplicationEventPublisherAware;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.server.ServerWebExchange;

import reactor.core.publisher.Mono;

/**
 * @author linqina
 */
@Service
public class GatewayService implements ApplicationEventPublisherAware {
	private static final Log log = LogFactory.getLog(GatewayService.class);

	@Autowired
	private RouteDefinitionWriter routeDefinitionWriter;
	@Autowired
	private RouteDefinitionLocator routeDefinitionLocator;
	@Autowired
	private RouteLocator routeLocator;
	@Autowired
	private RedisRepository redisRepository;
	private ApplicationEventPublisher publisher;

	public Mono<ResponseEntity<Void>> save(Mono<RouteAndAclVO> route) {
		return routeDefinitionWriter.save(route.map((r) -> {
			log.debug("Saving route: " + route);
			int order = getRouteOrder(r);
			r.setOrder(order);
			RouteDefinition routeDefinition = myRouter2OldRouter(r);
			return routeDefinition;
		})).then(Mono.defer(() -> {
			refresh();
			return Mono.just(ResponseEntity.created(URI.create("/routes/")).build());
		}));
	}

	public Mono<List<RouteAndAclVO>> routes() {
		Mono<List<RouteDefinition>> routeDefs = this.routeDefinitionLocator.getRouteDefinitions().collectList();
		Mono<List<Route>> routes = this.routeLocator.getRoutes().collectList();
		return Mono.zip(routeDefs, routes).map((tuple) -> {
			List<RouteDefinition> routeList = tuple.getT1();
			List<RouteAndAclVO> routeAndAclVOList = new ArrayList<>();
			for (int i = 0; i < routeList.size(); i++) {
				routeAndAclVOList.add(oldRoute2MyRouter(routeList.get(i)));
			}
			return routeAndAclVOList;
		});
	}

	public Mono<ResponseEntity<Object>> delete(@PathVariable String id) {
		return this.routeDefinitionWriter.delete(Mono.just(id)).then(Mono.defer(() -> {
			refresh();
			return Mono.just(ResponseEntity.ok().build());
		})).onErrorResume((t) -> {
			return t instanceof NotFoundException;
		}, (t) -> {
			return Mono.just(ResponseEntity.notFound().build());
		});
	}

	public Mono<ResponseEntity<RouteAndAclVO>> route(@PathVariable String id) {
		return this.routeDefinitionLocator.getRouteDefinitions().filter((route) -> {
			return route.getId().equals(id);
		}).singleOrEmpty().map((route) -> {
			RouteAndAclVO routeAndAclVO = oldRoute2MyRouter(route);
			return ResponseEntity.ok(routeAndAclVO);
		}).switchIfEmpty(Mono.just(ResponseEntity.notFound().build()));
	}

	public List<String> getGroups(ServerWebExchange serverWebExchange) {
		Map<String, Object> groups = (Map) serverWebExchange.getAttribute(ServerWebExchangeUtils.WEIGHT_ATTR);
		List<String> groupList = new ArrayList<String>(groups.keySet());
		return groupList;
	}

	public Mono<Void> refresh() {
		this.publisher.publishEvent(new RefreshRoutesEvent(this));
		return Mono.empty();
	}

	@Override
	public void setApplicationEventPublisher(ApplicationEventPublisher applicationEventPublisher) {
		this.publisher = applicationEventPublisher;
	}

	private RouteAndAclVO oldRoute2MyRouter(RouteDefinition routeDefinition) {
		RouteAndAclVO vo = new RouteAndAclVO();
		vo.setId(routeDefinition.getId());
		vo.setUri(routeDefinition.getUri().toString());
		vo.setOrder(routeDefinition.getOrder());
		List<PredicateDefinition> predicateDefinitions = routeDefinition.getPredicates();
		List<FilterDefinition> filterDefinitions = routeDefinition.getFilters();
		for (int i = 0; i < predicateDefinitions.size(); i++) {
			PredicateDefinition predicate = predicateDefinitions.get(i);
			String pName = predicate.getName();
			Map<String, String> pArgs = predicate.getArgs();
			switch (pName) {
			case "Path":
				vo.setPath(pArgs.get("_genkey_0"));
				break;
			case "Method":
				vo.setMethod(new ArrayList<>(pArgs.values()));
				break;
			case "Weight":
				vo.setGroup(pArgs.get("_genkey_0"));
				vo.setWeight(pArgs.get("_genkey_1"));
				break;
			}
		}
		for (int i = 0; i < filterDefinitions.size(); i++) {
			FilterDefinition filterDefinition = filterDefinitions.get(i);
			String fName = filterDefinition.getName();
			if ("StripPrefix".equals(fName)) {
				vo.setStripPrefix(Integer.parseInt(filterDefinition.getArgs().get("_genkey_0")));
			}
		}
		return vo;
	}

	private RouteDefinition myRouter2OldRouter(RouteAndAclVO routeAndAclVO) {
		RouteDefinition routeDefinition = new RouteDefinition();
		routeDefinition.setId(routeAndAclVO.getId());
		routeDefinition.setUri(URI.create(routeAndAclVO.getUri()));
		List<PredicateDefinition> predicateDefinitions = new ArrayList<>();
		List<FilterDefinition> filterDefinitions = new ArrayList<>();
		if (StringUtils.isNotBlank(routeAndAclVO.getPath())) {
			PredicateDefinition predicateDefinition = new PredicateDefinition();
			predicateDefinition.setName("Path");
			HashMap<String, String> map = new HashMap<>();
			map.put("_genkey_0", routeAndAclVO.getPath());
			predicateDefinition.setArgs(map);
			predicateDefinitions.add(predicateDefinition);
		}
		if (StringUtils.isNotBlank(routeAndAclVO.getGroup()) && StringUtils.isNotBlank(routeAndAclVO.getWeight())) {
			PredicateDefinition predicateDefinition = new PredicateDefinition();
			predicateDefinition.setName("Weight");
			HashMap<String, String> map = new HashMap<>();
			map.put("_genkey_0", routeAndAclVO.getGroup());
			map.put("_genkey_1", routeAndAclVO.getWeight());
			predicateDefinition.setArgs(map);
			predicateDefinitions.add(predicateDefinition);
		}
		if (!CollectionUtils.isEmpty(routeAndAclVO.getMethod())) {
			PredicateDefinition predicateDefinition = new PredicateDefinition();

			List<String> methods = routeAndAclVO.getMethod();
			predicateDefinition.setName("Method");
			HashMap<String, String> map = new HashMap<>();
			for (int i = 0; i < methods.size(); i++) {
				map.put("_genkey_" + i, methods.get(i));
			}
			predicateDefinition.setArgs(map);
			predicateDefinitions.add(predicateDefinition);
		}
		if (null != routeAndAclVO.getStripPrefix() && 0 != routeAndAclVO.getStripPrefix()) {
			FilterDefinition filterDefinition = new FilterDefinition();
			filterDefinition.setName("StripPrefix");
			HashMap<String, String> map = new HashMap<>();
			map.put("_genkey_0", routeAndAclVO.getStripPrefix() + "");
			filterDefinition.setArgs(map);
			filterDefinitions.add(filterDefinition);
		}
		if (null != routeAndAclVO.getOrder() && 0 != routeAndAclVO.getOrder()) {
			routeDefinition.setOrder(routeAndAclVO.getOrder());
		}

		routeDefinition.setPredicates(predicateDefinitions);
		routeDefinition.setFilters(filterDefinitions);
		return routeDefinition;
	}

	private int getRouteOrder(RouteAndAclVO routeDefinition) {
		int order = 0;
		String path = routeDefinition.getPath();
		if (StringUtils.isNotBlank(path)) {
			if ("/".equals(path) || "/**".equals(path) || "**".equals(path) || "//**".equals(path)) {
				return 100000;
			}
			// 将path去掉前缀'/',去掉后缀'/**'
			path = routeDefinition.getPath().startsWith("/") ? routeDefinition.getPath().substring(1)
					: routeDefinition.getPath();
			path = path.endsWith("/**") ? path.substring(0, path.length() - 3) : path;
			String[] paths = path.split("/");
			List<String> pathComb = new ArrayList<String>();
			// 将path进行拆分 如：/api/security --> [/api/security,/api]
			StringBuffer pathPeer = new StringBuffer();
			for (int i = 0; i < paths.length; i++) {
				pathPeer.append("/" + paths[i]);
				pathComb.add(pathPeer.toString());
			}
			// 倒序
			Collections.reverse(pathComb);
			// 查询已配置的路由
			List<RouteDefinition> routes = redisRepository.getRouteDefinitions();
			if (!CollectionUtils.isEmpty(routes)) {
				// 最大匹配的序号
				int maxOrder = 0;
				// 当前前缀往前挪n个/
				int currentStep = 0;
				// 列表中所匹配到的往前挪n个/
				int oriStep = 0;
				// 若已经匹配，则退出循环
				boolean hasVerb = false;

				for (int i = 0; i < pathComb.size(); i++) {
					String pathPre = pathComb.get(i);
					/**
					 * 算法描述 最终序号 = 最大匹配的序号 - 当前前缀往前挪n个 + 列表中所匹配到的往前挪n个
					 */
					// 如果已经匹配
					if (hasVerb) {
						break;
					} else {
						currentStep = i;
						for (int j = 0; j < routes.size(); j++) {
							String oriPath = "";
							// 取出每一个的path
							RouteDefinition route = routes.get(j);
							List<PredicateDefinition> predicateDefinitions = route.getPredicates();
							for (int k = 0; k < predicateDefinitions.size(); k++) {
								PredicateDefinition predicate = predicateDefinitions.get(k);
								String pName = predicate.getName();
								Map<String, String> pArgs = predicate.getArgs();
								if ("Path".equals(pName)) {
									oriPath = pArgs.get("_genkey_0");
									break;
								}
							}
							// 判断是否已存在且从开始位置
							if (oriPath.indexOf(pathPre) != -1 && oriPath.startsWith(pathPre)) {
								if (route.getOrder() >= maxOrder) {
									maxOrder = route.getOrder();
									// 通过判断所匹配到的path剩余后半段判断往前退几格
									String verb = oriPath.substring(pathPre.length());
									verb = verb.endsWith("/**") ? verb.substring(0, verb.length() - 3) : verb;
									oriStep = verb.length() - verb.replaceAll("/", "").length();
								}
								hasVerb = true;
							}
						}
					}
				}
				order = maxOrder - currentStep + oriStep;
			}
		}

		return order;
	}

}

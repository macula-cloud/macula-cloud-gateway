package org.macula.cloud.gateway.controller;

import java.util.List;
import java.util.UUID;

import org.apache.commons.lang.StringUtils;
import org.macula.cloud.gateway.domain.RouteAndAclVO;
import org.macula.cloud.gateway.service.GatewayService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ServerWebExchange;

import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/admin")
public class GatewayController {
	@Autowired
	private GatewayService gatewayService;

	@GetMapping({ "/login" })
	public String login() {
		return "hello";
	}

	@GetMapping({ "/routes" })
	public Mono<List<RouteAndAclVO>> routes() {
		return gatewayService.routes();
	}

	@PostMapping({ "/routes" })
	public Mono<ResponseEntity<Void>> save(@RequestBody Mono<RouteAndAclVO> route) {
//        return route.map((r) -> {
//            String id  = StringUtils.isNotBlank(r.getId()) ? r.getId() : UUID.randomUUID().toString();
//            r.setId(id);
//            gatewayService.save(id, route);
//            return r;
//        }).then(Mono.defer(() -> {
//            return Mono.just(ResponseEntity.created(URI.create("/routes/")).build());
//        }));

		return gatewayService.save(route.map((r) -> {
			String id = StringUtils.isNotBlank(r.getId()) ? r.getId() : UUID.randomUUID().toString();
			r.setId(id);
			return r;
		}));
	}

	@GetMapping({ "/routes/{id}" })
	public Mono<ResponseEntity<RouteAndAclVO>> route(@PathVariable String id) {
		return gatewayService.route(id);
	}

	@DeleteMapping({ "/routes/{id}" })
	public Mono<ResponseEntity<Object>> delete(@PathVariable String id) {
		return gatewayService.delete(id);
	}

	/**
	 * 用于查看权重分组
	 *
	 * @param serverWebExchange
	 * @return
	 */
	@GetMapping({ "/groups" })
	public List<String> groups(ServerWebExchange serverWebExchange) {
		return gatewayService.getGroups(serverWebExchange);
	}
}

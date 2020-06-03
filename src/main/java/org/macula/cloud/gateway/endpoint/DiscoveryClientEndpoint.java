package org.macula.cloud.gateway.endpoint;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.AllArgsConstructor;

/**
 * 服务发现控制器
 */
@RestController
@AllArgsConstructor
public class DiscoveryClientEndpoint {

	private final DiscoveryClient discoveryClient;

	/**
	 * 获取服务实例
	 */
	@GetMapping("/actuator/discovery")
	public Map<String, List<ServiceInstance>> instances() {
		Map<String, List<ServiceInstance>> instances = new HashMap<>(16);
		List<String> services = discoveryClient.getServices();
		services.forEach(s -> {
			List<ServiceInstance> list = discoveryClient.getInstances(s);
			instances.put(s, list);
		});
		return instances;
	}

}

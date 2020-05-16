package org.macula.cloud.gateway.service;

import java.util.Map;
import java.util.Set;

import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

@Service
public class GatewayAuthorizationService {

	@Cacheable(cacheNames = "authorities")
	public Map<String, Set<String>> loadAuthorizations() {
		return null;
	}

}

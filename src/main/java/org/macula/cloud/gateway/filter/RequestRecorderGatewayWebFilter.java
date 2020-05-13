package org.macula.cloud.gateway.filter;

import org.macula.cloud.gateway.util.GatewayLogRecordUtils;
import org.macula.cloud.core.context.CloudApplicationContext;
import org.macula.cloud.core.event.GatewayLogRecordEvent;
import org.macula.cloud.core.event.InstanceProcessEvent;
import org.macula.cloud.core.utils.SecurityUtils;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.web.server.context.ServerSecurityContextRepository;
import org.springframework.security.web.server.context.WebSessionServerSecurityContextRepository;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;

import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

@Slf4j
public class RequestRecorderGatewayWebFilter implements WebFilter {

	private static final String AlreadyFilteredAttribute = RequestRecorderGatewayWebFilter.class.getName();

	private ServerSecurityContextRepository repository = new WebSessionServerSecurityContextRepository();

	@Override
	public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
		long start = System.currentTimeMillis();
		return chain.filter(exchange).doFinally(c -> {
			if (!exchange.getAttributes().containsKey(AlreadyFilteredAttribute)) {
				exchange.getAttributes().putIfAbsent(AlreadyFilteredAttribute, Boolean.TRUE);
				long end = System.currentTimeMillis();
				Mono<SecurityContext> authentication = repository.load(exchange);
				authentication.map(SecurityContext::getAuthentication).defaultIfEmpty(SecurityUtils.getAnonymous())
						.map(t -> GatewayLogRecordUtils.create(exchange, start, end, t)).subscribe(logRecord -> {
							if (log.isDebugEnabled()) {
								log.debug("Created GatewayLogRecord: {} , and publish...", logRecord);
							}
							CloudApplicationContext.getContainer()
									.publishEvent(InstanceProcessEvent.wrap(new GatewayLogRecordEvent(logRecord)));
						});
			}
		});
	}

	public void setSecurityContextRepository(ServerSecurityContextRepository serverSecurityContextRepository) {
		this.repository = serverSecurityContextRepository;
	}

}

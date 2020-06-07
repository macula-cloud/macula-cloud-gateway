package org.macula.cloud.gateway.filter;

import org.macula.cloud.core.principal.SubjectPrincipal;
import org.macula.cloud.core.utils.SecurityUtils;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextImpl;
import org.springframework.security.jwt.crypto.sign.Signer;
import org.springframework.security.web.server.context.ServerSecurityContextRepository;
import org.springframework.web.server.ServerWebExchange;

import lombok.AllArgsConstructor;
import reactor.core.publisher.Mono;

@AllArgsConstructor
public class JWTAuthenticationSignFilter implements GlobalFilter {

	private static final SecurityContext EMPTY_CONTEXT = new SecurityContextImpl();
	private final ServerSecurityContextRepository repository;
	private Signer jwtSigner;

	@Override
	public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
		return repository.load(exchange).defaultIfEmpty(EMPTY_CONTEXT).map(context -> {
			SubjectPrincipal principal = SecurityUtils.getLoginedPrincipal();
			if (principal == null && context != EMPTY_CONTEXT) {
				principal = SecurityUtils.getLoginedPrincipal(context.getAuthentication());
			}
			if (principal != null) {
				String jwt = SecurityUtils.convertPrincipal(principal, jwtSigner);
				if (jwt != null) {
					ServerHttpRequest request = exchange.getRequest().mutate().header("Authorization", jwt).build();
					return exchange.mutate().request(request).build();
				}
			}
			return exchange;
		}).flatMap(ex -> {
			return chain.filter(ex);
		});
	}

}

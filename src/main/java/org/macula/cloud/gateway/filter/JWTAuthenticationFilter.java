package org.macula.cloud.gateway.filter;

import org.macula.cloud.core.utils.SecurityUtils;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.jwt.JwtHelper;
import org.springframework.security.jwt.crypto.sign.Signer;
import org.springframework.security.web.server.context.ServerSecurityContextRepository;
import org.springframework.security.web.server.context.WebSessionServerSecurityContextRepository;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

@Slf4j
public class JWTAuthenticationFilter implements WebFilter {

	private final ServerSecurityContextRepository repository = new WebSessionServerSecurityContextRepository();

	private final ObjectMapper objectMapper = new ObjectMapper();

	private Signer jwtSigner;

	public JWTAuthenticationFilter(Signer jwtSigner) {
		this.jwtSigner = jwtSigner;
	}

	@Override
	public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
		return chain.filter(exchange).doFinally(c -> {
			Mono<SecurityContext> authentication = repository.load(exchange);
			authentication.map(SecurityContext::getAuthentication).defaultIfEmpty(SecurityUtils.cast(SecurityUtils.getSubjectPrincipal()))
					.subscribe(principal -> {
						try {
							String token = objectMapper.writeValueAsString(principal);
							String jwt = "Bearer " + JwtHelper.encode(token, jwtSigner).getEncoded();
							exchange.getRequest().getHeaders().add("Authentication", jwt);
						} catch (JsonProcessingException e) {
							log.error("JWTAuthentication parse principal error: ", e);
						}
					});
		});
	}
}

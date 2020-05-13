package org.macula.cloud.gateway.principal;

import java.util.Optional;

import org.macula.cloud.core.principal.SubjectPrincipal;
import org.macula.cloud.core.principal.SubjectPrincipalSessionStorage;
import org.macula.cloud.core.session.Session;
import org.macula.cloud.core.utils.SecurityUtils;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.server.authentication.ServerAuthenticationConverter;
import org.springframework.web.server.ServerWebExchange;

import reactor.core.publisher.Mono;

public class PrincipalAuthenticationConverter implements ServerAuthenticationConverter {

	private final SubjectPrincipalSessionStorage sessionStorage;

	public PrincipalAuthenticationConverter(SubjectPrincipalSessionStorage sessionStorage) {
		this.sessionStorage = sessionStorage;
	}

	@Override
	public Mono<Authentication> convert(ServerWebExchange exchange) {
		Optional<String> token = SessionIdPayload.extract(exchange);
		Authentication authentication = null;
		if (token.isPresent()) {
			authentication = loadAuthentication(token.get());
		}
		return Mono.justOrEmpty(authentication);
	}

	protected Authentication loadAuthentication(String token) {
		if (token != null) {
			Session session = sessionStorage.checkoutSession(token);
			if (session != null) {
				SubjectPrincipal subjectPrincipal = sessionStorage.checkoutPrincipal(session.getGuid());
				if (subjectPrincipal != null) {
					return SecurityUtils.cast(subjectPrincipal);
				}
			}
		}
		return null;
	}

}

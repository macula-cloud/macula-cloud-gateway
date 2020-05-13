package org.macula.cloud.gateway.openapi;

import org.macula.cloud.core.exception.OpenApiParameterException;
import org.macula.cloud.core.principal.SubjectPrincipal;
import org.macula.cloud.core.principal.SubjectType;
import org.macula.cloud.core.utils.SecurityUtils;
import org.macula.cloud.core.utils.ServerRequestUtils;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.server.authentication.ServerAuthenticationConverter;
import org.springframework.web.server.ServerWebExchange;

import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

@Slf4j
public class OpenApiAuthenticationConverter implements ServerAuthenticationConverter {

	private static final String RESOLVED_ATTR = OpenApiAuthenticationConverter.class.getName();

	@Override
	public Mono<Authentication> convert(ServerWebExchange exchange) {
		Authentication authentication = exchange.getAttribute(RESOLVED_ATTR);
		if (authentication == null && ServerRequestUtils.isOpenAPIRequest(exchange.getRequest())) {
			log.info("Resolve open api authentication from exchange ");
			try {
				OpenApiAuthenticationHelper.validate(exchange.getRequest(), exchange.getResponse(), "");
			} catch (OpenApiParameterException ex) {
				log.error("OpenApiAuthenticationConverter error :", ex);
				throw ex;
			}
			String username = OpenApiAuthenticationHelper.getRequestUsername(exchange.getRequest());
			SubjectPrincipal principal = new SubjectPrincipal(username, SubjectType.CLIENT);
			authentication = SecurityUtils.cast(principal);
			exchange.getAttributes().put(RESOLVED_ATTR, authentication);
		}
		return Mono.justOrEmpty(authentication);
	}

}

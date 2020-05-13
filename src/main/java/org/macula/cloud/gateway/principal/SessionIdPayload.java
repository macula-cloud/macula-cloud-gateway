package org.macula.cloud.gateway.principal;

import java.util.Optional;

import org.macula.cloud.core.session.SessionId;
import org.springframework.http.HttpCookie;
import org.springframework.http.ResponseCookie;
import org.springframework.web.server.ServerWebExchange;

public class SessionIdPayload {

	public static Optional<String> extract(ServerWebExchange serverWebExchange) {
		String authorization = serverWebExchange.getRequest().getHeaders().getFirst(SessionId.AUTHORIZATION);
		if (authorization == null) {
			HttpCookie cookie = serverWebExchange.getRequest().getCookies().getFirst(SessionId.AUTHORIZATION);
			if (cookie != null) {
				authorization = cookie.getValue();
			}
		}
		return Optional.ofNullable(authorization);
	}

	public static void assemble(ServerWebExchange serverWebExchange, String token) {
		if (token == null) {
			return;
		}
		Optional<String> authorization = extract(serverWebExchange);

		if (!authorization.isPresent() || !token.equals(authorization.get())) {
			serverWebExchange.getResponse().getHeaders().add(SessionId.AUTHORIZATION, token);
			ResponseCookie cookie = ResponseCookie.from(SessionId.AUTHORIZATION, token).build();
			serverWebExchange.getResponse().addCookie(cookie);
		}
	}
}

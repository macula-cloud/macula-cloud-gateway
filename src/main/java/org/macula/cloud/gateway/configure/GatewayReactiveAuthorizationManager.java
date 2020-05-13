package org.macula.cloud.gateway.configure;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.macula.cloud.core.configure.CoreConfigurationProperties;
import org.macula.cloud.sdk.utils.J2CacheUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.authorization.AuthorizationDecision;
import org.springframework.security.authorization.ReactiveAuthorizationManager;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.server.authorization.AuthorizationContext;
import org.springframework.security.web.server.context.WebSessionServerSecurityContextRepository;
import org.springframework.security.web.server.util.matcher.PathPatternParserServerWebExchangeMatcher;
import org.springframework.security.web.server.util.matcher.ServerWebExchangeMatcher;
import org.springframework.security.web.server.util.matcher.ServerWebExchangeMatcherEntry;
import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Component
@Slf4j
public class GatewayReactiveAuthorizationManager implements ReactiveAuthorizationManager<AuthorizationContext> {

	private static final String URL_ROLE_MAPPING_CACHE = "url_role_mapping";

	private List<ServerWebExchangeMatcherEntry<Set<String>>> mappings;

	@Autowired
	private CoreConfigurationProperties configuration;

	WebSessionServerSecurityContextRepository repository = new WebSessionServerSecurityContextRepository();

	@Override
	public Mono<AuthorizationDecision> check(Mono<Authentication> authentication, AuthorizationContext context) {

		Set<String> attrAuthorities = findRequestNeedAuthorities(context);
		if (attrAuthorities.isEmpty()) {
			return Mono.just(new AuthorizationDecision(true));
		}

		return authentication.filter(a -> a.isAuthenticated()).flatMapIterable(a -> a.getAuthorities()).map(g -> g.getAuthority())
				.any(t -> attrAuthorities.contains(t)).map(hasAuthority -> new AuthorizationDecision(hasAuthority))
				.defaultIfEmpty(new AuthorizationDecision(false));
	}

	@Scheduled(fixedRate = 3600 * 1000, initialDelay = 1000)
	public void loadUrlRoleMappings() {
		log.info("Scheduled execute loadUrlRoleMappings");
		List<ServerWebExchangeMatcherEntry<Set<String>>> loadingMappings = new ArrayList<ServerWebExchangeMatcherEntry<Set<String>>>();

		String[] paths = configuration.getSecurity().getIgnorePaths();
		if (paths != null && paths.length > 0) {
			for (String path : configuration.getSecurity().getIgnorePaths()) {
				PathPatternParserServerWebExchangeMatcher matcher = new PathPatternParserServerWebExchangeMatcher(path);
				ServerWebExchangeMatcherEntry<Set<String>> matcherEntry = new ServerWebExchangeMatcherEntry<Set<String>>(matcher,
						new HashSet<String>());
				loadingMappings.add(matcherEntry);
			}
		}

		Map<String, Set<String>> urlRoleMapping = J2CacheUtils.get(J2CacheUtils.CACHE_REGION, URL_ROLE_MAPPING_CACHE);
		if (urlRoleMapping != null) {
			for (Map.Entry<String, Set<String>> entry : urlRoleMapping.entrySet()) {
				PathPatternParserServerWebExchangeMatcher matcher = new PathPatternParserServerWebExchangeMatcher(entry.getKey());
				ServerWebExchangeMatcherEntry<Set<String>> matcherEntry = new ServerWebExchangeMatcherEntry<Set<String>>(matcher, entry.getValue());
				loadingMappings.add(matcherEntry);
			}
		}
		mappings = loadingMappings;
	}

	private Set<String> findRequestNeedAuthorities(final AuthorizationContext context) {
		Set<String> authorities = new HashSet<String>();
		if (mappings != null) {
			Flux.fromIterable(mappings).concatMap(mapping -> mapping.getMatcher().matches(context.getExchange())
					.filter(ServerWebExchangeMatcher.MatchResult::isMatch).map(r -> mapping.getEntry())).next()
					.subscribe(consumer -> authorities.addAll(consumer));
		}
		return authorities;
	}

}

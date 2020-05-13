package org.macula.cloud.gateway.configure;

import org.macula.cloud.core.configure.CoreConfigurationProperties;
import org.macula.cloud.core.principal.SubjectPrincipalSessionStorage;
import org.macula.cloud.gateway.filter.RequestRecorderGatewayWebFilter;
import org.macula.cloud.gateway.oauth2.OAuth2AuthenticationManager;
import org.macula.cloud.gateway.openapi.OpenApiAuthenticationConverter;
import org.macula.cloud.gateway.openapi.OpenApiAuthenticationManager;
import org.macula.cloud.gateway.principal.PrincipalAuthenticationConverter;
import org.macula.cloud.gateway.principal.PrincipalAuthenticationManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.oauth2.resource.UserInfoTokenServices;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.ReactiveAuthenticationManager;
import org.springframework.security.authorization.ReactiveAuthorizationManager;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.SecurityWebFiltersOrder;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.oauth2.server.resource.web.server.ServerBearerTokenAuthenticationConverter;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.security.web.server.authentication.AuthenticationWebFilter;
import org.springframework.security.web.server.authorization.AuthorizationContext;
import org.springframework.security.web.server.context.ServerSecurityContextRepository;
import org.springframework.security.web.server.context.WebSessionServerSecurityContextRepository;
import org.springframework.web.server.WebFilter;

@Configuration
@EnableWebFluxSecurity
public class GatewaySecurityConfiguration {

	@Autowired
	private CoreConfigurationProperties configurationProperties;

	@Autowired
	private ReactiveAuthorizationManager<AuthorizationContext> authorizationManager;

	@Autowired
	private SubjectPrincipalSessionStorage sessionStorage;

	@Autowired
	private UserInfoTokenServices userInfoTokenServices;

	@Autowired
	private UserDetailsService userDetailsService;

	@Bean
	public ServerSecurityContextRepository serverSecurityContextRepository() {
		return new WebSessionServerSecurityContextRepository();
	}

	@Bean
	public SecurityWebFilterChain springSecurityFilterChain(ServerHttpSecurity http) {
		http.httpBasic().disable().formLogin().disable().logout().disable();
		http.authorizeExchange().pathMatchers(configurationProperties.getSecurity().getPublicPaths()).permitAll().anyExchange()
				.access(authorizationManager);
		http.addFilterAt(requestRecorderGatewayWebFilter(), SecurityWebFiltersOrder.FIRST);
		http.addFilterAt(principalResolveAuthenticationWebFilter(), SecurityWebFiltersOrder.HTTP_BASIC);
		http.addFilterAt(openApiAuthenticationWebFilter(), SecurityWebFiltersOrder.HTTP_BASIC);
		http.addFilterAt(oauth2AuthenticationWebFilter(), SecurityWebFiltersOrder.OAUTH2_AUTHORIZATION_CODE);
		return http.build();
	}

	@Bean
	public WebFilter oauth2AuthenticationWebFilter() {
		ServerBearerTokenAuthenticationConverter bearerTokenConverter = new ServerBearerTokenAuthenticationConverter();
		bearerTokenConverter.setAllowUriQueryParameter(true);
		OAuth2AuthenticationManager authenticationManager = new OAuth2AuthenticationManager();
		authenticationManager.setTokenServices(userInfoTokenServices);
		AuthenticationWebFilter oauth2WebFilter = new AuthenticationWebFilter(authenticationManager);
		oauth2WebFilter.setServerAuthenticationConverter(bearerTokenConverter);
		oauth2WebFilter.setSecurityContextRepository(serverSecurityContextRepository());
		return oauth2WebFilter;

	}

	@Bean
	public WebFilter principalResolveAuthenticationWebFilter() {
		ReactiveAuthenticationManager authenticationManager = new PrincipalAuthenticationManager();
		PrincipalAuthenticationConverter authenticationConvert = new PrincipalAuthenticationConverter(sessionStorage);
		AuthenticationWebFilter principalWebFilter = new AuthenticationWebFilter(authenticationManager);
		principalWebFilter.setServerAuthenticationConverter(authenticationConvert);
		principalWebFilter.setSecurityContextRepository(serverSecurityContextRepository());
		return principalWebFilter;
	}

	@Bean
	public WebFilter openApiAuthenticationWebFilter() {
		ReactiveAuthenticationManager authenticationManager = new OpenApiAuthenticationManager();
		OpenApiAuthenticationConverter authenticationConvert = new OpenApiAuthenticationConverter(userDetailsService);
		AuthenticationWebFilter openApiWebFilter = new AuthenticationWebFilter(authenticationManager);
		openApiWebFilter.setServerAuthenticationConverter(authenticationConvert);
		openApiWebFilter.setSecurityContextRepository(serverSecurityContextRepository());
		return openApiWebFilter;
	}

	@Bean
	public RequestRecorderGatewayWebFilter requestRecorderGatewayWebFilter() {
		RequestRecorderGatewayWebFilter filter = new RequestRecorderGatewayWebFilter();
		filter.setSecurityContextRepository(serverSecurityContextRepository());
		return filter;
	}

}

package org.macula.cloud.gateway.configure;

import org.macula.cloud.gateway.oauth2.GatewayUserInfoTokenServices;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.oauth2.resource.ResourceServerProperties;
import org.springframework.boot.autoconfigure.security.oauth2.resource.UserInfoTokenServices;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.client.OAuth2RestTemplate;
import org.springframework.security.oauth2.client.resource.OAuth2ProtectedResourceDetails;

@Configuration
public class GatewayConfiguration {

	@Autowired
	private OAuth2ProtectedResourceDetails client;

	@Autowired
	private ResourceServerProperties resource;

	@Bean
	@ConfigurationProperties(prefix = "oauth2")
	public OAuth2Config gatewayOAuth2Config() {
		return new OAuth2Config();
	}

	@Bean
	public UserInfoTokenServices cacheableUserInfoTokenServices() {
		GatewayUserInfoTokenServices tokenServices = new GatewayUserInfoTokenServices(resource.getUserInfoUri(), resource.getClientId());
		tokenServices.setRestTemplate(new OAuth2RestTemplate(client));
		return tokenServices;
	}

}

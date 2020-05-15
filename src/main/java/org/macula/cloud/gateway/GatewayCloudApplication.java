package org.macula.cloud.gateway;

import org.springframework.boot.SpringApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cloud.client.SpringCloudApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.scheduling.annotation.EnableScheduling;

import reactor.core.publisher.Hooks;

@SpringCloudApplication
@EnableFeignClients
@EnableScheduling
@EnableCaching
@EnableJpaRepositories
public class GatewayCloudApplication {

	public static void main(String[] args) {
		Hooks.onOperatorDebug();
		SpringApplication.run(GatewayCloudApplication.class, args);
	}
}

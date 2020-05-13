package org.macula.cloud.gateway.openapi;

import org.macula.cloud.core.context.CloudApplicationContext;
import org.macula.cloud.core.event.InstanceProcessEvent;
import org.macula.cloud.core.principal.SubjectAuthentication;
import org.macula.cloud.core.principal.SubjectPrincipal;
import org.macula.cloud.core.principal.SubjectPrincipalCreatedEvent;
import org.springframework.security.authentication.ReactiveAuthenticationManager;
import org.springframework.security.core.Authentication;

import reactor.core.publisher.Mono;

public class OpenApiAuthenticationManager implements ReactiveAuthenticationManager {

	@Override
	public Mono<Authentication> authenticate(Authentication authentication) {
		if (authentication instanceof SubjectAuthentication) {
			if (authentication.getPrincipal() instanceof SubjectPrincipal) {
				String guid = ((SubjectPrincipal) authentication.getPrincipal()).getGuid();
				SubjectPrincipalCreatedEvent event = new SubjectPrincipalCreatedEvent(guid);
				CloudApplicationContext.getContainer().publishEvent(InstanceProcessEvent.wrap(event));
			}
		}
		return Mono.justOrEmpty(authentication);
	}

}

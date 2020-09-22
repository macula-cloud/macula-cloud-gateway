package org.macula.cloud.gateway.event;

import org.macula.cloud.core.domain.GatewayRoute;
import org.macula.cloud.core.event.MaculaEvent;

public class GatewayRouteChangeEvent extends MaculaEvent<GatewayRoute> {

	private static final long serialVersionUID = 1L;

	public GatewayRouteChangeEvent(GatewayRoute source) {
		super(source);
	}

}

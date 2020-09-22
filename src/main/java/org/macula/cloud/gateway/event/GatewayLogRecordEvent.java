package org.macula.cloud.gateway.event;

import org.macula.cloud.core.domain.GatewayLogRecord;
import org.macula.cloud.core.event.MaculaEvent;

public class GatewayLogRecordEvent extends MaculaEvent<GatewayLogRecord> {

	private static final long serialVersionUID = 1L;

	public GatewayLogRecordEvent(GatewayLogRecord source) {
		super(source);
	}

}

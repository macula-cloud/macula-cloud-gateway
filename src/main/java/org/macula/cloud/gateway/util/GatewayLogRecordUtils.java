package org.macula.cloud.gateway.util;

import org.macula.cloud.gateway.domain.SysApplication;
import org.macula.cloud.core.domain.GatewayLogRecord;
import org.springframework.cloud.gateway.route.Route;
import org.springframework.cloud.gateway.support.ServerWebExchangeUtils;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.security.core.Authentication;
import org.springframework.web.server.ServerWebExchange;

import com.alibaba.fastjson.JSONObject;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class GatewayLogRecordUtils {

	public static GatewayLogRecord create(ServerWebExchange exchange, long start, long end,
			Authentication authentication) {
		log.info("GatewayLogRecord: => url [{}], start [{}], end [{}], user [{}]", exchange.getRequest().getURI(),
				start, end, authentication.getName());
		GatewayLogRecord logRecord = new GatewayLogRecord();
		ServerHttpRequest request = exchange.getRequest();
		JSONObject user = (JSONObject) exchange.getAttributes().get(Constants.FILTER_USER);
		SysApplication openApplication = (SysApplication) exchange.getAttributes()
				.get(Constants.FILTER_OPEN_APPLICATION);
		logRecord.setUrl(request.getURI().toString());
		logRecord.setRemoteAddress(
				request.getRemoteAddress().getHostName() + ":" + request.getRemoteAddress().getPort());
		logRecord.setMethod(request.getMethod().toString());
		logRecord.setStatusCode(
				null == exchange.getResponse().getStatusCode() ? 200 : exchange.getResponse().getStatusCode().value());
		logRecord.setUsername(null == user ? "" : (user.getString("username") + ":" + user.getString("origin")));
		logRecord.setApplication(null == openApplication ? "" : openApplication.getAppName());
		logRecord.setStartTime(start);
		logRecord.setEndTime(end);
		logRecord.setHeader(request.getHeaders().toString());
		Route route = (Route) exchange.getAttributes().get(ServerWebExchangeUtils.GATEWAY_ROUTE_ATTR);
		if (null == route) {
			logRecord.setUri(request.getPath().pathWithinApplication().value());
			logRecord.setUriId("gateway");
		} else {
			logRecord.setUri(route.getUri().toString());
			logRecord.setUriId(route.getId());
		}
		return logRecord;
	}
}

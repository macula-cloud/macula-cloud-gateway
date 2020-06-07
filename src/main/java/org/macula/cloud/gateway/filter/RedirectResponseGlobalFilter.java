package org.macula.cloud.gateway.filter;

import java.net.URI;
import java.util.Arrays;

import org.reactivestreams.Publisher;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.cloud.gateway.filter.NettyWriteResponseFilter;
import org.springframework.core.Ordered;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpHeaders;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.http.server.reactive.ServerHttpResponseDecorator;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ServerWebExchange;

import reactor.core.publisher.Mono;

public class RedirectResponseGlobalFilter implements GlobalFilter, Ordered {
	@Override
	public int getOrder() {
		return NettyWriteResponseFilter.WRITE_RESPONSE_FILTER_ORDER - 1;
	}

	@Override
	public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
		ServerHttpResponse originalResponse = exchange.getResponse();
		ServerHttpResponseDecorator decoratedResponse = new ServerHttpResponseDecorator(originalResponse) {
			@Override
			public Mono<Void> writeWith(Publisher<? extends DataBuffer> body) {
				switch (getDelegate().getStatusCode().value()) {
				case 301:
				case 302:
					URI uri = exchange.getRequest().getURI();
					HttpHeaders headers = getDelegate().getHeaders();
					String location = headers.getFirst(HttpHeaders.LOCATION);
					// TODO 根据Route来判断是否需要子路径
					// Route route = exchange.getAttribute(ServerWebExchangeUtils.GATEWAY_ROUTE_ATTR);
					// String mergedUrl = exchange.getAttribute(ServerWebExchangeUtils.GATEWAY_REQUEST_URL_ATTR);
					int i = -1;
					if (!StringUtils.isEmpty(location)) {
						i = location.indexOf("/", 8);
					}
					String newLocation = uri.getScheme() + "://" + uri.getHost() + ":" + (uri.getPort() > 0 ? uri.getPort() : 80);
					if (i > -1) {
						newLocation += location.substring(i);
					}
					headers.put(HttpHeaders.LOCATION, Arrays.asList(newLocation));
					break;
				default:
					break;
				}
				return super.writeWith(body);
			}
		};
		return chain.filter(exchange.mutate().response(decoratedResponse).build());
	}
}

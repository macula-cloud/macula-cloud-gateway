package org.macula.cloud.gateway.props;

import java.util.ArrayList;
import java.util.List;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;

import lombok.Data;

/**
 * 路由配置类
 */
@Data
@RefreshScope
@ConfigurationProperties("macula.document")
public class RouteProperties {

	private final List<RouteResource> resources = new ArrayList<>();

}

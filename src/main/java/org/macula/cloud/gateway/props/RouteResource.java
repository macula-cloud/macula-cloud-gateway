package org.macula.cloud.gateway.props;

import org.macula.cloud.core.utils.CloudConstants;

import lombok.Data;

/**
 * Swagger聚合文档属性
 */
@Data
public class RouteResource {

	/**
	 * 文档名
	 */
	private String name;

	/**
	 * 文档所在服务地址
	 */
	private String location;

	/**
	 * 文档版本
	 */
	private String version = CloudConstants.APPLICATION_VERSION;

}

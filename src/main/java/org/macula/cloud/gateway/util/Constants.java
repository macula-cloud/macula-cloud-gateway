package org.macula.cloud.gateway.util;

import org.springframework.context.annotation.Configuration;

/**
 * Created by linqina on 2018/9/18 下午4:52.
 */
@Configuration
public class Constants {
	// 网关
	public static final String GATEWAY_ROUTES = "geteway_routes";

	// 登录URL
	public static String URL_LOGIN = "login";
	// 不需要经过过滤器的URL
	public static String[] URL_WITHOUT_FILTER = { URL_LOGIN };

	// token超时时间
	public static Long TOKEN_TIMEOUT = 60 * 60L;

	// oauth超时时间
	public static Long OAUTH_TIMEOUT = 30 * 60L;

	// oauth key
	public static String OAUTH_KEY = "oauth_key";

	// token key
	public static String TOKEN_KEY = "token_key";

	// oauth_code key
	public static String OAUTH_CODE_KEY = "oauth_code_key";

	/**
	 * 使用moria的权限系统
	 */
	public static String MORIA_SELF_SYSTEM_YES = "self";
	/**
	 * 使用指定的权限系统
	 */
	public static String MORIA_SELF_SYSTEM_NO = "other";
	/**
	 * 需要登录才可以访问
	 */
	public static String API_NEED_SIGN_IN_YES = "yes";
	/**
	 * 任何用户都可以访问
	 */
	public static String API_NEED_SIGN_IN_NO = "no";

	/**
	 * cookie_token_key
	 */
	public static final String COOKIE_TOKEN_KEY = "SESSION";

	/**
	 * auth_server_token_key
	 */
	public static final String OAUTH_SERVER_SESSION = "OAUTHSERVERSESSION";

	/**
	 * header_token_key
	 */
	public static final String HEADER_TOKEN_KEY = "MACULA-AUTH-TOKEN";

	/**
	 * header_token_key_mobile
	 */
	public static final String HEADER_TOKEN_MOBILE_KEY = "Authorization";

	/**
	 * header_THIRD_PARTY
	 */
	public static final String OPEN_APPLICATION = "OPEN-APPLICATION";

	/**
	 * header_macula_userinfo
	 */
	public static final String HEADER_MACULA_USERINFO = "HEADER-USERINFO";

	/**
	 * header_macula_open_application
	 */
	public static final String HEADER_MACULA_OPEN_APPLICATION = "HEADER-OPEN-APPLICATION";

	/**
	 * AJAX_REQUEST_HEADER
	 */
	public static final String AJAX_REQUEST_HEADER = "X-Requested-With";

	/**
	 * GATEWAY_USER_REQUEST_VALUE
	 */
	public static final String GATEWAY_USER_REQUEST_VALUE = "GatewayUserRequest";

	/**
	 * GATEWAY_OPEN_SERVICE_REQUEST_VALUE
	 */
	public static final String GATEWAY_OPEN_SERVICE_REQUEST_VALUE = "GatewayOpenServiceRequest";

	/**
	 * source_OAuth_ADFS
	 */
	public static final String SOURCE_OAUTH_ADFS = "ADFS";

	/**
	 * source_OAuth_SRM
	 */
	public static final String SOURCE_OAUTH_SRM = "SRM";

	/**
	 * source_OAuth_SNC
	 */
	public static final String SOURCE_OAUTH_SNC = "SNC";

	/**
	 * source_OAuth_NEW
	 */
	public static final String SOURCE_OAUTH_NEW = "NEW";

	/**
	 * source_OAuth_MOBILE
	 */
	public static final String SOURCE_OAUTH_MOBILE = "MOBILE";

	/**
	 * LOGIN_BY_ADFS
	 */
	public static final String LOGIN_BY_ADFS = "LOGIN_BY_ADFS";

	/**
	 * LOGIN_BY_OAUTH2
	 */
	public static final String LOGIN_BY_OAUTH2 = "LOGIN_BY_OAUTH2";

	/**
	 * USERNAME_ADD_ORIGIN
	 */
	public static final String USERNAME_ADD_ORIGIN_TAG = "<-macula->";

	public static final String FILTER_USER = "FILTER_USER";

	public static final String FILTER_HAS_LOG = "FILTER_HAS_LOG";

	public static final String FILTER_OPEN_APPLICATION = "FILTER_OPEN_APPLICATION";

	/**
	 * JWT 秘钥 重要保密！
	 */
	public static final String JWT_SECRET = "93b0020e736c4aac818a733696969391";

}

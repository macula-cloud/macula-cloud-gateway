package org.macula.cloud.gateway.openapi;

import java.util.Date;
import java.util.TreeMap;

import org.macula.cloud.core.exception.OpenApiParameterException;
import org.macula.cloud.core.utils.ServerRequestUtils;
import org.macula.cloud.core.utils.SignatureUtils;
import org.macula.cloud.core.utils.StringUtils;
import org.macula.cloud.core.utils.SystemUtils;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.util.MultiValueMap;

public class OpenApiAuthenticationHelper {

	public final static String APP_KEY = "app_key";

	public final static String SIGN = "sign";

	public final static String TIMESTAMP = "timestamp";

	public final static String SESSION = "session";

	public final static String SESSION_USER = "session_user";

	public final static String FORMAT = "format";

	public final static String FORMAT_XML = "xml";

	public final static String FORMAT_JSON = "json";

	public final static String VERSION = "v";

	public final static String SIGN_METHOD = "sign_method";

	public final static String SIGN_METHOD_MD5 = "md5";

	public final static String SIGN_METHOD_HMAC = "hmac";

	public final static String ACCESS_TOKEN = "access_token";

	public static boolean validate(ServerHttpRequest request, ServerHttpResponse response, String appSecret) {
		MultiValueMap<String, String> queryParams = request.getQueryParams();
		// 检测是否含有appKey
		// 检测是否含有timestamp
		// 检测是否含有sign
		String appKey = queryParams.getFirst(APP_KEY);
		String timestamp = queryParams.getFirst(TIMESTAMP);
		String sign = queryParams.getFirst(SIGN);
		String signMethod = queryParams.getFirst(SIGN_METHOD);
		// 0. 检测必填参数是否齐全
		if (StringUtils.isEmpty(appKey) || StringUtils.isEmpty(sign) || StringUtils.isEmpty(timestamp)) {
			// 抛出必填参数不足的异常
			throw new OpenApiParameterException("param.10");
		}

		// 1.提取appSecret，判断appKey是否存在
		// String appSecret = application == null ? "" : application.getSecureKey();
		if (StringUtils.isEmpty(appSecret)) {
			throw new OpenApiParameterException("param.20");
		}

		// 2.检测签名是否有效
		// 将所有请求参数除sign和图片等除外放入TreeMap
		TreeMap<String, String> params = ServerRequestUtils.getOpenApiRequestParams(request);

		// 检测SIGN是否有效
		String signed = null;
		if (StringUtils.isNotEmpty(signMethod) && SIGN_METHOD_HMAC.equals(signMethod)) {
			signed = SignatureUtils.hmacSignature(params, appSecret);
		} else {
			signed = SignatureUtils.md5Signature(params, appSecret);
		}
		if (!sign.equals(signed)) {
			// 检测是否含有sessionKey
			// 不正常则抛出500响应，并且返回Response类型的异常信息
			// 抛出参数签名错误的异常
			throw new OpenApiParameterException("param.30");
		}

		// 3.检测timestamp是否超时
		Date now = SystemUtils.getCurrentTime();
		long diff = (now.getTime() - Long.parseLong(timestamp));
		if (diff > 5 * 60 * 1000) {
			// 请求超时异常
			throw new OpenApiParameterException("param.40");
		}

		// 4.判断客户端需要的格式
		String format = queryParams.getFirst(FORMAT);
		if (StringUtils.isEmpty(format)) {
			// 默认是JSON格式
			format = FORMAT_JSON;
		}
		if (FORMAT_JSON.equals(format) || FORMAT_XML.equals(format)) {
			return true;
		}
		throw new OpenApiParameterException("param.50");
	}

	public static String getRequestUsername(ServerHttpRequest request) {
		return request.getQueryParams().getFirst(APP_KEY);
	}

	public static String getAccessTokenFromRequest(ServerHttpRequest request) {
		String accessToken = request.getQueryParams().getFirst(OpenApiAuthenticationHelper.ACCESS_TOKEN);
		if (StringUtils.isEmpty(accessToken)) {
			accessToken = request.getHeaders().getFirst(OpenApiAuthenticationHelper.ACCESS_TOKEN);
		}
		return accessToken;
	}
}

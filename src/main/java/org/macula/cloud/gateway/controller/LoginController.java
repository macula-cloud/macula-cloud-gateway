package org.macula.cloud.gateway.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.apache.commons.lang.StringUtils;
import org.macula.cloud.gateway.domain.OAuth2LoginReq;
import org.macula.cloud.gateway.domain.SysAuthority;
import org.macula.cloud.gateway.repository.RedisRepository;
import org.macula.cloud.gateway.service.LoginService;
import org.macula.cloud.gateway.util.Constants;
import org.macula.cloud.gateway.util.JWTUtil;
import org.macula.cloud.gateway.util.TimeUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.server.ServerWebExchange;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;

import eu.bitwalker.useragentutils.Browser;
import eu.bitwalker.useragentutils.BrowserType;
import io.jsonwebtoken.Claims;

/**
 * Created by linqina on 2018/12/8 3:58 PM.
 */
//@RequestMapping
//@RestController
public class LoginController {
	@Autowired
	private LoginService loginService;
	@Autowired
	private RedisRepository redisRepository;
	@Value("${adfs.logoutADFSUri}")
	private String logoutADFSUri;
	@Value("${gate.sessionTime}")
	private Long sessionTime;

	@PostMapping("/api/portal/login")
	@ResponseBody
	public ResponseEntity<?> login(@RequestBody OAuth2LoginReq oAuth2LoginReq,
			@RequestHeader(value = "Cookie", required = false) String oriCookie, ServerHttpResponse response,
			ServerWebExchange exchange) {
		this.saveCurrentUser(exchange, oAuth2LoginReq.getUsername(), oAuth2LoginReq.getSource());
		JSONObject jsonObject = new JSONObject();
		jsonObject.put("code", 0);
		if (StringUtils.isBlank(oAuth2LoginReq.getUsername()) || StringUtils.isBlank(oAuth2LoginReq.getPassword())
				|| StringUtils.isBlank(oAuth2LoginReq.getVcode()) || StringUtils.isBlank(oAuth2LoginReq.getSource())
				|| Constants.SOURCE_OAUTH_ADFS.equals(oAuth2LoginReq.getSource())) {
			jsonObject.put("code", -1);
			jsonObject.put("message", "参数有误");
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(jsonObject);
		}

		String cookie = loginService.loginOAuth2(oAuth2LoginReq, null, false, oriCookie);
		if (cookie.indexOf("false") != -1) {
			jsonObject.put("code", -1);
			jsonObject.put("message", "null".equals(cookie.substring(5)) ? "系统繁忙，请稍后重试..." : cookie.substring(5));
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(jsonObject);
		}
		// 进行下一步认证
		String code = loginService.authorize(cookie);
		if ("false".equals(code)) {
			jsonObject.put("code", -1);
			jsonObject.put("message", "认证失败");
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(jsonObject);
		}

		System.out.println("code==>" + code);
		if ("false".equals(code)) {
			jsonObject.put("code", -1);
			jsonObject.put("message", "认证失败");
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(jsonObject);
		}

		// 登录OAuth获取token
		String token = loginService.token(code);
		String userInfo = loginService.getUserInfoUri(token);
		if ("false".equals(userInfo)) {
			jsonObject.put("code", -1);
			jsonObject.put("message", "认证失败");
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(jsonObject);
		}
		// 生成网关sessionId
		String gatewaySessionId = UUID.randomUUID().toString().replaceAll("-", "");
		// 将sessionId及用户信息存到redis
		loginService.saveToken(gatewaySessionId, userInfo, token, Constants.LOGIN_BY_OAUTH2);

		// set网关cookie到浏览器
		ResponseCookie responseCookie = ResponseCookie.from(Constants.COOKIE_TOKEN_KEY, gatewaySessionId).httpOnly(true)
				.path("/").secure(false).build();
		response.addCookie(responseCookie);
		// 将OAuth返回的cookie给浏览器
		String[] keyValues = cookie.split("=");
		ResponseCookie responseCookie1 = ResponseCookie.from(keyValues[0], keyValues[1]).httpOnly(true).path("/")
				.secure(false).build();
		response.addCookie(responseCookie1);

		jsonObject.put("message", "登录成功");
		JSONObject tokenData = new JSONObject();
		tokenData.put("data", gatewaySessionId);
		jsonObject.put("result", tokenData);
		return ResponseEntity.status(HttpStatus.OK).body(jsonObject);

	}

	@PostMapping("/api/portal/mobile/login")
	@ResponseBody
	public ResponseEntity<?> loginMobile(@RequestBody OAuth2LoginReq oAuth2LoginReq,
			@RequestHeader(value = "User-Agent", required = false) String userAgent, ServerWebExchange exchange) {
		this.saveCurrentUser(exchange, oAuth2LoginReq.getUsername(), oAuth2LoginReq.getSource());
		JSONObject jsonObject = new JSONObject();
		if (StringUtils.isBlank(oAuth2LoginReq.getUsername()) || StringUtils.isBlank(oAuth2LoginReq.getPassword())
				|| StringUtils.isBlank(oAuth2LoginReq.getSource())) {
			jsonObject.put("code", -1);
			jsonObject.put("message", "参数有误");
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(jsonObject);
		}
		jsonObject.put("code", -1);
		oAuth2LoginReq.setSource("NEW");
		// 是否来自移动端的请求
		if (BrowserType.MOBILE_BROWSER == Browser.parseUserAgentString(userAgent).getBrowserType()) {
			String cookie = loginService.loginOAuth2(oAuth2LoginReq, null, true, null);
			if (cookie.indexOf("false") != -1) {
				jsonObject.put("code", -1);
				jsonObject.put("message", cookie.substring(5));
				return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(jsonObject);
			}

			// 进行下一步认证
			String code = loginService.authorize(cookie);
			if ("false".equals(code)) {
				jsonObject.put("code", -1);
				jsonObject.put("message", "认证失败");
				return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(jsonObject);
			}

			System.out.println("code==>" + code);
			// 登录OAuth获取token
			String token = loginService.token(code);
			String userInfo = loginService.getUserInfoUri(token);
			if ("false".equals(userInfo)) {
				jsonObject.put("code", -1);
				jsonObject.put("message", "认证失败");
				return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(jsonObject);
			}

			String user = loginService.getUserInfo2String(userInfo, token, Constants.LOGIN_BY_OAUTH2);
			JSONObject userRes = JSON.parseObject(user);
			String jwtId = UUID.randomUUID().toString().replaceAll("-", "");
			// 创建payload的私有声明（根据特定的业务需要添加，如果要拿这个做验证，一般是需要和jwt的接收方提前沟通好验证方式的）
			Map<String, Object> claims = new HashMap<String, Object>();
			claims.put("uid", userRes.getString("userId"));
			claims.put("user_name", userRes.getString("username"));
			claims.put("full_name", userRes.getString("fullName"));
			claims.put("nick_name", userRes.getString("nickName"));
			claims.put("phone", userRes.getString("phone"));
			claims.put("company", userRes.getString("company"));
			claims.put("companyId", userRes.getString("companyId"));
			claims.put("department", userRes.getString("department"));
			claims.put("departmentId", userRes.getString("departmentId"));
			claims.put("departmentCode", userRes.getString("departmentCode"));
			claims.put("departmentShortName", userRes.getString("departmentShortName"));
			claims.put("roleList", userRes.get("roleList"));
			claims.put("permissions", userRes.get("permissions"));
			claims.put("email", userRes.getString("email"));
			claims.put("origin", userRes.getString("origin"));
			String jwt = JWTUtil.createJWT(jwtId, userRes.getString("username"), claims, TimeUtil.expDayInMillis());
			jsonObject.put("result", jwt);
			jsonObject.put("message", "登录成功");
			jsonObject.put("code", 0);
			return ResponseEntity.status(HttpStatus.OK).body(jsonObject);
		} else {
			jsonObject.put("code", -1);
			jsonObject.put("message", "非法请求");
			return ResponseEntity.status(HttpStatus.FORBIDDEN).body(jsonObject);
		}
	}

	/**
	 * 返回的jwt已包含该权限列表信息，准备废弃该方法
	 *
	 * @param mobileToken
	 * @return
	 */
	@GetMapping("/api/portal/mobile/authoritis")
	public ResponseEntity<?> mobileAuthority(
			@RequestHeader(value = Constants.HEADER_TOKEN_MOBILE_KEY) String mobileToken, ServerWebExchange exchange) {
		JSONObject jsonObject = new JSONObject();
		jsonObject.put("code", -1);
		jsonObject.put("message", "非法请求");
		if (mobileToken.startsWith("Bearer ")) {
			try {
				Claims claims = JWTUtil.parseJWT(mobileToken.substring(7));
				long timeStamp = claims.getExpiration().getTime();
				if (System.currentTimeMillis() > timeStamp) {
					return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(jsonObject);
				}
				String username = claims.getSubject();
				List<SysAuthority> permissions = loginService.getUserAuthority(username,
						claims.get("origin", String.class));
				jsonObject.put("code", 0);
				jsonObject.put("message", "获取权限列表成功");
				jsonObject.put("result", permissions);
				this.saveCurrentUser(exchange, username, claims.get("origin", String.class));
			} catch (Exception e) {
				e.printStackTrace();
				return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(jsonObject);
			}
		}
		return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(jsonObject);
	}

	@GetMapping("/api/portal/logout")
	public ResponseEntity<?> logout(@CookieValue(value = Constants.COOKIE_TOKEN_KEY, required = false) String cookie,
			@CookieValue(value = Constants.OAUTH_SERVER_SESSION, required = false) String oauthCookie,
			ServerWebExchange exchange) {
		JSONObject jsonObject = new JSONObject();
		jsonObject.put("code", 0);
		JSONObject ori = new JSONObject();
		ori.put("origin", Constants.LOGIN_BY_OAUTH2);

		// 删除cookie
		if (StringUtils.isNotBlank(cookie)) {
			String token = redisRepository.get(cookie);
			if (StringUtils.isNotBlank(token)) {
				JSONObject obj = JSON.parseObject(token);
				if (Constants.SOURCE_OAUTH_ADFS.equals(obj.getString("origin"))) {
					ori.put("origin", Constants.LOGIN_BY_ADFS);
					ori.put("logoutUri", logoutADFSUri);
				}
				this.saveCurrentUser(exchange, obj.getString("username"), obj.getString("origin"));
				redisRepository.del(cookie);
			}
		}

		// 删除oauth的cookie
		if (StringUtils.isNotBlank(oauthCookie)) {
			if (redisRepository.hasKey(oauthCookie)) {
				redisRepository.del(oauthCookie);
			}
			loginService.logoutOAuth2(oauthCookie);
		}

		jsonObject.put("result", ori);
		jsonObject.put("message", "登出成功");
		return ResponseEntity.status(HttpStatus.OK).body(jsonObject);
	}

	@PostMapping(value = "/api/portal/sso/login", consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
	@ResponseBody
	public ResponseEntity<?> login(@RequestParam(value = "redirect", required = false) String redirect,
			@RequestBody() String adfsToken, @RequestHeader(value = "Cookie", required = false) String oriCookie,
			ServerHttpResponse response, ServerWebExchange exchange) {
		JSONObject jsonObject = new JSONObject();
		jsonObject.put("code", -1);
		jsonObject.put("message", "认证失败");
		if (StringUtils.isNotBlank(adfsToken)) {
			for (String keyValue : adfsToken.split("&")) {
				if (keyValue.startsWith("Token")) {
					adfsToken = keyValue.substring(6);
					break;
				}
			}
		} else {
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(jsonObject);
		}

		String userInfoADFS = loginService.validateADFSToken(adfsToken);
		if ("false".equals(userInfoADFS)) {
			jsonObject.put("message", "员工平台验证token失败");
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(jsonObject);
		}

		// 进行下一步认证
		jsonObject.put("code", 0);
		JSONObject user = JSON.parseObject(userInfoADFS);
		this.saveCurrentUser(exchange, user.getString("employeeID"), Constants.SOURCE_OAUTH_ADFS);
		String cookie = loginService.loginOAuth2(null, user.getString("employeeID"), false, oriCookie);

		if (cookie.indexOf("false") != -1) {
			jsonObject.put("code", -1);
			jsonObject.put("message", "OAuth2认证失败：" + cookie.substring(5));
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(jsonObject);
		}
		// 进行下一步认证
		String code = loginService.authorize(cookie);
		if ("false".equals(code)) {
			jsonObject.put("code", -1);
			jsonObject.put("message", "OAuth2授权失败");
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(jsonObject);
		}

		// 登录OAuth获取token
		String token = loginService.token(code);
		String userInfoOAuth = loginService.getUserInfoUri(token);
//                    String user = loginService.getUserInfoUri(token);
		// 生成网关sessionId
		String gatewaySessionId = UUID.randomUUID().toString().replaceAll("-", "");
		// 将sessionId及用户信息存到redis
		loginService.saveToken(gatewaySessionId, userInfoOAuth, token, Constants.LOGIN_BY_ADFS);

		// set网关cookie到浏览器
		ResponseCookie responseCookie = ResponseCookie.from(Constants.COOKIE_TOKEN_KEY, gatewaySessionId).httpOnly(true)
				.path("/").secure(false).build();
		response.addCookie(responseCookie);
		// 将OAuth返回的cookie给浏览器
		String[] keyValues = cookie.split("=");
		ResponseCookie responseCookie1 = ResponseCookie.from(keyValues[0], keyValues[1]).httpOnly(true).path("/")
				.secure(false).build();
		response.addCookie(responseCookie1);

		jsonObject.put("message", "登录成功");
		JSONObject tokenData = new JSONObject();
		tokenData.put("data", token);
		jsonObject.put("result", tokenData);
		if (StringUtils.isNotBlank(redirect)) {
			response.getHeaders().add("Location", redirect);
			return ResponseEntity.status(HttpStatus.FOUND).body(jsonObject);
		} else {
			return ResponseEntity.status(HttpStatus.OK).body(jsonObject);
		}
	}

	@GetMapping("/api/portal/userInfo")
	public ResponseEntity<?> userInfo(@CookieValue(value = Constants.COOKIE_TOKEN_KEY, required = false) String cookie,
			ServerWebExchange exchange) {
		JSONObject jsonObject = new JSONObject();
		String token = null;
		String user = null;
		jsonObject.put("code", 0);
		if (!StringUtils.isBlank(cookie)) {
			user = redisRepository.get(cookie);
			if (null == user) {
				// 之前的代码，当gateway登出时，还会去找oauth验证一下是否还是登录中，如果是则自动登录；
//                            if (cookiePeer1.indexOf(Constants.OAUTH_SERVER_SESSION) != -1) {
//                                String gatewaySessionId = loginService.validate(cookiePeer1);
//                                if (null == gatewaySessionId) {
//                                    jsonObject.put("code", -1);
//                                    jsonObject.put("message", "请先登录");
//                                    return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(jsonObject);
//                                } else {
//                                    ResponseCookie responseCookie = ResponseCookie.from(Constants.COOKIE_TOKEN_KEY, gatewaySessionId).httpOnly(true).path("/").secure(false).build();
//                                    response.addCookie(responseCookie);
//                                    user = redisRepository.get(gatewaySessionId);
//                                    token = gatewaySessionId;
//                                }
//                                break;
//                            }
//                        }
				jsonObject.put("code", -1);
				jsonObject.put("message", "请先登录");
				return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(jsonObject);
			}
			token = cookie;
			redisRepository.refreshLiveTime(cookie, sessionTime);
		} else {
			jsonObject.put("code", -1);
			jsonObject.put("message", "请先登录");
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(jsonObject);
		}
		if (StringUtils.isBlank(user)) {
			jsonObject.put("code", -1);
			jsonObject.put("message", "请先登录");
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(jsonObject);
		}
		jsonObject.put("message", "获取用户信息");
		JSONObject resData = new JSONObject();
		JSONObject userRes = JSON.parseObject(user);
		userRes.put("token", token);
		resData.put("data", userRes);
		jsonObject.put("result", resData);
		this.saveCurrentUser(exchange, userRes.getString("username"), userRes.getString("origin"));
		return ResponseEntity.status(HttpStatus.OK).body(jsonObject);
	}

	private void saveCurrentUser(ServerWebExchange serverWebExchange, String username, String origin) {
		JSONObject user = new JSONObject();
		user.put("username", username);
		user.put("origin", origin);
		serverWebExchange.getAttributes().put(Constants.FILTER_USER, user);
	}

}

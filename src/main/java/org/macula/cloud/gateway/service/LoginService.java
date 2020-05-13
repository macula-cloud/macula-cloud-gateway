package org.macula.cloud.gateway.service;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang.StringUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.macula.cloud.gateway.configure.OAuth2Config;
import org.macula.cloud.gateway.domain.OAuth2LoginReq;
import org.macula.cloud.gateway.domain.SysAuthority;
import org.macula.cloud.gateway.repository.RedisRepository;
import org.macula.cloud.gateway.util.Constants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

@Service
public class LoginService {

	private String loginUrl;
	private String authorizeUrl;
	private String tokenUrl;
	private String client_id;
	private String secert;
	private String authorizationGrantType;
	private String redirect_uri;
	private String userInfoUri;
	private String logoutOAuth2Uri;
	private String adfsUserInfoUri;
	private Long sessionTime = 180000L;

	@Autowired
	private IUserService userService;

	@Autowired
	private RedisRepository redisRepository;

	public LoginService(OAuth2Config oauth2Config) {
		loginUrl = oauth2Config.getLoginUrl();
		authorizeUrl = oauth2Config.getAuthorizeUrl();
		tokenUrl = oauth2Config.getTokenUrl();
		client_id = oauth2Config.getClientId();
		secert = oauth2Config.getSecert();
		authorizationGrantType = oauth2Config.getAuthorizationGrantType();
		redirect_uri = oauth2Config.getRedirectUri();
		userInfoUri = oauth2Config.getUserInfoUri();
		logoutOAuth2Uri = oauth2Config.getLogoutOAuth2Uri();

	}

	public String loginOAuth2(OAuth2LoginReq oAuth2LoginReq, String username, boolean isMobile, String oriCookie) {
		try {
			HttpClient httpClient = HttpClientBuilder.create().build();
			HttpPost httpPost = new HttpPost(loginUrl);
			List<BasicNameValuePair> pairList = new ArrayList<BasicNameValuePair>();
			if (null != oAuth2LoginReq) {
				pairList.add(new BasicNameValuePair("username",
						oAuth2LoginReq.getUsername() + Constants.USERNAME_ADD_ORIGIN_TAG + oAuth2LoginReq.getSource()));
				pairList.add(new BasicNameValuePair("password", oAuth2LoginReq.getPassword()));
				pairList.add(new BasicNameValuePair("vcode", oAuth2LoginReq.getVcode()));
				if (isMobile) {
					pairList.add(new BasicNameValuePair("source", Constants.SOURCE_OAUTH_MOBILE));
				} else {
					pairList.add(new BasicNameValuePair("source", oAuth2LoginReq.getSource()));
				}
			} else {
				pairList.add(new BasicNameValuePair("username",
						username + Constants.USERNAME_ADD_ORIGIN_TAG + Constants.SOURCE_OAUTH_ADFS));
				pairList.add(new BasicNameValuePair("source", Constants.SOURCE_OAUTH_ADFS));
			}
			httpPost.setEntity(new UrlEncodedFormEntity(pairList, "utf-8"));
			httpPost.setHeader("Cookie", oriCookie);
			HttpResponse response = httpClient.execute(httpPost);
			HttpEntity httpEntity = response.getEntity();
			String loginRes = EntityUtils.toString(httpEntity);
			JSONObject obj = JSON.parseObject(loginRes);
			int statusMsg = obj.getIntValue("httpStatus");
			String msg = obj.getString("errorMessage");
			if (HttpStatus.SC_OK == statusMsg) {
				String cookie = response.getFirstHeader("Set-Cookie").toString();
				cookie = cookie.substring(cookie.lastIndexOf("Set-Cookie:") + 12, cookie.indexOf(";"));
				return cookie;
			} else {
				return "false" + msg;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return "false";
	}

	public String authorize(String cookie) {
		try {
			HttpClient httpClient = HttpClientBuilder.create().build();
			HttpPost httpPost = new HttpPost(authorizeUrl);
			List<BasicNameValuePair> pairList = new ArrayList<BasicNameValuePair>();
			pairList.add(new BasicNameValuePair("client_id", client_id));
			pairList.add(new BasicNameValuePair("redirect_uri", redirect_uri));
			pairList.add(new BasicNameValuePair("response_type", "code"));
			httpPost.setEntity(new UrlEncodedFormEntity(pairList, "utf-8"));
			httpPost.addHeader("Cookie", cookie);
			HttpResponse response = httpClient.execute(httpPost);
			int status = response.getStatusLine().getStatusCode();
			if (HttpStatus.SC_MOVED_TEMPORARILY == status) {
				String location = response.getFirstHeader("Location").toString();
				String code = location.substring(location.lastIndexOf("code=") + 5);
				return code;
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		return "false";
	}

	public String token(String code) {
		try {
			HttpClient httpClient = HttpClientBuilder.create().build();
			HttpPost httpPost = new HttpPost(tokenUrl);
			List<BasicNameValuePair> pairList = new ArrayList<BasicNameValuePair>();
			pairList.add(new BasicNameValuePair("grant_type", authorizationGrantType));
			pairList.add(new BasicNameValuePair("redirect_uri", redirect_uri));
			pairList.add(new BasicNameValuePair("code", code));
			httpPost.setEntity(new UrlEncodedFormEntity(pairList, "utf-8"));
			String auth = client_id + ":" + secert;
			byte[] encodedAuth = Base64.encodeBase64(auth.getBytes(Charset.forName("utf-8")));
			httpPost.addHeader("Authorization", "Basic " + new String(encodedAuth));
			HttpResponse response = httpClient.execute(httpPost);
			int status = response.getStatusLine().getStatusCode();
			if (HttpStatus.SC_OK == status) {
				HttpEntity httpEntity = response.getEntity();
				String tokenRes = EntityUtils.toString(httpEntity);
				JSONObject token = JSON.parseObject(tokenRes);
//                String token = UUID.randomUUID().toString().replaceAll("-", "");
				return token.getString("access_token");
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		return "false";
	}

	public String getUserInfoUri(String token) {
		try {
			HttpClient httpClient = HttpClientBuilder.create().build();
			HttpGet httpGet = new HttpGet(userInfoUri + "?access_token=" + token);
			HttpResponse response = httpClient.execute(httpGet);
			int status = response.getStatusLine().getStatusCode();
			if (HttpStatus.SC_OK == status) {
				HttpEntity httpEntity = response.getEntity();
				String userRes = EntityUtils.toString(httpEntity);
				return userRes;
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		return "false";
	}

	// 验证其OAuth2是否已经登录过
	public String validate(String cookie) {
		String gatewaySessionId = null;
		String code = this.authorize(cookie);
		if ("false".equals(code)) {
			return gatewaySessionId;
		} else {
			// 登录OAuth获取token
			String token = this.token(code);
			if ("false".equals(token)) {
				return gatewaySessionId;
			} else {
				String user = this.getUserInfoUri(token);
				if ("false".equals(user)) {
					return gatewaySessionId;
				} else {
					// 生成网关sessionId
					gatewaySessionId = UUID.randomUUID().toString().replaceAll("-", "");
					// 将sessionId及用户信息存到redis
					this.saveToken(gatewaySessionId, user, token, Constants.LOGIN_BY_OAUTH2);
				}
			}
		}
		return gatewaySessionId;
	}

	// 验证ADFS的Token是否有效
	public String validateADFSToken(String token) {
		try {
			HttpClient httpClient = HttpClientBuilder.create().build();
			HttpPost httpPost = new HttpPost(adfsUserInfoUri);
			List<BasicNameValuePair> pairList = new ArrayList<BasicNameValuePair>();
			pairList.add(new BasicNameValuePair("Token", token));
			httpPost.setEntity(new UrlEncodedFormEntity(pairList, "utf-8"));
			HttpResponse response = httpClient.execute(httpPost);
			int status = response.getStatusLine().getStatusCode();
			if (HttpStatus.SC_OK == status) {
				HttpEntity httpEntity = response.getEntity();
				String userRes = EntityUtils.toString(httpEntity);
				if (StringUtils.isNotBlank(userRes)) {
					return userRes;
				} else {
					return "false";
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		return "false";
	}

	public void logoutOAuth2(String cookie) {
		try {
			HttpClient httpClient = HttpClientBuilder.create().build();
			HttpGet httpGet = new HttpGet(logoutOAuth2Uri);
			httpGet.setHeader("Cookie", cookie);
			httpClient.execute(httpGet);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public String getUserInfo2String(String userInfo, String token, String origin) {
		JSONObject user = JSON.parseObject(userInfo);
		JSONObject userAuthentication = user.getJSONObject("userAuthentication");
		String username = userAuthentication.getString("name");
		JSONObject details = userAuthentication.getJSONObject("details").getJSONObject("object");
		if (Constants.LOGIN_BY_ADFS.equals(origin)) {
			origin = Constants.SOURCE_OAUTH_ADFS;
		} else {
			origin = details.getString("source");
		}

		// 将sessionId及用户信息存到redis
		JSONObject tokenValue = new JSONObject();
		tokenValue.put("access_token", token);
		tokenValue.put("userId", details.getString("id"));
		tokenValue.put("username", username);
		tokenValue.put("fullName", details.getString("fullName"));
		tokenValue.put("nickName", details.getString("nickName"));
		tokenValue.put("email", details.getString("email"));
		tokenValue.put("phone", details.getString("phone"));
		tokenValue.put("employeeId", details.getString("employeeId"));
		tokenValue.put("company", details.getString("company"));
		tokenValue.put("companyId", details.getString("companyId"));
		tokenValue.put("department", details.getString("department"));
		tokenValue.put("departmentId", details.getString("departmentId"));
		tokenValue.put("departmentCode", details.getString("departmentCode"));
		tokenValue.put("departmentShortName", details.getString("departmentShortName"));
		tokenValue.put("roleList", details.get("roleList"));
		tokenValue.put("origin", origin);
		try {
			tokenValue.put("permissions", this.getUserAuthority(username, origin));
		} catch (Exception e) {
			tokenValue.put("permissions", new JSONArray());
			System.out.println("获取权限列表失败：" + e.getMessage());
		}
		return tokenValue.toString();
	}

	public void saveToken(String gatewaySessionId, String userInfo, String token, String origin) {
		String tokenValue = this.getUserInfo2String(userInfo, token, origin);
		if (!"false".equals(tokenValue)) {
			redisRepository.set(gatewaySessionId, tokenValue, sessionTime);
		}
	}

	public List<SysAuthority> getUserAuthority(String username, String origin) {
		List<SysAuthority> permissions = null;
		String usernameAndSource = username + Constants.USERNAME_ADD_ORIGIN_TAG + origin;
		try {
			permissions = userService.getPermissionByUsername(usernameAndSource);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return permissions;
	}
}

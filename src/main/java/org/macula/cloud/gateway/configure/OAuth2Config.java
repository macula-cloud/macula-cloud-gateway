package org.macula.cloud.gateway.configure;

public class OAuth2Config {

	private String loginUrl;
	private String authorizeUrl;
	private String tokenUrl;
	private String clientId;
	private String secert;
	private String authorizationGrantType;
	private String redirectUri;
	private String userInfoUri;
	private String logoutOAuth2Uri;

	public String getLoginUrl() {
		return loginUrl;
	}

	public void setLoginUrl(String loginUrl) {
		this.loginUrl = loginUrl;
	}

	public String getAuthorizeUrl() {
		return authorizeUrl;
	}

	public void setAuthorizeUrl(String authorizeUrl) {
		this.authorizeUrl = authorizeUrl;
	}

	public String getTokenUrl() {
		return tokenUrl;
	}

	public void setTokenUrl(String tokenUrl) {
		this.tokenUrl = tokenUrl;
	}

	public String getClientId() {
		return clientId;
	}

	public void setClientId(String clientId) {
		this.clientId = clientId;
	}

	public String getSecert() {
		return secert;
	}

	public void setSecert(String secert) {
		this.secert = secert;
	}

	public String getAuthorizationGrantType() {
		return authorizationGrantType;
	}

	public void setAuthorizationGrantType(String authorizationGrantType) {
		this.authorizationGrantType = authorizationGrantType;
	}

	public String getRedirectUri() {
		return redirectUri;
	}

	public void setRedirectUri(String redirectUri) {
		this.redirectUri = redirectUri;
	}

	public String getUserInfoUri() {
		return userInfoUri;
	}

	public void setUserInfoUri(String userInfoUri) {
		this.userInfoUri = userInfoUri;
	}

	public String getLogoutOAuth2Uri() {
		return logoutOAuth2Uri;
	}

	public void setLogoutOAuth2Uri(String logoutOAuth2Uri) {
		this.logoutOAuth2Uri = logoutOAuth2Uri;
	}

}

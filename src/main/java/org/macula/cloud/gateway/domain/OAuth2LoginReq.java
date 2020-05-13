package org.macula.cloud.gateway.domain;

/**
 * Created by linqina on 2018/12/8 5:42 PM.
 */
public class OAuth2LoginReq {
	private String username;

	private String password;

	private String vcode;

	private String source;

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public String getVcode() {
		return vcode;
	}

	public void setVcode(String vcode) {
		this.vcode = vcode;
	}

	public String getSource() {
		return source;
	}

	public void setSource(String source) {
		this.source = source;
	}
}

package org.macula.cloud.gateway.domain;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * @Auther: Aaron
 * @Date: 2018/12/5 15:11
 * @Description:
 */

@JsonIgnoreProperties(ignoreUnknown = true)
public class SysApplication {

	private Long id;

	private String appId;

	private String appName;

	private String appKey;

	private String appSecret;

	List<SysAuthority> authorities;

	public SysApplication() {
	}

	public SysApplication(Long id, String appId, String appName, String appKey, String appSecret,
			List<SysAuthority> authorities) {
		this.id = id;
		this.appId = appId;
		this.appName = appName;
		this.appKey = appKey;
		this.appSecret = appSecret;
		this.authorities = authorities;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getAppId() {
		return appId;
	}

	public void setAppId(String appId) {
		this.appId = appId;
	}

	public String getAppName() {
		return appName;
	}

	public void setAppName(String appName) {
		this.appName = appName;
	}

	public String getAppKey() {
		return appKey;
	}

	public void setAppKey(String appKey) {
		this.appKey = appKey;
	}

	public String getAppSecret() {
		return appSecret;
	}

	public void setAppSecret(String appSecret) {
		this.appSecret = appSecret;
	}

	public List<SysAuthority> getAuthorities() {
		return authorities;
	}

	public void setAuthorities(List<SysAuthority> authorities) {
		this.authorities = authorities;
	}
}

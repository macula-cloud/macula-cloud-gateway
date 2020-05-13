package org.macula.cloud.gateway.domain;

import java.io.Serializable;
import java.util.List;

/**
 * @Auther: Aaron
 * @Date: 2018/9/5 16:06
 * @Description:
 */
public class AccessToken implements Serializable {

	private String token;

	private String userId;

	/**
	 * 角色集合
	 */
	private List<String> roleIds;

	/**
	 * 用户部门集合，一个用户可能有多个部门
	 */
	private List<String> deptIds;

	/**
	 * 过期时间
	 */
	private Long timeout;

	public String getToken() {
		return token;
	}

	public void setToken(String token) {
		this.token = token;
	}

	public Long getTimeout() {
		return timeout;
	}

	public void setTimeout(Long timeout) {
		this.timeout = timeout;
	}

	public String getUserId() {
		return userId;
	}

	public void setUserId(String userId) {
		this.userId = userId;
	}

	public List<String> getRoleIds() {
		return roleIds;
	}

	public void setRoleIds(List<String> roleIds) {
		this.roleIds = roleIds;
	}

	public List<String> getDeptIds() {
		return deptIds;
	}

	public void setDeptIds(List<String> deptIds) {
		this.deptIds = deptIds;
	}
}

package org.macula.cloud.gateway.domain;

public class SysAuthority {

	private String code;
	private Integer type;
	private String uri;
	private String method;
	private String name;

	public SysAuthority() {
	}

	public SysAuthority(String code, Integer type, String uri, String method, String name) {
		this.code = code;
		this.type = type;
		this.uri = uri;
		this.method = method;
		this.name = name;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getMethod() {
		return method;
	}

	public void setMethod(String method) {
		this.method = method;
	}

	public String getCode() {
		return code;
	}

	public void setCode(String code) {
		this.code = code;
	}

	public Integer getType() {
		return type;
	}

	public void setType(Integer type) {
		this.type = type;
	}

	public String getUri() {
		return uri;
	}

	public void setUri(String uri) {
		this.uri = uri;
	}

}

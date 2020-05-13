package org.macula.cloud.gateway.service.fallback;

import java.util.List;

import org.macula.cloud.gateway.domain.SysApplication;
import org.macula.cloud.gateway.domain.SysAuthority;
import org.macula.cloud.gateway.service.IUserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.PathVariable;

/**
 * Created by linqina on 2018/12/4 下午6:42.
 */
@Component
public class UserServiceFallback implements IUserService {
	Logger log = LoggerFactory.getLogger("UserServiceFallback");

	@Override
	public List<SysAuthority> getPermissionByUsername(@PathVariable("username") String username) {
		log.error("调用{}异常{}", "getPermissionByUsername", username);
		return null;
	}

	@Override
	public List<SysAuthority> getAllPermissionInfo() {
		log.error("调用{}异常", "getAllPermissionInfo");
		return null;
	}

	@Override
	public List<SysApplication> getAppInfoById(String appId, String appKey) {
		log.error("调用{}异常", "getAppInfoById");
		return null;
	}
}

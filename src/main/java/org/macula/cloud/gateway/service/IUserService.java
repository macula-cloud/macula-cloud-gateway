package org.macula.cloud.gateway.service;

import java.util.List;

import org.macula.cloud.gateway.domain.SysApplication;
import org.macula.cloud.gateway.domain.SysAuthority;
import org.macula.cloud.gateway.service.fallback.UserServiceFallbackFactory;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * Created by linqina on 2018/12/4 下午6:42.
 */
@FeignClient(value = "macula-cloud-admin", fallbackFactory = UserServiceFallbackFactory.class)
public interface IUserService {
	@RequestMapping(value = "/rest/permissions/un/{username}", method = RequestMethod.GET)
	List<SysAuthority> getPermissionByUsername(@PathVariable("username") String username);

	@RequestMapping(value = "/rest/permissions", method = RequestMethod.GET)
	List<SysAuthority> getAllPermissionInfo();

	@RequestMapping(value = "/rest/applications/authorities")
	List<SysApplication> getAppInfoById(@RequestParam("appId") String appId, @RequestParam("appKey") String appKey);

}

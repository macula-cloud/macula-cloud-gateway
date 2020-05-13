package org.macula.cloud.gateway.service.fallback;

import org.macula.cloud.gateway.service.IUserService;
import org.springframework.stereotype.Component;

import feign.hystrix.FallbackFactory;

/**
 * Created by linqina on 2018/12/20 11:44 AM.
 */
@Component
public class UserServiceFallbackFactory implements FallbackFactory<IUserService> {

	private final UserServiceFallback userServiceFallback;

	public UserServiceFallbackFactory(UserServiceFallback userServiceFallback) {
		this.userServiceFallback = userServiceFallback;
	}

	@Override
	public IUserService create(Throwable throwable) {
		// 打印下异常
		throwable.printStackTrace();
		return userServiceFallback;
	}
}

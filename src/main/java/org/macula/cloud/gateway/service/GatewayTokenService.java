package org.macula.cloud.gateway.service;

import org.apache.commons.lang.StringUtils;
import org.macula.cloud.gateway.domain.AccessToken;
import org.macula.cloud.gateway.repository.RedisRepository;
import org.macula.cloud.gateway.util.Constants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.alibaba.fastjson.JSON;

/**
 * @Auther: Aaron
 * @Date: 2018/9/5 16:31
 * @Description:
 */
@Service
public class GatewayTokenService {
	@Autowired
	private RedisRepository redisRepository;

	private static final String REDIS_EMPTY_CHARS = "nil";

	@Transactional
	public void saveToken(AccessToken token) {
		redisRepository.set(token.getToken(), JSON.toJSONString(token), Constants.TOKEN_TIMEOUT);
	}

	public void updateToken(AccessToken token) {
		String tokenStr = redisRepository.get(token.getToken());
		if (StringUtils.isNotBlank(tokenStr) && !REDIS_EMPTY_CHARS.equals(tokenStr)) {
			redisRepository.set(token.getToken(), JSON.toJSONString(token), Constants.TOKEN_TIMEOUT);

		}
	}

	public AccessToken getToken(String tokenId) {
		AccessToken token = null;
		String tokenStr = redisRepository.get(tokenId);
		if (StringUtils.isNotBlank(tokenStr) && !REDIS_EMPTY_CHARS.equals(tokenStr)) {
			token = JSON.parseObject(tokenStr, AccessToken.class);
		}
		return token;
	}

	public void deleteToken(String tokenId) {
		redisRepository.del(tokenId);
	}

}

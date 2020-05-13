package org.macula.cloud.gateway.controller;

import java.net.URI;

import org.macula.cloud.gateway.domain.AccessToken;
import org.macula.cloud.gateway.service.GatewayTokenService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import reactor.core.publisher.Mono;

/**
 * @Auther: Aaron
 * @Date: 2018/9/5 15:58
 * @Description: 对外API
 */
//@RestController
@RequestMapping("/tokens")
public class GatewayTokenController {

	@Autowired
	private GatewayTokenService gatewaytokenService;

	/**
	 *
	 * 功能描述: 保存token
	 *
	 * @param: * @param null
	 * @return:
	 * @auther: Aaron
	 * @date: 2018/9/5 16:14
	 */
	@PostMapping("")
	public Mono<ResponseEntity<Void>> saveToken(AccessToken token) {
		gatewaytokenService.saveToken(token);
		return Mono.just(ResponseEntity.created(URI.create("/" + token.getToken())).build());
	}

	@PatchMapping("/{tokenId}")
	public Mono<ResponseEntity<Void>> updateToken(@PathVariable("tokenId") String tokenId, AccessToken token) {
		if (gatewaytokenService.getToken(tokenId) == null) {
			return Mono.just(ResponseEntity.notFound().build());
		}
		gatewaytokenService.updateToken(token);
		return Mono.just(ResponseEntity.created(URI.create("/" + token.getToken())).build());
	}

	@DeleteMapping("/{tokenId}")
	public Mono<ResponseEntity<Void>> deleteToken(@PathVariable("tokenId") String tokenId) {
		if (gatewaytokenService.getToken(tokenId) == null) {
			return Mono.just(ResponseEntity.notFound().build());
		}
		gatewaytokenService.deleteToken(tokenId);
		return Mono.just(ResponseEntity.noContent().build());
	}

	@GetMapping("/{tokenId}")
	public Mono<ResponseEntity<AccessToken>> getToken(@PathVariable("tokenId") String tokenId) {
		AccessToken token = gatewaytokenService.getToken(tokenId);
		if (token == null) {
			return Mono.just(ResponseEntity.notFound().build());
		}
		return Mono.just(ResponseEntity.ok(token));
	}

}

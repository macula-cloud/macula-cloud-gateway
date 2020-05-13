package com.foreveross.gateway;

import java.util.HashMap;
import java.util.Map;

import org.macula.cloud.gateway.util.SignatureUtils;

/**
 * @className TestSign
 * @auther: linqina
 * @date: 2019/2/12 6:35 PM
 * @description: 用于生成签名，用于第三方应用访问
 */
public class TestSign {

	public static void main(String[] args) {
		Map<String, String> map = new HashMap<>();
		long currentTime = System.currentTimeMillis();
		map.put("appId", "demo-consumer");
		map.put("app_key", "MCwwDQYJKoZIhvcNAQEBBQADGwAwGAIRANkvxBN7FC7Sk5R3VpPWosUCAwEAAQ==");
		map.put("timestamp", currentTime + "");
		map.put("sign_method", "md5");
		String secret = "MHkCAQAwDQYJKoZIhvcNAQEBBQAEZTBjAgEAAhEA2S/EE3sULtKTlHdWk9aixQIDAQABAhBnUAB+n9KW/nWMkexLdn/VAgkA/TKF9P2BKjsCCQDblzM9fl2W/wIIGkR2jz8JemcCCQC02b8quyMrUwIJALYW069eHAVJ";
		System.out.println("currentTime=>" + currentTime);
		System.out.println("==>" + SignatureUtils.md5SignatureForTest(map, secret));
	}
}

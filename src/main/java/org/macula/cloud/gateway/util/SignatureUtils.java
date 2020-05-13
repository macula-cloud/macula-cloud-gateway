/**
 * Copyright 2010-2012 the original author or authors.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.macula.cloud.gateway.util;

import java.security.MessageDigest;
import java.util.Iterator;
import java.util.Map;
import java.util.TreeMap;

import javax.crypto.Mac;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

import org.apache.commons.lang.StringUtils;
import org.springframework.core.convert.converter.Converter;
import org.springframework.core.serializer.support.SerializingConverter;
import org.springframework.util.MultiValueMap;

/**
 * @Auther: Aaron
 * @Date: 2018/12/22 13:59
 * @Description: MD5、HMAC签名助手类
 */
public abstract class SignatureUtils {

	private static Converter<Object, byte[]> serializer = new SerializingConverter();

	/**
	 * 将内容MD5
	 *
	 * @param context
	 * @return MD5 String
	 */
	public static String md5(Object context) {
		return byte2hex(md5tobytes(context));
	}

	public static byte[] md5tobytes(Object context) {
		try {
			MessageDigest md = MessageDigest.getInstance("MD5");
			if (context instanceof String) {
				return md.digest(context.toString().getBytes("UTF-8"));
			}
			return md.digest(serializer.convert(context));
		} catch (Exception e) {
			throw new RuntimeException("md5 error!", e);
		}
	}

	/**
	 * 将内容SHA1
	 *
	 * @param context
	 * @return SHA1 String
	 */
	public static String sha1(Object context) {
		return byte2hex(sha1tobytes(context));
	}

	public static byte[] sha1tobytes(Object context) {
		try {
			MessageDigest md = MessageDigest.getInstance("SHA1");
			if (context instanceof String) {
				return md.digest(context.toString().getBytes("UTF-8"));
			}
			return md.digest(serializer.convert(context));
		} catch (Exception e) {
			throw new RuntimeException("sha1 error！", e);
		}
	}

	/**
	 * 新的md5签名，首尾放secret。
	 *
	 * @param params 传给服务器的参数
	 * @param secret 分配给您的APP_SECRET
	 */
	public static String md5Signature(MultiValueMap<String, String> params, String secret) {
		String result = null;
		StringBuffer orgin = getBeforeSign(params, new StringBuffer());
		if (orgin == null) {
			return result;
		}
		// secret last
		orgin.append(secret);
		try {
			MessageDigest md = MessageDigest.getInstance("MD5");
			result = byte2hex(md.digest(orgin.toString().getBytes("utf-8")));
		} catch (Exception e) {
			throw new RuntimeException("md5 sign error !", e);
		}
		return result;
	}

	/**
	 * 新的md5签名，hmac加密
	 *
	 * @param params 传给服务器的参数
	 * @param secret 分配给您的APP_SECRET
	 */
	public static String hmacSignature(MultiValueMap<String, String> params, String secret) {
		String result = null;
		StringBuffer orgin = getBeforeSign(params, new StringBuffer());
		if (orgin == null) {
			return result;
		}
		try {
			result = byte2hex(encryptHMAC(orgin.toString().getBytes("utf-8"), secret));
		} catch (Exception e) {
			throw new RuntimeException("sign error !", e);
		}
		return result;
	}

	/**
	 * 二行制转字符串
	 * 
	 * @param b 二进制数组
	 */
	public static String byte2hex(byte[] b) {
		StringBuffer hs = new StringBuffer();
		String stmp = "";
		for (int n = 0; n < b.length; n++) {
			stmp = (Integer.toHexString(b[n] & 0XFF));
			if (stmp.length() == 1) {
				hs.append("0").append(stmp);
			} else {
				hs.append(stmp);
			}
		}
		return StringUtils.upperCase(hs.toString());
	}

	/**
	 * 添加参数的封装方法
	 *
	 * @param params 参数
	 * @param orgin  原字符串
	 */
	private static StringBuffer getBeforeSign(MultiValueMap<String, String> params, StringBuffer orgin) {
		if (params == null) {
			return null;
		}

//		MultiValueMap<String, String> treeMap = new LinkedMultiValueMap<String, String>();
		Map<String, String> treeMap = new TreeMap<String, String>();
		treeMap.putAll(params.toSingleValueMap());
		treeMap.remove("sign");
		Iterator<String> iter = treeMap.keySet().iterator();
		while (iter.hasNext()) {
			String name = iter.next();
			orgin.append(name).append(params.getFirst(name));
		}
		return orgin;
	}

	/**
	 * HMAC加密算法
	 *
	 * @param data 待签名字节数组
	 * @param key  密钥
	 */
	private static byte[] encryptHMAC(byte[] data, String key) throws Exception {
		SecretKey secretKey = new SecretKeySpec(key.getBytes("utf-8"), "HmacMD5");
		Mac mac = Mac.getInstance(secretKey.getAlgorithm());
		mac.init(secretKey);
		return mac.doFinal(data);
	}

	/**
	 * 新的md5签名，首尾放secret。
	 *
	 * @param params 传给服务器的参数
	 * @param secret 分配给您的APP_SECRET
	 */
	public static String md5SignatureForTest(Map<String, String> params, String secret) {
		String result = null;
		StringBuffer orgin = getBeforeSignForTest(params, new StringBuffer());
		if (orgin == null) {
			return result;
		}
		// secret last
		orgin.append(secret);
		try {
			MessageDigest md = MessageDigest.getInstance("MD5");
			result = byte2hex(md.digest(orgin.toString().getBytes("utf-8")));
		} catch (Exception e) {
			throw new RuntimeException("md5 sign error !", e);
		}
		return result;
	}

	/**
	 * 添加参数的封装方法
	 *
	 * @param params 参数
	 * @param orgin  原字符串
	 */
	private static StringBuffer getBeforeSignForTest(Map<String, String> params, StringBuffer orgin) {
		if (params == null) {
			return null;
		}

//		MultiValueMap<String, String> treeMap = new LinkedMultiValueMap<String, String>();
		Map<String, String> treeMap = new TreeMap<String, String>();
		treeMap.putAll(params);
		treeMap.remove("sign");
		Iterator<String> iter = treeMap.keySet().iterator();
		while (iter.hasNext()) {
			String name = iter.next();
			orgin.append(name).append(params.get(name));
		}
		return orgin;
	}

	/**
	 * 新的md5签名，hmac加密
	 *
	 * @param params 传给服务器的参数
	 * @param secret 分配给您的APP_SECRET
	 */
	public static String hmacSignatureForTest(Map<String, String> params, String secret) {
		String result = null;
		StringBuffer orgin = getBeforeSignForTest(params, new StringBuffer());
		if (orgin == null) {
			return result;
		}
		try {
			result = byte2hex(encryptHMAC(orgin.toString().getBytes("utf-8"), secret));
		} catch (Exception e) {
			throw new RuntimeException("sign error !", e);
		}
		return result;
	}
}

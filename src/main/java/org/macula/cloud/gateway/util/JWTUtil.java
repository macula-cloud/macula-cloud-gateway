package org.macula.cloud.gateway.util;

import java.util.Date;
import java.util.Map;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

import org.apache.commons.codec.binary.Base64;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtBuilder;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;

/**
 * Created by linqina on 2018/12/18 2:33 PM.
 */
public class JWTUtil {
	/**
	 * 创建jwt
	 *
	 * @param id
	 * @param subject
	 * @param ttlMillis 过期的时间长度
	 * @return
	 * @throws Exception
	 */
	public static String createJWT(String id, String subject, Map<String, Object> claims, long ttlMillis) {
		// 指定签名的时候使用的签名算法，也就是header那部分，jjwt已经将这部分内容封装好了。
		SignatureAlgorithm signatureAlgorithm = SignatureAlgorithm.HS256;
		// 生成JWT的时间
		long nowMillis = System.currentTimeMillis();
		Date now = new Date(nowMillis);
		// 生成签名的时候使用的秘钥secret,这个方法本地封装了的，一般可以从本地配置文件中读取，切记这个秘钥不能外露哦。它就是你服务端的私钥，在任何场景都不应该流露出去。一旦客户端得知这个secret,
		// 那就意味着客户端是可以自我签发jwt了。
		SecretKey key = generalKey();
		// 下面就是在为payload添加各种标准声明和私有声明了
		// 这里其实就是new一个JwtBuilder，设置jwt的body
		JwtBuilder builder = Jwts.builder()
				// 如果有私有声明，一定要先设置这个自己创建的私有的声明，这个是给builder的claim赋值，一旦写在标准的声明赋值之后，就是覆盖了那些标准的声明的
				.setClaims(claims)
				// 设置jti(JWT ID)：是JWT的唯一标识，根据业务需要，这个可以设置为一个不重复的值，主要用来作为一次性token,从而回避重放攻击。
				.setId(id)
				// iat: jwt的签发时间
				.setIssuedAt(now)
				// sub(Subject)：代表这个JWT的主体，即它的所有人，这个是一个json格式的字符串，可以存放什么userid，roldid之类的，作为什么用户的唯一标志。
				.setSubject(subject)
				// 设置签名使用的签名算法和签名使用的秘钥
				.signWith(signatureAlgorithm, key);
		if (ttlMillis >= 0) {
			long expMillis = nowMillis + ttlMillis;
			Date exp = new Date(expMillis);
			// 设置过期时间
			builder.setExpiration(exp);
		}
		// 就开始压缩为xxxxxxxxxxxxxx.xxxxxxxxxxxxxxx.xxxxxxxxxxxxx这样的jwt
		return builder.compact();
		// 打印了一哈哈确实是下面的这个样子
		// eyJhbGciOiJIUzI1NiJ9.eyJ1aWQiOiJEU1NGQVdEV0FEQVMuLi4iLCJzdWIiOiIiLCJ1c2VyX25hbWUiOiJhZG1pbiIsIm5pY2tfbmFtZSI6IkRBU0RBMTIxIiwiZXhwIjoxNTE3ODI4MDE4LCJpYXQiOjE1MTc4Mjc5NTgsImp0aSI6Imp3dCJ9.xjIvBbdPbEMBMurmwW6IzBkS3MPwicbqQa2Y5hjHSyo
	}

	/**
	 * 解密jwt
	 *
	 * @param jwt
	 * @return
	 * @throws Exception
	 */
	public static Claims parseJWT(String jwt) throws Exception {
		// 签名秘钥，和生成的签名的秘钥一模一样
		SecretKey key = generalKey();
		// 得到DefaultJwtParser
		Claims claims = Jwts.parser()
				// 设置签名的秘钥
				.setSigningKey(key)
				// 设置需要解析的jwt
				.parseClaimsJws(jwt).getBody();
		return claims;
	}

	/**
	 * 由字符串生成加密key
	 *
	 * @return
	 */
	private static SecretKey generalKey() {
		// 本地配置文件中加密的密文
		String stringKey = Constants.JWT_SECRET;
		// 本地的密码解码
		byte[] encodedKey = Base64.decodeBase64(stringKey);
		// 根据给定的字节数组使用AES加密算法构造一个密钥，使用 encodedKey中的始于且包含 0 到前 leng
		// 个字节这是当然是所有。（后面的文章中马上回推出讲解Java加密和解密的一些算法）
		SecretKey key = new SecretKeySpec(encodedKey, 0, encodedKey.length, "AES");
		return key;
	}

}

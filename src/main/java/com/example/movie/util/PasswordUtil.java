package com.example.movie.util;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;

public final class PasswordUtil {

	private PasswordUtil() {}
	
	public static String sha256(String raw) {
		
		try 
		{
			//using var sha = SHA256.Create();
			//byte[] hash = sha.ComputeHash(Encoding.UTF8.GetBytes(raw));
			//Convert.ToHexString(hash).ToLower();
			MessageDigest md = MessageDigest.getInstance("SHA-256");
			byte[] hash = md.digest(raw.getBytes(StandardCharsets.UTF_8));
			return HexFormat.of().formatHex(hash);
		}
		catch(NoSuchAlgorithmException e) 
		{
			 throw new IllegalStateException("SHA-256 不可用", e);
		}
		
	}
}

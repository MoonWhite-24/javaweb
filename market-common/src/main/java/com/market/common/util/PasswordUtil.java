package com.market.common.util;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.HexFormat;

public class PasswordUtil {

    private static final String ALGORITHM = "SHA-256";
    private static final SecureRandom RANDOM = new SecureRandom();

    public static String sha256(String input) {
        try {
            MessageDigest md = MessageDigest.getInstance(ALGORITHM);
            byte[] digest = md.digest(input.getBytes(java.nio.charset.StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(digest);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 not available", e);
        }
    }

    public static String generateSalt(int length) {
        byte[] bytes = new byte[length * 2];
        RANDOM.nextBytes(bytes);
        String encoded = Base64.getEncoder().encodeToString(bytes);
        StringBuilder sb = new StringBuilder();
        for (char c : encoded.toCharArray()) {
            if (Character.isLetterOrDigit(c) && sb.length() < length) {
                sb.append(c);
            }
        }
        while (sb.length() < length) {
            sb.append((char) ('a' + RANDOM.nextInt(26)));
        }
        return sb.toString();
    }

    public static String hashPassword(String rawPassword, String salt) {
        return sha256(salt + rawPassword);
    }

    public static boolean verify(String rawPassword, String salt, String hashedPassword) {
        return hashPassword(rawPassword, salt).equals(hashedPassword);
    }
}

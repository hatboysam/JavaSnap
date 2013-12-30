package com.habosa.javasnap;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Author: samstern
 * Date: 12/27/13
 */
public class TokenLib {

    private static final String SECRET = "iEk21fuwZApXlz93750dmW22pw389dPwOk";
    private static final String PATTERN = "0001110111101110001111010101111011010001001110011000110001000110";

    private static final String STATIC_TOKEN = "m198sOkJEn37DjqZ32lpRu76xmw288xSQ9";

    private static final String SHA256 = "SHA-256";

    /**
     * Generate a SnapChat Request Token from an Auth Token and a UNIX Timestamp.
     *
     * @param authToken the SnapChat Auth Token
     * @param timestamp the UNIX timestamp (seconds)
     * @return the Request Token.
     */
    public static String requestToken(String authToken, Long timestamp) {
        // Create bytesToHex of secret + authToken
        String firstHex = hexDigest(SECRET + authToken);

        // Create bytesToHex of timestamp + secret
        String secondHex = hexDigest(timestamp.toString() + SECRET);

        // Combine according to pattern
        StringBuilder sb = new StringBuilder();
        char[] patternChars = PATTERN.toCharArray();
        for (int i = 0; i < patternChars.length; i++) {
            char c = patternChars[i];
            if (c == '0') {
                sb.append(firstHex.charAt(i));
            } else {
                sb.append(secondHex.charAt(i));
            }
        }

        return sb.toString();
    }

    /**
     * Get a Request Token for the login requestJson, which uses a static Auth Token.
     *
     * @param timestamp the UNIX timestamp (seconds)
     * @return the Request Token.
     */
    public static String staticRequestToken(Long timestamp) {
        return requestToken(STATIC_TOKEN, timestamp);
    }

    /**
     * Get the SHA-256 Digest of a String in Hexadecimal.
     */
    private static String hexDigest(String toDigest) {
        try {
            MessageDigest sha256 = MessageDigest.getInstance(SHA256);
            byte[] digested = sha256.digest(toDigest.getBytes());
            return bytesToHex(digested);
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Convert a byte array to a hex string.
     * Source: http://stackoverflow.com/questions/9655181/convert-from-byte-array-to-hex-string-in-java
     */
    private static String bytesToHex(byte[] digested) {
        char[] hexArray = "0123456789abcdef".toCharArray();
        char[] hexChars = new char[digested.length * 2];

        for (int i = 0; i < digested.length; i++) {
            int v = digested[i] & 0xFF;
            hexChars[i * 2] = hexArray[v >>> 4];
            hexChars[(i * 2) + 1] = hexArray[v & 0x0F];
        }

        return (new String(hexChars));
    }

}

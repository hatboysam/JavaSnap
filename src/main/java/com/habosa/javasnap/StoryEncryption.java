package com.habosa.javasnap;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import org.apache.commons.codec.binary.Base64;

/**
 * Modified version of Snap Encryption for Stories by Liam Cottle
 * Date: 06/04/2014
 */
public class StoryEncryption {

    private static final char[] HEX_CHARS = {'0','1','2','3','4','5','6','7','8','9','a','b','c','d','e','f'};

    public static byte[] decrypt(byte[] storyData, String MediaKey, String MediaIV) {
    
        byte[] key = Base64.decodeBase64(MediaKey.getBytes());
        byte[] iv = Base64.decodeBase64(MediaIV.getBytes());
        
        IvParameterSpec ivspec = new IvParameterSpec(iv);
        SecretKeySpec keyspec = new SecretKeySpec(key, "AES");
        
        Cipher cipher = null;
        byte[] decrypted = null;
        try {
          cipher = Cipher.getInstance("AES/CBC/PKCS5Padding"); //Uses PKCS7Padding which is same as PKCS5Padding
          cipher.init(Cipher.DECRYPT_MODE, keyspec, ivspec);
          decrypted = cipher.doFinal(storyData);
        } catch (Exception e) {
          e.printStackTrace();
        }
       return decrypted;
    }
}

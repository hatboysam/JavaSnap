package com.habosa.javasnap;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.codec.binary.StringUtils;

import java.security.NoSuchAlgorithmException;
import javax.crypto.Cipher;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

/**
 * Modified version of Snap Encryption for Stories by Liam Cottle
 * Date: 06/04/2014
 */
public class StoryEncryption {

    static char[] HEX_CHARS = {'0','1','2','3','4','5','6','7','8','9','a','b','c','d','e','f'};

    public static byte[] decrypt(byte[] storyData, String MediaKey, String MediaIV) {
    
        byte[] key = Base64.decodeBase64(MediaKey.getBytes());
        byte[] iv = Base64.decodeBase64(MediaIV.getBytes());
        
        IvParameterSpec ivspec = new IvParameterSpec(iv);
        SecretKeySpec keyspec = new SecretKeySpec(key, "AES");
        
        Cipher cipher = null;
        try {
          cipher = Cipher.getInstance("AES/CBC/NoPadding");
        } catch (NoSuchAlgorithmException e) {
          // TODO Auto-generated catch block
          e.printStackTrace();
        } catch (NoSuchPaddingException e) {
          // TODO Auto-generated catch block
          e.printStackTrace();
        }
        
        byte[] decrypted = null;
        try {
          cipher.init(Cipher.DECRYPT_MODE, keyspec, ivspec);
          decrypted = cipher.doFinal(storyData);
          //Remove trailing zeroes, don't need this?
          /*
          if( decrypted.length > 0){
            int trim = 0;
            for( int i = decrypted.length - 1; i >= 0; i-- ) if( decrypted[i] == 0 ) trim++;
              if( trim > 0 ){
                byte[] newArray = new byte[decrypted.length - trim];
                System.arraycopy(decrypted, 0, newArray, 0, decrypted.length - trim);
                decrypted = newArray;
               }
           }
           */
       } catch (Exception e){
          e.printStackTrace();
          //return new byte[0];
       }
       return decrypted;
    }
}

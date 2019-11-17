package org.xenei.bloom.encrypted;

import java.security.GeneralSecurityException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.KeySpec;

import javax.crypto.Cipher;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;

public class Ende_AES256 implements Ende {
   // private static String secretKey = "boooooooooom!!!!";
   // private static String salt = "ssshhhhhhhhhhh!!!!";
    public static final String ALGORITHM = "AES";
    public static final String DEFAULT_KEY_ALGORITHM="PBKDF2WithHmacSHA256";

    private SecretKeySpec secretKey;

    public Ende_AES256( SecretKeySpec secretKey ) {
        this.secretKey = secretKey;
        if ( ! ALGORITHM.equals(secretKey.getAlgorithm()))
        {
            throw new IllegalArgumentException( String.format( "Key algorithm (%s) does not match %s", secretKey.getAlgorithm(), ALGORITHM));
        }
    }

    public static SecretKeySpec makeKey( String secretKey ) throws GeneralSecurityException {
        byte[] salt = new byte[16];
        SecureRandom random = new SecureRandom();
        random.nextBytes( salt );
      SecretKeyFactory factory = SecretKeyFactory.getInstance(DEFAULT_KEY_ALGORITHM);
      KeySpec spec = new PBEKeySpec(secretKey.toCharArray(), salt, 65536, 256);
      SecretKey tmp = factory.generateSecret(spec);
      return new SecretKeySpec(tmp.getEncoded(), ALGORITHM);
    }

    @Override
    public byte[] encrypt(byte[] plainText) throws GeneralSecurityException
    {
            byte[] iv = new byte[16];
            SecureRandom random = new SecureRandom();
            random.nextBytes( iv );
            IvParameterSpec ivspec = new IvParameterSpec(iv);
            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
            cipher.init(Cipher.ENCRYPT_MODE, secretKey, ivspec);
            byte[] encrypted = cipher.doFinal( plainText );
            byte[] cipherText = new byte[16+encrypted.length];
            System.arraycopy(iv, 0, cipherText, 0, 16);
            System.arraycopy(encrypted, 0, cipherText, 16, encrypted.length);
            return cipherText;
    }


    @Override
    public byte[] decrypt(byte[] cipherText) throws GeneralSecurityException {
            byte[] iv = new byte[16];
            byte[] innerText = new byte[cipherText.length-16];
            System.arraycopy(cipherText, 0, iv, 0, 16);
            System.arraycopy(cipherText, 16, innerText, 0, innerText.length);
            IvParameterSpec ivspec = new IvParameterSpec(iv);
            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5PADDING");
            cipher.init(Cipher.DECRYPT_MODE, secretKey, ivspec);
            return cipher.doFinal(innerText);
    }

}

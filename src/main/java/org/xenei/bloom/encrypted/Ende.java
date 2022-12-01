package org.xenei.bloom.encrypted;

import java.security.GeneralSecurityException;

public interface Ende {

    public byte[] encrypt(byte[] plainText) throws GeneralSecurityException;

    public byte[] decrypt(byte[] cipherText) throws GeneralSecurityException;

}

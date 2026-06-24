package org.temochko.Business.DTOs;

import java.io.Serializable;

public class EncryptedAesKeyExchange implements Serializable {
    public byte[] encryptedAesKey; // AES ключ, зашифрований публічним RSA ключем

    public EncryptedAesKeyExchange(byte[] encryptedAesKey) {
        this.encryptedAesKey = encryptedAesKey;
    }
}
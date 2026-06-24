package org.temochko.Business.DTOs;

import java.io.Serializable;
import java.security.PublicKey;

public class RsaPublicKeyExchange implements Serializable {
    public PublicKey publicKey;

    public RsaPublicKeyExchange(PublicKey publicKey) {
        this.publicKey = publicKey;
    }
}

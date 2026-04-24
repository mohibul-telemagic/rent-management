package com.renteasebd.security;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class CryptoKeyInitializer {

    public CryptoKeyInitializer(@Value("${app.crypto.aes256-key-base64:}") String keyBase64) {
        AesGcmStringConverter.configureBase64Key(keyBase64);
    }
}

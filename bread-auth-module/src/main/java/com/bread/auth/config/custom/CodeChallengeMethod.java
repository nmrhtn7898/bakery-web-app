package com.bread.auth.config.custom;

import org.bouncycastle.util.encoders.Hex;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;

import static java.nio.charset.StandardCharsets.US_ASCII;

public enum CodeChallengeMethod {

    S256 {
        @Override
        public String transform(String codeVerifier) {
            try {
                MessageDigest messageDigest = MessageDigest.getInstance("SHA-256");
                byte[] hash = messageDigest.digest(codeVerifier.getBytes(US_ASCII));
                return Base64.getUrlEncoder().encodeToString(Hex.encode(hash));
            } catch (NoSuchAlgorithmException e) {
                throw new IllegalStateException(e);
            }
        }
    },
    PLAIN {
        @Override
        public String transform(String codeVerifier) {
            return codeVerifier;
        }
    },
    NONE {
        @Override
        public String transform(String codeVerifier) {
            throw new UnsupportedOperationException();
        }
    };

    public abstract String transform(String codeVerifier);

}

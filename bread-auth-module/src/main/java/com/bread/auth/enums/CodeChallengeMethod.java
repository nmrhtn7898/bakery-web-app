package com.bread.auth.enums;

import java.io.Serializable;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import static java.nio.charset.StandardCharsets.US_ASCII;
import static java.security.MessageDigest.getInstance;
import static java.util.Base64.getUrlEncoder;
import static org.bouncycastle.util.encoders.Hex.encode;

public enum CodeChallengeMethod {

    S256 {
        @Override
        public String transform(String codeVerifier) {
            try {
                MessageDigest messageDigest = getInstance("SHA-256");
                byte[] bytes = codeVerifier.getBytes(US_ASCII);
                byte[] digest = messageDigest.digest(bytes);
                return getUrlEncoder().withoutPadding().encodeToString(digest);
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

package com.bread.api.controller;

import com.bread.api.client.LoginClient;
import com.bread.api.model.LoginToken;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;

@RestController
@RequiredArgsConstructor
public class LoginController {

    private final LoginClient loginClient;

    @PostMapping("/api/v1/login")
    public ResponseEntity login(@RequestBody @Valid LoginRequest request) {
        LoginToken token = loginClient.login(request.getEmail(), request.getPassword());
        String refresh_token = token.getRefresh_token();
        return ResponseEntity.ok(token);
    }

    @Getter
    public static class LoginRequest {
        private String email;
        private String password;
    }

    @Getter
    public static class LoginResponse {

        private String access_token;

        private Integer expires_in;

        private String jti;

    }

}

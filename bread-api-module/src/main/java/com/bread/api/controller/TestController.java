package com.bread.api.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class TestController {

    @GetMapping("/api/v1/test")
    public ResponseEntity test(OAuth2Authentication auth2Authentication) {
        return ResponseEntity.ok(auth2Authentication);
    }


}

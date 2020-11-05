package com.bread.auth.controller;

import com.bread.auth.repository.Oauth2ClientRepository;
import com.bread.auth.service.Oauth2ClientService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
public class Oauth2ClientController {

    private final Oauth2ClientService oauth2ClientService;

    private final Oauth2ClientRepository oauth2ClientRepository;

    @GetMapping("/api/v1/clients")
    public ResponseEntity getUsers() {
        return ResponseEntity.ok().build();
    }

    @GetMapping("/api/v1/clients/{id}")
    public ResponseEntity getUser(@PathVariable Long id) {
        return ResponseEntity.ok().build();
    }

    @PostMapping("/api/v1/clients")
    public ResponseEntity generateClient() {
        return ResponseEntity.created(null).body(null);
    }

    @PutMapping("/api/v1/clients/{id}")
    public ResponseEntity putClient(@PathVariable Long id) {
        return ResponseEntity.ok().build();
    }

    @PatchMapping("/api/v1/clients/{id}")
    public ResponseEntity patchClient(@PathVariable Long id) {
        return ResponseEntity.ok().build();
    }

}

package com.bread.auth.controller;

import com.bread.auth.repository.AccountRepository;
import com.bread.auth.service.AccountService;
import com.bread.common.ErrorSerializer;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
public class AccountController {

    private final AccountService accountService;

    private final AccountRepository accountRepository;

    @GetMapping("/api/v1/users")
    public ResponseEntity getUsers() {
        return ResponseEntity.ok().build();
    }

    @GetMapping("/api/v1/users/{id}")
    public ResponseEntity getUser(@PathVariable Long id) {
        return ResponseEntity.ok().build();
    }

    @PostMapping("/api/v1/users")
    public ResponseEntity generateUser() {
        return ResponseEntity.ok().build();
    }

    @PatchMapping("/api/v1/users/{id}")
    public ResponseEntity patchUser(@PathVariable Long id) {
        return ResponseEntity.ok().build();
    }


}

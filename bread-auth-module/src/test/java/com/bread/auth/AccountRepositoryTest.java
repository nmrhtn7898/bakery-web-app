package com.bread.auth;

import com.bread.auth.base.AbstractDataJpaTest;
import com.bread.auth.entity.Account;
import com.bread.auth.repository.AccountRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.junit.jupiter.api.Assertions.*;

public class AccountRepositoryTest extends AbstractDataJpaTest {

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Test
    public void findByEmail_Success() {
        String email = "user";
        Account account = accountRepository
                .findByEmail("user")
                .orElseThrow(() -> new UsernameNotFoundException(email));
        assertEquals(account.getEmail(), email);
        assertTrue(passwordEncoder.matches("user", account.getPassword()));
        assertEquals(account.getAuthorities().get(0).getAuthority().getName(), "user");
    }

    @Test
    public void findByEmail_Fail() {
        String email = "invalid email";
        assertThrows(UsernameNotFoundException.class, () ->
                accountRepository
                        .findByEmail(email)
                        .orElseThrow(() -> new UsernameNotFoundException(email))
        );
    }

}

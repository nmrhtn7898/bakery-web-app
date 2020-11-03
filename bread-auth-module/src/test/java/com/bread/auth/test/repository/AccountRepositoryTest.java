package com.bread.auth.test.repository;

import com.bread.auth.base.AbstractDataJpaTest;
import com.bread.auth.entity.Account;
import com.bread.auth.repository.AccountRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;

import static java.util.stream.Collectors.toList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.*;

public class AccountRepositoryTest extends AbstractDataJpaTest {

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Test
    public void findByEmail_Success() {
        // when
        Account account = accountRepository
                .findByEmail(testProperties.getUsers().getMaster().getUsername())
                .orElseThrow(() -> new UsernameNotFoundException(testProperties.getUsers().getMaster().getUsername()));
        // then
        assertEquals(account.getEmail(), testProperties.getUsers().getMaster().getUsername());
        assertTrue(passwordEncoder.matches(testProperties.getUsers().getMaster().getPassword(), account.getPassword()));
        assertThat(
                account
                        .getAuthorities()
                        .stream().map(a -> a.getAuthority().getName())
                        .collect(toList()),
                is(testProperties.getUsers().getMaster().getAuthorities())
        );
    }

    @Test
    public void findByEmail_Fail() {
        // given
        String email = "invalid email";
        // when & then
        assertThrows(
                UsernameNotFoundException.class,
                () ->
                        accountRepository
                                .findByEmail(email)
                                .orElseThrow(() -> new UsernameNotFoundException(email))
        );
    }

}

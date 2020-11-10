package com.bread.auth.test.repository;

import com.bread.auth.base.AbstractDataJpaTest;
import com.bread.auth.entity.Account;
import com.bread.auth.entity.AccountAuthority;
import com.bread.auth.entity.Authority;
import com.bread.auth.repository.AccountRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;

import static java.util.Collections.singletonList;
import static java.util.stream.Collectors.toList;
import static org.junit.jupiter.api.Assertions.*;

public class AccountRepositoryTest extends AbstractDataJpaTest {

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @BeforeEach
    public void beforeEach() {
        accountRepository.deleteAll();
    }

    @Test
    public void findByEmail_Success() {
        // given
        String email = "test";
        String password = "1234";
        Authority authority = Authority
                .builder()
                .name("user")
                .build();
        Account account = Account
                .builder()
                .password(passwordEncoder.encode(password))
                .email(email)
                .build();
        AccountAuthority accountAuthority = AccountAuthority
                .builder()
                .authority(authority)
                .account(account)
                .build();
        accountRepository.save(account);
        // when
        Account find = accountRepository
                .findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException(email));
        // then
        assertEquals(email, find.getEmail());
        assertTrue(passwordEncoder.matches(password, find.getPassword()));
        assertIterableEquals(singletonList(accountAuthority), find.getAuthorities());
        assertIterableEquals(
                singletonList(authority),
                find
                        .getAuthorities()
                        .stream()
                        .map(AccountAuthority::getAuthority)
                        .collect(toList())
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

    @Test
    public void save_Success() {
        // given
        String email = "test";
        String password = "1234";
        Authority authority = Authority
                .builder()
                .name("user")
                .build();
        Account account = Account
                .builder()
                .password(passwordEncoder.encode(password))
                .email(email)
                .build();
        AccountAuthority accountAuthority = AccountAuthority
                .builder()
                .authority(authority)
                .account(account)
                .build();
        Account save = accountRepository.save(account);
        // then
        assertEquals(email, save.getEmail());
        assertTrue(passwordEncoder.matches(password, save.getPassword()));
        assertIterableEquals(singletonList(accountAuthority), save.getAuthorities());
        assertIterableEquals(
                singletonList(authority),
                save
                        .getAuthorities()
                        .stream()
                        .map(AccountAuthority::getAuthority)
                        .collect(toList())
        );
    }

    @Test
    public void save_Fail() {
        // given
        accountRepository.save(
                Account
                        .builder()
                        .password(passwordEncoder.encode("1234"))
                        .email("duplicate")
                        .build()
        );
        // when & then
        assertThrows(
                DataIntegrityViolationException.class,
                () -> accountRepository.save(
                        Account
                                .builder()
                                .password(passwordEncoder.encode("1234"))
                                .email("duplicate")
                                .build()
                )
        );
        assertThrows(
                DataIntegrityViolationException.class,
                () -> accountRepository.save(
                        Account
                                .builder()
                                .email("notnull")
                                .build()
                )
        );
    }

}

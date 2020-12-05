package com.bakery.auth.test.service;

import com.bakery.auth.entity.Account;
import com.bakery.auth.entity.AccountAuthority;
import com.bakery.auth.entity.Authority;
import com.bakery.auth.model.AccountDetails;
import com.bakery.auth.base.AbstractServiceTest;
import com.bakery.auth.repository.AccountRepository;
import com.bakery.auth.service.AccountService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import static java.util.Optional.of;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@DisplayName("AccountService 단위 테스트")
public class AccountServiceTest extends AbstractServiceTest {

    @InjectMocks
    private AccountService accountService;

    @Mock
    private AccountRepository accountRepository;

    @Test
    @DisplayName("email 기준으로 조회 성공하는 경우")
    public void loadUserByUsername_Success() {
        // given
        String email = "test";
        Account account = generate(email);
        AccountDetails expect = new AccountDetails(account);
        when(accountRepository.findByEmail(email)).thenReturn(of(account));
        // when
        AccountDetails actual = (AccountDetails) accountService.loadUserByUsername(email);
        // then
        assertEquals(expect.getId(), actual.getId());
        assertEquals(expect.getPassword(), actual.getPassword());
        assertEquals(expect.getUsername(), actual.getUsername());
        assertEquals(expect.isEnabled(), actual.isEnabled());
        assertEquals(expect.isCredentialsNonExpired(), actual.isCredentialsNonExpired());
        assertEquals(expect.isAccountNonLocked(), actual.isAccountNonLocked());
        assertEquals(expect.isAccountNonExpired(), actual.isAccountNonExpired());
        assertIterableEquals(expect.getAuthorities(), actual.getAuthorities());
    }

    @Test
    @DisplayName("email 기준으로 조회 실패하는 경우")
    public void loadUserByUsername_Fail() {
        // given
        String email = "not exists email";
        // when & then
        assertThrows(
                UsernameNotFoundException.class,
                () -> accountService.loadUserByUsername(email)
        );
    }

    private Account generate(String email) {
        Authority authority = Authority
                .builder()
                .name("user")
                .build();
        Account account = Account
                .builder()
                .password("1234")
                .email(email)
                .build();
        AccountAuthority
                .builder()
                .authority(authority)
                .account(account)
                .build();
        return account;
    }

}

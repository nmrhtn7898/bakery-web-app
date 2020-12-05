package com.bread.auth.test.repository;

import com.bread.auth.base.AbstractDataJpaTest;
import com.bread.auth.entity.Account;
import com.bread.auth.entity.AccountAuthority;
import com.bread.auth.entity.Authority;
import com.bread.auth.repository.AccountRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

@DisplayName("AccountRepository 단위 테스트")
public class AccountRepositoryTest extends AbstractDataJpaTest {

    @Autowired
    private AccountRepository accountRepository;

    @BeforeEach
    public void beforeEach() {
        accountRepository.deleteAll();
    }

    @Test
    @DisplayName("email 조건으로 조회 성공하는 경우")
    public void findByEmail_Success() {
        // given
        String email = "test";
        Account expect = generate(email);
        expect = accountRepository.save(expect);
        // when
        Account actual = accountRepository
                .findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException(email));
        // then
        assertEquals(expect.getId(), actual.getId());
        assertEquals(expect.getEmail(), actual.getEmail());
        assertEquals(expect.getPassword(), actual.getPassword());
        assertEquals(expect.getCreated(), actual.getCreated());
        assertEquals(expect.getModified(), actual.getModified());
        for (int i = 0; i < expect.getAuthorities().size(); i++) {
            assertEquals(expect.getAuthorities().get(i).getId(), actual.getAuthorities().get(i).getId());
            assertEquals(expect.getAuthorities().get(i).getCreated(), actual.getAuthorities().get(i).getCreated());
            assertEquals(expect.getAuthorities().get(i).getModified(), actual.getAuthorities().get(i).getModified());
            assertEquals(expect.getAuthorities().get(i).getAuthority().getId(), actual.getAuthorities().get(i).getAuthority().getId());
            assertEquals(expect.getAuthorities().get(i).getAuthority().getName(), actual.getAuthorities().get(i).getAuthority().getName());
            assertEquals(expect.getAuthorities().get(i).getAuthority().getCreated(), actual.getAuthorities().get(i).getAuthority().getModified());
            assertEquals(expect.getAuthorities().get(i).getAuthority().getCreated(), actual.getAuthorities().get(i).getAuthority().getModified());
        }
    }

    @Test
    @DisplayName("email 조건으로 조회 실패하는 경우")
    public void findByEmail_Fail() {
        // given
        String email = "not exists email";
        // when & then
        assertFalse(accountRepository.findByEmail(email).isPresent());
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

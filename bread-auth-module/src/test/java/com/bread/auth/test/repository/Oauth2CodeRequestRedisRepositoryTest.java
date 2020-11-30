package com.bread.auth.test.repository;

import com.bread.auth.base.AbstractDataRedisTest;
import com.bread.auth.entity.Account;
import com.bread.auth.entity.AccountAuthority;
import com.bread.auth.entity.Authority;
import com.bread.auth.model.AccountDetails;
import com.bread.auth.model.Oauth2CodeRequest;
import com.bread.auth.repository.Oauth2CodeRequestRedisRepository;
import javassist.NotFoundException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.provider.OAuth2Request;

import java.util.*;

import static com.bread.auth.enums.CodeChallengeMethod.S256;
import static java.util.Collections.singleton;
import static java.util.UUID.randomUUID;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.security.core.authority.AuthorityUtils.createAuthorityList;

public class Oauth2CodeRequestRedisRepositoryTest extends AbstractDataRedisTest {

    @Autowired
    private Oauth2CodeRequestRedisRepository oauth2CodeRequestRedisRepository;

    @Test
    public void findById_Success() throws NotFoundException {
        // given
        String code = randomUUID().toString();
        Oauth2CodeRequest expect = generate(code);
        expect = oauth2CodeRequestRedisRepository.save(expect);
        // when
        Oauth2CodeRequest actual = oauth2CodeRequestRedisRepository
                .findById(code)
                .orElseThrow(() -> new NotFoundException(code));
        // then
        assertEquals(expect.getCode(), actual.getCode());
        assertEquals(expect.getCodeChallenge(), actual.getCodeChallenge());
        assertEquals(expect.getCodeChallengeMethod(), actual.getCodeChallengeMethod());
        assertEquals(expect.getOAuth2Request().getClientId(), actual.getOAuth2Request().getClientId());
        assertEquals(expect.getOAuth2Request().getRequestParameters(), actual.getOAuth2Request().getRequestParameters());
        assertEquals(expect.getOAuth2Request().getRedirectUri(), actual.getOAuth2Request().getRedirectUri());
        assertEquals(expect.getOAuth2Request().getResponseTypes(), actual.getOAuth2Request().getResponseTypes());
        assertEquals(expect.getOAuth2Request().getExtensions(), actual.getOAuth2Request().getExtensions());
        assertIterableEquals(expect.getOAuth2Request().getAuthorities(), actual.getOAuth2Request().getAuthorities());
        assertIterableEquals(expect.getOAuth2Request().getScope(), actual.getOAuth2Request().getScope());
        assertIterableEquals(expect.getOAuth2Request().getResourceIds(), actual.getOAuth2Request().getResourceIds());
        assertEquals(expect.getAccountDetails().getId(), actual.getAccountDetails().getId());
        assertEquals(expect.getAccountDetails().getUsername(), actual.getAccountDetails().getUsername());
        assertEquals(expect.getAccountDetails().getPassword(), actual.getAccountDetails().getPassword());
        assertEquals(expect.getAccountDetails().isAccountNonExpired(), actual.getAccountDetails().isAccountNonExpired());
        assertEquals(expect.getAccountDetails().isAccountNonLocked(), actual.getAccountDetails().isAccountNonLocked());
        assertEquals(expect.getAccountDetails().isCredentialsNonExpired(), actual.getAccountDetails().isCredentialsNonExpired());
        assertEquals(expect.getAccountDetails().isEnabled(), actual.getAccountDetails().isEnabled());
        assertEquals(expect.getAccountDetails().getAuthorities(), actual.getAccountDetails().getAuthorities());
    }

    @Test
    public void findById_Fail() {
        // given
        String code = "not exists code";
        // when & then
        assertFalse(oauth2CodeRequestRedisRepository.findById(code).isPresent());
    }

    @Test
    public void deleteById_Success() {
        // given
        String code = randomUUID().toString();
        Oauth2CodeRequest oauth2CodeRequest = generate(code);
        oauth2CodeRequestRedisRepository.save(oauth2CodeRequest);
        // when
        oauth2CodeRequestRedisRepository.deleteById(code);
        // then
        assertFalse(oauth2CodeRequestRedisRepository.findById(code).isPresent());
    }

    private Oauth2CodeRequest generate(String code) {
        String responseType = "code";
        String redirectUri = "http://www.example.com";
        String clientId = "test";
        String codeChallenge = randomUUID().toString();
        Set<String> resourceIds = singleton("resource");
        Authority authority = Authority
                .builder()
                .id(1L)
                .name("user")
                .build();
        Account account = Account
                .builder()
                .id(1L)
                .email("test")
                .password("1234")
                .build();
        AccountAuthority
                .builder()
                .id(1L)
                .authority(authority)
                .account(account)
                .build();
        AccountDetails accountDetails = new AccountDetails(account);
        List<GrantedAuthority> authorities = createAuthorityList("user", "admin");
        Set<String> responseTypes = singleton(responseType);
        Set<String> scopes = new HashSet<>(Arrays.asList("read", "write"));
        Map<String, String> params = new HashMap<>();
        params.put("scope", "read write");
        params.put("response_type", responseType);
        params.put("redirect_uri", redirectUri);
        params.put("code_challenge_method", S256.toString());
        params.put("state", randomUUID().toString());
        params.put("client_id", clientId);
        params.put("code_challenge", codeChallenge);
        OAuth2Request oAuth2Request = new OAuth2Request(
                params,
                clientId,
                authorities,
                true,
                scopes,
                resourceIds,
                redirectUri,
                responseTypes,
                Collections.emptyMap()
        );
        return new Oauth2CodeRequest(
                code,
                codeChallenge,
                S256,
                oAuth2Request,
                accountDetails
        );
    }

}

package com.bakery.auth.test.integration;

import com.bakery.auth.base.AbstractIntegrationTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.mock.web.MockHttpSession;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static org.springframework.http.HttpHeaders.CONTENT_TYPE;
import static org.springframework.restdocs.headers.HeaderDocumentation.*;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.restdocs.payload.PayloadDocumentation.responseFields;
import static org.springframework.restdocs.request.RequestDocumentation.parameterWithName;
import static org.springframework.restdocs.request.RequestDocumentation.requestParameters;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@DisplayName("Oauth2 Endpoint 통합 테스트")
public class AuthConfigTest extends AbstractIntegrationTest {

    @Test
    @DisplayName("토큰 유효성 검증 성공 200")
    public void checkToken_200() throws Exception {
        getCheckTokenResponse(
                testProperties.getClients().getMaster().getClientId(),
                testProperties.getClients().getMaster().getClientSecret(),
                getAccessToken(
                        testProperties.getUsers().getMaster().getUsername(),
                        testProperties.getUsers().getMaster().getPassword(),
                        testProperties.getClients().getMaster().getClientId(),
                        testProperties.getClients().getMaster().getClientSecret(),
                        testProperties.getClients().getMaster().getScopes().replace(",", " ")
                )
        )
                .andExpect(status().isOk())
                .andExpect(jsonPath("aud").exists())
                .andExpect(jsonPath("user_name").exists())
                .andExpect(jsonPath("scope").exists())
                .andExpect(jsonPath("active").exists())
                .andExpect(jsonPath("exp").exists())
                .andExpect(jsonPath("authorities").exists())
                .andExpect(jsonPath("jti").exists())
                .andExpect(jsonPath("client_id").exists())
                .andDo(print())
                .andDo(
                        document(
                                "check-token",
                                requestHeaders(
                                        headerWithName(CONTENT_TYPE).description("요청 본문 타입"),
                                        headerWithName(AUTHORIZATION).description("클라이언트 정보 Basic BASE64(client_id:client_secret)")
                                ),
                                requestParameters(
                                        parameterWithName("token").description("엑세스 토큰")
                                ),
                                responseHeaders(
                                        headerWithName(CONTENT_TYPE).description("응답 본문 타입")
                                ),
                                responseFields(
                                        fieldWithPath("aud").description("토큰 대상자"),
                                        fieldWithPath("user_id").description("사용자 식별키"),
                                        fieldWithPath("user_name").description("사용자 아이디"),
                                        fieldWithPath("scope").description("토큰 접근 범위"),
                                        fieldWithPath("active").description("토큰 유효 여부"),
                                        fieldWithPath("exp").description("토큰 만료 시간"),
                                        fieldWithPath("authorities").description("토큰 접근 권한"),
                                        fieldWithPath("jti").description("토큰 고유 식별키"),
                                        fieldWithPath("client_id").description("클라이언트 ID")
                                )
                        )
                );
    }

    @Test
    @DisplayName("토큰 유효성 검증 잘못된 토큰/만료된 토큰 실패 400")
    public void checkToken_400() throws Exception {
        // Invalid Token
        getCheckTokenResponse(
                testProperties.getClients().getMaster().getClientId(),
                testProperties.getClients().getMaster().getClientSecret(),
                "invalid token"
        )
                .andExpect(status().isBadRequest())
                .andDo(print());
        // Expired Token
        getCheckTokenResponse(
                testProperties.getClients().getMaster().getClientId(),
                testProperties.getClients().getMaster().getClientSecret(),
                "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJhdWQiOlsiYXV0aCJdLCJ1c2VyX25hbWUiOiJ1c2VyIiwic2NvcGUiOlsicmVhZCJdLCJleHAiOjE1MDM3ODQ3NzksImF1dGhvcml0aWVzIjpbInVzZXIiXSwianRpIjoiMWIxNmU4MGEtZWU0OS00ODFkLTk3ZGItN2U5NmNjOWI5OTA5IiwiY2xpZW50X2lkIjoidGVzdCJ9.oSqltl_AncyFdnFBj77NjdxyG88xmDBXQnjZYy0XHgk"
        )
                .andExpect(status().isBadRequest())
                .andDo(print());
    }

    @Test
    @DisplayName("토큰 유효성 검증 클라이언트 정보 잘못된 경우 실패 401")
    public void checkToken_401() throws Exception {
        // Invalid Client Id
        getCheckTokenResponse(
                "invalid client id",
                testProperties.getClients().getMaster().getClientSecret(),
                getAccessToken(
                        testProperties.getUsers().getMaster().getUsername(),
                        testProperties.getUsers().getMaster().getPassword(),
                        testProperties.getClients().getMaster().getClientId(),
                        testProperties.getClients().getMaster().getClientSecret(),
                        testProperties.getClients().getMaster().getScopes().replace(",", " ")
                )
        )
                .andExpect(status().isUnauthorized())
                .andDo(print());
        // Invalid Client Secret
        getCheckTokenResponse(
                testProperties.getClients().getMaster().getClientId(),
                "invalid client secret",
                getAccessToken(
                        testProperties.getUsers().getMaster().getUsername(),
                        testProperties.getUsers().getMaster().getPassword(),
                        testProperties.getClients().getMaster().getClientId(),
                        testProperties.getClients().getMaster().getClientSecret(),
                        testProperties.getClients().getMaster().getScopes().replace(",", " ")
                )
        )
                .andExpect(status().isUnauthorized())
                .andDo(print());
    }

    @Test
    @DisplayName("Authorization Code With PKCE 방식 토큰 발급 성공 200")
    public void getToken_AuthorizationCodeGrantWithPkce_200() throws Exception {
        getAuthorizeResponse(
                testProperties.getUsers().getMaster().getUsername(),
                testProperties.getClients().getMaster().getClientId(),
                testProperties.getClients().getMaster().getRedirectUris().split(",")[0],
                testProperties.getClients().getMaster().getScopes().replace(",", " "),
                testProperties.getClients().getMaster().getCodeChallenge(),
                testProperties.getClients().getMaster().getCodeChallengeMethod(),
                testProperties.getClients().getMaster().getState()
        )
                .andExpect(status().isOk())
                .andDo(print())
                .andDo(
                        document(
                                "authorization-code-with-pkce-grant-get",
                                requestParameters(
                                        parameterWithName("client_id").description("클라이언트 id"),
                                        parameterWithName("response_type").description("응답 방식, code 고정"),
                                        parameterWithName("redirect_uri").description("리다이렉트 경로"),
                                        parameterWithName("scope").description("토큰 접근 범위, 여러 개인 경우 공백으로 구분").optional(),
                                        parameterWithName("code_challenge").description("코드 비교 값, BASE64(SHA256(code_verifier))"),
                                        parameterWithName("code_challenge_method").description("코드 비교 값 암호화 방식, S256 고정"),
                                        parameterWithName("state").description("인가 코드 발급 콜백시 전달하는 파라미터").optional()
                                )
                        ))
                .andDo(result ->
                        getAuthorizeConfirmResponse(
                                (MockHttpSession) result.getRequest().getSession(),
                                testProperties.getClients().getMaster().getClientId(),
                                testProperties.getClients().getMaster().getRedirectUris().split(",")[0],
                                testProperties.getClients().getMaster().getScopes().replace(",", " ")
                        )
                                .andExpect(status().is3xxRedirection())
                                .andDo(print())
                                .andDo(
                                        document(
                                                "authorization-code-with-pkce-grant-post",
                                                responseHeaders(
                                                        headerWithName(HttpHeaders.LOCATION).description("리다이렉트 경로")
                                                )
                                        )
                                )
                                .andDo(result2 -> {
                                    String redirectedUrl = result2
                                            .getResponse()
                                            .getRedirectedUrl();
                                    assertTrue(redirectedUrl.contains(testProperties.getClients().getMaster().getRedirectUris().split(",")[0]));
                                    assertTrue(redirectedUrl.contains("?code="));
                                    assertTrue(redirectedUrl.contains("&state=" + testProperties.getClients().getMaster().getState()));
                                    getTokenAuthorizationCodeWithPkceResponse(
                                            testProperties.getClients().getMaster().getClientId(),
                                            testProperties.getClients().getMaster().getClientSecret(),
                                            redirectedUrl.substring(redirectedUrl.indexOf("=") + 1, redirectedUrl.indexOf("&")),
                                            testProperties.getClients().getMaster().getRedirectUris().split(",")[0],
                                            testProperties.getClients().getMaster().getCodeVerifier()
                                    )
                                            .andExpect(status().isOk())
                                            .andExpect(jsonPath("access_token").exists())
                                            .andExpect(jsonPath("token_type").value("bearer"))
                                            .andExpect(jsonPath("refresh_token").exists())
                                            .andExpect(jsonPath("expires_in").exists())
                                            .andExpect(jsonPath("scope").exists())
                                            .andExpect(jsonPath("jti").exists())
                                            .andDo(print())
                                            .andDo(document(
                                                    "token-authorization-code-with-pkce-grant",
                                                    requestHeaders(
                                                            headerWithName(CONTENT_TYPE).description("요청 본문 타입")
                                                    ),
                                                    requestParameters(
                                                            parameterWithName("client_id").description("클라이언트 id"),
                                                            parameterWithName("client_secret").description("클라이언트 secret").optional(),
                                                            parameterWithName("code").description("인가 코드"),
                                                            parameterWithName("grant_type").description("토큰 발급 방식, authorization_code 고정"),
                                                            parameterWithName("redirect_uri").description("리다이렉트 경로"),
                                                            parameterWithName("code_verifier").description("코드 검증 값")
                                                    ),
                                                    responseHeaders(
                                                            headerWithName(CONTENT_TYPE).description("응답 본문 타입")
                                                    ),
                                                    responseFields(
                                                            fieldWithPath("access_token").description("엑세스 토큰"),
                                                            fieldWithPath("token_type").description("토큰 타입, bearer 고정"),
                                                            fieldWithPath("refresh_token").description("재발급 토큰").optional(),
                                                            fieldWithPath("expires_in").description("토큰 유효 시간(초)"),
                                                            fieldWithPath("scope").description("토큰 접근 범위"),
                                                            fieldWithPath("jti").description("토큰 고유 식별키")
                                                    )
                                            ));
                                })
                );

    }

    @Test
    @DisplayName("Authorization Code With PKCE 방식 토큰 발급 부정확한 Code Verifier/Code/Redirect Uri 값으로 실패하는 경우 400")
    public void getTokenAuthorizationCodeGrant_400() throws Exception {
        // Invalid Code Verifier
        getAuthorizeResponse(
                testProperties.getUsers().getMaster().getUsername(),
                testProperties.getClients().getMaster().getClientId(),
                testProperties.getClients().getMaster().getRedirectUris().split(",")[0],
                testProperties.getClients().getMaster().getScopes().replace(",", " "),
                testProperties.getClients().getMaster().getCodeChallenge(),
                testProperties.getClients().getMaster().getCodeChallengeMethod(),
                testProperties.getClients().getMaster().getState()
        )
                .andDo(print())
                .andExpect(status().isOk())
                .andDo(result ->
                        getAuthorizeConfirmResponse(
                                (MockHttpSession) result.getRequest().getSession(),
                                testProperties.getClients().getMaster().getClientId(),
                                testProperties.getClients().getMaster().getClientSecret(),
                                testProperties.getClients().getMaster().getScopes().replace(",", " ")
                        )
                                .andDo(print())
                                .andExpect(status().is3xxRedirection())
                                .andDo(result2 -> {
                                    String redirectedUrl = result2
                                            .getResponse()
                                            .getRedirectedUrl();
                                    assertTrue(redirectedUrl.contains(testProperties.getClients().getMaster().getRedirectUris().split(",")[0]));
                                    assertTrue(redirectedUrl.contains("?code="));
                                    assertTrue(redirectedUrl.contains("&state=" + testProperties.getClients().getMaster().getState()));
                                    getTokenAuthorizationCodeWithPkceResponse(
                                            testProperties.getClients().getMaster().getClientId(),
                                            testProperties.getClients().getMaster().getClientSecret(),
                                            redirectedUrl.substring(redirectedUrl.indexOf("=") + 1),
                                            testProperties.getClients().getMaster().getRedirectUris().split(",")[0],
                                            "invalid code verifier"
                                    )
                                            .andExpect(status().isBadRequest())
                                            .andDo(print());
                                })
                );
        // Invalid Code
        getTokenAuthorizationCodeWithPkceResponse(
                testProperties.getClients().getMaster().getClientId(),
                testProperties.getClients().getMaster().getClientSecret(),
                "invalid code",
                testProperties.getClients().getMaster().getRedirectUris().split(",")[0],
                testProperties.getClients().getMaster().getCodeVerifier()
        )
                .andExpect(status().isBadRequest())
                .andDo(print());
        // Invalid Grant Type
        getAuthorizeResponse(
                testProperties.getUsers().getMaster().getUsername(),
                testProperties.getClients().getNoGrantTypes().getClientId(),
                testProperties.getClients().getNoGrantTypes().getRedirectUris().split(",")[0],
                testProperties.getClients().getNoGrantTypes().getScopes().replace(",", " "),
                testProperties.getClients().getNoGrantTypes().getCodeChallenge(),
                testProperties.getClients().getNoGrantTypes().getCodeChallengeMethod(),
                testProperties.getClients().getMaster().getState()
        )
                .andDo(print())
                .andExpect(status().isBadRequest());
        // Invalid Redirect Uri
        getAuthorizeResponse(
                testProperties.getUsers().getMaster().getUsername(),
                testProperties.getClients().getMaster().getClientId(),
                "invalid redirect uri",
                testProperties.getClients().getMaster().getScopes().replace(",", " "),
                testProperties.getClients().getMaster().getCodeChallenge(),
                testProperties.getClients().getMaster().getCodeChallengeMethod(),
                testProperties.getClients().getMaster().getState()
        )
                .andDo(print())
                .andExpect(status().isBadRequest());

        getAuthorizeResponse(
                testProperties.getUsers().getMaster().getUsername(),
                testProperties.getClients().getMaster().getClientId(),
                testProperties.getClients().getMaster().getRedirectUris().split(",")[0],
                testProperties.getClients().getMaster().getScopes().replace(",", " "),
                testProperties.getClients().getMaster().getCodeChallenge(),
                testProperties.getClients().getMaster().getCodeChallengeMethod(),
                testProperties.getClients().getMaster().getState()
        )
                .andDo(print())
                .andExpect(status().isOk())
                .andDo(result ->
                        getAuthorizeConfirmResponse(
                                (MockHttpSession) result.getRequest().getSession(),
                                testProperties.getClients().getMaster().getClientId(),
                                testProperties.getClients().getMaster().getClientSecret(),
                                testProperties.getClients().getMaster().getScopes().replace(",", " ")
                        )
                                .andDo(print())
                                .andExpect(status().is3xxRedirection())
                                .andDo(result2 -> {
                                    String redirectedUrl = result2
                                            .getResponse()
                                            .getRedirectedUrl();
                                    assertTrue(redirectedUrl.contains(testProperties.getClients().getMaster().getRedirectUris().split(",")[0]));
                                    assertTrue(redirectedUrl.contains("?code="));
                                    assertTrue(redirectedUrl.contains("&state=" + testProperties.getClients().getMaster().getState()));
                                    getTokenAuthorizationCodeWithPkceResponse(
                                            testProperties.getClients().getMaster().getClientId(),
                                            testProperties.getClients().getMaster().getClientSecret(),
                                            redirectedUrl.substring(redirectedUrl.indexOf("=") + 1),
                                            "invalid redirect url",
                                            testProperties.getClients().getMaster().getCodeVerifier()
                                    )
                                            .andExpect(status().isBadRequest())
                                            .andDo(print());
                                })
                );
    }

    @Test
    @DisplayName("Authorization Code With PKCE 방식 토큰 발급 부정확한 Scope/Code Challenge Method 값으로 실패하는 경우 303")
    public void getTokenAuthorizationCodeGrant_303() throws Exception {
        // Invalid Scopes
        getAuthorizeResponse(
                testProperties.getUsers().getMaster().getUsername(),
                testProperties.getClients().getNoScopes().getClientId(),
                testProperties.getClients().getNoScopes().getRedirectUris().split(",")[0],
                testProperties.getClients().getMaster().getScopes().replace(",", " "),
                testProperties.getClients().getMaster().getCodeChallenge(),
                testProperties.getClients().getMaster().getCodeChallengeMethod(),
                testProperties.getClients().getMaster().getState()
        )
                .andDo(print())
                .andExpect(status().is3xxRedirection())
                .andExpect(result2 -> {
                    String redirectedUrl = result2
                            .getResponse()
                            .getRedirectedUrl();
                    assertTrue(redirectedUrl.contains(testProperties.getClients().getNoScopes().getRedirectUris().split(",")[0]));
                    assertTrue(redirectedUrl.contains("?error="));
                    assertTrue(redirectedUrl.contains("&error_description="));
                    assertTrue(redirectedUrl.contains("&scope="));
                    assertFalse(redirectedUrl.contains("&code="));
                });
        // Invalid Code Challenge Method
        getAuthorizeResponse(
                testProperties.getUsers().getMaster().getUsername(),
                testProperties.getClients().getMaster().getClientId(),
                testProperties.getClients().getMaster().getRedirectUris().split(",")[0],
                testProperties.getClients().getMaster().getScopes().replace(",", " "),
                testProperties.getClients().getMaster().getCodeChallenge(),
                "invalid hash algorithm",
                testProperties.getClients().getMaster().getState()
        )
                .andDo(print())
                .andExpect(status().isOk())
                .andDo(result ->
                        getAuthorizeConfirmResponse(
                                (MockHttpSession) result.getRequest().getSession(),
                                testProperties.getClients().getMaster().getClientId(),
                                testProperties.getClients().getMaster().getClientSecret(),
                                testProperties.getClients().getMaster().getScopes().replace(",", " ")
                        )
                                .andDo(print())
                                .andExpect(status().is3xxRedirection())
                                .andExpect(result2 -> {
                                    String redirectedUrl = result2
                                            .getResponse()
                                            .getRedirectedUrl();
                                    assertTrue(redirectedUrl.contains(testProperties.getClients().getNoScopes().getRedirectUris().split(",")[0]));
                                    assertTrue(redirectedUrl.contains("?error="));
                                    assertTrue(redirectedUrl.contains("&error_description="));
                                    assertFalse(redirectedUrl.contains("&code="));
                                })
                );
    }

    @Test
    @DisplayName("Authorization Code With PKCE 방식 토큰 발급 부정확한 클라이언트 정보로 실패하는 경우 401")
    public void getTokenAuthorizationCodeGrant_401() throws Exception {
        // Invalid Client Id
        getAuthorizeResponse(
                testProperties.getUsers().getMaster().getUsername(),
                "invalid client id",
                testProperties.getClients().getMaster().getRedirectUris().split(",")[0],
                testProperties.getClients().getMaster().getScopes().replace(",", " "),
                testProperties.getClients().getMaster().getCodeChallenge(),
                testProperties.getClients().getMaster().getCodeChallengeMethod(),
                testProperties.getClients().getMaster().getState()
        )
                .andDo(print())
                .andExpect(status().isUnauthorized());

        getAuthorizeResponse(
                testProperties.getUsers().getMaster().getUsername(),
                testProperties.getClients().getMaster().getClientId(),
                testProperties.getClients().getMaster().getRedirectUris().split(",")[0],
                testProperties.getClients().getMaster().getScopes().replace(",", " "),
                testProperties.getClients().getMaster().getCodeChallenge(),
                testProperties.getClients().getMaster().getCodeChallengeMethod(),
                testProperties.getClients().getMaster().getState()
        )
                .andDo(print())
                .andExpect(status().isOk())
                .andDo(result ->
                        getAuthorizeConfirmResponse(
                                (MockHttpSession) result.getRequest().getSession(),
                                testProperties.getClients().getMaster().getClientId(),
                                testProperties.getClients().getMaster().getClientSecret(),
                                testProperties.getClients().getMaster().getScopes().replace(",", " ")
                        )
                                .andDo(print())
                                .andExpect(status().is3xxRedirection())
                                .andDo(result2 -> {
                                    String redirectedUrl = result2
                                            .getResponse()
                                            .getRedirectedUrl();
                                    assertTrue(redirectedUrl.contains(testProperties.getClients().getMaster().getRedirectUris().split(",")[0]));
                                    assertTrue(redirectedUrl.contains("?code="));
                                    assertTrue(redirectedUrl.contains("&state=" + testProperties.getClients().getMaster().getState()));
                                    getTokenAuthorizationCodeWithPkceResponse(
                                            "invalid client id",
                                            testProperties.getClients().getMaster().getClientSecret(),
                                            redirectedUrl.substring(redirectedUrl.indexOf("=") + 1),
                                            testProperties.getClients().getMaster().getRedirectUris().split(",")[0],
                                            testProperties.getClients().getMaster().getCodeVerifier()
                                    )
                                            .andExpect(status().isUnauthorized())
                                            .andDo(print());
                                })
                );
        // Invalid Client Secret
        getAuthorizeResponse(
                testProperties.getUsers().getMaster().getUsername(),
                testProperties.getClients().getMaster().getClientId(),
                testProperties.getClients().getMaster().getRedirectUris().split(",")[0],
                testProperties.getClients().getMaster().getScopes().replace(",", " "),
                testProperties.getClients().getMaster().getCodeChallenge(),
                testProperties.getClients().getMaster().getCodeChallengeMethod(),
                testProperties.getClients().getMaster().getState()
        )
                .andDo(print())
                .andExpect(status().isOk())
                .andDo(result ->
                        getAuthorizeConfirmResponse(
                                (MockHttpSession) result.getRequest().getSession(),
                                testProperties.getClients().getMaster().getClientId(),
                                testProperties.getClients().getMaster().getClientSecret(),
                                testProperties.getClients().getMaster().getScopes().replace(",", " ")
                        )
                                .andDo(print())
                                .andExpect(status().is3xxRedirection())
                                .andDo(result2 -> {
                                    String redirectedUrl = result2
                                            .getResponse()
                                            .getRedirectedUrl();
                                    assertTrue(redirectedUrl.contains(testProperties.getClients().getMaster().getRedirectUris().split(",")[0]));
                                    assertTrue(redirectedUrl.contains("?code="));
                                    assertTrue(redirectedUrl.contains("&state=" + testProperties.getClients().getMaster().getState()));
                                    getTokenAuthorizationCodeWithPkceResponse(
                                            testProperties.getClients().getMaster().getClientId(),
                                            "invalid client secret",
                                            redirectedUrl.substring(redirectedUrl.indexOf("=") + 1),
                                            testProperties.getClients().getMaster().getRedirectUris().split(",")[0],
                                            testProperties.getClients().getMaster().getCodeVerifier()
                                    )
                                            .andExpect(status().isUnauthorized())
                                            .andDo(print());
                                })
                );
    }

    @Test
    @DisplayName("Resource Owner Password Credentials 방식 토큰 발급 성공 200")
    public void getToken_PasswordGrant_200() throws Exception {
        getTokenPasswordGrantResponse(
                testProperties.getUsers().getMaster().getUsername(),
                testProperties.getUsers().getMaster().getPassword(),
                testProperties.getClients().getMaster().getClientId(),
                testProperties.getClients().getMaster().getClientSecret(),
                testProperties.getClients().getMaster().getScopes().replace(",", " ")
        )
                .andExpect(status().isOk())
                .andExpect(jsonPath("access_token").exists())
                .andExpect(jsonPath("refresh_token").exists())
                .andExpect(jsonPath("token_type").value("bearer"))
                .andExpect(jsonPath("expires_in").exists())
                .andExpect(jsonPath("scope").exists())
                .andExpect(jsonPath("jti").exists())
                .andDo(print())
                .andDo(
                        document(
                                "token-password-grant",
                                requestHeaders(
                                        headerWithName(CONTENT_TYPE).description("요청 본문 타입")
                                ),
                                requestParameters(
                                        parameterWithName("client_id").description("클라이언트 id"),
                                        parameterWithName("client_secret").description("클라이언트 secret").optional(),
                                        parameterWithName("username").description("사용자 아이디"),
                                        parameterWithName("password").description("사용자 패스워드"),
                                        parameterWithName("grant_type").description("토큰 발급 방식, password 고정"),
                                        parameterWithName("scope").description("토큰 접근 범위, 여러 개인 경우 공백으로 구분").optional()
                                ),
                                responseHeaders(
                                        headerWithName(CONTENT_TYPE).description("응답 본문 타입")
                                ),
                                responseFields(
                                        fieldWithPath("access_token").description("엑세스 토큰"),
                                        fieldWithPath("token_type").description("토큰 타입, bearer 고정"),
                                        fieldWithPath("refresh_token").description("재발급 토큰").optional(),
                                        fieldWithPath("expires_in").description("토큰 유효 시간(초)"),
                                        fieldWithPath("scope").description("토큰 접근 범위"),
                                        fieldWithPath("jti").description("토큰 고유 식별키")
                                )
                        )
                );
    }

    @Test
    @DisplayName("Resource Owner Password Credentials 방식 토큰 발급 잘못된 유저 정보/Scope 값으로 실패 400")
    public void getToken_PasswordGrant_400() throws Exception {
        // Invalid Username
        getTokenPasswordGrantResponse(
                "invalid username",
                testProperties.getUsers().getMaster().getPassword(),
                testProperties.getClients().getMaster().getClientId(),
                testProperties.getClients().getMaster().getClientSecret(),
                testProperties.getClients().getMaster().getScopes().replace(",", " ")
        )
                .andExpect(status().isBadRequest())
                .andDo(print());
        // Invalid Password
        getTokenPasswordGrantResponse(
                testProperties.getUsers().getMaster().getUsername(),
                "invalid password",
                testProperties.getClients().getMaster().getClientId(),
                testProperties.getClients().getMaster().getClientSecret(),
                testProperties.getClients().getMaster().getScopes().replace(",", " ")
        )
                .andExpect(status().isBadRequest())
                .andDo(print());
        // Invalid Scope
        getTokenPasswordGrantResponse(
                testProperties.getUsers().getMaster().getUsername(),
                testProperties.getUsers().getMaster().getPassword(),
                testProperties.getClients().getNoScopes().getClientId(),
                testProperties.getClients().getNoScopes().getClientSecret(),
                testProperties.getClients().getMaster().getScopes().replace(",", " ")
        )
                .andExpect(status().isBadRequest())
                .andDo(print());
    }

    @Test
    @DisplayName("Resource Owner Password Credentials 방식 토큰 발급 클라이언트 정보 잘못된 경우 실패 401")
    public void getToken_PasswordGrant_401() throws Exception {
        // Invalid Client Id
        getTokenPasswordGrantResponse(
                testProperties.getUsers().getMaster().getUsername(),
                testProperties.getUsers().getMaster().getPassword(),
                "invalid client id",
                testProperties.getClients().getMaster().getClientSecret(),
                testProperties.getClients().getMaster().getScopes().replace(",", " ")
        )
                .andExpect(status().isUnauthorized())
                .andDo(print());
        // Invalid Client Secret
        getTokenPasswordGrantResponse(
                testProperties.getUsers().getMaster().getUsername(),
                testProperties.getUsers().getMaster().getPassword(),
                testProperties.getClients().getMaster().getClientId(),
                "invalid client secret",
                testProperties.getClients().getMaster().getScopes().replace(",", " ")
        )
                .andExpect(status().isUnauthorized())
                .andDo(print());
        // Invalid Grant Type
        getTokenPasswordGrantResponse(
                testProperties.getUsers().getMaster().getUsername(),
                testProperties.getUsers().getMaster().getPassword(),
                testProperties.getClients().getNoGrantTypes().getClientId(),
                testProperties.getClients().getNoGrantTypes().getClientId(),
                testProperties.getClients().getNoGrantTypes().getScopes().replace(",", " ")
        )
                .andExpect(status().isUnauthorized())
                .andDo(print());
    }

    @Test
    @DisplayName("Refresh Token 방식 토큰 발급 성공 200")
    public void getToken_RefreshTokenGrant_200() throws Exception {
        getTokenRefreshTokenGrantResponse(
                getRefreshToken(
                        testProperties.getUsers().getMaster().getUsername(),
                        testProperties.getUsers().getMaster().getPassword(),
                        testProperties.getClients().getMaster().getClientId(),
                        testProperties.getClients().getMaster().getClientSecret(),
                        testProperties.getClients().getMaster().getScopes().replace(",", " ")
                ),
                testProperties.getClients().getMaster().getScopes().replace(",", " "),
                testProperties.getClients().getMaster().getClientId(),
                testProperties.getClients().getMaster().getClientSecret()
        )
                .andExpect(status().isOk())
                .andExpect(jsonPath("access_token").exists())
                .andExpect(jsonPath("refresh_token").exists())
                .andExpect(jsonPath("token_type").value("bearer"))
                .andExpect(jsonPath("expires_in").exists())
                .andExpect(jsonPath("scope").exists())
                .andExpect(jsonPath("jti").exists())
                .andDo(print())
                .andDo(
                        document(
                                "token-refresh-token-grant",
                                requestHeaders(
                                        headerWithName(CONTENT_TYPE).description("요청 본문 타입")
                                ),
                                requestParameters(
                                        parameterWithName("client_id").description("클라이언트 id"),
                                        parameterWithName("client_secret").description("클라이언트 secret"),
                                        parameterWithName("refresh_token").description("재발급 토큰"),
                                        parameterWithName("grant_type").description("토큰 발급 방식, refresh_token 고정"),
                                        parameterWithName("scope").description("토큰 접근 범위, 여러 개인 경우 공백으로 구분").optional()
                                ),
                                responseHeaders(
                                        headerWithName(CONTENT_TYPE).description("응답 본문 타입")
                                ),
                                responseFields(
                                        fieldWithPath("access_token").description("엑세스 토큰"),
                                        fieldWithPath("token_type").description("토큰 타입, bearer 고정"),
                                        fieldWithPath("refresh_token").description("재발급 토큰"),
                                        fieldWithPath("expires_in").description("토큰 유효 시간(초)"),
                                        fieldWithPath("scope").description("토큰 접근 범위"),
                                        fieldWithPath("jti").description("토큰 고유 식별키")
                                )
                        )
                );
    }

    @Test
    @DisplayName("Refresh Token 방식 토큰 발급 재발급 토큰 기한 만료된 경우 실패 400")
    public void getToken_RefreshTokenGrant_400() throws Exception {
        // Expired Refresh Token
        getTokenRefreshTokenGrantResponse(
                "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJhdWQiOlsiYXV0aCJdLCJ1c2VyX25hbWUiOiJ1c2VyIiwic2NvcGUiOlsicmVhZCJdLCJhdGkiOiIyNTNhNmMxMC0wYTM3LTQyODktODRiNy01OGI3MjVjNWUyNzUiLCJleHAiOjE1MDQ2MjEwOTcsImF1dGhvcml0aWVzIjpbInVzZXIiXSwianRpIjoiZWVhZTIyYTAtNWJmZS00ODA2LTg0MGMtODU2NTAzYTNlNjBhIiwiY2xpZW50X2lkIjoidGVzdCJ9.VlIahUnYKyMdXVkcL9uhcw7QxWzJdGm8n5y5zbqpyAs",
                testProperties.getClients().getMaster().getScopes().replace(",", " "),
                testProperties.getClients().getMaster().getClientId(),
                testProperties.getClients().getMaster().getClientSecret()
        )
                .andExpect(status().isUnauthorized())
                .andDo(print());
    }

    @Test
    @DisplayName("Refresh Token 방식 토큰 발급 잘못된 재발급 토큰/클라이언트 정보 값으로 실패 401")
    public void getToken_RefreshTokenGrant_Invalid_401() throws Exception {
        // Invalid Refresh Token
        getTokenRefreshTokenGrantResponse(
                "invalid refresh token",
                testProperties.getClients().getMaster().getScopes().replace(",", " "),
                testProperties.getClients().getMaster().getClientId(),
                testProperties.getClients().getMaster().getClientSecret()
        )
                .andExpect(status().isUnauthorized())
                .andDo(print());
        // Invalid Client Id, Secret
        getTokenRefreshTokenGrantResponse(
                getRefreshToken(
                        testProperties.getUsers().getMaster().getUsername(),
                        testProperties.getUsers().getMaster().getPassword(),
                        testProperties.getClients().getMaster().getClientId(),
                        testProperties.getClients().getMaster().getClientSecret(),
                        testProperties.getClients().getMaster().getScopes().replace(",", " ")
                ),
                testProperties.getClients().getMaster().getScopes().replace(",", " "),
                "invalid clientId",
                testProperties.getClients().getMaster().getClientSecret()
        )
                .andExpect(status().isUnauthorized())
                .andDo(print());
        // Invalid Client Secret
        getTokenRefreshTokenGrantResponse(
                getRefreshToken(
                        testProperties.getUsers().getMaster().getUsername(),
                        testProperties.getUsers().getMaster().getPassword(),
                        testProperties.getClients().getMaster().getClientId(),
                        testProperties.getClients().getMaster().getClientSecret(),
                        testProperties.getClients().getMaster().getScopes().replace(",", " ")
                ),
                testProperties.getClients().getMaster().getScopes().replace(",", " "),
                testProperties.getClients().getMaster().getClientId(),
                "invalid client secret"
        )
                .andExpect(status().isUnauthorized())
                .andDo(print());
        // Invalid Grant Type
        getTokenRefreshTokenGrantResponse(
                getRefreshToken(
                        testProperties.getUsers().getMaster().getUsername(),
                        testProperties.getUsers().getMaster().getPassword(),
                        testProperties.getClients().getNoGrantTypes().getClientId(),
                        testProperties.getClients().getNoGrantTypes().getClientSecret(),
                        testProperties.getClients().getNoGrantTypes().getScopes().replace(",", " ")
                ),
                testProperties.getClients().getNoGrantTypes().getScopes().replace(",", " "),
                testProperties.getClients().getNoGrantTypes().getClientId(),
                testProperties.getClients().getNoGrantTypes().getClientSecret()
        )
                .andExpect(status().isUnauthorized())
                .andDo(print());
    }

    @Test
    @DisplayName("Client Credentials 방식 토큰 발급 성공 200")
    public void getToken_ClientCredentialsGrant_200() throws Exception {
        getClientCredentialsGrantResponse(
                testProperties.getClients().getMaster().getClientId(),
                testProperties.getClients().getMaster().getClientSecret(),
                testProperties.getClients().getMaster().getScopes().replace(",", " ")
        )
                .andExpect(status().isOk())
                .andExpect(jsonPath("access_token").exists())
                .andExpect(jsonPath("token_type").value("bearer"))
                .andExpect(jsonPath("expires_in").exists())
                .andExpect(jsonPath("scope").exists())
                .andExpect(jsonPath("jti").exists())
                .andDo(print())
                .andDo(
                        document(
                                "token-client-credentials-grant",
                                requestHeaders(
                                        headerWithName(CONTENT_TYPE).description("요청 본문 타입")
                                ),
                                requestParameters(
                                        parameterWithName("client_id").description("클라이언트 id"),
                                        parameterWithName("client_secret").description("클라이언트 secret").optional(),
                                        parameterWithName("grant_type").description("토큰 발급 방식, client_credentials 고정"),
                                        parameterWithName("scope").description("토큰 접근 범위, 여러 개인 경우 공백으로 구분").optional()
                                ),
                                responseHeaders(
                                        headerWithName(CONTENT_TYPE).description("응답 본문 타입")
                                ),
                                responseFields(
                                        fieldWithPath("access_token").description("엑세스 토큰"),
                                        fieldWithPath("token_type").description("토큰 타입, bearer 고정"),
                                        fieldWithPath("expires_in").description("토큰 유효 시간(초)"),
                                        fieldWithPath("scope").description("토큰 접근 범위"),
                                        fieldWithPath("jti").description("토큰 고유 식별키")
                                )
                        )
                );
    }

    @Test
    @DisplayName("Client Credentials 방식 부정확한 클라이언트 정보로 실패하는 경우 401")
    public void getToken_ClientCredentialsGrant_401() throws Exception {
        // Invalid Client Id
        getClientCredentialsGrantResponse(
                "invalid client id",
                testProperties.getClients().getMaster().getClientSecret(),
                testProperties.getClients().getMaster().getScopes().replace(",", " ")
        )
                .andExpect(status().isUnauthorized())
                .andDo(print());
        // Invalid Client Secret
        getClientCredentialsGrantResponse(
                testProperties.getClients().getMaster().getClientId(),
                "invalid client secret",
                testProperties.getClients().getMaster().getScopes().replace(",", " ")
        )
                .andExpect(status().isUnauthorized())
                .andDo(print());
        // Invalid Grant Type
        getClientCredentialsGrantResponse(
                testProperties.getClients().getNoGrantTypes().getClientId(),
                testProperties.getClients().getNoGrantTypes().getClientSecret(),
                testProperties.getClients().getNoGrantTypes().getScopes().replace(",", " ")
        )
                .andExpect(status().isUnauthorized())
                .andDo(print());
    }

    @Test
    @DisplayName("Client Credentials 방식 부정확한 클라이언트 Scope 값으로 실패하는 경우 400")
    public void getToken_ClientCredentialsGrant_400() throws Exception {
        getClientCredentialsGrantResponse(
                testProperties.getClients().getNoScopes().getClientId(),
                testProperties.getClients().getNoScopes().getClientSecret(),
                testProperties.getClients().getMaster().getScopes().replace(",", " ")
        )
                .andExpect(status().isBadRequest())
                .andDo(print());
    }

}

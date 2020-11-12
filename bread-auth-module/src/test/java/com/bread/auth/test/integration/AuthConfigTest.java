package com.bread.auth.test.integration;

import com.bread.auth.base.AbstractIntegrationTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static org.springframework.http.HttpHeaders.CONTENT_TYPE;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.restdocs.headers.HeaderDocumentation.*;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.post;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.restdocs.payload.PayloadDocumentation.responseFields;
import static org.springframework.restdocs.request.RequestDocumentation.parameterWithName;
import static org.springframework.restdocs.request.RequestDocumentation.requestParameters;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

public class AuthConfigTest extends AbstractIntegrationTest {

    @Test
    @DisplayName("인증 서버 토큰 유효성 검증 성공 200")
    public void checkToken_200() throws Exception {
        mockMvc
                .perform(
                        post("/oauth/check_token")
                                .accept(APPLICATION_JSON)
                                .queryParam(
                                        "token",
                                        getAccessToken(
                                                testProperties.getUsers().getMaster().getUsername(),
                                                testProperties.getUsers().getMaster().getPassword(),
                                                testProperties.getClients().getMaster().getClientId(),
                                                testProperties.getClients().getMaster().getClientSecret(),
                                                testProperties.getClients().getMaster().getScopes().replace(",", " ")
                                        )
                                )
                                .with(httpBasic(testProperties.getClients().getMaster().getClientId(), testProperties.getClients().getMaster().getClientSecret()))
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
                                        headerWithName(AUTHORIZATION).description("클라이언트 ID/SECRET 인코딩 값")
                                ),
                                requestParameters(
                                        parameterWithName("token").description("인증 토큰")
                                ),
                                responseHeaders(
                                        headerWithName(CONTENT_TYPE).description("응답 본문 타입")
                                ),
                                responseFields(
                                        fieldWithPath("aud").description("토큰 발행자"),
                                        fieldWithPath("user_id").description("사용자 식별키"),
                                        fieldWithPath("user_name").description("사용자 아이디"),
                                        fieldWithPath("scope").description("토큰의 접근 범위"),
                                        fieldWithPath("active").description("토큰 유효 여부"),
                                        fieldWithPath("exp").description("토큰 만료 시간"),
                                        fieldWithPath("authorities").description("토큰의 접근 권한"),
                                        fieldWithPath("jti").description("토큰의 고유 식별자"),
                                        fieldWithPath("client_id").description("클라이언트 ID")
                                )
                        )
                );
    }

    @Test
    @DisplayName("인증 서버 토큰 유효성 검증 잘못된 토큰/만료된 토큰 실패 400")
    public void checkToken_400() throws Exception {
        // Invalid Token
        mockMvc
                .perform(
                        post("/oauth/check_token")
                                .accept(APPLICATION_JSON)
                                .queryParam("token", "invalid token")
                                .with(httpBasic(testProperties.getClients().getMaster().getClientId(), testProperties.getClients().getMaster().getClientSecret()))
                )
                .andExpect(status().isBadRequest())
                .andDo(print());
        // Expired Token
        mockMvc
                .perform(
                        post("/oauth/check_token")
                                .accept(APPLICATION_JSON)
                                .queryParam("token", "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJhdWQiOlsiYXV0aCJdLCJ1c2VyX25hbWUiOiJ1c2VyIiwic2NvcGUiOlsicmVhZCJdLCJleHAiOjE1MDM3ODQ3NzksImF1dGhvcml0aWVzIjpbInVzZXIiXSwianRpIjoiMWIxNmU4MGEtZWU0OS00ODFkLTk3ZGItN2U5NmNjOWI5OTA5IiwiY2xpZW50X2lkIjoidGVzdCJ9.oSqltl_AncyFdnFBj77NjdxyG88xmDBXQnjZYy0XHgk")
                                .with(httpBasic(testProperties.getClients().getMaster().getClientId(), testProperties.getClients().getMaster().getClientSecret()))
                )
                .andExpect(status().isBadRequest())
                .andDo(print());
    }

    @Test
    @DisplayName("인증 서버 토큰 유효성 검증 클라이언트 정보 잘못된 경우 실패 401")
    public void checkToken_401() throws Exception {
        mockMvc
                .perform(
                        post("/oauth/check_token")
                                .accept(APPLICATION_JSON)
                                .queryParam(
                                        "token",
                                        getAccessToken(
                                                testProperties.getUsers().getMaster().getUsername(),
                                                testProperties.getUsers().getMaster().getPassword(),
                                                testProperties.getClients().getMaster().getClientId(),
                                                testProperties.getClients().getMaster().getClientSecret(),
                                                testProperties.getClients().getMaster().getScopes().replace(",", " ")
                                        )
                                )
                                .with(httpBasic("invalid clientId", "invalid clientSecret"))
                )
                .andExpect(status().isUnauthorized())
                .andDo(print());
    }

    @Test
    @DisplayName("인증 서버 AUTHORIZATION CODE WITH PKCE 방식 토큰 발급 성공 200")
    public void getToken_AuthorizationCodeGrantWithPkce_200() throws Exception {
        getAuthorizeResponse(
                "code",
                testProperties.getUsers().getMaster().getUsername(),
                testProperties.getClients().getNoSecret().getClientId(),
                testProperties.getClients().getNoSecret().getRedirectUris().split(",")[0],
                testProperties.getClients().getNoSecret().getScopes().replace(",", " "),
                testProperties.getClients().getNoSecret().getCodeChallenge(),
                testProperties.getClients().getNoSecret().getCodeChallengeMethod()
        )
                .andDo(result -> {
                    String redirectedUrl = result
                            .getResponse()
                            .getRedirectedUrl();
                    String code = redirectedUrl.substring(redirectedUrl.indexOf("=") + 1);
                    mockMvc
                            .perform(
                                    post("/oauth/token")
                                            .with(httpBasic(testProperties.getClients().getNoSecret().getClientId(), testProperties.getClients().getNoSecret().getClientSecret()))
                                            .param("code", code)
                                            .param("grant_type", "authorization_code")
                                            .param("redirect_uri", testProperties.getClients().getNoSecret().getRedirectUris().split(",")[0])
                                    .param("code_verifier", testProperties.getClients().getNoSecret().getCodeVerifier())
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
                                            headerWithName(AUTHORIZATION).description("클라이언트 ID/SECRET 인코딩 값")
                                    ),
                                    requestParameters(
                                            parameterWithName("code").description("토큰을 발급받을 수 있는 코드 값"),
                                            parameterWithName("grant_type").description("인증 토큰 발급 방식"),
                                            parameterWithName("redirect_uri").description("리다이렉트 경로"),
                                            parameterWithName("code_verifier").description("PKCE 코드 검증 값")
                                    ),
                                    responseHeaders(
                                            headerWithName(CONTENT_TYPE).description("응답 본문 타입")
                                    ),
                                    responseFields(
                                            fieldWithPath("access_token").description("인증 토큰"),
                                            fieldWithPath("refresh_token").description("재발급 토큰"),
                                            fieldWithPath("token_type").description("토큰 타입"),
                                            fieldWithPath("expires_in").description("토큰 유효 시간, 초 단위"),
                                            fieldWithPath("scope").description("토큰의 접근 범위"),
                                            fieldWithPath("jti").description("토큰의 고유 식별자")
                                    )
                            ));
                });
    }

    @Test
    @DisplayName("인증 서버 AUTHORIZATION CODE 방식 토큰 발급 성공 200")
    public void getToken_AuthorizationCodeGrant_200() throws Exception {
        getAuthorizeResponse(
                "code",
                testProperties.getUsers().getMaster().getUsername(),
                testProperties.getClients().getMaster().getClientId(),
                testProperties.getClients().getMaster().getRedirectUris().split(",")[0],
                testProperties.getClients().getMaster().getScopes().replace(",", " ")
        )
                .andDo(result -> {
                    String redirectedUrl = result
                            .getResponse()
                            .getRedirectedUrl();
                    String code = redirectedUrl.substring(redirectedUrl.indexOf("=") + 1);
                    mockMvc
                            .perform(
                                    post("/oauth/token")
                                            .with(httpBasic(testProperties.getClients().getMaster().getClientId(), testProperties.getClients().getMaster().getClientSecret()))
                                            .param("code", code)
                                            .param("grant_type", "authorization_code")
                                            .param("redirect_uri", testProperties.getClients().getMaster().getRedirectUris().split(",")[0])
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
                                    "token-authorization-code-grant",
                                    requestHeaders(
                                            headerWithName(AUTHORIZATION).description("클라이언트 ID/SECRET 인코딩 값")
                                    ),
                                    requestParameters(
                                            parameterWithName("code").description("토큰을 발급받을 수 있는 코드 값"),
                                            parameterWithName("grant_type").description("인증 토큰 발급 방식"),
                                            parameterWithName("redirect_uri").description("리다이렉트 경로")
                                    ),
                                    responseHeaders(
                                            headerWithName(CONTENT_TYPE).description("응답 본문 타입")
                                    ),
                                    responseFields(
                                            fieldWithPath("access_token").description("인증 토큰"),
                                            fieldWithPath("refresh_token").description("재발급 토큰"),
                                            fieldWithPath("token_type").description("토큰 타입"),
                                            fieldWithPath("expires_in").description("토큰 유효 시간, 초 단위"),
                                            fieldWithPath("scope").description("토큰의 접근 범위"),
                                            fieldWithPath("jti").description("토큰의 고유 식별자")
                                    )
                            ));
                });
    }

    @Test
    @DisplayName("인증 서버 AUTHORIZATION CODE 방식 토큰 발급 부정확한 코드/Redirect Uri 값으로 실패하는 경우 400")
    public void getTokenAuthorizationCodeGrant_400() throws Exception {
        // Invalid Code
        mockMvc
                .perform(
                        post("/oauth/token")
                                .with(httpBasic(testProperties.getClients().getMaster().getClientId(), testProperties.getClients().getMaster().getClientSecret()))
                                .param("code", "invalid code")
                                .param("grant_type", "authorization_code")
                                .param("redirect_uri", testProperties.getClients().getMaster().getRedirectUris().split(",")[0])
                )
                .andExpect(status().isBadRequest())
                .andDo(print());
        // Invalid Redirect Uri
        getAuthorizeResponse(
                "code",
                testProperties.getUsers().getMaster().getUsername(),
                testProperties.getClients().getNoScopes().getClientId(),
                "invalid redirect uri",
                testProperties.getClients().getMaster().getScopes().replace(",", " ")
        )
                .andDo(print())
                .andExpect(status().isBadRequest());
        getAuthorizeResponse(
                "code",
                testProperties.getUsers().getMaster().getUsername(),
                testProperties.getClients().getNoScopes().getClientId(),
                testProperties.getClients().getMaster().getRedirectUris().split(",")[0],
                testProperties.getClients().getMaster().getScopes().replace(",", " ")
        )
                .andDo(result -> {
                    String redirectedUrl = result
                            .getResponse()
                            .getRedirectedUrl();
                    String code = redirectedUrl.substring(redirectedUrl.indexOf("=") + 1);
                    mockMvc
                            .perform(
                                    post("/oauth/token")
                                            .with(httpBasic(testProperties.getClients().getMaster().getClientId(), testProperties.getClients().getMaster().getClientSecret()))
                                            .param("code", code)
                                            .param("grant_type", "authorization_code")
                                            .param("redirect_uri", "invalid redirect url")
                            )
                            .andDo(print())
                            .andExpect(status().isBadRequest());
                });
    }

    @Test
    @DisplayName("인증 서버 AUTHORIZATION CODE 방식 토큰 발급 부정확한 Scope 값으로 실패하는 경우 303")
    public void getTokenAuthorizationCodeGrant_303() throws Exception {
        getAuthorizeResponse(
                "code",
                testProperties.getUsers().getMaster().getUsername(),
                testProperties.getClients().getNoScopes().getClientId(),
                testProperties.getClients().getNoScopes().getRedirectUris().split(",")[0],
                testProperties.getClients().getMaster().getScopes().replace(",", " ")
        )
                .andDo(print())
                .andExpect(status().is3xxRedirection());
    }

    @Test
    @DisplayName("인증 서버 AUTHORIZATION CODE 방식 토큰 발급 부정확한 클라이언트 정보로 실패하는 경우 401")
    public void getTokenAuthorizationCodeGrant_401() throws Exception {
        getAuthorizeResponse(
                "code",
                testProperties.getUsers().getMaster().getUsername(),
                "invalid client id",
                testProperties.getClients().getMaster().getRedirectUris().split(",")[0],
                testProperties.getClients().getMaster().getScopes().replace(",", " ")
        )
                .andDo(print())
                .andExpect(status().isUnauthorized());
        getAuthorizeResponse(
                "code",
                testProperties.getUsers().getMaster().getUsername(),
                testProperties.getClients().getMaster().getClientId(),
                testProperties.getClients().getMaster().getRedirectUris().split(",")[0],
                testProperties.getClients().getMaster().getScopes().replace(",", " ")
        )
                .andDo(print())
                .andDo(result -> {
                    String redirectedUrl = result
                            .getResponse()
                            .getRedirectedUrl();
                    String code = redirectedUrl.substring(redirectedUrl.indexOf("=") + 1);
                    mockMvc
                            .perform(
                                    post("/oauth/token")
                                            .with(httpBasic("invalid id", "invalid secret"))
                                            .param("code", code)
                                            .param("grant_type", "authorization_code")
                                            .param("redirect_uri", testProperties.getClients().getMaster().getRedirectUris().split(",")[0])
                            )
                            .andExpect(status().isUnauthorized())
                            .andDo(print());
                });
    }

    @Test
    @DisplayName("인증 서버 Implicit 방식 토큰 발급 성공 200")
    public void getToken_ImplicitGrant_200() throws Exception {
        getAuthorizeResponse(
                "token",
                testProperties.getUsers().getMaster().getUsername(),
                testProperties.getClients().getMaster().getClientId(),
                testProperties.getClients().getMaster().getRedirectUris().split(",")[0],
                testProperties.getClients().getMaster().getScopes().replace(",", " ")
        )
                .andDo(print())
                .andDo(result2 -> {
                    String redirectedUrl = result2.getResponse().getRedirectedUrl();
                    assertTrue(redirectedUrl.contains("access_token="));
                    assertTrue(redirectedUrl.contains("token_type="));
                    assertTrue(redirectedUrl.contains("expires_in="));
                });
    }

    @Test
    @DisplayName("인증 서버 Implicit 방식 토큰 발급 부정확한 클라이언트 정보로 실패하는 경우 401")
    public void getToken_ImplicitGrant_401() throws Exception {
        getAuthorizeResponse(
                "token",
                testProperties.getUsers().getMaster().getUsername(),
                "invalid client id",
                testProperties.getClients().getMaster().getRedirectUris().split(",")[0],
                testProperties.getClients().getMaster().getScopes().replace(",", " ")
        )
                .andDo(print())
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("인증 서버 Implicit 방식 토큰 발급 부정확한 Scope 값으로 실패하는 경우 303")
    public void getToken_ImplicitGrant_303() throws Exception {
        getAuthorizeResponse(
                "token",
                testProperties.getUsers().getMaster().getUsername(),
                testProperties.getClients().getNoScopes().getClientId(),
                testProperties.getClients().getNoScopes().getRedirectUris().split(",")[0],
                testProperties.getClients().getMaster().getScopes().replace(",", " ")
        )
                .andDo(print())
                .andExpect(status().is3xxRedirection());
    }

    @Test
    @DisplayName("인증 서버 Implicit 방식 토큰 발급 부정확한 Redirect Uri 정보로 실패하는 경우 400")
    public void getToken_ImplicitGrant_400() throws Exception {
        getAuthorizeResponse(
                "token",
                testProperties.getUsers().getMaster().getUsername(),
                testProperties.getClients().getMaster().getClientId(),
                "invalid redirect uri",
                testProperties.getClients().getMaster().getScopes().replace(",", " ")
        )
                .andDo(print())
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("인증 서버 PASSWORD 방식 토큰 발급 성공 200")
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
                                        headerWithName(AUTHORIZATION).description("클라이언트 ID/SECRET 인코딩 값")
                                ),
                                requestParameters(
                                        parameterWithName("username").description("사용자 아이디"),
                                        parameterWithName("password").description("사용자 패스워드"),
                                        parameterWithName("grant_type").description("인증 토큰 발급 방식"),
                                        parameterWithName("scope").description("토큰의 접근 범위").optional()
                                ),
                                responseHeaders(
                                        headerWithName(CONTENT_TYPE).description("응답 본문 타입")
                                ),
                                responseFields(
                                        fieldWithPath("access_token").description("인증 토큰"),
                                        fieldWithPath("refresh_token").description("재발급 토큰"),
                                        fieldWithPath("token_type").description("토큰 타입"),
                                        fieldWithPath("expires_in").description("토큰 유효 시간, 초 단위"),
                                        fieldWithPath("scope").description("토큰의 접근 범위"),
                                        fieldWithPath("jti").description("토큰의 고유 식별자")
                                )
                        )
                );
    }

    @Test
    @DisplayName("인증 서버 PASSWORD 방식 토큰 발급 잘못된 유저 정보/Scope 값으로 실패 400")
    public void getToken_PasswordGrant_400() throws Exception {
        // Invalid User
        getTokenPasswordGrantResponse(
                "invalid user",
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
    @DisplayName("인증 서버 PASSWORD 방식 토큰 발급 클라이언트 정보 잘못된 경우 실패 401")
    public void getToken_PasswordGrant_401() throws Exception {
        getTokenPasswordGrantResponse(
                testProperties.getUsers().getMaster().getUsername(),
                testProperties.getUsers().getMaster().getPassword(),
                "invalid client id",
                "invalid client secret",
                testProperties.getClients().getMaster().getScopes().replace(",", " ")
        )
                .andExpect(status().isUnauthorized())
                .andDo(print());
    }

    @Test
    @DisplayName("인증 서버 REFRESH TOKEN 방식 토큰 발급 성공 200")
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
                                        headerWithName(AUTHORIZATION).description("클라이언트 ID/SECRET 인코딩 값")
                                ),
                                requestParameters(
                                        parameterWithName("refresh_token").description("재발급 토큰"),
                                        parameterWithName("grant_type").description("인증 토큰 발급 방식"),
                                        parameterWithName("scope").description("토큰의 접근 범위").optional()
                                ),
                                responseHeaders(
                                        headerWithName(CONTENT_TYPE).description("응답 본문 타입")
                                ),
                                responseFields(
                                        fieldWithPath("access_token").description("인증 토큰"),
                                        fieldWithPath("refresh_token").description("재발급 토큰"),
                                        fieldWithPath("token_type").description("토큰 타입"),
                                        fieldWithPath("expires_in").description("토큰 유효 시간, 초 단위"),
                                        fieldWithPath("scope").description("토큰의 접근 범위"),
                                        fieldWithPath("jti").description("토큰의 고유 식별자")
                                )
                        )
                );
    }

    @Test
    @DisplayName("인증 서버 REFRESH TOKEN 방식 토큰 발급 재발급 토큰 기한 만료된 경우 실패 400")
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
    @DisplayName("인증 서버 REFRESH TOKEN 방식 토큰 발급 잘못된 재발급 토큰/클라이언트 정보 값으로 실패 401")
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
                "invalid clientSecret"
        )
                .andExpect(status().isUnauthorized())
                .andDo(print());
    }

    @Test
    @DisplayName("인증 서버 Client Credentials 방식 토큰 발급 성공 200")
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
                                        headerWithName(AUTHORIZATION).description("클라이언트 ID/SECRET 인코딩 값")
                                ),
                                requestParameters(
                                        parameterWithName("grant_type").description("인증 토큰 발급 방식"),
                                        parameterWithName("scope").description("토큰의 접근 범위").optional()
                                ),
                                responseHeaders(
                                        headerWithName(CONTENT_TYPE).description("응답 본문 타입")
                                ),
                                responseFields(
                                        fieldWithPath("access_token").description("인증 토큰"),
                                        fieldWithPath("token_type").description("토큰 타입"),
                                        fieldWithPath("expires_in").description("토큰 유효 시간, 초 단위"),
                                        fieldWithPath("scope").description("토큰의 접근 범위"),
                                        fieldWithPath("jti").description("토큰의 고유 식별자")
                                )
                        )
                );
    }

    @Test
    @DisplayName("인증 서버 Client Credentials 방식 부정확한 클라이언트 정보로 실패하는 경우 401")
    public void getToken_ClientCredentialsGrant_401() throws Exception {
        getClientCredentialsGrantResponse(
                "invalid client id",
                "invalid client secret",
                testProperties.getClients().getMaster().getScopes().replace(",", " ")
        )
                .andExpect(status().isUnauthorized())
                .andDo(print());
    }

    @Test
    @DisplayName("인증 서버 Client Credentials 방식 부정확한 클라이언트 Scope 값으로 실패하는 경우 400")
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

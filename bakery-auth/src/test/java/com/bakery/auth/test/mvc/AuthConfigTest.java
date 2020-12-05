package com.bakery.auth.test.mvc;

import com.bakery.auth.enums.CodeChallengeMethod;
import com.bakery.auth.base.AbstractWebMvcTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import static com.bakery.auth.enums.Oauth2GrantType.*;
import static org.springframework.http.HttpHeaders.*;
import static org.springframework.http.MediaType.APPLICATION_FORM_URLENCODED;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.restdocs.headers.HeaderDocumentation.*;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.get;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.post;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.restdocs.payload.PayloadDocumentation.responseFields;
import static org.springframework.restdocs.request.RequestDocumentation.parameterWithName;
import static org.springframework.restdocs.request.RequestDocumentation.requestParameters;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@DisplayName("Oauth2 Endpoint 단위 테스트 및 API 문서화")
public class AuthConfigTest extends AbstractWebMvcTest {

    @Test
    @DisplayName("토큰 유효성 검증 API 문서화")
    public void checkToken_200() throws Exception {
        mockMvc
                .perform(
                        post("/auth/oauth/check_token")
                                .with(httpBasic(testProperties.getClients().getMaster().getClientId(), testProperties.getClients().getMaster().getClientSecret()))
                                .contentType(APPLICATION_FORM_URLENCODED)
                                .accept(APPLICATION_JSON)
                                .param("token", "access_token")
                )
                .andExpect(status().isOk())
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
    @DisplayName("Authorization Code With PKCE 방식 토큰 발급 API 문서화")
    public void generateRestApiDocsTokenAuthorizationCodeGrant() throws Exception {
        mockMvc
                .perform(
                        get("/auth/oauth/authorize")
                                .with(user(testProperties.getUsers().getMaster().getUsername()).password(testProperties.getUsers().getMaster().getPassword()))
                                .queryParam("client_id", testProperties.getClients().getMaster().getClientId())
                                .queryParam("response_type", "code")
                                .queryParam("redirect_uri", testProperties.getClients().getMaster().getRedirectUris().split(",")[0])
                                .queryParam("scope", testProperties.getClients().getMaster().getScopes().replace(",", " "))
                                .queryParam("code_challenge", testProperties.getClients().getMaster().getCodeChallenge())
                                .queryParam("code_challenge_method", CodeChallengeMethod.S256.toString())
                                .queryParam("state", testProperties.getClients().getMaster().getState())
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
                .andDo(result -> {
                    MockHttpSession session = (MockHttpSession) result
                            .getRequest()
                            .getSession();
                    MockHttpServletRequestBuilder builder = post("/auth/oauth/authorize")
                            .session(session)
                            .with(csrf())
                            .param("response_type", "code")
                            .param("client_id", testProperties.getClients().getMaster().getClientId())
                            .param("redirect_uri", testProperties.getClients().getMaster().getRedirectUris().split(",")[0])
                            .param("scope", testProperties.getClients().getMaster().getScopes().replace(",", " "))
                            .param("user_oauth_approval", "true");
                    for (String scope : testProperties.getClients().getMaster().getScopes().split(",")) {
                        builder.param("scope." + scope, "true");
                    }
                    mockMvc
                            .perform(builder)
                            .andExpect(status().is3xxRedirection())
                            .andDo(print())
                            .andDo(
                                    document(
                                            "authorization-code-with-pkce-grant-post",
                                            responseHeaders(
                                                    headerWithName(LOCATION).description("리다이렉트 경로")
                                            )
                                    )
                            );
                });
        mockMvc
                .perform(
                        post("/auth/oauth/token")
                                .contentType(APPLICATION_FORM_URLENCODED)
                                .accept(APPLICATION_JSON)
                                .param("client_id", testProperties.getClients().getMaster().getClientId())
                                .param("client_secret", testProperties.getClients().getMaster().getClientSecret())
                                .param("code", "JXrJ2y")
                                .param("grant_type", AUTHORIZATION_CODE.toString())
                                .param("redirect_uri", testProperties.getClients().getMaster().getRedirectUris().split(",")[0])
                                .param("code_verifier", testProperties.getClients().getMaster().getCodeVerifier())
                )
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
    }

    @Test
    @DisplayName("Resource Owner Password Credentials 방식 토큰 발급 API 문서화")
    public void generateRestApiDocsTokenPasswordGrant() throws Exception {
        mockMvc
                .perform(
                        post("/auth/oauth/token")
                                .contentType(APPLICATION_FORM_URLENCODED)
                                .accept(APPLICATION_JSON)
                                .param("client_id", testProperties.getClients().getMaster().getClientId())
                                .param("client_secret", "f388eebc-36b4-11eb-adc1-0242ac120002")
                                .param("username", testProperties.getUsers().getMaster().getUsername())
                                .param("password", testProperties.getUsers().getMaster().getPassword())
                                .param("grant_type", PASSWORD.toString())
                                .param("scope", testProperties.getClients().getMaster().getScopes().replace(",", " "))
                )
                .andExpect(status().isOk())
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
    @DisplayName("Refresh Token 방식 토큰 발급 API 문서화")
    public void generateRestApiDocsTokenRefreshTokenGrant() throws Exception {
        mockMvc
                .perform(
                        post("/auth/oauth/token")
                                .contentType(APPLICATION_FORM_URLENCODED)
                                .accept(APPLICATION_JSON)
                                .param("client_id", testProperties.getClients().getMaster().getClientId())
                                .param("client_secret", testProperties.getClients().getMaster().getClientSecret())
                                .param("refresh_token", "refresh_token")
                                .param("grant_type", REFRESH_TOKEN.toString())
                                .param("scope", testProperties.getClients().getMaster().getScopes().replace(",", " "))
                )
                .andExpect(status().isOk())
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
    @DisplayName("Client Credentials 방식 토큰 발급 API 문서화")
    public void generateRestApiDocsTokenClientCredentialsGrant() throws Exception {
        mockMvc
                .perform(
                        post("/auth/oauth/token")
                                .contentType(APPLICATION_FORM_URLENCODED)
                                .accept(APPLICATION_JSON)
                                .param("client_id", testProperties.getClients().getMaster().getClientId())
                                .param("client_secret", testProperties.getClients().getMaster().getClientSecret())
                                .param("grant_type", CLIENT_CREDENTIALS.toString())
                                .param("scope", testProperties.getClients().getMaster().getScopes().replace(",", " "))

                )
                .andExpect(status().isOk())
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

}

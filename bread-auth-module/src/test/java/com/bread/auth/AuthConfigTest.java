package com.bread.auth;

import com.bread.auth.config.TestConfig;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.restdocs.AutoConfigureRestDocs;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.restdocs.headers.HeaderDocumentation.*;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.post;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.restdocs.payload.PayloadDocumentation.responseFields;
import static org.springframework.restdocs.request.RequestDocumentation.parameterWithName;
import static org.springframework.restdocs.request.RequestDocumentation.requestParameters;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@ActiveProfiles("test")
@Import(TestConfig.class)
@AutoConfigureRestDocs
@AutoConfigureMockMvc
public class AuthConfigTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    @DisplayName("인증 서버 PASSWORD 방식 토큰 발급 성공")
    public void getToken_PasswordGrant_200() throws Exception {
        mockMvc
                .perform(
                        post("/oauth/token")
                                .accept(MediaType.APPLICATION_JSON)
                                .queryParam("username", "user")
                                .queryParam("password", "1234")
                                .queryParam("grant_type", "password")
                                .queryParam("scope", "READ")
                                .with(httpBasic("test", "1234"))
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
                                requestHeaders(headerWithName(HttpHeaders.AUTHORIZATION).description("클라이언트 ID/SECRET 인코딩 값]")),
                                requestParameters(
                                        parameterWithName("username").description("사용자 아이디"),
                                        parameterWithName("password").description("사용자 패스워드"),
                                        parameterWithName("grant_type").description("인증 토큰 발급 방식"),
                                        parameterWithName("scope").description("토큰의 접근 범위").optional()
                                ),
                                responseHeaders(
                                        headerWithName(HttpHeaders.CONTENT_TYPE).description("응답 본문 타입")
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

}

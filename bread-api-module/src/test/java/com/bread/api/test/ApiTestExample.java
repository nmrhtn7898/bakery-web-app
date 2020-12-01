package com.bread.api.test;

import com.bread.api.annotation.MockOauth2Authentication;
import com.bread.api.annotation.MockOauth2Client;
import com.bread.api.annotation.MockOauth2User;
import com.bread.api.base.AbstractIntegrationTest;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.test.context.TestContext;

import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Authorization Header 엑세스 토큰 기반의 API 테스트는 아래와 같이 진행하시면 됩니다.
 *
 * @see com.bread.api.annotation.MockOauth2Authentication
 * @see com.bread.api.annotation.MockOauth2Client
 * @see com.bread.api.annotation.MockOauth2User
 * @see com.bread.api.config.custom.WithOauth2AuthenticationTestExecutionListener#beforeTestMethod(TestContext)
 */
public class ApiTestExample extends AbstractIntegrationTest {

    /**
     * Authorization Header 값으로 `Bearer {access_token}`을 넘겨주시면 유효한 토큰으로 인증 처리 됩니다.
     */
    @Test
    @MockOauth2Authentication
    public void successByAccessToken() throws Exception {
        mockMvc
                .perform(
                        get("/api/v1/test")
                                .header(HttpHeaders.AUTHORIZATION, "Bearer {access_token}")
                )
                .andDo(print())
                .andExpect(status().isOk());
    }

    /**
     * Authorization Header 값으로 `Bearer {access_token}`이 아닌 그 외의 값을 넘겨주시면 유효하지 않은 토큰으로 API 접근이 불가합니다.
     * 애노테이션에 토큰의 기본 클라이언트/유저 정보 값이 세팅되어 있으나 원하는 응답 결과를 받기 위해 값을 변경할 수 있습니다.
     */
    @Test
    @MockOauth2Authentication(
            client = @MockOauth2Client(resourceIds = {"api", "example"}, scopes = {"read"}, redirectUri = "/", clientId = "example"),
            user = @MockOauth2User(userId = 5L, username = "example", credentials = "example", authorities = {"user"})
    )
    public void failByInvalidToken() throws Exception {
        mockMvc
                .perform(
                        get("/api/v1/test")
                                .header(HttpHeaders.AUTHORIZATION, "Bearer {invalid_token}")
                )
                .andDo(print())
                .andExpect(status().isUnauthorized());
    }

}

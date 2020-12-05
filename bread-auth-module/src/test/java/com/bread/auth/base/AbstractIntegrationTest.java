package com.bread.auth.base;

import com.bread.auth.config.EmbeddedRedisConfig;
import com.bread.auth.config.RestDocsConfig;
import com.bread.auth.config.TestDataConfig;
import com.bread.auth.config.TestDataConfig.TestProperties;
import org.junit.jupiter.api.Disabled;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.restdocs.AutoConfigureRestDocs;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.oauth2.common.util.Jackson2JsonParser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import static com.bread.auth.enums.Oauth2GrantType.*;
import static org.springframework.http.MediaType.APPLICATION_FORM_URLENCODED;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.get;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.post;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.*;

@Disabled
@SpringBootTest
@ActiveProfiles("test")
@Import(value = {RestDocsConfig.class, EmbeddedRedisConfig.class, TestDataConfig.class})
@AutoConfigureRestDocs
@AutoConfigureMockMvc
public abstract class AbstractIntegrationTest {

    @Autowired
    protected MockMvc mockMvc;

    @Autowired
    protected Jackson2JsonParser parser;

    @Autowired
    protected UserDetailsService userDetailsService;

    @Autowired
    protected TestProperties testProperties;

    protected ResultActions getCheckTokenResponse(String clientId, String clientSecret, String token) throws Exception {
        return mockMvc
                .perform(
                        post("/auth/oauth/check_token")
                                .contentType(APPLICATION_FORM_URLENCODED)
                                .accept(APPLICATION_JSON)
                                .with(httpBasic(clientId, clientSecret))
                                .param("token", token)
                );
    }

    protected ResultActions getAuthorizeResponse(String username, String clientId, String redirectUri, String scopes,
                                                 String codeChallenge, String codeChallengeMethod, String state) throws Exception {
        return mockMvc
                .perform(
                        get("/auth/oauth/authorize")
                                .with(user(userDetailsService.loadUserByUsername(username)))
                                .queryParam("client_id", clientId)
                                .queryParam("response_type", "code")
                                .queryParam("redirect_uri", redirectUri)
                                .queryParam("scope", scopes)
                                .queryParam("code_challenge", codeChallenge)
                                .queryParam("code_challenge_method", codeChallengeMethod)
                                .queryParam("state", state)
                );
    }

    protected ResultActions getAuthorizeConfirmResponse(MockHttpSession session, String clientId,
                                                        String redirectUri, String scopes) throws Exception {
        MockHttpServletRequestBuilder requestBuilder = post("/auth/oauth/authorize")
                .session(session)
                .with(csrf())
                .param("response_type", "code")
                .param("client_id", clientId)
                .param("redirect_uri", redirectUri)
                .param("scope", scopes)
                .param("user_oauth_approval", "true");
        for (String scope : scopes.split(",")) {
            requestBuilder.param("scope." + scope, "true");
        }
        return mockMvc.perform(requestBuilder);
    }

    protected ResultActions getTokenAuthorizationCodeWithPkceResponse(String clientId, String clientSecret,
                                                                      String code, String redirectUri,
                                                                      String codeVerifier) throws Exception {
        return mockMvc
                .perform(
                        post("/auth/oauth/token")
                                .contentType(APPLICATION_FORM_URLENCODED)
                                .accept(APPLICATION_JSON)
                                .param("client_id", clientId)
                                .param("client_secret", clientSecret)
                                .param("code", code)
                                .param("grant_type", AUTHORIZATION_CODE.toString())
                                .param("redirect_uri", redirectUri)
                                .param("code_verifier", codeVerifier)
                );
    }

    protected ResultActions getTokenPasswordGrantResponse(String username, String password,
                                                          String clientId, String clientSecret,
                                                          String scopes) throws Exception {
        return mockMvc
                .perform(
                        post("/auth/oauth/token")
                                .contentType(APPLICATION_FORM_URLENCODED)
                                .accept(APPLICATION_JSON)
                                .param("client_id", clientId)
                                .param("client_secret", clientSecret)
                                .param("username", username)
                                .param("password", password)
                                .param("grant_type", PASSWORD.toString())
                                .param("scope", scopes)
                );
    }

    protected ResultActions getTokenRefreshTokenGrantResponse(String refreshToken, String scopes,
                                                              String clientId, String clientSecret) throws Exception {
        return mockMvc
                .perform(
                        post("/auth/oauth/token")
                                .contentType(APPLICATION_FORM_URLENCODED)
                                .accept(APPLICATION_JSON)
                                .param("client_id", clientId)
                                .param("client_secret", clientSecret)
                                .param("refresh_token", refreshToken)
                                .param("grant_type", REFRESH_TOKEN.toString())
                                .param("scope", scopes)
                );
    }

    protected ResultActions getClientCredentialsGrantResponse(String clientId, String clientSecret,
                                                              String scopes) throws Exception {
        return mockMvc
                .perform(
                        post("/auth/oauth/token")
                                .contentType(APPLICATION_FORM_URLENCODED)
                                .accept(APPLICATION_JSON)
                                .param("client_id", clientId)
                                .param("client_secret", clientSecret)
                                .param("grant_type", CLIENT_CREDENTIALS.toString())
                                .param("scope", scopes)

                );
    }

    protected String getAccessToken(String username, String password,
                                    String clientId, String clientSecret,
                                    String scopes) throws Exception {
        String responseBody = getTokenPasswordGrantResponse(username, password, clientId, clientSecret, scopes)
                .andReturn()
                .getResponse()
                .getContentAsString();
        return (String) parser
                .parseMap(responseBody)
                .get("access_token");
    }

    protected String getRefreshToken(String username, String password,
                                     String clientId, String clientSecret,
                                     String scopes) throws Exception {
        String responseBody = getTokenPasswordGrantResponse(username, password, clientId, clientSecret, scopes)
                .andReturn()
                .getResponse()
                .getContentAsString();
        return (String) parser
                .parseMap(responseBody)
                .get("refresh_token");
    }

}

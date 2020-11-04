package com.bread.auth.base;

import com.bread.auth.config.DataConfig;
import com.bread.auth.config.DataConfig.TestProperties;
import com.bread.auth.config.EmbeddedRedisConfig;
import com.bread.auth.config.RestDocsConfig;
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
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import javax.persistence.Inheritance;

import static org.springframework.http.HttpStatus.OK;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.get;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.post;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.*;

@Disabled
@SpringBootTest
@Inheritance
@ActiveProfiles("test")
@Import(value = {RestDocsConfig.class, EmbeddedRedisConfig.class, DataConfig.class})
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

    protected ResultActions getAuthorizeResponse(String responseType, String username,
                                                 String clientId, String redirectUri,
                                                 String scopes) throws Exception {
        ResultActions resultActions = mockMvc
                .perform(
                        get("/oauth/authorize")
                                .with(user(userDetailsService.loadUserByUsername(username)))
                                .param("client_id", clientId)
                                .param("response_type", responseType)
                                .param("redirect_uri", redirectUri)
                                .param("scope", scopes)
                );
        MvcResult mvcResult = resultActions.andReturn();
        if (mvcResult.getResponse().getStatus() != OK.value()) {
            return resultActions;
        }
        MockHttpSession session = (MockHttpSession) mvcResult
                .getRequest()
                .getSession();
        MockHttpServletRequestBuilder requestBuilder = post("/oauth/authorize")
                .session(session)
                .with(csrf())
                .param("response_type", responseType)
                .param("client_id", clientId)
                .param("redirect_uri", redirectUri)
                .param("scope", scopes)
                .param("user_oauth_approval", "true");
        for (String scope : scopes.split(",")) {
            requestBuilder.param("scope." + scope, "true");
        }
        return mockMvc.perform(requestBuilder);
    }

    protected ResultActions getTokenPasswordGrantResponse(String username, String password,
                                                          String clientId, String clientSecret,
                                                          String scopes) throws Exception {
        return mockMvc
                .perform(
                        post("/oauth/token")
                                .accept(APPLICATION_JSON)
                                .queryParam("username", username)
                                .queryParam("password", password)
                                .queryParam("grant_type", "password")
                                .queryParam("scope", scopes)
                                .with(httpBasic(clientId, clientSecret))
                );
    }

    protected ResultActions getTokenRefreshTokenGrantResponse(String refreshToken, String scopes,
                                                              String clientId, String clientSecret) throws Exception {
        return mockMvc
                .perform(
                        post("/oauth/token")
                                .accept(APPLICATION_JSON)
                                .queryParam("refresh_token", refreshToken)
                                .queryParam("grant_type", "refresh_token")
                                .queryParam("scope", scopes)
                                .with(httpBasic(clientId, clientSecret))
                );
    }

    protected ResultActions getClientCredentialsGrantResponse(String clientId, String clientSecret,
                                                              String scopes) throws Exception {
        return mockMvc
                .perform(
                        post("/oauth/token")
                                .with(httpBasic(clientId, clientSecret))
                                .accept(APPLICATION_JSON)
                                .param("grant_type", "client_credentials")
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

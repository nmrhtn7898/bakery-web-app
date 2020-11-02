package com.bread.auth.test.controller;

import com.bread.auth.base.AbstractWebMvcTest;
import com.bread.auth.entity.Oauth2Client;
import com.bread.auth.service.Oauth2ClientService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.hateoas.MediaTypes;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.when;
import static org.springframework.hateoas.MediaTypes.HAL_JSON;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class Oauth2ClientControllerTest extends AbstractWebMvcTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private Oauth2ClientService oauth2ClientService;

/*    @Test
    public void generateClient_Success() throws Exception {
        Oauth2Client request = Oauth2Client
                .builder()
                .clientId("mock")
                .clientSecret("secret")
                .resourceIds("auth")
                .scope("read,write")
                .authorizedGrantTypes("password,refresh_token")
                .authorities("user")
                .webServerRedirectUri("/")
                .accessTokenValidity(1800)
                .refreshTokenValidity(1800)
                .build();
        when(oauth2ClientService.generateClient(request)).thenReturn(request);
        mockMvc
                .perform(post("/api/v1/clients"))
                .andDo(print())
                .andExpect(status().isCreated());
    }

    @Test
    public void generateClient_Fail() throws Exception {
        Oauth2Client request = Oauth2Client
                .builder()
                .build();
        when(oauth2ClientService.generateClient(request)).thenReturn(request);
        mockMvc
                .perform(
                        post("/api/v1/clients")
                                .content(objectMapper.writeValueAsString(request))
                                .contentType(MediaType.APPLICATION_JSON)
                                .accept(HAL_JSON)
                        .header()
                )
                .andDo(print())
                .andExpect(status().isBadRequest());
    }*/

}

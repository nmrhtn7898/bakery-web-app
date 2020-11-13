package com.bread.api.client;

import com.bread.api.model.LoginToken;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import static org.springframework.http.MediaType.APPLICATION_FORM_URLENCODED;

@Service
@RequiredArgsConstructor
public class LoginClient {

    private final RestTemplate restTemplate;

    @Value("${auth.domain.url}")
    private String authDomainUrl;

    @Value("${client.id}")
    private String clientId;

    @Value("${client.secret}")
    private String clientSecret;

    public LoginToken login(String email, String password) {
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.setContentType(APPLICATION_FORM_URLENCODED);
        httpHeaders.setBasicAuth(clientId, clientSecret);
        MultiValueMap<String, String> httpMaps = new LinkedMultiValueMap<>();
        httpMaps.add("username", email);
        httpMaps.add("password", password);
        httpMaps.add("grant_type", "password");
        HttpEntity<MultiValueMap<String, String>> httpEntity = new HttpEntity<>(httpMaps, httpHeaders);
        return restTemplate
                .postForEntity(
                        authDomainUrl + "/oauth/token",
                        httpEntity,
                        LoginToken.class
                )
                .getBody();
    }

}

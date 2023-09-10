package com.oauth.service;

import java.util.HashMap;
import java.util.Map;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.databind.JsonNode;
import com.oauth.config.OAuth2GoogleProperties;

@Service
public class OauthService {

    OAuth2GoogleProperties pro;
    private final RestTemplate restTemplate = new RestTemplate();
    
    public OauthService(OAuth2GoogleProperties pro) {
        this.pro = pro;
    }
    public Map<String, String> socialLogin(String code, String registrationId) {
    	
        String accessToken = getAccessToken(code, registrationId);
        JsonNode userResourceNode = getUserResource(accessToken);
        System.out.println("userResourceNode = " + userResourceNode);

        String id = userResourceNode.get("id").asText();
        String email = userResourceNode.get("email").asText();
        String nickname = userResourceNode.get("name").asText();
        
        System.out.println("================ Resource User Info ===============");
        System.out.println("id = " + id);
        System.out.println("email = " + email);
        System.out.println("nickname = " + nickname);
        System.out.println("===================================================");
        
        Map<String, String> map = new HashMap<>();
        map.put("id", id);
        map.put("email", email);
        map.put("nickname", nickname);
        
        return map;
    }

    private String getAccessToken(String authorizationCode, String registrationId) {
    	
        String clientId = pro.getClientId();
        String clientSecret = pro.getClientSecret();
        String redirectUri = pro.getRedirectUri();
        String tokenUri = pro.getTokenUri();
        System.out.println("clientId == " + clientId);
        System.out.println("clientSecret == " + clientSecret);

        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("code", authorizationCode);
        params.add("client_id", clientId);
        params.add("client_secret", clientSecret);
        params.add("redirect_uri", redirectUri);
        params.add("grant_type", "authorization_code");

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        HttpEntity entity = new HttpEntity(params, headers);

        ResponseEntity<JsonNode> responseNode = restTemplate.exchange(tokenUri, HttpMethod.POST, entity, JsonNode.class);
        JsonNode accessTokenNode = responseNode.getBody();
        return accessTokenNode.get("access_token").asText();
    }

    private JsonNode getUserResource(String accessToken) {
        String resourceUri = pro.getResourceUri();

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + accessToken);
        HttpEntity entity = new HttpEntity(headers);
        return restTemplate.exchange(resourceUri, HttpMethod.GET, entity, JsonNode.class).getBody();
    }
}
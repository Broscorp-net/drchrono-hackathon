package com.remind.me.doc.service;

import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

@Service
public class UpdateTokenService {

  public String getAccessToken() {
    return accessToken;
  }

  private String accessToken;

  @Value("${refresh.token}")
  private String refreshToken;
  @Value("${dr.chrono.url}")
  private String defaultUrl;
  @Value("${redirect.uri}")
  private String redirectUrl;
  @Value("${drchrono.clientId}")
  private String clientId;
  @Value("${drchrono.clientSecret}")
  private String clientSecret;

  @Scheduled(fixedDelay = 172000_000)
  private void updateToken() {
    RestTemplate restTemplate = new RestTemplate();
    HttpHeaders headers = new HttpHeaders();
    headers.set("Authorization", "Bearer " + accessToken);
    String url = defaultUrl + "/o/token/";
    UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(url)
            .queryParam("grant_type", "refresh_token")
            .queryParam("redirect_uri", redirectUrl + "/callback/login")
            .queryParam("client_id", clientId)
            .queryParam("client_secret", clientSecret)
            .queryParam("refresh_token", refreshToken);
    HttpEntity entity = new HttpEntity(headers);
    ResponseEntity<String> response = restTemplate.exchange(
            builder.toUriString(), HttpMethod.POST, entity, String.class);
    if (response.getStatusCode().value() == 200) {
      JSONObject jsonObject = new JSONObject(response.getBody());
      accessToken = jsonObject.get("access_token").toString();
    }
    System.out.println(accessToken);
  }


}

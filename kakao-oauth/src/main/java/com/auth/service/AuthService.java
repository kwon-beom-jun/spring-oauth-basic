package com.auth.service;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

@Service
public class AuthService {
	
    private final String restapiKey;
    private final String redirectUrl;
    
	@Autowired
    public AuthService(
    		@Value("${kakao.restapi.key}") String restapiKey,
			@Value("${kakao.redirect.url}") String redirectUrl ) {
		this.restapiKey = restapiKey;
        this.redirectUrl = redirectUrl;
    }

	public String getKakaoAccessToken (String code) {
	    String accessToken = "";
	    String refreshToken = "";
	    String requestURL = "https://kauth.kakao.com/oauth/token";

	    try {
	        URL url = new URL(requestURL);
	        HttpURLConnection conn = (HttpURLConnection) url.openConnection();

	        conn.setRequestMethod("POST");
	        // setDoOutput()은 OutputStream으로 POST 데이터를 넘겨 주겠다는 옵션이다.
	        // POST 요청을 수행하려면 setDoOutput()을 true로 설정한다.
	        conn.setDoOutput(true);

	        // POST 요청에서 필요한 파라미터를 OutputStream을 통해 전송
	        BufferedWriter bufferedWriter = new BufferedWriter(new OutputStreamWriter(conn.getOutputStream()));
	        String sb = "grant_type=authorization_code" +
		                "&client_id=" + restapiKey + // REST_API_KEY
		                "&redirect_uri=" + redirectUrl + // REDIRECT_URI
		                "&code=" + code; // Authorization_Code
	        bufferedWriter.write(sb);
	        bufferedWriter.flush();

	        int responseCode = conn.getResponseCode();
	        System.out.println("responseCode : " + responseCode);

	        // 요청을 통해 얻은 데이터를 InputStreamReader을 통해 읽어 오기
	        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
	        String line = "";
	        StringBuilder result = new StringBuilder();

	        while ((line = bufferedReader.readLine()) != null) {
	            result.append(line);
	        }
	        System.out.println("response body : " + result);

	        JsonElement element = JsonParser.parseString(result.toString());

	        accessToken = element.getAsJsonObject().get("access_token").getAsString();
	        refreshToken = element.getAsJsonObject().get("refresh_token").getAsString();

	        System.out.println("accessToken : " + accessToken);
	        System.out.println("refreshToken : " + refreshToken);

	        bufferedReader.close();
	        bufferedWriter.close();
	    } catch (IOException e) {
	        e.printStackTrace();
	    }

	    return accessToken;
	}
	
	
	public HashMap<String, Object> getUserInfo(String accessToken) {
	    HashMap<String, Object> userInfo = new HashMap<>();
	    String postURL = "https://kapi.kakao.com/v2/user/me";

	    try {
	        URL url = new URL(postURL);
	        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
	        conn.setRequestMethod("POST");

	        conn.setRequestProperty("Authorization", "Bearer " + accessToken);

	        int responseCode = conn.getResponseCode();
	        System.out.println("responseCode : " + responseCode);

	        BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
	        String line = "";
	        StringBuilder result = new StringBuilder();

	        while ((line = br.readLine()) != null) {
	            result.append(line);
	        }
	        System.out.println("response body : " + result);

	        JsonElement element = JsonParser.parseString(result.toString());
	        JsonObject properties = element.getAsJsonObject().get("properties").getAsJsonObject();
	        JsonObject kakaoAccount = element.getAsJsonObject().get("kakao_account").getAsJsonObject();

	        String nickname = properties.getAsJsonObject().get("nickname").getAsString();
	        userInfo.put("nickname", nickname);
	        
	        if (kakaoAccount.getAsJsonObject().get("email") != null) {
	        	String email = kakaoAccount.getAsJsonObject().get("email").getAsString();
	        	userInfo.put("email", email);
			}


	    } catch (IOException exception) {
	        exception.printStackTrace();
	    }

	    return userInfo;
	}
	
}

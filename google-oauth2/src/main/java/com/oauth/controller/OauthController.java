package com.oauth.controller;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.view.RedirectView;

import com.oauth.config.OAuth2GoogleProperties;
import com.oauth.service.OauthService;

/**
<br> 프로세스
<br> 
<br> 1./oauth2/authorization/{registrationId} 엔드포인트는 Spring Security OAuth2 클라이언트의 "시작점"
<br> 		/oauth2/authorization/{registrationId}를 자동으로 설정하여 OAuth2 제공자의 로그인 및 승인 화면으로 사용자를 리다이렉트
<br> 			{registrationId} : application.properties 파일에서 설정한 제공자의 ID -> Google의 경우 google
<br> 		/oauth2/authorization/google 경로에 접근하면 Spring Boot 애플리케이션은 Google의 인증 페이지로 사용자를 리다이렉트
<br> 
<br> 2. Spring Security는 OAuth2 제공자의 승인 페이지로 리다이렉트하기 위한 완전한 URL을 구성
<br> 		-> redirect_uri, response_type, client_id, scope 등이 포함
<br>		-> redirect-uri를 명시적으로 설정하지 않으면 Spring Boot는 기본 리다이렉트 URI 패턴인 /login/oauth2/code/{registrationId}를 사용
<br>		-> 리다이렉트 : 인증 서버에서 발행한 인증 코드를 받기 위한 엔드포인트로 동작
<br> 
<br> 3. 구성된 URL로 사용자를 리다이렉트
<br> 
<br> 4. 승인 완료
<br> 
<br> 5. 현재) 승인 완료 후 / 맵핑이 타짐 → spring-boot-starter-oauth2-client 사용시 기본적으로 Spring Security와 함께 사용되도록 설계
<br> 
<br> 시큐리티 사용시
<br> 	승인 완료 후 OAuth2LoginAuthenticationFilter의 onAuthenticationSuccess 메서드와 관련된 설정에 따라 달라짐
<br> 		-> 기본적으로 SavedRequestAwareAuthenticationSuccessHandler가 사용되며, 이 핸들러는 사용자가 인증 전에 원래 방문하려던 URL로 리다이렉트
<br> 		-> 다른 경로로 리다이렉트하고 싶다면, 이 AuthenticationSuccessHandler를 커스텀화하거나 오버라이드
<br> 
<br> ※ 주의 ※
<br> OAuth2의 Authorization Code Flow에서 인증 코드를 받기 위한 엔드포인트!!!!
<br> 
<br> spring oauth2 기능없이 접속할때
<br> https://accounts.google.com/o/oauth2/v2/auth/oauthchooseaccount?response_type=code&client_id=""&redirect_uri=""&scope=openid%20profile%20email
<br> 		-> "" 없이 client_id, redirect_uri 값만 입력
<br> 
 */
@Controller // Model, Thymeleaf은 RestController 사용 X
@RequestMapping
public class OauthController {

    OauthService loginService;
    OAuth2GoogleProperties pro;

	public OauthController(OauthService loginService, OAuth2GoogleProperties pro) {
		this.loginService = loginService;
		this.pro = pro;
	}
    
    @GetMapping("/")
    public String loginHome(Model model) throws UnsupportedEncodingException {
    	model.addAttribute("clientId", pro.getClientId());
		model.addAttribute("redirectUrl", pro.getRedirectUri());
		model.addAttribute("scope", pro.getScope());
    	return "login";
    }

    @GetMapping("/login")
    public RedirectView googleLogin() throws UnsupportedEncodingException {
        String url = "https://accounts.google.com/o/oauth2/v2/auth" +
	                "?client_id=" + pro.getClientId() +
	                "&redirect_uri=" + pro.getRedirectUri() +
	                "&response_type=code" +
	                "&scope=" + pro.getScope();
        return new RedirectView(url);
    }
    
    @GetMapping("/login/oauth2/code/{registrationId}")
    public String googleLoginCallback(
    		@RequestParam("code") String authCode, 
    		@RequestParam("scope") String scope,
    		@RequestParam("prompt") String prompt,
    		@RequestParam("authuser") String authuser,
    		@PathVariable String registrationId,
    		Model model
	  ) {
    	
    	System.out.println("==================== Client Info ====================");
    	System.out.println("authCode = " + authCode+"\n"
    					 + "scope = " + scope+"\n"
    					 + "prompt = " + prompt+"\n"
    					 + "authuser = " + authuser+"\n"
    					 + "registrationId = " + registrationId);
    	System.out.println("===================================================");
    	
    	model.addAttribute("GoogleUserInfo", loginService.socialLogin(authCode, registrationId));
    	
        return "login_success";
    }
    
}

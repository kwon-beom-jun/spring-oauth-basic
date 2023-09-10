package com.auth.controller;

import java.util.HashMap;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.auth.service.AuthService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;


@Controller
@RequestMapping
public class AuthController {

	private static final long serialVersionUID = 1L;
	
	private AuthService authService;
    private final String scriptKey;
    private final String redirectUrl;
	
	public AuthController(
			AuthService authService,
			@Value("${kakao.script.key}") String scriptKey,
			@Value("${kakao.redirect.url}") String redirectUrl) {
		this.authService = authService;
		this.scriptKey = scriptKey;
		this.redirectUrl = redirectUrl;
	}
	
	@GetMapping("/")
	public String loginPage(Model model) {
		model.addAttribute("scriptKey", scriptKey);
		model.addAttribute("redirectUrl", redirectUrl);
		return "kakao/login/kakao_login";
	}

	@GetMapping("/kakaoapi")
	public String loginSuccess(@RequestParam(required = false) String code, HttpServletRequest request) {

		System.out.println(code);
		
		// URL에 포함된 code를 이용하여 액세스 토큰 발급
        String accessToken = authService.getKakaoAccessToken(code);
        System.out.println(accessToken);
        
        // 액세스 토큰을 이용하여 카카오 서버에서 유저 정보(닉네임, 이메일) 받아오기
        HashMap<String, Object> userInfo = authService.getUserInfo(accessToken);
        System.out.println("login Controller : " + userInfo);
		
        HttpSession session = request.getSession();
        
        session.setAttribute("nickname", userInfo.get("nickname"));
        if (userInfo.get("email") != null) {
        	session.setAttribute("email", userInfo.get("email"));
		}
        
        return "kakao/login/kakao_login_success";
	}

	
}

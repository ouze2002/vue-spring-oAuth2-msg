package com.example.demo.service;

import com.example.demo.dto.LoginResponseDto;
import com.example.demo.entity.Users;
import com.example.demo.repository.UsersRepository;
import com.example.demo.util.JwtTokenProvider;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class KakaoOAuthService {

    private final JwtTokenProvider jwtTokenProvider;
    private final UsersRepository usersRepository;
    private final RestTemplate restTemplate = new RestTemplate();

    @Value("${kakao.client-id}")
    private String clientId;

    @Value("${kakao.redirect-uri}")
    private String redirectUri;

    private static final String TOKEN_URL = "https://kauth.kakao.com/oauth/token";
    private static final String USER_INFO_URL = "https://kapi.kakao.com/v2/user/me";

    public LoginResponseDto kakaoLogin(String code, HttpServletResponse response) {

        // 1. 인가코드로 토큰 요청
        String tokenUrl = TOKEN_URL;
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("grant_type", "authorization_code");
        params.add("client_id", clientId);
        params.add("redirect_uri", redirectUri);
        params.add("code", code);

        HttpEntity<MultiValueMap<String, String>> tokenRequest = new HttpEntity<>(params, headers);
        ResponseEntity<Map<String, Object>> tokenResponse = restTemplate.exchange(
            tokenUrl,
            HttpMethod.POST,
            tokenRequest,
            new ParameterizedTypeReference<Map<String, Object>>() {}
        );
        String kakaoAccessToken = (String) Optional.ofNullable(tokenResponse.getBody())
                .map(body -> body.get("access_token"))
                .orElseThrow(() -> new RuntimeException("카카오 액세스 토큰이 없습니다."));

        // 2. 사용자 정보 요청
        HttpHeaders userInfoHeaders = new HttpHeaders();
        userInfoHeaders.setBearerAuth(kakaoAccessToken);
        HttpEntity<?> userInfoRequest = new HttpEntity<>(userInfoHeaders);

        ResponseEntity<Map<String, Object>> userInfoResponse = restTemplate.exchange(
            USER_INFO_URL,
            HttpMethod.GET,
            userInfoRequest,
            new ParameterizedTypeReference<Map<String, Object>>() {}
        );

        Map<String, Object> userInfoBody = userInfoResponse.getBody();
        if (userInfoBody == null || !userInfoBody.containsKey("id")) {
            throw new RuntimeException("카카오 사용자 정보 조회 실패");
        }

        String kakaoId = String.valueOf(userInfoBody.get("id"));
        // Map<String, Object> kakaoAccount = (Map<String, Object>) userInfoBody.get("kakao_account");
        // String email = kakaoAccount != null ? (String) kakaoAccount.get("email") : null;

        String username = "kakao_" + kakaoId;

        // 3. 사용자 등록 또는 조회
        Users user = usersRepository.findByUsername(username)
                .orElseGet(() -> {
                    Users newUser = new Users(username, null, "ROLE_USER"); // 비밀번호 null로 설정
                    return usersRepository.save(newUser);
                });

        // 4. JWT 생성
        String accessToken = jwtTokenProvider.generateAccessToken(username, user.getRole());
        String refreshToken = jwtTokenProvider.generateRefreshToken(username);

        user.setRefreshToken(refreshToken);
        usersRepository.save(user);

        // 5. 쿠키 설정
        Cookie refreshCookie = new Cookie("refreshToken", refreshToken);
        refreshCookie.setHttpOnly(true);
        refreshCookie.setMaxAge(7 * 24 * 60 * 60);
        refreshCookie.setPath("/");
        response.addCookie(refreshCookie);

        return new LoginResponseDto(accessToken, null);
    }
}

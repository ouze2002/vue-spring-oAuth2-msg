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
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class GoogleOAuthService {

    private final JwtTokenProvider jwtTokenProvider;
    private final UsersRepository usersRepository;
    private final RestTemplate restTemplate = new RestTemplate();

    @Value("${google.client-id}")
    private String clientId;

    @Value("${google.client-secret}")
    private String clientSecret;

    @Value("${google.redirect-uri}")
    private String redirectUri;

    private static final String TOKEN_URL = "https://oauth2.googleapis.com/token";
    private static final String USER_INFO_URL = "https://www.googleapis.com/oauth2/v2/userinfo";

    public LoginResponseDto loginWithGoogle(String code, HttpServletResponse response) {

        // 1. 구글 토큰 요청
        String tokenUrl = TOKEN_URL;
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("code", code);
        params.add("client_id", clientId);
        params.add("client_secret", clientSecret);
        params.add("redirect_uri", redirectUri);
        params.add("grant_type", "authorization_code");

        HttpEntity<MultiValueMap<String, String>> tokenRequest = new HttpEntity<>(params, headers);
        ResponseEntity<Map<String, Object>> tokenResponse = restTemplate.exchange(
            tokenUrl,
            HttpMethod.POST,
            tokenRequest,
            new ParameterizedTypeReference<Map<String, Object>>() {}
        );

        String accessToken = (String) Optional.ofNullable(tokenResponse.getBody())
                .map(body -> body.get("access_token"))
                .orElseThrow(() -> new RuntimeException("구글 액세스 토큰이 없습니다."));

        // 2. 사용자 정보 요청
        HttpHeaders infoHeaders = new HttpHeaders();
        infoHeaders.setBearerAuth(accessToken);

        HttpEntity<?> userInfoRequest = new HttpEntity<>(infoHeaders);
        ResponseEntity<Map<String, Object>> userInfoResponse = restTemplate.exchange(
            USER_INFO_URL,
            HttpMethod.GET,
            userInfoRequest,
            new ParameterizedTypeReference<Map<String, Object>>() {}
        );

        Map<String, Object> userInfoBody = userInfoResponse.getBody();
        if (userInfoBody == null || !userInfoBody.containsKey("id")) {
            throw new RuntimeException("구글 사용자 정보 조회 실패");
        }

        String googleId = String.valueOf(userInfoBody.get("id"));

        //String email = (String) userInfoBody.get("email");
        String username = "google_" + googleId;

        // 3. DB 저장 or 조회
        Users user = usersRepository.findByUsername(username)
                .orElseGet(() -> {
                    Users newUser = new Users(username, null, "ROLE_USER");
                    return usersRepository.save(newUser);
                });

        String jwtAccess = jwtTokenProvider.generateAccessToken(username, user.getRole());
        String refreshToken = jwtTokenProvider.generateRefreshToken(username);

        user.setRefreshToken(refreshToken);
        usersRepository.save(user);

        Cookie refreshCookie = new Cookie("refreshToken", refreshToken);
        refreshCookie.setHttpOnly(true);
        refreshCookie.setPath("/");
        refreshCookie.setMaxAge(7 * 24 * 60 * 60);
        response.addCookie(refreshCookie);

        return new LoginResponseDto(jwtAccess, null);
    }
}

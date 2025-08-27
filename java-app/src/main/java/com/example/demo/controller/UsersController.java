package com.example.demo.controller;

import java.security.Principal;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.dto.LoginRequestDto;
import com.example.demo.dto.LoginResponseDto;
import com.example.demo.dto.UsersRequestDto;
import com.example.demo.entity.Users;
import com.example.demo.service.GoogleOAuthService;
import com.example.demo.service.KakaoOAuthService;
import com.example.demo.service.UsersService;
import com.example.demo.util.JwtTokenProvider;

import lombok.RequiredArgsConstructor;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse; 

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class UsersController {
    private final JwtTokenProvider jwtTokenProvider;
    private final UsersService usersService;
    private final KakaoOAuthService kakaoOAuthService;
    private final GoogleOAuthService googleOAuthService;

    @PostMapping("/signup")
    public String signup(@RequestBody UsersRequestDto request) {
        usersService.signup(request);
        return "회원가입 성공!";
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponseDto> login(@RequestBody LoginRequestDto request,
                                                  HttpServletResponse response) {
        LoginResponseDto tokens = usersService.login(request);

        // Refresh Token을 HttpOnly 쿠키로 설정
        Cookie refreshCookie = new Cookie("refreshToken", tokens.getRefreshToken());
        refreshCookie.setHttpOnly(true);
        refreshCookie.setPath("/");
        refreshCookie.setMaxAge(7 * 24 * 60 * 60); // 7일
        response.addCookie(refreshCookie);

        // Access Token은 클라이언트가 저장
        return ResponseEntity.ok(new LoginResponseDto(tokens.getAccessToken(), null));
    }

    @PostMapping("/refresh")
    public ResponseEntity<?> refresh(HttpServletRequest request) {
        // 쿠키에서 Refresh Token 추출
        String refreshToken = null;
        if (request.getCookies() != null) {
            for (Cookie cookie : request.getCookies()) {
                if (cookie.getName().equals("refreshToken")) {
                    refreshToken = cookie.getValue();
                }
            }
        }

        if (refreshToken == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("리프레시 토큰이 없습니다.");
        }

        try {
            LoginResponseDto tokens = usersService.refresh(refreshToken); // Service 계층의 refresh 호출
            return ResponseEntity.ok(new LoginResponseDto(tokens.getAccessToken(), null)); // Access Token만 반환
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(e.getMessage());
        }
    }

    @PostMapping("/logout")
    public ResponseEntity<String> logout(@RequestHeader("Authorization") String accessToken,
                                        HttpServletResponse response) {
        String token = accessToken.replace("Bearer ", "");
        String username = jwtTokenProvider.getUsernameFromToken(token);

        Users user = usersService.getUser(username);
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("사용자를 찾을 수 없습니다.");
        }

        user.setRefreshToken(null);
        usersService.updateUsers(user);

        // 쿠키 삭제
        Cookie refreshCookie = new Cookie("refreshToken", null);
        refreshCookie.setHttpOnly(true);
        refreshCookie.setMaxAge(0);
        refreshCookie.setPath("/");
        response.addCookie(refreshCookie);

        return ResponseEntity.ok("로그아웃 성공");
    }

    @PostMapping("/kakao")
    public ResponseEntity<?> kakaoLogin(@RequestBody Map<String, String> body, HttpServletResponse response) {
        String code = body.get("code");
        LoginResponseDto tokens = kakaoOAuthService.kakaoLogin(code, response);
        return ResponseEntity.ok(tokens);
    }

    @PostMapping("/google")
    public ResponseEntity<?> googleLogin(@RequestBody Map<String, String> body, HttpServletResponse response) {
        String code = body.get("code");
        LoginResponseDto tokens = googleOAuthService.loginWithGoogle(code, response);
        return ResponseEntity.ok(tokens);
    }

    // 현재 로그인된 사용자 정보를 반환하는 엔드포인트 추가
    @GetMapping("/me")
    public ResponseEntity<?> getCurrentUser(Principal principal) {
        if (principal == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("인증된 사용자가 없습니다.");
        }
        String username = principal.getName(); // 현재 로그인된 사용자의 username
        Users user = usersService.getUser(username);

        if (user == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("사용자를 찾을 수 없습니다.");
        }

        // 사용자 이름만 포함하는 DTO 또는 Map을 반환하는 것이 좋습니다.
        // 여기서는 간단하게 Map으로 반환합니다.
        Map<String, String> userInfo = Map.of("username", user.getUsername());
        return ResponseEntity.ok(userInfo);
    }
}
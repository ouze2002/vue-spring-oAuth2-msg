package com.example.demo.service;

import com.example.demo.dto.LoginRequestDto;
import com.example.demo.dto.LoginResponseDto;
import com.example.demo.dto.UsersRequestDto;
import com.example.demo.entity.Users;
import com.example.demo.repository.UsersRepository;
import com.example.demo.util.JwtTokenProvider;
import com.example.demo.util.EncryptionUtil; // EncryptionUtil 임포트

import lombok.RequiredArgsConstructor;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;


@Service
@RequiredArgsConstructor
public class UsersService {
    private final UsersRepository usersRepository;
    private final PasswordEncoder passwordEncoder;

    private final JwtTokenProvider jwtTokenProvider;

    public void signup(UsersRequestDto request) {
        if (usersRepository.findByUsername(request.getUsername()).isPresent()) {
            throw new RuntimeException("이미 존재하는 사용자입니다.");
        }

        Users user = Users.builder()
                .username(request.getUsername())
                .password(passwordEncoder.encode(request.getPassword()))
                .role("ROLE_USER")
                .build();

        usersRepository.save(user);
    }

    public LoginResponseDto login(LoginRequestDto request) {
        Users user = usersRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new RuntimeException("비밀번호가 일치하지 않습니다.");
        }

        String accessToken = jwtTokenProvider.generateAccessToken(user.getUsername(), user.getRole());
        String refreshToken = jwtTokenProvider.generateRefreshToken(user.getUsername());

        // 리프레시 토큰 암호화하여 저장
        user.setRefreshToken(EncryptionUtil.encrypt(refreshToken));
        usersRepository.save(user);

        return new LoginResponseDto(accessToken, refreshToken);
    }

    public LoginResponseDto refresh(String refreshToken) {
        String token = refreshToken.replace("Bearer ", "");

        if (!jwtTokenProvider.validateToken(token)) {
            throw new RuntimeException("Refresh Token이 유효하지 않습니다.");
        }

        String username = jwtTokenProvider.getUsernameFromToken(token);
        Users user = usersRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("사용자 없음"));

        // DB에 저장된 암호화된 리프레시 토큰 복호화
        String storedRefreshToken = EncryptionUtil.decrypt(user.getRefreshToken());

        if (!token.equals(storedRefreshToken)) { // 복호화된 토큰과 비교
            throw new RuntimeException("서버에 저장된 Refresh Token과 다릅니다.");
        }

        String newAccessToken = jwtTokenProvider.generateAccessToken(username, user.getRole());
        String newRefreshToken = jwtTokenProvider.generateRefreshToken(username);

        // 새로운 리프레시 토큰 암호화하여 저장
        user.setRefreshToken(EncryptionUtil.encrypt(newRefreshToken));
        usersRepository.save(user);

        return new LoginResponseDto(newAccessToken, newRefreshToken);
    }

    public String logout(String accessToken) {
        String token = accessToken.replace("Bearer ", "");
        String username = jwtTokenProvider.getUsernameFromToken(token);

        Users user = usersRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("사용자 없음"));

        user.setRefreshToken(null);
        usersRepository.save(user);

        return "로그아웃 성공";
    }

    public Users getUser(String username) {
        return usersRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));
    }

    public void updateUsers(Users user) {
        usersRepository.save(user);
    }
    
}
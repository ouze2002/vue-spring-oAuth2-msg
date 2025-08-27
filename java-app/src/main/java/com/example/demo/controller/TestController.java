package com.example.demo.controller;


import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/private")
public class TestController {
    @GetMapping("/hello")
    public String hello() {
        return "🔐 인증된 사용자만 접근 가능한 API입니다!";
    }
}

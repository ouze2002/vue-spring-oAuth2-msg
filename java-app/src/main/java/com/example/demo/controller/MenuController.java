package com.example.demo.controller;

import com.example.demo.entity.Menu;
import com.example.demo.service.MenuService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/menus")
public class MenuController {

    @Autowired
    private MenuService menuService;

    @GetMapping
    public ResponseEntity<List<Menu>> getMenus(Authentication authentication) {
        Set<String> userRoles;
        if (authentication != null && authentication.isAuthenticated()) {
            userRoles = authentication.getAuthorities().stream()
                    .map(GrantedAuthority::getAuthority)
                    .collect(Collectors.toSet());
            userRoles.add("ROLE_AUTHENTICATED"); // 로그인한 사용자에게는 "ROLE_AUTHENTICATED" 역할 부여
        } else {
            userRoles = Collections.singleton("ROLE_ANONYMOUS"); // 로그인하지 않은 사용자에게는 "ROLE_ANONYMOUS" 역할 부여
        }

        List<Menu> menus = menuService.getAccessibleMenus(userRoles);
        return ResponseEntity.ok(menus);
    }
}
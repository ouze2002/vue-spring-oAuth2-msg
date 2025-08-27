package com.example.demo.service;

import com.example.demo.entity.Menu;
import com.example.demo.repository.MenuRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class MenuService {

    @Autowired
    private MenuRepository menuRepository;

    public List<Menu> getAllMenus() {
        return menuRepository.findAllByOrderByMenuOrderAsc();
    }

    public List<Menu> getAccessibleMenus(Set<String> userRoles) {
        List<Menu> allMenus = menuRepository.findAllByOrderByMenuOrderAsc();
        List<Menu> accessibleMenus = new ArrayList<>();

        for (Menu menu : allMenus) {
            if (isMenuAccessible(menu, userRoles)) {
                accessibleMenus.add(menu);
            }
        }
        return accessibleMenus;
    }

    private boolean isMenuAccessible(Menu menu, Set<String> userRoles) {
        if (menu.getRequiredRole() == null || menu.getRequiredRole().isEmpty()) {
            return true; // 역할 제한이 없는 메뉴는 모두에게 표시
        }

        Set<String> requiredRoles = Arrays.stream(menu.getRequiredRole().split(","))
                .map(String::trim)
                .collect(Collectors.toSet());

        // 사용자의 역할 중 하나라도 requiredRoles에 포함되면 접근 허용
        return userRoles.stream().anyMatch(requiredRoles::contains);
    }
}
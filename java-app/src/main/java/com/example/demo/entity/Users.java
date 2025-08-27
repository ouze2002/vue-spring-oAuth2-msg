package com.example.demo.entity;


import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Users {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String username;

    private String password;

    private String role; // ex) ROLE_USER, ROLE_ADMIN

    @Column(length = 500) //추가
    private String refreshToken;

    public Users(String username, String password, String roleUser) {
        this.username = username;
        this.role = roleUser;
        this.password = password;
    }
}
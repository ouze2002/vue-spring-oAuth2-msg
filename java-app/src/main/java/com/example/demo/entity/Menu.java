package com.example.demo.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "menus")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Menu {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;
    private String path;
    private String icon;

    @Column(name = "parent_id")
    private Long parentId;

    @Column(name = "menu_order")
    private Integer menuOrder;

    @Column(name = "required_role")
    private String requiredRole;

    private String component;
}
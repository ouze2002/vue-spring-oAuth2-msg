package com.example.demo.repository;

import com.example.demo.entity.Menu;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MenuRepository extends JpaRepository<Menu, Long> {
    List<Menu> findAllByOrderByMenuOrderAsc();
    List<Menu> findByParentIdIsNullOrderByMenuOrderAsc();
    List<Menu> findByParentIdOrderByMenuOrderAsc(Long parentId);
}

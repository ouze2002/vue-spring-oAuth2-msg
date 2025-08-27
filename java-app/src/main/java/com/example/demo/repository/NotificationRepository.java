package com.example.demo.repository;

import com.example.demo.entity.Notification;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface NotificationRepository extends JpaRepository<Notification, Long> {
    
    /**
     * 생성일자 내림차순으로 알림 목록을 페이징하여 조회합니다.
     * @param pageable 페이징 정보
     * @return 페이징된 알림 목록
     */
    Page<Notification> findAllByOrderByCreatedAtDesc(Pageable pageable);
}

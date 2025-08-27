package com.example.demo.service;

import com.example.demo.dto.NotificationDto;
import com.example.demo.dto.NotificationResponseDto;
import com.example.demo.entity.Notification;
import com.example.demo.repository.NotificationRepository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationService {

    private final NotificationRepository notificationRepository;

    @Transactional
    public Notification saveNotification(NotificationDto notificationDto) {
        Notification notification = Notification.builder()
                .title(notificationDto.getTitle())
                .message(notificationDto.getMessage())
                .type(notificationDto.getType())
                .isRead(false)
                .build();

        return notificationRepository.save(notification);
    }

    @Transactional(readOnly = true)
    public Notification getNotification(Long id) {
        return notificationRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Notification not found with id: " + id));
    }

    @Transactional
    public void markAsRead(Long id) {
        Notification notification = getNotification(id);
        if (!notification.isRead()) {
            notification.setRead(true);
            notification.setReadAt(java.time.LocalDateTime.now());
            notificationRepository.save(notification);
        }
    }
    
    /**
     * 최신순으로 알림 목록을 페이징하여 조회합니다.
     * @param pageable 페이징 정보 (기본값: page=0, size=20, sort=createdAt,desc)
     * @return 페이징된 알림 목록
     */
    @Transactional(readOnly = true)
    public Page<NotificationResponseDto> getNotifications(Pageable pageable) {
        return notificationRepository.findAllByOrderByCreatedAtDesc(pageable)
                .map(NotificationResponseDto::from);
    }
}

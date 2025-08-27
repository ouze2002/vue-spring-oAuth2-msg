package com.example.demo.controller;

import java.util.UUID;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.access.prepost.PreAuthorize;

import com.example.demo.dto.NotificationDto;
import com.example.demo.dto.NotificationResponseDto;
import com.example.demo.redis.RedisSubscriber;
import com.example.demo.service.NotificationService;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/notifications")
public class NotificationController {

    private final ObjectMapper objectMapper;
    private final RedisTemplate<String, Object> redisTemplate;
    private final NotificationService notificationService;
    private static final long SSE_TIMEOUT = 60 * 60 * 1000L; // 1 hour

    @PostMapping("/send")
    public ResponseEntity<String> sendNotification(@RequestBody NotificationDto notificationDto) {
        try {
            // 1. 데이터베이스에 알림 저장
            notificationService.saveNotification(notificationDto);
            
            // 2. Redis를 통해 모든 구독자에게 알림 전송
            String message = objectMapper.writeValueAsString(notificationDto);
            redisTemplate.convertAndSend("notification", message);
            
            return ResponseEntity.ok("Notification sent and saved successfully");
        } catch (Exception e) {
            log.error("Error processing notification", e);
            return ResponseEntity.status(500).body("Error processing notification: " + e.getMessage());
        }
    }
    
    /**
     * 인증된 사용자의 알림 목록을 최신순으로 조회합니다.
     * @param pageable 페이징 정보 (기본값: page=0, size=20, sort=createdAt,desc)
     * @return 페이징된 알림 목록
     */
    @PreAuthorize("isAuthenticated()")
    @GetMapping
    public ResponseEntity<Page<NotificationResponseDto>> getNotifications(
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        return ResponseEntity.ok(notificationService.getNotifications(pageable));
    }

    /**
     * 특정 알림을 읽음 처리합니다.
     * @param id 알림 ID
     * @return 처리 결과
     */
    @PreAuthorize("isAuthenticated()")
    @PatchMapping("/{id}/read")
    public ResponseEntity<Void> markAsRead(@PathVariable Long id) {
        notificationService.markAsRead(id);
        return ResponseEntity.ok().build();
    }

    /**
     * 클라이언트가 SSE(Server-Sent Events)를 구독하기 위한 엔드포인트입니다.
     * 클라이언트는 이 엔드포인트로 연결을 열어두고, 서버에서 이벤트가 발생할 때마다 실시간으로 데이터를 수신할 수 있습니다.
     * 
     * @return SseEmitter - 서버에서 클라이언트로 이벤트를 전송하기 위한 SseEmitter 객체
     */
    @GetMapping(value = "/subscribe", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter subscribe() {
        // 고유한 클라이언트 ID 생성
        String id = UUID.randomUUID().toString();
        
        // SseEmitter 객체 생성 (타임아웃: 1시간)
        SseEmitter emitter = new SseEmitter(SSE_TIMEOUT);
        
        // 생성된 SseEmitter를 클라이언트 ID와 함께 맵에 저장
        // 이후 이 클라이언트에게 이벤트를 보낼 때 사용됨
        RedisSubscriber.sseEmitters.put(id, emitter);
        
        // 타임아웃 핸들러 설정 (1시간 후에 자동으로 호출됨)
        emitter.onTimeout(() -> {
            log.info("클라이언트 {}의 SSE 연결이 타임아웃되었습니다.", id);
            emitter.complete();
            RedisSubscriber.sseEmitters.remove(id);
        });
        
        // 완료 핸들러 설정 (클라이언트가 연결을 종료하면 호출됨)
        emitter.onCompletion(() -> {
            log.info("클라이언트 {}의 SSE 연결이 완료되었습니다.", id);
            RedisSubscriber.sseEmitters.remove(id);
        });
        
        // 에러 핸들러 설정 (에러 발생 시 호출됨)
        emitter.onError((ex) -> {
            log.error("SSE error for client {}: {}", id, ex.getMessage(), ex);
            emitter.complete();
            RedisSubscriber.sseEmitters.remove(id);
        });
        
        try {
            // Send initial connection event
            emitter.send(SseEmitter.event()
                    .name("open")
                    .data("connected")
                    .reconnectTime(5000));
        } catch (Exception e) {
            log.error("Error sending initial event to client {}", id, e);
            emitter.completeWithError(e);
            RedisSubscriber.sseEmitters.remove(id);
            return emitter;
        }
        
        log.info("New SSE client connected: {}", id);
        return emitter;
    }
}

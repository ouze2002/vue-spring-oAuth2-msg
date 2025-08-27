package com.example.demo.redis;

import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import lombok.extern.slf4j.Slf4j;

/**
 * Redis 메시지 구독자 클래스입니다.
 * Redis 채널에서 메시지를 수신하면, 연결된 모든 SSE 클라이언트에게 메시지를 브로드캐스팅합니다.
 */
@Slf4j
@Component
public class RedisSubscriber implements MessageListener {
    
    /**
     * 연결된 SSE 클라이언트들을 관리하는 맵입니다.
     * key: 클라이언트 고유 ID
     * value: SseEmitter 객체
     */
    public static Map<String, SseEmitter> sseEmitters = new HashMap<>();

    /**
     * Redis 채널로부터 메시지를 수신했을 때 호출되는 메서드입니다.
     * 수신된 메시지를 모든 연결된 SSE 클라이언트에게 전달합니다.
     * 
     * @param message Redis로부터 수신된 메시지
     * @param pattern 메시지가 수신된 채널 패턴
     */
    @Override
    public void onMessage(@NonNull Message message, @Nullable byte[] pattern) {
        try {
            // Redis 메시지를 UTF-8 문자열로 변환
            String msg = new String(message.getBody(), StandardCharsets.UTF_8);
            log.info("수신된 메시지: {}", msg);
            
            // 동시성 문제를 피하기 위해 맵의 복사본 생성
            Map<String, SseEmitter> emitters = new HashMap<>(sseEmitters);
            
            // 모든 연결된 클라이언트에게 메시지 전송
            emitters.forEach((id, emitter) -> {
                try {
                    if (emitter != null) {
                        // 동시성 제어를 위한 동기화 블록
                        synchronized (emitter) {
                            try {
                                // SSE 이벤트 생성 및 전송
                                emitter.send(SseEmitter.event()
                                        .name("notification")  // 이벤트 이름
                                        .data(msg));          // 전송할 데이터
                            } catch (IllegalStateException e) {
                                // 이미 닫힌 연결인 경우
                                log.debug("이미 닫힌 연결입니다. 클라이언트 ID: {}", id);
                                emitter.complete();
                                sseEmitters.remove(id);
                            } catch (Exception e) {
                                // 메시지 전송 중 오류 발생 시
                                log.error("클라이언트 {}에게 메시지 전송 중 오류 발생: {}", id, e.getMessage());
                                emitter.completeWithError(e);
                                sseEmitters.remove(id);
                            }
                        }
                    }
                } catch (Exception e) {
                    // 예상치 못한 오류 처리
                    log.error("클라이언트 {} 처리 중 예상치 못한 오류 발생: {}", id, e.getMessage());
                    if (emitter != null) {
                        emitter.completeWithError(e);
                        sseEmitters.remove(id);
                    }
                }
            });
        } catch (Exception e) {
            // 메시지 처리 중 발생한 오류 로깅
            log.error("메시지 처리 중 오류 발생: {}", e.getMessage(), e);
        }
    }
    
    /**
     * 바이트 배열을 16진수 문자열로 변환하는 유틸리티 메서드입니다.
     * 
     * @param bytes 변환할 바이트 배열
     * @return 16진수 문자열로 변환된 결과
     */
    public static String bytesToHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }
}

# Java SSE with Redis Example

이 프로젝트는 Java Spring Boot와 Redis를 사용하여 확장 가능한 실시간 알림 시스템을 구현한 예제입니다.

## 기술 스택

- **Backend**: Spring Boot 3.5.4
- **In-Memory Data Store**: Redis (Pub/Sub)
- **Build Tool**: Maven/Gradle
- **Java**: 21

## SSE (Server-Sent Events) 란?

SSE는 서버에서 클라이언트로 단방향으로 실시간 데이터를 스트리밍하기 위한 HTML5 표준 기술입니다. HTTP 프로토콜을 기반으로 동작하며, 클라이언트가 한번 연결을 맺으면 서버는 필요할 때마다 지속적으로 데이터를 보내줄 수 있습니다.

## Redis Pub/Sub 패턴이란?

Redis의 Publish/Subscribe 패턴은 발행자(Publisher)가 메시지를 특정 채널에 발행하면, 해당 채널을 구독(Subscribe)하고 있는 모든 구독자(Subscriber)에게 메시지가 전달되는 메시징 패턴입니다.

## 아키텍처 개요

```
[Client] <--SSE--> [Spring Boot Server 1] <---> [Redis] <---> [Spring Boot Server 2]
```

## 주요 기능

- 실시간 알림 기능 (SSE + Redis Pub/Sub)
- 다중 서버 환경에서의 알림 브로드캐스팅
- 안정적인 메시지 전달 보장
- 확장 가능한 아키텍처

## 구현 내용

### 1. Redis 설정

`application.yml`에 Redis 연결 정보를 설정합니다:

```yaml
spring:
  redis:
    host: localhost
    port: 6379
    password: 
```

### 2. Redis 메시지 리스너 설정

`RedisConfig.java`에서 Redis 메시지 리스너를 설정합니다. 이 리스너는 특정 채널에서 메시지를 수신하면, 해당 메시지를 구독 중인 클라이언트에게 전달합니다.

### 3. NotificationController

`com.example.demo.controller.NotificationController` 에서 SSE 연결을 처리합니다.

- **`@PostMapping("/send")`**: 알림을 생성하고 Redis 채널에 발행합니다.
- **`@GetMapping("/subscribe/{userId}")`**: 클라이언트가 SSE 구독을 요청하는 엔드포인트입니다.
- **`@DeleteMapping("/{id}")`**: 특정 알림을 삭제합니다.
- **`@PatchMapping("/{id}/read")`**: 알림을 읽음 상태로 표시합니다.
- **`@GetMapping`**: 사용자의 알림 목록을 조회합니다.

### 4. RedisMessageListener

Redis 채널에서 메시지를 수신하면, 해당 메시지를 구독 중인 클라이언트에게 전달합니다.

## 데이터 흐름

1. **알림 생성 및 발행**
   - 클라이언트가 `/api/notifications/send`로 알림을 전송합니다.
   - 서버는 알림을 데이터베이스에 저장합니다.
   - Redis 채널(`notifications`)에 알림을 발행합니다.

2. **SSE 구독**
   - 클라이언트가 `/api/notifications/subscribe/{userId}`로 SSE 연결을 요청합니다.
   - 서버는 `SseEmitter`를 생성하고, 클라이언트 ID를 키로 하여 메모리에 저장합니다.
   - Redis 채널(`notifications:{userId}`)을 구독합니다.

3. **실시간 알림 수신**
   - Redis 채널에 알림이 발행되면, 해당 채널을 구독 중인 `RedisMessageListener`가 메시지를 수신합니다.
   - 수신된 메시지를 해당 사용자의 `SseEmitter`를 통해 클라이언트에게 전송합니다.

4. **연결 종료**
   - 클라이언트가 연결을 종료하거나, 타임아웃이 발생하면 `SseEmitter`가 완료됩니다.
   - `SseEmitter`가 완료되면 메모리에서 제거됩니다.

## 테스트 방법

### 사전 요구사항
- Redis 서버가 실행 중이어야 합니다.
- 애플리케이셩 실행 전 Redis 연결 정보가 올바르게 설정되어 있는지 확인하세요.

### 1. 애플리케이션 실행

```bash
./mvnw spring-boot:run
```

### 2. SSE 구독

새 터미널을 열고 다음 명령어로 SSE 구독을 시작합니다:

```bash
# 사용자 ID가 1인 클라이언트가 SSE 구독
curl -N http://localhost:8080/api/notifications/subscribe/1
```

### 3. 알림 전송

다른 터미널에서 다음 명령어로 알림을 전송합니다:

```bash
# 사용자 ID 1에게 알림 전송
curl -X POST http://localhost:8080/api/notifications/send \
  -H "Content-Type: application/json" \
  -d '{
    "senderId": 2,
    "receiverId": 1,
    "content": "새로운 메시지가 도착했습니다.",
    "type": "MESSAGE"
  }'
```

### 4. 알림 확인

1. **SSE 클라이언트 터미널**에서 실시간으로 수신된 알림을 확인할 수 있습니다.
2. **알림 목록 조회**
   ```bash
   curl http://localhost:8080/api/notifications?userId=1
   ```
3. **알림 읽음 처리**
   ```bash
   curl -X PATCH http://localhost:8080/api/notifications/1/read
   ```
4. **알림 삭제**
   ```bash
   curl -X DELETE http://localhost:8080/api/notifications/1
   ```

## 확장성 고려사항

1. **로드 밸런싱**
   - 다중 서버 환경에서는 로드 밸런서가 세션 지속성(Sticky Session)을 지원해야 합니다.
   - 또는 Redis를 사용하여 모든 서버가 모든 메시지를 수신할 수 있도록 할 수 있습니다.

2. **장애 대응**
   - Redis 클러스터를 구성하여 고가용성을 보장할 수 있습니다.
   - 메시지 유실을 방지하기 위해 Redis Streams를 고려할 수 있습니다.

3. **성능 최적화**
   - 대량의 연결을 처리하기 위해 Netty 기반의 웹 서버를 고려할 수 있습니다.
   - 연결당 스레드를 생성하는 대신, 이벤트 루프 모델을 사용하는 것이 좋습니다.

## 결론

이 구현은 Redis의 Pub/Sub 기능을 활용하여 확장 가능한 실시간 알림 시스템을 구축하는 방법을 보여줍니다. SSE를 사용하면 HTTP 연결을 유지하면서 서버에서 클라이언트로 실시간 데이터를 효율적으로 전송할 수 있습니다.

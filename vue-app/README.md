# Full Stack Dynamic Menu Project

이 프로젝트는 Spring Boot 백엔드와 Vue.js 프론트엔드로 구성된 풀 스택 애플리케이션입니다. 주요 특징은 데이터베이스에 정의된 메뉴 정보를 기반으로 프론트엔드의 페이지 라우팅과 사이드바 메뉴가 동적으로 생성되는 것입니다.

## 기술 스택

- **Backend**: Java, Spring Boot
- **Frontend**: Vue.js, Vite, Pinia, Vue Router
- **Database**: PostgreSQL (또는 다른 관계형 데이터베이스)

---

## 동적 메뉴 설정 과정

이 애플리케이션의 핵심 기능은 관리자가 데이터베이스에서 메뉴를 추가/수정/삭제하면, 사용자가 로그인할 때 해당 메뉴 구조가 자동으로 프론트엔드에 반영되는 것입니다. 소스 코드 수정이나 재배포 없이 메뉴와 페이지를 관리할 수 있습니다.

### 1. 백엔드 (Java-App) 흐름

백엔드는 사용자의 권한에 맞는 메뉴 데이터를 제공하는 API 역할을 합니다.

#### 1.1. `Menu.java` 엔티티

메뉴 정보를 담는 JPA 엔티티입니다. 주요 필드는 다음과 같습니다.

- `name`: 화면에 표시될 메뉴 이름 (예: "대시보드")
- `path`: 웹 브라우저의 URL 경로 (예: "/dashboard")
- `icon`: 메뉴 옆에 표시될 아이콘 이름 (Google Material Icons 기준)
- `component`: 해당 경로에 렌더링될 Vue 컴포넌트의 경로 (예: "dashboard/Dashboard.vue")
- `parentId`: 상위 메뉴의 ID. 이를 통해 메뉴의 계층 구조(Sub-menu)를 구현합니다.
- `requiredRole`: 이 메뉴에 접근하기 위해 필요한 사용자 역할 (예: "ROLE_ADMIN")

#### 1.2. `MenuController` API

- **`GET /api/menus`**: 현재 로그인한 사용자의 인증 정보(JWT)를 바탕으로 `requiredRole`을 확인하여, 해당 사용자가 접근할 수 있는 메뉴 목록만 필터링하여 JSON 형태로 반환합니다.

### 2. 프론트엔드 (Vue-App) 흐름

프론트엔드는 백엔드로부터 받은 메뉴 데이터를 사용하여, 앱이 시작될 때 페이지 경로와 UI를 동적으로 생성합니다.

#### 2.1. 앱 초기화 (`main.js`)

- Vue 앱이 생성되고 마운트되기 전, `main.js`는 `setupDynamicRoutes`라는 비동기 함수를 먼저 호출합니다.
- 이 함수는 사용자가 로그인(인증 토큰 보유)한 상태일 경우, 백엔드에서 메뉴 정보를 가져와 라우터를 설정하는 작업을 **미리 완료**합니다.
- 모든 동적 라우트가 준비된 후에야 Vue 앱을 마운트(`app.mount('#app')`)하여, "경로를 찾을 수 없음" 오류를 원천적으로 방지합니다.

#### 2.2. 동적 라우트 생성 (`router/index.js`)

- **`setupDynamicRoutes()` 함수**: 이 함수의 역할은 다음과 같습니다.
  1.  Pinia 스토어의 `fetchMenus` 액션을 호출하여 백엔드의 `GET /api/menus` API로부터 메뉴 목록을 가져옵니다.
  2.  가져온 메뉴 목록의 각 항목을 순회하며, `menu.path`와 `menu.component` 정보를 사용해 Vue Router의 라우트 객체를 생성합니다.
  3.  Vite의 `import.meta.glob` 기능을 사용하여 `menu.component`에 저장된 문자열 경로(예: "dashboard/Dashboard.vue")를 실제 Vue 컴포넌트로 매핑합니다.
  4.  `router.addRoute()` 함수를 사용해 생성된 라우트를 Vue Router 인스턴스에 동적으로 추가합니다.

- **`beforeEach` 가드**: 라우트 생성 역할이 분리된 후, `beforeEach` 가드는 인증이 필요한 페이지에 비인증 사용자가 접근하는 것을 막는 등 본연의 네비게이션 보호 역할만 수행합니다.

#### 2.3. 상태 관리 (`store/user.js`)

- `fetchMenus` 액션은 API로부터 받은 메뉴 데이터를 스토어의 상태(`state`)에 저장합니다.
- 데이터는 두 가지 형태로 저장됩니다.
  1.  `rawMenus`: API에서 받은 원본 목록
  2.  `menus`: `parentId`를 기반으로 계층 구조(Tree)로 가공된 목록. 사이드바 UI 렌더링에 사용됩니다.

#### 2.4. UI 렌더링 (`Sidebar.vue` & `MenuItem.vue`)

- `Sidebar.vue`는 Pinia 스토어에서 계층 구조로 가공된 `menus` 데이터를 가져옵니다.
- `v-for` 디렉티브를 사용해 최상위 메뉴들을 순회하며, 각 메뉴 항목을 `MenuItem.vue` 컴포넌트로 렌더링합니다.
- `MenuItem.vue`는 재귀 컴포넌트로 설계되어, 하위 메뉴(`children`)가 있을 경우 자기 자신을 다시 렌더링하여 중첩된 서브메뉴 구조를 완벽하게 표현합니다.
- 또한 `menu.icon` 데이터를 사용해 아이콘을 표시하고, `menu.path`를 `<router-link>`에 연결하여 실제 페이지 이동을 처리합니다.

---

## 실시간 알림 기능 (SSE)

이 프로젝트는 **SSE(Server-Sent Events)** 기술을 사용하여 서버에서 클라이언트로 실시간 알림을 푸시합니다. 이를 통해 사용자는 페이지를 새로고침하지 않아도 새로운 정보를 즉시 받아볼 수 있습니다.

### 1. 백엔드 (Java-App) 흐름

- **`GET /api/notifications/subscribe`**: 클라이언트가 이 엔드포인트로 요청을 보내면, 서버는 `SseEmitter`를 생성하여 클라이언트와의 연결을 유지합니다.
- 서버는 특정 이벤트(예: 새로운 주문, 중요한 시스템 알림 등)가 발생하면, 연결된 모든 클라이언트의 `SseEmitter`를 통해 `notification`이라는 이름의 이벤트를 전송합니다.

### 2. 프론트엔드 (Vue-App) 흐름

프론트엔드의 실시간 알림 기능은 `src/services/notificationService.js` 파일이 핵심적인 역할을 담당합니다.

#### 2.1. `notificationService.js`

- **싱글톤 클래스**: 이 서비스는 싱글톤 패턴으로 구현되어 앱 전체에서 단 하나의 인스턴스만 사용됩니다.
- **SSE 연결 (`connect` 메서드)**: 사용자가 로그인하면 `App.vue`에서 이 메서드를 호출합니다. 내부적으로 브라우저의 `EventSource` API를 사용하여 백엔드의 `/api/notifications/subscribe` 엔드포인트에 연결을 시도합니다.
- **이벤트 리스닝**: `EventSource` 객체에 다음과 같은 이벤트 리스너를 등록합니다.
  - `open`: SSE 연결이 성공적으로 열렸을 때 호출됩니다.
  - `notification`: 서버에서 `notification` 이벤트를 보냈을 때 호출됩니다. 이 리스너는 수신한 데이터를 JSON으로 파싱하여 화면에 팝업 알림을 표시하는 `showNotification` 메서드를 호출합니다.
  - `error`: 연결 오류가 발생했을 때 호출되며, 5초 후 자동으로 재연결을 시도합니다.
- **연결 해제 (`disconnect` 메서드)**: 사용자가 로그아웃하면 `EventSource` 연결을 안전하게 종료합니다.

#### 2.2. UI 피드백

- **팝업 알림**: `showNotification` 메서드는 수신한 알림 정보를 바탕으로 동적으로 HTML 엘리먼트를 생성하여 화면 우측 상단에 팝업 형태로 표시합니다. 이 알림은 5초 후에 자동으로 사라집니다.
- **연결 상태 표시 (`Sidebar.vue`)**: 사이드바 하단에 SSE 연결 상태를 나타내는 작은 인디케이터가 있습니다. `notificationService`의 연결 상태에 따라 "실시간 알림 연결됨"(초록색 점) 또는 "실시간 알림 연결 끊김"(빨간색 점)으로 표시되어 사용자에게 시각적인 피드백을 제공합니다.

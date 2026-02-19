# MoneyLog Working Memory

> 목적: Codex/개발자가 작업 시작 전에 빠르게 읽고, 수정 지점과 검증 루틴을 즉시 파악하기 위한 실행용 문서
> 기준 시점: 2026-02-13

## 1. 프로젝트 한 줄 요약
- MoneyLog는 Spring Boot(백엔드) + React/Vite(프론트)로 구성된 가계부 서비스이며, JWT 기반 인증과 JPA/MyBatis 혼합 데이터 접근을 사용한다.

## 2. 작업 전 60초 체크리스트
- 요청이 기능 변경인지 문서/리팩토링인지 먼저 분류한다.
- 영향 도메인(예: `transaction`, `account`, `user`)을 1개로 좁힌다.
- 아래 경로에서 Controller -> Service -> Repository/Mapper 흐름을 먼저 확인한다.
- API 변경이면 프론트 API 모듈(`src/moneylog/src/api`) 동시 수정 여부를 확인한다.
- 완료 전 최소 검증(컴파일/테스트 또는 정적 대조)을 수행한다.

## 3. 핵심 경로 맵

### 백엔드
- 앱 시작점: `src/main/java/com/moneylog_backend/MoneyLogApplication.java`
- 보안 설정: `src/main/java/com/moneylog_backend/global/config/SecurityConfig.java`
- JWT 필터/토큰: `src/main/java/com/moneylog_backend/global/auth/jwt/JwtAuthenticationFilter.java`, `src/main/java/com/moneylog_backend/global/auth/jwt/JwtProvider.java`
- 전역 예외: `src/main/java/com/moneylog_backend/global/exception/GlobalExceptionHandler.java`
- 설정: `src/main/resources/application.yml`
- SQL/스키마: `src/main/resources/sql/schema.sql`
- MyBatis XML: `src/main/resources/mappers/*.xml`

### 프론트엔드
- 라우팅 진입점: `src/moneylog/src/App.tsx`
- 페이지: `src/moneylog/src/Pages/*`
- API 모듈: `src/moneylog/src/api/authApi.ts`, `src/moneylog/src/api/ledgerApi.ts`, `src/moneylog/src/api/axiosConfig.js`
- 상태 저장소: `src/moneylog/src/stores/*`
- 주요 컴포넌트: `src/moneylog/src/components/*`

## 4. 도메인별 수정 포인트 (백엔드)
- 공통 패턴: `{domain}/controller` -> `{domain}/service` -> `{domain}/repository` + `{domain}/mapper`
- 주요 도메인 루트
  - `src/main/java/com/moneylog_backend/moneylog/user`
  - `src/main/java/com/moneylog_backend/moneylog/transaction`
  - `src/main/java/com/moneylog_backend/moneylog/account`
  - `src/main/java/com/moneylog_backend/moneylog/category`
  - `src/main/java/com/moneylog_backend/moneylog/budget`
  - `src/main/java/com/moneylog_backend/moneylog/payment`
  - `src/main/java/com/moneylog_backend/moneylog/fixed`
  - `src/main/java/com/moneylog_backend/moneylog/bank`
  - `src/main/java/com/moneylog_backend/moneylog/schedule`

## 5. 보안/인증 작업 시 확인 순서
- 공개 URL 정책 확인: `SecurityConfig`의 `requestMatchers`
- 인증 객체 사용부 확인: Controller의 `Authentication` 또는 `@LoginUser`
- 로그인/로그아웃 변경 시 `UserController` + `UserService` + JWT/Redis 연계 점검
- 인증 오류 응답 변경 시 `GlobalExceptionHandler` 동시 확인

## 6. API 변경 시 동시 수정 규칙
- 백엔드에서 URL/파라미터/응답 DTO를 바꾸면 아래를 같이 본다.
- Controller 시그니처 (`@RequestParam`, `@RequestBody`, `@Valid`)
- Service 메서드 시그니처
- Mapper 인터페이스와 XML SQL id/resultType
- 프론트 호출부 (`authApi.ts`, `ledgerApi.ts`, `queries.ts`)와 타입(`src/moneylog/src/types/*`)
- 화면 컴포넌트에서 필드명 의존 여부

## 7. 최소 검증 루틴
- 문서 수정만: 경로/엔드포인트/설정 값이 실제 파일과 일치하는지 대조
- 백엔드 변경 시 권장
  - `./gradlew test` 또는 최소 `./gradlew build`
- 프론트 변경 시 권장
  - `src/moneylog`에서 `npm run build` 또는 `npm run lint` (환경에 따라 선택)
- 실행이 어려운 환경이면 "무엇을 검증했고 무엇을 못 했는지"를 결과에 명시

## 8. 작업 로그/문서 갱신 규칙
- 작업 전 계획은 당일 변경 로그 파일(`docs/changeLog/CHANGELOG_YYYY-MM-DD.md`)에 `[PLAN]`으로 기록
- 승인 기반 절차가 요구될 때는 승인 문구/시간을 로그에 남김
- 작업 완료 시 "계획 대비 차이/추가 이슈/계획 외 수정"을 반드시 기록
- 구조 변경이 발생하면 `project_flow.md`, `docs/PROJECT_KNOWLEDGE.md`, 본 문서를 함께 갱신

## 9. 참고 문서
- 상세 지식 베이스: `docs/PROJECT_KNOWLEDGE.md`
- 프로젝트 흐름도: `project_flow.md`
- 변경 이력: `docs/changeLog/CHANGELOG_2026-02-10.md`, `docs/changeLog/CHANGELOG_2026-02-13.md`

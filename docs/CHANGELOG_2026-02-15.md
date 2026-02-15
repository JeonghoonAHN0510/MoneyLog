# CHANGELOG 2026-02-15

## [TIME] 13:29 (KST) — [START] 예외 메시지 영어 문구 한글화 계획 수립

### 작업 목표 요약
- 백엔드 예외 처리 구간에서 영어로 노출되는 예외 메시지를 한국어로 통일한다.
- 기능 동작은 유지하고, 사용자/API 응답 메시지의 일관성만 개선한다.

## [TIME] 13:29 (KST) — [PLAN] 예외 메시지 영어 문구 한글화

### 실행 계획
# 🧠 실행 계획 보고

## 0. 이동할 브랜치
- 기준 브랜치: `master`
- 이동 예정 브랜치: `fix/exception-message-korean`
- 현재 브랜치 확인 결과: `master`
- 참고: 현재 워크트리가 대규모 변경 상태이므로, 승인 후 실제 구현 시 `master` 기준 여부를 다시 확인하고 진행

## 1. 작업 목표
- 영어 예외 메시지(`IllegalArgumentException`, `RuntimeException`)를 한국어 메시지로 교체
- 동일 도메인의 에러 문구를 일관된 톤으로 정리
- 필요 시 메시지 상수화로 재사용성 확보

## 2. 현재 상태 분석
- 관련 파일
  - `src/main/java/com/moneylog_backend/global/type/StatusEnum.java`
  - `src/main/java/com/moneylog_backend/global/type/ScheduleEnum.java`
  - `src/main/java/com/moneylog_backend/global/type/RoleEnum.java`
  - `src/main/java/com/moneylog_backend/global/type/ProviderEnum.java`
  - `src/main/java/com/moneylog_backend/global/type/PaymentEnum.java`
  - `src/main/java/com/moneylog_backend/global/type/CategoryEnum.java`
  - `src/main/java/com/moneylog_backend/global/type/AccountTypeEnum.java`
  - `src/main/java/com/moneylog_backend/moneylog/schedule/service/ScheduleService.java`
  - `src/main/java/com/moneylog_backend/global/constant/ErrorMessageConstants.java` (필요 시 확장)
- 현재 로직 요약
  - 대부분 도메인은 한국어 예외 메시지를 사용하고 있으나, Enum 변환 실패 및 스케줄 예외 일부는 영어 문구를 직접 throw
  - 영어 메시지가 API 응답 `message`로 전달될 수 있어 사용자 경험과 메시지 일관성이 떨어짐
- 문제 원인
  - 초기 구현 시 디버깅용/기본 영어 메시지가 잔존
  - 공통 메시지 관리 범위가 Not Found 계열 위주로 제한되어 있음

## 3. 변경 예정 파일 목록
- `src/main/java/com/moneylog_backend/global/type/StatusEnum.java`
- `src/main/java/com/moneylog_backend/global/type/ScheduleEnum.java`
- `src/main/java/com/moneylog_backend/global/type/RoleEnum.java`
- `src/main/java/com/moneylog_backend/global/type/ProviderEnum.java`
- `src/main/java/com/moneylog_backend/global/type/PaymentEnum.java`
- `src/main/java/com/moneylog_backend/global/type/CategoryEnum.java`
- `src/main/java/com/moneylog_backend/global/type/AccountTypeEnum.java`
- `src/main/java/com/moneylog_backend/moneylog/schedule/service/ScheduleService.java`
- `src/main/java/com/moneylog_backend/global/constant/ErrorMessageConstants.java` (선택)
- `docs/CHANGELOG_2026-02-15.md`

## 4. 변경 전략
- 1차(필수): 영어 예외 문구를 한국어로 직접 치환
- 2차(권장): 반복되는 메시지는 `ErrorMessageConstants`로 이동해 상수 참조로 통일
- 3차(검증): 한글 치환 후 기존 예외 흐름(HTTP 상태코드/핸들러 매핑) 변화가 없는지 확인
- 대안 비교
  - 대안 A: 문자열 직접 치환만 수행 (빠름, 즉시 반영 가능)
  - 대안 B: 상수화까지 동시 적용 (일관성/유지보수 유리, 수정 범위 소폭 증가) -> 승인 후 코드 상태를 보고 선택

## 5. 예상 영향 범위
- 영향 있음: 백엔드 API 예외 응답 메시지 텍스트
- 영향 없음: DB 스키마, 인증/인가 로직, 외부 API, 빌드 설정
- 프론트 영향: 메시지 텍스트 비교(하드코딩) 로직이 있다면 표시 문구만 달라질 수 있음

## 6. 리스크 분석
- 실패 가능성
  - 메시지 문구 변경으로 기존 테스트(문자열 단정)가 실패할 가능성
  - 예외 문맥에 맞지 않는 번역 문구 선택 시 운영 로그 해석성이 낮아질 가능성
- 롤백 방법
  - 변경 파일 단위로 즉시 롤백 가능
  - 상수화 적용 시 상수 참조만 원복하면 영향 최소화 가능

## 7. 테스트 계획
- 승인 후 실행 예정 검증
  - `./gradlew test` (가능 시)
  - 최소 `./gradlew build` 또는 `./gradlew`
- 수동 검증
  - Enum 변환 실패 케이스(잘못된 code/null)에서 한국어 메시지 반환 확인
  - 스케줄 수정 실패 시 메시지 한국어 반환 확인
- 새로운 테스트 추가 여부
  - 가능하면 예외 메시지 단정 단위 테스트 1~2건 추가 검토 (범위/시간에 따라 결정)

## [TIME] 13:32 (KST) — [PLAN] 예외 메시지 상수 통일 및 프론트 에러 메시지 연동 보강

### 실행 계획
# 🧠 실행 계획 보고

## 0. 이동할 브랜치
- 기준 브랜치: `master`
- 이동 예정 브랜치: `fix/exception-message-centralization`
- 현재 브랜치: `master`
- 실제 구현 전 `master` 기준 여부 재확인 후 진행

## 1. 작업 목표
- 백엔드 예외 메시지를 단일 상수 클래스에서 관리하도록 통일
- 영어 예외 메시지를 한국어로 전환
- 프론트에서 백엔드 에러 응답(`errorMessage`)을 공통으로 파싱해 사용자에게 노출하도록 개선

## 2. 현재 상태 분석
- 관련 파일
  - 백엔드
    - `src/main/java/com/moneylog_backend/global/constant/ErrorMessageConstants.java`
    - `src/main/java/com/moneylog_backend/global/type/StatusEnum.java`
    - `src/main/java/com/moneylog_backend/global/type/ScheduleEnum.java`
    - `src/main/java/com/moneylog_backend/global/type/RoleEnum.java`
    - `src/main/java/com/moneylog_backend/global/type/ProviderEnum.java`
    - `src/main/java/com/moneylog_backend/global/type/PaymentEnum.java`
    - `src/main/java/com/moneylog_backend/global/type/CategoryEnum.java`
    - `src/main/java/com/moneylog_backend/global/type/AccountTypeEnum.java`
    - `src/main/java/com/moneylog_backend/moneylog/schedule/service/ScheduleService.java`
    - `src/main/java/com/moneylog_backend/global/exception/GlobalExceptionHandler.java`
  - 프론트
    - `src/moneylog/src/api/axiosConfig.js`
    - `src/moneylog/src/Pages/FinancePage.tsx`
    - `src/moneylog/src/Pages/LoginPage.tsx`
    - `src/moneylog/src/Pages/SignUpPage.tsx`
    - `src/moneylog/src/components/ScheduleDialog.tsx`
    - (신규 예정) `src/moneylog/src/utils/error.ts` 또는 동등 유틸 파일
- 현재 로직 요약
  - 백엔드 일부 예외 메시지가 영어 하드코딩(`Unknown ...`, `Reschedule failed`, `Job not found ...`) 상태
  - 프론트는 다수 화면에서 catch 시 고정 `toast.error("...실패")`만 사용하며 `error.response.data.errorMessage`를 공통 처리하지 않음
  - `ErrorResponse` 구조는 `errorCode`, `errorMessage`를 제공하고 있어 프론트 연동 여지는 확보됨
- 문제 원인
  - 예외 메시지 상수화 범위가 제한되어 파일별 문자열 직접 throw가 남음
  - 프론트 공통 에러 추출 유틸 부재로 화면별 중복·누락 처리 발생

## 3. 변경 예정 파일 목록
- 백엔드
  - `src/main/java/com/moneylog_backend/global/constant/ErrorMessageConstants.java`
  - `src/main/java/com/moneylog_backend/global/type/StatusEnum.java`
  - `src/main/java/com/moneylog_backend/global/type/ScheduleEnum.java`
  - `src/main/java/com/moneylog_backend/global/type/RoleEnum.java`
  - `src/main/java/com/moneylog_backend/global/type/ProviderEnum.java`
  - `src/main/java/com/moneylog_backend/global/type/PaymentEnum.java`
  - `src/main/java/com/moneylog_backend/global/type/CategoryEnum.java`
  - `src/main/java/com/moneylog_backend/global/type/AccountTypeEnum.java`
  - `src/main/java/com/moneylog_backend/moneylog/schedule/service/ScheduleService.java`
  - `src/main/java/com/moneylog_backend/global/exception/GlobalExceptionHandler.java` (필요 시 권한 오류 메시지 표준화)
- 프론트
  - `src/moneylog/src/utils/error.ts` (신규)
  - `src/moneylog/src/Pages/FinancePage.tsx`
  - `src/moneylog/src/Pages/LoginPage.tsx`
  - `src/moneylog/src/Pages/SignUpPage.tsx`
  - `src/moneylog/src/components/ScheduleDialog.tsx`
  - `src/moneylog/src/api/axiosConfig.js` (필요 시 전역 처리 보완)
- 로그
  - `docs/CHANGELOG_2026-02-15.md`

## 4. 변경 전략
- 1차(백엔드): `ErrorMessageConstants`에 도메인별 예외 메시지(잘못된 enum/스케줄 실패/스케줄 미존재)를 추가하고, 관련 throw 구문을 상수 참조로 교체
- 2차(프론트): Axios 에러에서 `response.data.errorMessage`를 우선 추출하는 공통 함수(`getApiErrorMessage`)를 추가
- 3차(프론트 적용): 주요 실패 토스트/폼 에러 처리 지점에서 공통 함수를 사용해 서버 메시지를 우선 노출하고, 없으면 기존 기본 문구 fallback
- 4차(정리): 중복 하드코딩 에러 메시지 최소화 및 타입 안전성 보강
- 대안 비교
  - 대안 A: 페이지별 개별 수정만 수행 (빠르지만 누락 가능성 높음)
  - 대안 B: 공통 유틸 + 주요 페이지 적용 (일관성 높고 유지보수 용이) -> 채택

## 5. 예상 영향 범위
- 백엔드 API 에러 응답 메시지 텍스트 변경
- 프론트 에러 토스트/폼 에러 문구 표시 방식 변경
- 인증/DB 스키마/외부 API/빌드 설정 영향 없음

## 6. 리스크 분석
- 실패 가능성
  - 프론트가 서버 메시지를 그대로 노출하면서 UX 톤이 화면별로 다르게 보일 가능성
  - Axios 에러 객체 형태 불일치(네트워크 에러) 시 파싱 누락 가능성
- 완화 방안
  - 공통 유틸에서 네트워크/미정의 케이스 fallback 기본 문구 제공
  - 기존 고정 문구를 fallback으로 유지
- 롤백 방법
  - 상수화/유틸 적용 파일 단위 원복으로 즉시 복구 가능

## 7. 테스트 계획
- 승인 후 실행 예정
  - `./gradlew` (최소)
  - 가능 시 `./gradlew test`
- 프론트 수동 검증
  - 잘못된 입력/권한/리소스 없음 상황에서 백엔드 `errorMessage`가 토스트/폼에 노출되는지 확인
  - 네트워크 실패 시 fallback 문구 노출 확인
- 새로운 테스트 추가 여부
  - 이번 범위에서는 우선 미추가, 필요 시 프론트 유틸 단위 테스트 추가 검토

### 승인 여부
- 승인 완료 (사용자 응답: "진행해")
- 승인 시간: 13:34

## [TIME] 13:34 (KST) — [START] 예외 메시지 상수 통일 및 프론트 에러 처리 개선 구현 시작

### PRE-FLIGHT
- 현재 브랜치: `master`
- 기준 브랜치 확인: `master` 기준 작업 진행
- 현재 워크트리: 기존 변경 다수 존재, 요청 범위 파일만 최소 수정

## [TIME] 13:38 (KST) — [IMPLEMENT] 예외 메시지 상수 통일 및 프론트 에러 메시지 연동 적용

### 수행 내용
- 백엔드 예외 메시지 상수 통일
  - `src/main/java/com/moneylog_backend/global/constant/ErrorMessageConstants.java`
    - 공통 예외 메시지 상수 확장 (`ACCESS_DENIED`, `INVALID_JSON`, `LOGIN_FAILED`, `INTERNAL_SERVER_ERROR` 등)
    - 반복되는 enum 변환 오류 문구를 메서드 기반(`unknownStatus`, `unknownFrequency` 등)으로 통일
    - 스케줄 전용 메시지(`scheduleJobNotFound`, `SCHEDULE_RESCHEDULE_FAILED`) 추가
- 백엔드 하드코딩 예외 문자열 제거
  - `src/main/java/com/moneylog_backend/global/type/StatusEnum.java`
  - `src/main/java/com/moneylog_backend/global/type/ScheduleEnum.java`
  - `src/main/java/com/moneylog_backend/global/type/RoleEnum.java`
  - `src/main/java/com/moneylog_backend/global/type/ProviderEnum.java`
  - `src/main/java/com/moneylog_backend/global/type/PaymentEnum.java`
  - `src/main/java/com/moneylog_backend/global/type/CategoryEnum.java`
  - `src/main/java/com/moneylog_backend/global/type/AccountTypeEnum.java`
  - `src/main/java/com/moneylog_backend/moneylog/schedule/service/ScheduleService.java`
  - 위 파일들의 영어 throw 메시지를 상수 참조 한국어 메시지로 교체
- 전역 예외 핸들러 메시지 상수 연동
  - `src/main/java/com/moneylog_backend/global/exception/GlobalExceptionHandler.java`
  - 권한/JSON/로그인실패/내부오류 메시지를 상수 참조로 통일
- 프론트 공통 에러 파서 도입 및 적용
  - 신규: `src/moneylog/src/utils/error.ts`
    - `getApiErrorMessage(error, fallback)` 추가
    - 우선순위: `response.data.errorMessage` -> `response.data.message` -> 문자열 응답 -> fallback
  - 적용 파일
    - `src/moneylog/src/Pages/FinancePage.tsx`
    - `src/moneylog/src/Pages/LoginPage.tsx`
    - `src/moneylog/src/Pages/SignUpPage.tsx`
    - `src/moneylog/src/components/ScheduleDialog.tsx`
  - 기존 고정 실패 문구는 fallback으로 유지하여 네트워크/비표준 에러에서도 사용자 메시지 보장

### 영향 범위
- API 실패 시 사용자에게 표시되는 메시지 텍스트
- 예외 처리 흐름(HTTP status, 에러 코드)은 기존과 동일

### VERIFY
- 루트에서 `./gradlew` 실행
  - 결과: 성공 (`BUILD SUCCESSFUL`)
- 루트에서 `./gradlew test` 실행
  - 결과: 실패
  - 실패 핵심 로그
    - `MoneyLogApplicationTests > contextLoads() FAILED`
    - `IllegalStateException -> BeanCreationException -> ServiceException -> HibernateException (DialectFactoryImpl.java:191)`
  - 재현 방법: 프로젝트 루트에서 `./gradlew test`
- 프론트에서 `npm run build` 실행 (`src/moneylog`)
  - 결과: 실패
  - 실패 핵심 로그
    - `sh: 1: vite: not found`
  - 재현 방법: `src/moneylog` 디렉토리에서 `npm run build`
  - 원인 추정: 프론트 실행 의존성(vite) 미설치/실행 환경 미구성

# 📊 계획 대비 수행 결과 비교

## 계획 대비 차이
- 계획상 후보였던 `axiosConfig.js` 전역 인터셉터 변경은 이번에 적용하지 않음
- 대신 화면 레벨(`FinancePage`, `LoginPage`, `SignUpPage`, `ScheduleDialog`)에 공통 에러 파서를 우선 적용해 즉시 사용자 노출 개선

## 추가 발생 이슈
- 백엔드 테스트는 기존 환경 이슈(`contextLoads` + Hibernate Dialect 초기화 실패)로 실패
- 프론트 빌드는 `vite` 실행 파일 부재로 실패

## 계획 외 수정 사항
- `GlobalExceptionHandler`의 일부 기존 한글 하드코딩 메시지도 상수 참조로 교체하여 단일 관리 원칙을 강화

## [TIME] 13:53 (KST) — [PLAN] ResponseStatusException 표준 응답화로 회원가입 409 메시지 회귀 근본 해결

### 실행 계획
# 🧠 실행 계획 보고

## 0. 이동할 브랜치
- 현재 브랜치 유지: `refactor/error-message`
- 사용자 요청에 따라 브랜치 이동/생성 없이 현재 브랜치에서 작업

## 1. 작업 목표
- `ResponseStatusException`이 발생해도 프론트에서 항상 `errorMessage`를 안정적으로 수신하도록 백엔드 예외 처리 구조를 개선
- 회원가입 409(CONFLICT)에서 중복 아이디/이메일 안내가 누락되지 않도록 보장

## 2. 현재 상태 분석
- 관련 파일
  - `src/main/java/com/moneylog_backend/global/exception/GlobalExceptionHandler.java`
  - `src/main/java/com/moneylog_backend/moneylog/user/service/UserService.java`
  - `src/main/java/com/moneylog_backend/global/constant/ErrorMessageConstants.java`
- 현재 로직 요약
  - `UserService.checkIdOrEmailValidity`는 `ResponseStatusException(HttpStatus.CONFLICT, "...")`를 발생시킴
  - `GlobalExceptionHandler`에 `ResponseStatusException` 전용 핸들러가 없어 기본 스프링 에러 포맷으로 빠질 수 있음
  - 이 경우 프론트 공통 파서가 `errorMessage`를 못 받아 fallback 문구로 하락할 가능성 존재
- 문제 원인
  - 예외 타입별 응답 표준화가 `IllegalArgumentException`, `ResourceNotFoundException` 중심으로만 구성됨

## 3. 변경 예정 파일 목록
- `src/main/java/com/moneylog_backend/global/exception/GlobalExceptionHandler.java`
- `src/main/java/com/moneylog_backend/global/constant/ErrorMessageConstants.java`
- `src/main/java/com/moneylog_backend/moneylog/user/service/UserService.java`
- `docs/CHANGELOG_2026-02-15.md`

## 4. 변경 전략
- 1차: `GlobalExceptionHandler`에 `ResponseStatusException` 핸들러 추가
  - `status`/`reason`을 기반으로 `ErrorResponse(errorCode, errorMessage)`를 반환
  - `reason`이 비어 있으면 상태코드별 기본 메시지 fallback 적용
- 2차: 회원가입 중복 문구를 `ErrorMessageConstants`로 상수화하고 `UserService`에서 참조
- 3차: `./gradlew` 실행으로 컴파일/기본 검증

## 5. 예상 영향 범위
- 영향 있음: `ResponseStatusException` 기반 API의 에러 응답 포맷(표준화)
- 영향 없음: DB 스키마, 인증 플로우, 외부 API, 빌드 설정

## 6. 리스크 분석
- 실패 가능성
  - 기존에 스프링 기본 에러 바디를 직접 기대하던 클라이언트가 있다면 응답 포맷 변화 영향 가능
- 롤백 방법
  - `GlobalExceptionHandler`의 신규 핸들러 및 상수/참조 변경 파일 단위 원복 가능

## 7. 테스트 계획
- `./gradlew` 실행
- 가능 시 `./gradlew test` 실행
- 수동 검증 포인트: `/user/signup` 중복 ID/이메일 요청 시 409 + `errorMessage` 반환 확인

### 승인 여부
- 승인 완료 (사용자 응답: "백엔드 구조를 개선해서 근본적으로 문제를 해결하자. 브랜치를 유지하고 작업 시작해줘.")
- 승인 시간: 13:53

## [TIME] 13:53 (KST) — [START] ResponseStatusException 표준 응답화 구현 시작

## [TIME] 13:55 (KST) — [IMPLEMENT] ResponseStatusException 표준 에러 응답 구조 적용

### 수행 내용
- `GlobalExceptionHandler`에 `ResponseStatusException` 전용 핸들러 추가
  - 파일: `src/main/java/com/moneylog_backend/global/exception/GlobalExceptionHandler.java`
  - 처리 방식:
    - `statusCode`를 `errorCode`로 변환 (`CONFLICT`, `BAD_REQUEST` 등)
    - `reason`이 존재하면 `errorMessage`로 그대로 반환
    - `reason`이 비어 있으면 상태코드별 기본 메시지 fallback 제공
  - 결과: 스프링 기본 에러 바디 의존 없이 항상 `ErrorResponse(errorCode, errorMessage)` 포맷 보장
- 상태코드 fallback/중복가입 문구 상수화
  - 파일: `src/main/java/com/moneylog_backend/global/constant/ErrorMessageConstants.java`
  - 추가 상수: `DUPLICATE_LOGIN_ID`, `DUPLICATE_EMAIL`, `BAD_REQUEST`, `CONFLICT`, `NOT_FOUND`
- 회원가입 중복 예외 메시지 상수 참조로 통일
  - 파일: `src/main/java/com/moneylog_backend/moneylog/user/service/UserService.java`
  - 변경: `ResponseStatusException(HttpStatus.CONFLICT, ErrorMessageConstants.DUPLICATE_...)`

### 영향 범위
- `/user/signup`의 409 포함 `ResponseStatusException` 경로 전반
- 프론트는 기존 공통 파서(`errorMessage`)로 충돌 사유 메시지 수신 가능

### VERIFY
- 루트에서 `./gradlew` 실행
  - 결과: 성공 (`BUILD SUCCESSFUL`)
- 루트에서 `./gradlew test` 실행
  - 결과: 실패
  - 실패 핵심 로그
    - `MoneyLogApplicationTests > contextLoads() FAILED`
    - `IllegalStateException -> BeanCreationException -> ServiceException -> HibernateException (DialectFactoryImpl.java:191)`
  - 재현 방법: 프로젝트 루트에서 `./gradlew test`

# 📊 계획 대비 수행 결과 비교

## 계획 대비 차이
- 계획한 파일 범위 내에서 동일 전략으로 수행, 추가적인 아키텍처 변경 없음

## 추가 발생 이슈
- 기존과 동일한 테스트 환경 이슈(`contextLoads`/Dialect 초기화 실패)로 `./gradlew test` 실패

## 계획 외 수정 사항
- 없음

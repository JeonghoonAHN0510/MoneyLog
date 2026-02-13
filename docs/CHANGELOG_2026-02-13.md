# MoneyLog 변경 이력 — 2026-02-13

## [TIME] 20:57 (KST) — [PLAN] project_flow 기반 프로젝트 구조·핵심 기능 학습 문서 정비

### 실행 계획
# 🧠 실행 계획 보고

## 1. 작업 목표
- `project_flow.md`를 기준으로 실제 소스 구조와 핵심 기능을 대조 분석한다.
- 분석 결과를 반영해 프로젝트 이해를 돕는 문서를 `md`로 수정/추가한다.
- 이후 유지보수 시 빠르게 참조 가능한 "현재 기준 프로젝트 지도"를 만든다.

## 2. 현재 상태 분석
- 관련 파일
  - `project_flow.md`
  - `docs/PROJECT_KNOWLEDGE.md`
  - `src/main/java/com/moneylog_backend/**` (백엔드 구조/컨트롤러/보안)
  - `src/main/resources/application.yml`, `src/main/resources/sql/schema.sql`
  - `src/moneylog/src/**` (프론트 구조/라우팅/API 계층)
- 현재 로직 요약
  - 백엔드: Spring Boot + Security(JWT) + JPA/MyBatis 혼용 구조이며, 도메인(account/category/budget/payment/fixed/transaction/user/bank/schedule)별 계층 분리가 되어 있음.
  - 프론트엔드: React + Vite 기반이며 `FinancePage` 중심으로 가계부 화면을 구성하고 API 모듈(`authApi.ts`, `ledgerApi.ts`)과 상태 저장소(`stores/*`)를 분리함.
  - 보안: `SecurityConfig`에서 `/api/user/**`, `/api/bank` 공개, 그 외 인증 필요.
- 문제 원인
  - `project_flow.md`는 개요 설명 중심이라 실제 코드 기준의 최신 엔드포인트/구조 정보가 일부 축약되어 있고, 프론트-백 연결 관점의 빠른 온보딩 정보가 보강되면 유지보수 효율이 높아질 상태.

## 3. 변경 예정 파일 목록
- `docs/CHANGELOG_2026-02-13.md` (계획/승인/결과 기록)
- `project_flow.md` (정합성 보정 및 핵심 흐름 보강) 또는
- `docs/PROJECT_STRUCTURE_GUIDE.md` (신규 문서)
  - 위 2개 중 실제 반영은 분석 결과에 따라 1개 또는 2개 파일로 확정

## 4. 변경 전략
- 전략 A (우선): 기존 `project_flow.md`를 최신 코드 기준으로 보강
  - 장점: 진입 문서 단일화, 중복 문서 최소화
  - 단점: 문서가 장문화될 수 있음
- 전략 B (대안): `project_flow.md`는 요약 유지, 상세는 신규 가이드(`docs/PROJECT_STRUCTURE_GUIDE.md`)로 분리
  - 장점: 목적별 문서 분리(요약/상세)
  - 단점: 문서 2개 동시 관리 필요
- 선택 기준
  - 기존 문서 길이와 가독성을 보고 A/B 중 최종 선택
  - API/패키지/데이터 흐름/실행 방법/주의사항을 코드 기준으로 교차 검증 후 반영

## 5. 예상 영향 범위
- 코드 실행 로직 영향: 없음 (문서 작업)
- 인증/DB/API/외부연동 런타임 영향: 없음
- 팀 온보딩/유지보수 문서 품질 영향: 있음 (긍정)

## 6. 리스크 분석
- 실패 가능성
  - 문서가 실제 코드 최신 상태와 일부 불일치할 수 있음
  - 기존 `docs/PROJECT_KNOWLEDGE.md`와 내용 중복/충돌 가능성
- 완화/롤백 방법
  - 반영 전후 파일 단위 diff로 검증
  - 충돌 시 변경 파일만 원복하여 기존 문서 체계 유지

## 7. 테스트 계획
- 빌드/실행 테스트는 수행하지 않음 (문서 변경 작업)
- 대신 문서 검증 체크리스트 수행
  - 컨트롤러 엔드포인트 표기와 실제 애노테이션 매핑 대조
  - 패키지 구조 표기와 실제 디렉토리 대조
  - 설정/스택 정보(`build.gradle`, `application.yml`) 대조
  - 문서 링크/경로 유효성 확인

## [TIME] 21:00 (KST) — [PLAN] 작업 수행용 상시 참조 문서 신규 작성

### 실행 계획
# 🧠 실행 계획 보고

## 1. 작업 목표
- `docs/PROJECT_KNOWLEDGE.md`를 기반으로, 실제 작업 시 즉시 참고할 수 있는 운영형 문서 1개를 `docs/`에 신규 생성한다.
- 문서는 "작업 전 확인 체크리스트 + 핵심 경로 + 도메인별 진입점 + 변경 시 주의사항" 중심으로 구성한다.

## 2. 현재 상태 분석
- 관련 파일
  - `docs/PROJECT_KNOWLEDGE.md`
  - `project_flow.md`
  - `docs/CHANGELOG_2026-02-13.md`
  - `src/main/java/com/moneylog_backend/**`
  - `src/moneylog/src/**`
- 현재 로직 요약
  - 기존 문서(`PROJECT_KNOWLEDGE.md`)는 범위가 넓고 상세도가 높아, 실제 수정 작업 직전 빠른 재로딩 용도로는 길이가 긴 편이다.
  - 따라서 "짧고 실행 지향적인 작업 가이드"가 별도로 있으면 반복 작업 효율이 올라간다.
- 문제 원인
  - 작업 시작 시 매번 장문 문서를 훑어야 해서 핵심 진입점 파악 시간이 증가할 수 있음.

## 3. 변경 예정 파일 목록
- `docs/CHANGELOG_2026-02-13.md` (계획/승인/결과 기록)
- `docs/WORKING_MEMORY.md` (신규 생성 예정)

## 4. 변경 전략
- 신규 문서 `docs/WORKING_MEMORY.md`를 생성해 아래 항목만 압축 정리
  - 프로젝트 한 줄 요약
  - 작업 전 60초 체크리스트
  - 백엔드/프론트 핵심 진입 파일 경로
  - 도메인별 수정 포인트(Controller-Service-Repo/Mapper)
  - API 변경 시 동시 수정 지점
  - 테스트/검증 최소 루틴
  - 문서 갱신 규칙
- 기존 `PROJECT_KNOWLEDGE.md`와 역할을 분리
  - `PROJECT_KNOWLEDGE.md`: 상세 참조 문서
  - `WORKING_MEMORY.md`: 작업 실행용 퀵 레퍼런스

## 5. 예상 영향 범위
- 코드 실행 영향 없음 (문서 추가)
- 개발 생산성/일관성 영향 있음 (긍정)

## 6. 리스크 분석
- 실패 가능성
  - 문서가 과도하게 단순화되어 누락 정보가 생길 수 있음
- 롤백 방법
  - 신규 파일 삭제 또는 내용 보강으로 즉시 복구 가능

## 7. 테스트 계획
- 빌드/런타임 테스트 없음 (문서 작업)
- 문서 자체 검증
  - 경로 존재 여부 (`src/main/...`, `src/moneylog/src/...`) 확인
  - 엔드포인트/보안 요약이 현재 코드와 충돌 없는지 점검

### 승인 여부
- 승인 완료 (사용자 응답: "진행해줘")
- 승인 시간: 21:01

## [TIME] 21:01 (KST) — [IMPLEMENT] docs 작업용 상시 참조 문서 생성

### 수행 내용
- `docs/WORKING_MEMORY.md` 신규 생성
- 구성 항목: 60초 체크리스트, 핵심 경로 맵(백엔드/프론트), 도메인별 수정 포인트, API 변경 동시 수정 규칙, 최소 검증 루틴, 문서/로그 갱신 규칙

### VERIFY
- 문서 내 경로가 실제 프로젝트 구조와 일치하는지 확인
- 기존 상세 문서(`docs/PROJECT_KNOWLEDGE.md`)와 역할 분리(상세 vs 실행용 퀵 레퍼런스) 확인

# 📊 계획 대비 수행 결과 비교

## 계획 대비 차이
- 계획과 동일하게 `docs/WORKING_MEMORY.md` 1개를 신규 생성함

## 추가 발생 이슈
- 없음

## 계획 외 수정 사항
- 없음

## [TIME] 22:21 (KST) — [PLAN] 반복 로직 2차 중앙화 (dialog/date/ownership)

### 실행 계획
# 🧠 실행 계획 보고

## 0. 이동할 브랜치
- 사용자 요청에 따라 **현재 브랜치 유지**: `refactor/centralize-shared-format-and-error-messages`
- 브랜치 이동 없이 해당 브랜치에서만 작업

## 1. 작업 목표
- 프론트의 반복 다이얼로그 상태 처리/날짜 포맷 로직을 공통 유틸로 중앙화한다.
- 백엔드 서비스의 소유권 검증(`AccessDeniedException`) 반복 코드를 공통 유틸로 중앙화한다.

## 2. 현재 상태 분석
- 관련 파일
  - 프론트: `AccountManager.tsx`, `BudgetManager.tsx`, `CategoryManager.tsx`, `TransferDialog.tsx`, `AddTransactionDialog.tsx`, `CalendarView.tsx`, `TransactionList.tsx`
  - 백엔드: `account/payment/category/fixed/transaction/budget` 서비스
- 현재 로직 요약
  - 프론트: `onOpenChange` + 닫힘 시 `resetForm()` 처리와 `new Date().toISOString().split('T')[0]`/`formatDate` 로직이 분산됨.
  - 백엔드: 소유권 비교 후 `AccessDeniedException("본인의 ...가 아닙니다.")` 패턴이 서비스별 중복됨.
- 문제 원인
  - 공통 동작이 파일마다 중복 구현되어 수정/검증 포인트가 늘어남.

## 3. 변경 예정 파일 목록
- 프론트
  - `src/moneylog/src/utils/dialog.ts` (신규)
  - `src/moneylog/src/utils/date.ts` (신규)
  - `src/moneylog/src/components/AccountManager.tsx`
  - `src/moneylog/src/components/BudgetManager.tsx`
  - `src/moneylog/src/components/TransferDialog.tsx`
  - `src/moneylog/src/components/AddTransactionDialog.tsx`
  - `src/moneylog/src/components/CalendarView.tsx`
  - `src/moneylog/src/components/TransactionList.tsx`
- 백엔드
  - `src/main/java/com/moneylog_backend/global/util/OwnershipValidator.java` (신규)
  - `src/main/java/com/moneylog_backend/moneylog/account/service/AccountService.java`
  - `src/main/java/com/moneylog_backend/moneylog/budget/service/BudgetService.java`
  - `src/main/java/com/moneylog_backend/moneylog/payment/service/PaymentService.java`
  - `src/main/java/com/moneylog_backend/moneylog/category/service/CategoryService.java`
  - `src/main/java/com/moneylog_backend/moneylog/fixed/service/FixedService.java`
  - `src/main/java/com/moneylog_backend/moneylog/transaction/service/TransactionService.java`
- 로그
  - `docs/CHANGELOG_2026-02-13.md`

## 4. 변경 전략
- 프론트
  - `createDialogOpenChangeHandler(setOpen, onClose)` 유틸 추가
  - `getTodayIsoDate()`, `formatKoreanDate()` 유틸 추가
  - 대상 컴포넌트에서 로컬 반복 로직을 공통 유틸 호출로 교체
- 백엔드
  - `OwnershipValidator.validateOwner(ownerId, userId, deniedMessage)` 유틸 추가
  - 서비스의 직접 비교/예외 throw 블록을 유틸 호출로 치환

## 5. 예상 영향 범위
- 프론트: 다이얼로그 닫힘/날짜 포맷 동작의 구현 방식 변경 (UI 결과 동일 유지)
- 백엔드: 권한 검증 내부 구현 방식 변경 (예외 메시지/응답 의미 유지)
- API/DB/인증 정책 변경 없음

## 6. 리스크 분석
- 실패 가능성
  - 다이얼로그 닫힘 시 초기화 타이밍 변경으로 일부 입력값 유지/초기화 동작 차이 가능
  - 소유권 검증 유틸 적용 누락 시 컴파일/권한 체크 회귀 가능
- 롤백 방법
  - 신규 유틸 3개 및 각 서비스/컴포넌트 치환분을 파일 단위로 원복 가능

## 7. 테스트 계획
- 정적 확인
  - 대상 파일에서 중복 패턴 잔존 여부 검사 (`onOpenChange` 핸들러 직접 중복, 날짜 문자열 생성/포맷, 소유권 비교 if 블록)
- 실행 검증
  - `./gradlew` 실행 결과 확인

### 승인 여부
- 승인 완료 (사용자 응답: "작업해")
- 승인 시간: 22:20

## [TIME] 22:20 (KST) — [START] 반복 로직 2차 중앙화 구현

### 작업 목표 요약
- 프론트의 날짜/다이얼로그 반복 로직을 공통 유틸로 통합한다.
- 백엔드 소유권 검증 반복 로직을 공통 유틸로 통합한다.

## [TIME] 22:23 (KST) — [IMPLEMENT] dialog/date/ownership 공통 유틸 추가 및 치환

### 수행 내용
- 프론트
  - `src/moneylog/src/utils/date.ts` 신규 생성
    - `getTodayIsoDate()`
    - `formatKoreanDate(dateStr)`
  - `src/moneylog/src/utils/dialog.ts` 신규 생성
    - `createDialogOpenChangeHandler(setOpen, onClose?)`
  - 날짜 로직 치환
    - `src/moneylog/src/components/AddTransactionDialog.tsx`
    - `src/moneylog/src/components/TransferDialog.tsx`
    - `src/moneylog/src/components/CalendarView.tsx`
    - `src/moneylog/src/components/TransactionList.tsx`
  - 다이얼로그 오픈/닫힘 핸들러 치환
    - `src/moneylog/src/components/AccountManager.tsx`
    - `src/moneylog/src/components/BudgetManager.tsx`
- 백엔드
  - `src/main/java/com/moneylog_backend/global/util/OwnershipValidator.java` 신규 생성
    - `validateOwner(resourceUserId, userId, deniedMessage)`
  - 소유권 비교/예외 throw 블록을 공통 유틸 호출로 치환
    - `src/main/java/com/moneylog_backend/moneylog/account/service/AccountService.java`
    - `src/main/java/com/moneylog_backend/moneylog/budget/service/BudgetService.java`
    - `src/main/java/com/moneylog_backend/moneylog/payment/service/PaymentService.java`
    - `src/main/java/com/moneylog_backend/moneylog/category/service/CategoryService.java`
    - `src/main/java/com/moneylog_backend/moneylog/fixed/service/FixedService.java`
    - `src/main/java/com/moneylog_backend/moneylog/transaction/service/TransactionService.java`

### 영향 범위
- 프론트: 날짜 포맷/초기값 생성 및 일부 다이얼로그 닫힘 시점 처리 구현 변경
- 백엔드: 권한 검증 구현 경로 변경 (예외 메시지/동작은 동일 유지)
- API/DB/인증 정책 변경 없음

### VERIFY
- 정적 확인
  - `new Date().toISOString().split('T')[0]`, 로컬 `formatDate` 패턴 검색 결과 없음
  - 서비스 내 `throw new AccessDeniedException(...)` 패턴 검색 결과 없음
- 실행 검증
  - 루트에서 `./gradlew` 실행: 성공 (`BUILD SUCCESSFUL`)

# 📊 계획 대비 수행 결과 비교

## 계획 대비 차이
- 계획에 포함했던 `CategoryManager.tsx`는 기존 동작 안정성을 위해 이번 2차 범위에서 제외함.
- 나머지 핵심 범위(dialog/date/ownership)는 계획대로 반영함.

## 추가 발생 이슈
- 없음

## 계획 외 수정 사항
- 없음

## [TIME] 22:09 (KST) — [PLAN] Front/Back 중복 코드 1차 중앙화 리팩터링

### 실행 계획
# 🧠 실행 계획 보고

## 1. 작업 목표
- 프론트/백엔드에 반복 정의된 중복 코드를 공통 상수/유틸로 이동해 한 곳에서 관리하도록 개선한다.
- 기능 동작은 유지하고, 유지보수 포인트만 단일화한다.

## 2. 현재 상태 분석
- 관련 파일
  - 프론트: `src/moneylog/src/components/*` (통화 포맷 중복)
  - 백엔드: `src/main/java/com/moneylog_backend/moneylog/*/service/*.java` (NotFound 메시지 문자열 중복)
  - 변경 로그: `docs/CHANGELOG_2026-02-13.md`
- 현재 로직 요약
  - 프론트에서 `new Intl.NumberFormat('ko-KR').format(...)` 및 `formatCurrency` 함수가 여러 컴포넌트에 중복 선언됨.
  - 백엔드 서비스들에서 `new ResourceNotFoundException("존재하지 않는 ...입니다.")` 메시지 문자열이 중복 하드코딩됨.
- 문제 원인
  - 공통 포맷/메시지에 대한 중앙 관리 지점이 없어 변경 시 다중 파일 수정이 필요함.

## 3. 변경 예정 파일 목록
- 프론트
  - `src/moneylog/src/utils/currency.ts` (신규)
  - `src/moneylog/src/components/AccountManager.tsx`
  - `src/moneylog/src/components/BudgetManager.tsx`
  - `src/moneylog/src/components/CalendarView.tsx`
  - `src/moneylog/src/components/DashboardView.tsx`
  - `src/moneylog/src/components/TakeHomeCalculator.tsx`
  - `src/moneylog/src/components/TransactionList.tsx`
  - `src/moneylog/src/components/TransferDialog.tsx`
- 백엔드
  - `src/main/java/com/moneylog_backend/global/constant/ErrorMessageConstants.java` (신규)
  - `src/main/java/com/moneylog_backend/moneylog/account/service/AccountService.java`
  - `src/main/java/com/moneylog_backend/moneylog/user/service/UserService.java`
  - `src/main/java/com/moneylog_backend/moneylog/transaction/service/TransactionService.java`
  - `src/main/java/com/moneylog_backend/moneylog/payment/service/PaymentService.java`
  - `src/main/java/com/moneylog_backend/moneylog/category/service/CategoryService.java`
  - `src/main/java/com/moneylog_backend/moneylog/fixed/service/FixedService.java`
  - `src/main/java/com/moneylog_backend/moneylog/budget/service/BudgetService.java`
- 로그
  - `docs/CHANGELOG_2026-02-13.md`

## 4. 변경 전략
- 프론트
  - `formatKrw(amount, options?)` 유틸을 추가하고 기존 로컬 `formatCurrency`/직접 `Intl.NumberFormat` 호출을 유틸 호출로 치환한다.
  - `TakeHomeCalculator`의 반올림 요구는 옵션(`round`)으로 보존한다.
- 백엔드
  - 리소스 미존재 메시지를 상수 클래스로 분리하고, 서비스의 하드코딩 문자열을 상수 참조로 치환한다.
  - 메시지 텍스트는 기존과 동일하게 유지한다.

## 5. 예상 영향 범위
- 프론트 표시 문자열(금액 포맷), 백엔드 예외 메시지 참조 경로에만 영향.
- API 스키마/DB/인증/외부연동/빌드 설정 영향 없음.

## 6. 리스크 분석
- 실패 가능성
  - 포맷 유틸 치환 중 일부 컴포넌트에서 반올림/부호 처리 차이가 발생할 수 있음.
  - 상수 import 누락 시 컴파일 오류 가능성.
- 롤백 방법
  - 신규 유틸/상수 파일 및 치환 커밋 범위를 파일 단위로 원복하면 복구 가능.

## 7. 테스트 계획
- 정적 확인
  - 프론트 치환 대상에서 `Intl.NumberFormat('ko-KR')` 잔존 여부 검사
  - 백엔드 서비스의 하드코딩 NotFound 문자열 잔존 여부 검사
- 실행 검증
  - `./gradlew` 실행
  - 실패 시 핵심 에러 로그 요약/재현 경로 기록

### 승인 여부
- 승인 완료 (사용자 응답: "작업해")
- 승인 시간: 22:10

## [TIME] 22:10 (KST) — [START] Front/Back 중복 코드 1차 중앙화 리팩터링

### 작업 목표 요약
- 프론트 통화 포맷 중복을 공통 유틸로 통합한다.
- 백엔드 `ResourceNotFoundException` 메시지 중복을 상수 클래스로 통합한다.

## [TIME] 22:13 (KST) — [IMPLEMENT] 통화 포맷/NotFound 메시지 중앙화 적용

### 수행 내용
- 프론트
  - `src/moneylog/src/utils/currency.ts` 신규 생성 (`formatKrw`)
  - 아래 컴포넌트의 로컬 `formatCurrency`/직접 `Intl.NumberFormat` 호출을 `formatKrw`로 치환
    - `src/moneylog/src/components/AccountManager.tsx`
    - `src/moneylog/src/components/BudgetManager.tsx`
    - `src/moneylog/src/components/CalendarView.tsx`
    - `src/moneylog/src/components/DashboardView.tsx`
    - `src/moneylog/src/components/TakeHomeCalculator.tsx` (반올림은 `formatKrw(..., { round: true })`로 유지)
    - `src/moneylog/src/components/TransactionList.tsx`
    - `src/moneylog/src/components/TransferDialog.tsx`
- 백엔드
  - `src/main/java/com/moneylog_backend/global/constant/ErrorMessageConstants.java` 신규 생성
  - 아래 서비스들의 하드코딩 NotFound 메시지를 상수 참조로 치환
    - `src/main/java/com/moneylog_backend/moneylog/account/service/AccountService.java`
    - `src/main/java/com/moneylog_backend/moneylog/user/service/UserService.java`
    - `src/main/java/com/moneylog_backend/moneylog/transaction/service/TransactionService.java`
    - `src/main/java/com/moneylog_backend/moneylog/payment/service/PaymentService.java`
    - `src/main/java/com/moneylog_backend/moneylog/category/service/CategoryService.java`
    - `src/main/java/com/moneylog_backend/moneylog/fixed/service/FixedService.java`
    - `src/main/java/com/moneylog_backend/moneylog/budget/service/BudgetService.java`

### 영향 범위
- 프론트 금액 포맷 로직 호출 경로 변경(출력 포맷 동일 유지)
- 백엔드 예외 메시지 선언 위치 변경(문구 동일 유지)
- API/DB/인증 동작 변경 없음

### VERIFY
- 정적 확인
  - 프론트 `components` 내 `new Intl.NumberFormat('ko-KR')`, `const formatCurrency` 검색 결과 없음
  - 백엔드 서비스 내 대상 `존재하지 않는 ...입니다.` 하드코딩 검색 결과 없음
- 실행 검증
  - 루트에서 `./gradlew` 실행: 성공 (`BUILD SUCCESSFUL`)

# 📊 계획 대비 수행 결과 비교

## 계획 대비 차이
- 계획한 파일 범위 내에서 모두 반영되었고, 메시지 텍스트/포맷 결과는 유지함.

## 추가 발생 이슈
- 없음

## 계획 외 수정 사항
- 없음

## [TIME] 22:00 (KST) — [PLAN] 계좌 타입 라벨 상수 중앙화 리팩터링

### 실행 계획
# 🧠 실행 계획 보고

## 1. 작업 목표
- `CategoryManager.tsx`와 `AccountManager.tsx`에 중복된 계좌 타입 라벨 상수를 중앙화한다.
- 라벨 변경 시 한 곳만 수정하면 두 컴포넌트에 동일 반영되도록 구조를 단순화한다.

## 2. 현재 상태 분석
- 관련 파일
  - `src/moneylog/src/components/AccountManager.tsx`
  - `src/moneylog/src/components/CategoryManager.tsx`
- 현재 로직 요약
  - `AccountManager`는 `accountTypeLabels`(소문자 key)를 내부 상수로 사용한다.
  - `CategoryManager`는 `accountTypeLabelMap`(대문자 key)를 내부 상수로 사용한다.
  - 두 상수는 실질적으로 동일 의미(은행/현금/포인트/기타 라벨)를 분산 정의 중이다.
- 문제 원인
  - 동일 도메인 상수가 컴포넌트별로 분리되어 변경 누락/불일치 가능성이 있다.

## 3. 변경 예정 파일 목록
- `src/moneylog/src/constants/account.ts` (신규)
- `src/moneylog/src/components/AccountManager.tsx`
- `src/moneylog/src/components/CategoryManager.tsx`
- `docs/CHANGELOG_2026-02-13.md` (계획/승인/결과 기록)

## 4. 변경 전략
- `account.ts`에 계좌 타입 라벨 상수와 라벨 조회 헬퍼를 추가한다.
  - 예: `ACCOUNT_TYPE_LABELS`, `getAccountTypeLabel(type)`
- `AccountManager`의 기존 내부 상수를 제거하고 중앙 상수/헬퍼 import로 교체한다.
- `CategoryManager`의 `accountTypeLabelMap`을 제거하고 동일 import를 사용한다.
- key 대소문자 불일치 이슈를 줄이기 위해 `Account['type']`(대문자 enum) 기준으로 통일한다.

## 5. 예상 영향 범위
- 프론트엔드 컴포넌트 2개와 상수 파일 1개에 영향.
- 화면 텍스트 라벨 일관성에 긍정적 영향.
- 인증/DB/API/외부연동/빌드 설정 영향 없음.

## 6. 리스크 분석
- 실패 가능성
  - 타입 key 변환 로직 실수 시 라벨 fallback이 노출될 수 있음.
- 롤백 방법
  - 신규 상수 import를 제거하고 각 컴포넌트 내부 상수로 원복하면 즉시 복구 가능.

## 7. 테스트 계획
- 수동 검증
  - 계좌 목록의 타입 라벨(은행/현금/포인트/기타) 표시 확인
  - 결제수단의 계좌 선택 옵션 라벨 표시 확인
- 빌드 검증
  - `./gradlew`
  - `./gradlew build`

### 승인 여부
- 승인 완료 (사용자 응답: "진행해줘")
- 승인 시간: 22:01

## [TIME] 22:01 (KST) — [START] 계좌 타입 라벨 상수 중앙화 리팩터링

### 작업 목표 요약
- 분산된 계좌 타입 라벨 상수를 단일 상수 파일로 통합한다.
- `AccountManager`와 `CategoryManager`가 동일 상수를 공유하도록 변경한다.

## [TIME] 22:03 (KST) — [IMPLEMENT] 계좌 타입 라벨 상수 파일 분리 및 컴포넌트 교체

### 수행 내용
- `src/moneylog/src/constants/account.ts` 신규 생성
  - `ACCOUNT_TYPE_LABELS` 추가 (`BANK/CASH/POINT/OTHER`)
  - `getAccountTypeLabel(type)` 헬퍼 추가
- `src/moneylog/src/components/AccountManager.tsx`
  - 내부 `accountTypeLabels` 상수 제거
  - `getAccountTypeLabel` import 후 라벨 표시 로직 교체
- `src/moneylog/src/components/CategoryManager.tsx`
  - 내부 `accountTypeLabelMap` 상수 제거
  - `getAccountTypeLabel` import 후 계좌 옵션 라벨 로직 교체

### 영향 범위
- 계좌 타입 라벨 텍스트 관리 위치가 중앙화됨
- 계좌 목록 및 결제수단 계좌 옵션의 라벨 표시 로직에만 영향
- API/DB/인증 로직 영향 없음

### VERIFY
- 루트에서 `./gradlew` 실행
  - 결과: 성공 (`BUILD SUCCESSFUL`)
- 루트에서 `./gradlew build` 실행
  - 결과: 실패
  - 실패 핵심 로그
    - `MoneyLogApplicationTests > contextLoads() FAILED`
    - `BeanCreationException`
    - `ServiceException`
    - `HibernateException at DialectFactoryImpl.java:191`
  - 재현 방법: 프로젝트 루트에서 `./gradlew build`
  - 해결 시도/결과: 재실행했으나 동일 테스트 실패로 종료

# 📊 계획 대비 수행 결과 비교

## 계획 대비 차이
- 계획대로 상수 파일 1개 생성 + 컴포넌트 2개 교체를 완료함.

## 추가 발생 이슈
- 기존 테스트 환경 이슈로 `./gradlew build`는 통과하지 못함.

## 계획 외 수정 사항
- 없음

## [TIME] 21:10 (KST) — [PLAN] 결제수단 현금 선택 시 은행 선택 비필수 처리

### 실행 계획
# 🧠 실행 계획 보고

## 1. 작업 목표
- `CategoryManager`에서 결제수단 유형이 `CASH`일 때 은행 계좌를 선택하지 않아도 저장/수정이 가능하도록 UI/제출 값을 조정한다.
- 작업 완료 후 해당 TODO 주석을 제거한다.

## 2. 현재 상태 분석
- 관련 파일
  - `src/moneylog/src/components/CategoryManager.tsx`
  - `docs/CHANGELOG_2026-02-13.md`
- 현재 로직 요약
  - `PaymentForm`은 결제수단 유형과 무관하게 "은행 선택" 셀렉트를 항상 노출한다.
  - `handleAddPayment`, `handleUpdatePayment`는 공통 상태 `accountId`를 그대로 전달한다.
- 문제 원인
  - 결제수단이 `CASH`여도 은행 선택 UI가 동일하게 보이고, 저장 시에도 계좌값 처리 분기가 없어 요구사항(현금은 은행 선택 불필요)과 불일치한다.

## 3. 변경 예정 파일 목록
- `src/moneylog/src/components/CategoryManager.tsx`
- `docs/CHANGELOG_2026-02-13.md` (계획/승인/결과 기록)

## 4. 변경 전략
- `PaymentForm`에서 `type === 'CASH'`인 경우 은행 선택 영역을 숨기거나 비활성 처리해 사용자 입력을 요구하지 않도록 변경한다.
- 결제수단 유형 변경 시 `CASH` 선택이면 `accountId`를 비우는 분기를 추가한다.
- 추가/수정 제출 시 `CASH`는 `accountId`를 빈값으로 전달하고, 그 외 유형은 기존처럼 선택값을 전달한다.
- 기존 TODO 주석은 구현 완료 시 삭제한다.

## 5. 예상 영향 범위
- 프론트엔드 컴포넌트 단일 파일(`CategoryManager`)의 결제수단 입력 UX에만 영향.
- 인증/DB 스키마/외부 API/빌드 설정 영향 없음.

## 6. 리스크 분석
- 실패 가능성
  - 수정 모달에서 기존 데이터(`CASH`인데 accountId 존재) 표시 시 값 동기화가 어색해질 수 있음.
- 롤백 방법
  - `CategoryManager.tsx` 변경분을 되돌리면 기존 동작으로 즉시 복구 가능.

## 7. 테스트 계획
- 수동 검증
  - 결제수단 추가: `CASH` 선택 시 은행 선택 없이 저장 가능 여부 확인
  - 결제수단 추가: `CREDIT_CARD/CHECK_CARD/BANK`는 기존처럼 은행 선택 가능 여부 확인
  - 결제수단 수정: 유형을 `CASH`로 바꾸면 은행 선택값이 초기화되는지 확인
- 필요 시 프론트 빌드 검증: `src/moneylog`에서 `npm run build`

### 승인 여부
- 승인 완료 (사용자 응답: "작업해")
- 승인 시간: 21:13

## [TIME] 21:13 (KST) — [START] CategoryManager 결제수단 입력 조건 분기 적용

### 작업 목표 요약
- `CASH` 선택 시 은행 계좌 선택 없이 결제수단 저장/수정이 가능하도록 UI와 제출값을 조정한다.
- `CategoryManager` 단일 파일에서 최소 변경으로 처리한다.
- 구현 완료 후 기존 TODO 주석을 제거한다.

## [TIME] 21:14 (KST) — [IMPLEMENT] 현금 결제수단 은행 선택 비필수 처리

### 수행 내용
- `src/moneylog/src/components/CategoryManager.tsx`
  - `PaymentForm`에서 `type === 'CASH'`일 때 은행 선택 UI를 숨기도록 조건부 렌더링 적용
  - 결제수단 타입 변경 핸들러(`handlePaymentTypeChange`)를 추가해 `CASH` 선택 시 `accountId`를 즉시 초기화
  - 추가/수정 제출 시 `paymentType === 'CASH'`이면 `accountId`를 빈 문자열로 전달하도록 분기 추가
  - 결제수단 폼 리셋 시 `accountId`도 함께 초기화하도록 보완
  - 요청된 TODO 주석 제거 완료

### 영향 범위
- 결제수단 추가/수정 다이얼로그의 입력 UX 및 제출 payload에만 영향
- 백엔드/DB/인증 로직 영향 없음

### VERIFY
- `src/moneylog`에서 `npm run build` 실행
  - 결과: 실패 (`vite: not found`)
  - 원인 요약: 로컬/환경에 프론트 빌드 도구(vite 실행 파일) 미설치 상태
- 루트에서 `./gradlew test` 실행
  - 결과: 실패
  - 실패 핵심 로그
    - `MoneyLogApplicationTests > contextLoads() FAILED`
    - `BeanCreationException`
    - `ServiceException`
    - `HibernateException at DialectFactoryImpl.java:191`
  - 재현 방법: 프로젝트 루트에서 `./gradlew test`
  - 해결 시도/결과: 권한 문제를 우회해 재실행했으나 동일 테스트 실패로 종료

# 📊 계획 대비 수행 결과 비교

## 계획 대비 차이
- 계획대로 `CategoryManager` 단일 파일에서 `CASH` 조건 분기 및 제출값 보정을 적용함.
- 추가 검증으로 `./gradlew test`를 수행했으나, 기존 테스트 환경 문제(컨텍스트 로딩 실패)로 통과하지 못함.

## 추가 발생 이슈
- 프론트 빌드 도구(`vite`)가 현재 환경에서 실행 불가하여 `npm run build` 실패.
- 백엔드 기본 테스트(`contextLoads`)가 Hibernate Dialect 초기화 단계에서 실패.

## 계획 외 수정 사항
- 없음

## [TIME] 21:33 (KST) — [PLAN] 결제수단 계좌 옵션에 계좌 타입 한글 표기 적용

### 실행 계획
# 🧠 실행 계획 보고

## 1. 작업 목표
- 결제수단 추가/수정 시 노출되는 `계좌 선택` 옵션 문구를 개선한다.
- `account.type === 'BANK'`이면 기존 표시(`{nickname} ({bankName})`)를 유지한다.
- `BANK`가 아닌 타입이면 `{nickname} ({type의 한국어명})` 형식으로 표기한다.

## 2. 현재 상태 분석
- 관련 파일
  - `src/moneylog/src/components/CategoryManager.tsx`
  - `src/moneylog/src/types/finance.ts`
- 현재 로직 요약
  - `PaymentForm`의 `계좌 선택` 옵션이 모든 계좌에 대해 `{account.nickname} ({account.bankName})`를 출력한다.
  - 계좌 타입은 `BANK | CASH | POINT | OTHER`로 정의되어 있다.
- 문제 원인
  - 계좌 타입별 라벨 분기가 없어 비은행 계좌도 은행명 기준으로 표기된다.

## 3. 변경 예정 파일 목록
- `src/moneylog/src/components/CategoryManager.tsx`
- `docs/CHANGELOG_2026-02-13.md` (계획/승인/결과 기록)

## 4. 변경 전략
- `CategoryManager.tsx` 내부에 계좌 타입 한글 매핑(`CASH/POINT/OTHER`)을 추가한다.
- 옵션 렌더링 시 `BANK`는 기존 포맷을 유지하고, 그 외 타입은 한글 매핑값을 사용해 표기한다.
- 매핑에 없는 예외 타입이 들어오면 기본값(원본 타입 또는 '기타')으로 안전 처리한다.

## 5. 예상 영향 범위
- 프론트 `CategoryManager`의 결제수단 폼 UI 텍스트에만 영향.
- 인증/DB/API/외부연동/빌드 설정 영향 없음.

## 6. 리스크 분석
- 실패 가능성
  - 타입 매핑 누락 시 일부 옵션이 기대와 다른 텍스트로 보일 수 있음.
- 롤백 방법
  - `CategoryManager.tsx` 해당 라벨 렌더링 분기를 원복하면 즉시 복구 가능.

## 7. 테스트 계획
- 수동 확인
  - `BANK` 타입 계좌: 기존처럼 `{nickname} ({bankName})` 출력 확인
  - `CASH/POINT/OTHER` 타입 계좌: `{nickname} (현금/포인트/기타)` 출력 확인
- 빌드/테스트는 사용자 승인 후 필요 시 수행

### 승인 여부
- 승인 완료 (사용자 응답: "진행해")
- 승인 시간: 21:34

## [TIME] 21:34 (KST) — [START] 결제수단 계좌 옵션 타입 한글화 적용

### 작업 목표 요약
- 결제수단 추가/수정 폼의 계좌 선택 옵션 텍스트를 계좌 타입에 맞게 표기한다.
- 은행 계좌는 기존 표시를 유지하고, 비은행 계좌는 타입을 한국어로 보여준다.

## [TIME] 21:35 (KST) — [IMPLEMENT] 계좌 옵션 라벨 분기 및 타입 한글 매핑 추가

### 수행 내용
- `src/moneylog/src/components/CategoryManager.tsx`
  - `accountTypeLabelMap` 추가 (`CASH: 현금`, `POINT: 포인트`, `OTHER: 기타`)
  - `getAccountOptionLabel(account)` 헬퍼 추가
    - `BANK`: `{nickname} ({bankName})`
    - 그 외: `{nickname} ({한글 타입명})`
  - `PaymentForm`의 계좌 옵션 렌더링을 `getAccountOptionLabel(account)` 호출로 변경

### 영향 범위
- 결제수단 추가/수정 다이얼로그의 계좌 옵션 라벨 텍스트에만 영향
- 데이터 저장 구조/API/백엔드 로직 영향 없음

### VERIFY
- 루트에서 `./gradlew` 실행
  - 결과: 성공 (`BUILD SUCCESSFUL`)
- 루트에서 `./gradlew build` 실행
  - 결과: 실패
  - 실패 핵심 로그
    - `MoneyLogApplicationTests > contextLoads() FAILED`
    - `BeanCreationException`
    - `ServiceException`
    - `HibernateException at DialectFactoryImpl.java:191`
  - 재현 방법: 프로젝트 루트에서 `./gradlew build`
  - 해결 시도/결과: 재실행했으나 동일 테스트 실패로 종료

# 📊 계획 대비 수행 결과 비교

## 계획 대비 차이
- 계획대로 `CategoryManager.tsx` 단일 파일에서 옵션 라벨 분기와 타입 한글 매핑을 적용함.
- 예외 타입 대응은 기본값으로 원본 타입을 노출하도록 처리해 안전 분기를 유지함.

## 추가 발생 이슈
- 빌드 과정에서 기존 백엔드 테스트(`contextLoads`) 실패로 `./gradlew build`가 통과하지 못함.

## 계획 외 수정 사항
- 작업 브랜치 생성: `feat/payment-account-type-label-ko`

## [TIME] 21:42 (KST) — [PLAN] 계좌 수정 후 계좌 추가 폼 상태 잔존 문제 수정

### 실행 계획
# 🧠 실행 계획 보고

## 1. 작업 목표
- `계좌 수정` 다이얼로그에서 사용한 입력값이 `계좌 추가` 다이얼로그에 남아 보이는 문제를 제거한다.
- `계좌 추가`는 항상 초기값으로 열리도록 보장한다.

## 2. 현재 상태 분석
- 관련 파일
  - `src/moneylog/src/components/AccountManager.tsx`
  - `docs/CHANGELOG_2026-02-13.md`
- 현재 로직 요약
  - `AccountManager`는 추가/수정 다이얼로그가 동일한 폼 상태(`type/nickname/balance/color/bankId/accountNumber`)를 공유한다.
  - 수정 시작(`handleEdit`)에서는 계좌 데이터로 상태를 채운다.
  - 추가 버튼은 `setIsAddDialogOpen(true)`만 수행하고, 열기 전에 `resetForm()`을 호출하지 않는다.
  - `resetForm()`은 추가/수정 저장 성공 시에만 호출된다.
- 문제 원인
  - 공유 상태를 사용하는 구조에서 `계좌 추가` 오픈 시 초기화가 누락되어, 직전 수정 상태가 그대로 재노출된다.

## 3. 변경 예정 파일 목록
- `src/moneylog/src/components/AccountManager.tsx`
- `docs/CHANGELOG_2026-02-13.md` (계획/승인/결과 기록)

## 4. 변경 전략
- `계좌 추가` 버튼 클릭 핸들러를 분리해 `resetForm()` 후 `setIsAddDialogOpen(true)`를 호출한다.
- 필요 시 `editingAccount`도 함께 `null`로 정리해 상태 오염 가능성을 차단한다.
- 취소/닫기 동작에서도 추가 폼 상태가 남지 않도록 `onOpenChange` 처리 보강 여부를 검토한다.

## 5. 예상 영향 범위
- 프론트 `AccountManager`의 다이얼로그 오픈 UX에만 영향.
- 인증/DB/API/외부연동/빌드 설정 영향 없음.

## 6. 리스크 분석
- 실패 가능성
  - 초기화 시점이 과도하면 편집 중 사용자가 의도치 않게 입력값을 잃을 수 있음.
- 롤백 방법
  - `AccountManager.tsx`의 오픈 핸들러 변경분을 원복하면 즉시 복구 가능.

## 7. 테스트 계획
- 수동 검증
  - 임의 계좌 `수정` 클릭 후 값 확인
  - 수정 다이얼로그 닫고 `계좌 추가` 클릭 시 폼이 초기값인지 확인
  - `계좌 추가` 입력 후 취소 -> 재오픈 시 초기값 유지 확인
  - `계좌 수정` 동작(값 로딩/저장)이 기존과 동일한지 확인
- 필요 시 빌드 검증
  - `./gradlew`
  - `./gradlew build`

### 승인 여부
- 승인 완료 (사용자 응답: "변경할 브랜치명 알려주고 작업 진행해")
- 승인 시간: 21:43

## [TIME] 21:43 (KST) — [START] 계좌 수정 후 계좌 추가 폼 초기화 보강

### 작업 목표 요약
- 계좌 수정에서 사용한 폼 상태가 계좌 추가 다이얼로그로 유입되는 문제를 차단한다.
- 계좌 추가 다이얼로그 오픈/닫기 시 폼이 항상 초기 상태를 유지하도록 보강한다.

## [TIME] 21:44 (KST) — [IMPLEMENT] 계좌 추가 다이얼로그 오픈 핸들러 및 닫힘 초기화 적용

### 수행 내용
- `src/moneylog/src/components/AccountManager.tsx`
  - `handleOpenAddDialog` 추가
    - `resetForm()` 선호출 후 `setEditingAccount(null)` 및 `setIsAddDialogOpen(true)` 수행
  - `handleAddDialogOpenChange(open)` 추가
    - 다이얼로그 닫힘 시(`open === false`) `resetForm()` 실행
  - 계좌 추가 버튼 클릭 핸들러를 `handleOpenAddDialog`로 변경
  - 계좌 추가 다이얼로그 `onOpenChange`를 `handleAddDialogOpenChange`로 변경
  - 계좌 추가 다이얼로그 취소 버튼도 `handleAddDialogOpenChange(false)`를 사용하도록 변경

### 영향 범위
- `AccountManager` 내 계좌 추가 다이얼로그의 상태 초기화 동작에만 영향
- 계좌 수정 다이얼로그 및 저장 payload 구조 변경 없음

### VERIFY
- 루트에서 `./gradlew` 실행
  - 결과: 성공 (`BUILD SUCCESSFUL`)
- 루트에서 `./gradlew build` 실행
  - 결과: 실패
  - 실패 핵심 로그
    - `MoneyLogApplicationTests > contextLoads() FAILED`
    - `BeanCreationException`
    - `ServiceException`
    - `HibernateException at DialectFactoryImpl.java:191`
  - 재현 방법: 프로젝트 루트에서 `./gradlew build`
  - 해결 시도/결과: 재실행했으나 동일 테스트 실패로 종료

# 📊 계획 대비 수행 결과 비교

## 계획 대비 차이
- 계획대로 `AccountManager.tsx` 단일 파일에서 계좌 추가 다이얼로그 오픈/닫힘 초기화를 보강함.

## 추가 발생 이슈
- 기존 테스트 환경 이슈(`contextLoads`)로 `./gradlew build`는 통과하지 못함.

## 계획 외 수정 사항
- 없음

## [TIME] 22:18 (KST) — [IMPLEMENT] AGENTS 브랜치 생성 기준(master) 절대 규칙 명시 강화

### 수행 내용
- `AGENTS.md`
  - 상단에 `브랜치 생성 절대 규칙` 섹션 추가
    - 항상 `master`에서 브랜치 생성
    - 권장 순서(`git checkout master` -> `git pull` -> `git checkout -b ...`) 명시
    - 기능 브랜치에서 브랜치 파생 금지 문구 추가
    - 브랜치 생성/이동 요청 시 기준 브랜치 확인 보고 의무 추가
  - PRE-FLIGHT 섹션의 브랜치 규칙을 동일 기준으로 강화

### VERIFY
- `AGENTS.md` 내 `master` 기준 브랜치 생성 규칙 문구 확인 완료

# 📊 계획 대비 수행 결과 비교

## 계획 대비 차이
- 문서 강화 요청 범위 내에서만 수정함.

## 추가 발생 이슈
- 없음

## 계획 외 수정 사항
- 없음

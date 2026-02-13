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

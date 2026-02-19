# CHANGELOG 2026-02-15

## [MIGRATION-SUMMARY] CHANGELOG/WORKLOG 분리 적용
- 기존 상세 실행 이력은 `docs/workLog/WORKLOG_2026-02-15.md`로 이관
- 본 문서는 최종 결과 요약과 승인/결과 스냅샷만 유지

## [PLAN-SUMMARY] 예외 메시지 체계 정비
- 백엔드 예외 메시지 상수 통일 및 한글화
- 프론트 공통 에러 파서 도입으로 `errorMessage` 우선 노출
- Validation 메시지 품질 개선(필드 특화/제약코드 기반)

## [APPROVED] 주요 승인 이력
- 예외 메시지 상수 통일 및 프론트 연동 보강 승인
- ResponseStatusException 표준 응답화 승인
- Validation 메시지 정교화 및 안정성 보완 승인
- CHANGELOG/WORKLOG 전면 정리 승인

## [RESULT-SUMMARY] 주요 결과
- `ResponseStatusException`을 포함한 에러 응답 표준화로 메시지 일관성 개선
- `GlobalExceptionHandler` Validation 분기 로직 고도화
  - 커스텀 메시지/필드 특화 메시지/제약코드 메시지 우선순위 정비
  - `Range` 제약 처리 지원 추가
- `AGENTS.md` 문서 운영 규칙 개정
  - CHANGELOG/WORKLOG 역할 분리
  - 최종 보고 전 필수 더블체크 체크리스트 도입
- 문서 체계 정리
  - `docs/workLog/WORKLOG_2026-02-13.md`, `docs/workLog/WORKLOG_2026-02-14.md` 신규 생성
  - 기존 CHANGELOG 상세 원문을 WORKLOG로 이관

## [VERIFY-SUMMARY]
- 문서/정책/핸들러 수정 후 `./gradlew` 기준 반복 검증 성공
- 요청 기준에 따라 `./gradlew test`는 다수 작업에서 제외

## [RISK-SUMMARY]
- 일자별 세부 실행 로그, 실패 로그, 검증 원문은 `docs/workLog/WORKLOG_2026-02-15.md` 참조

## [RESULT-SUMMARY] CHANGELOG/WORKLOG 전면 정리 완료
- `docs/changeLog/CHANGELOG_2026-02-13.md`, `docs/changeLog/CHANGELOG_2026-02-14.md`, `docs/changeLog/CHANGELOG_2026-02-15.md`를 요약 중심으로 재구성
- 기존 상세 원문은 `docs/workLog/WORKLOG_2026-02-13.md`, `docs/workLog/WORKLOG_2026-02-14.md`, `docs/workLog/WORKLOG_2026-02-15.md`에 보존
- `AGENTS.md`에 마이그레이션 절차(선이관/후축약) 및 필수 요약 섹션 규칙을 추가

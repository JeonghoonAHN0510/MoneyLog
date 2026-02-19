# CHANGELOG 2026-02-14

## [MIGRATION-SUMMARY] CHANGELOG/WORKLOG 분리 적용
- 기존 상세 실행 이력은 `docs/workLog/WORKLOG_2026-02-14.md`로 이관
- 본 문서는 최종 결과 요약만 유지

## [RESULT-SUMMARY] 주요 결과
- 스케줄 설정 UI와 백엔드 스케줄 API 연동 안정화
- 스케줄 조회 경로 정리 및 저장 후 캐시 갱신 흐름 정비
- `cronExpression` 파싱을 통한 편집 초기값 동기화 및 입력 검증 강화
- 스케줄 파싱 로직 분리 리팩터링으로 컴포넌트 복잡도 완화

## [VERIFY-SUMMARY]
- `./gradlew` 성공
- `./gradlew build` 및 프론트 빌드 환경 이슈는 상세 로그에 기록

## [RISK-SUMMARY]
- 상세 변경 근거/실행 로그/검증 원문은 `docs/workLog/WORKLOG_2026-02-14.md` 참조

# CHANGELOG_2026-02-26

## [PLAN-SUMMARY]
- 로컬/AWS 이중 프로필 파일 업로드 구현 계획 수립
- PLAN 경로: `docs/2026-02-26/PLAN_2026-02-26.md`
- WORKLOG 경로: `docs/2026-02-26/WORKLOG_2026-02-26.md`

## [APPROVED]
- 사용자 승인(`Implement the plan.`) 확인, 20:26 (KST)

## [START-SUMMARY]
- 저장소 추상화, 공용 업로드 API 추가, 설정 분리 작업 시작

## [RESULT-SUMMARY]
- `FileStorage` 기반 local/s3 저장소 계층 구현
- `/api/files/upload` 엔드포인트 추가
- `application.yml`(AWS 기본), `application-local.yml`(local override) 분리
- 기존 `/uploads/...` 경로 하위호환 유지

## [VERIFY-SUMMARY]
- `./gradlew test`: 실패 (MySQL 연결 실패로 `contextLoads()` 실패)
- `./gradlew compileJava`: 성공
- 상세 실행 로그: `docs/2026-02-26/WORKLOG_2026-02-26.md`

---

## [PLAN-SUMMARY-2]
- 프로필 이미지 DB 업데이트 및 FinancePage 이미지 출력 반영 계획
- PLAN 경로: `docs/2026-02-26/PLAN_2026-02-26.md`
- WORKLOG 경로: `docs/2026-02-26/WORKLOG_2026-02-26.md`

## [APPROVED-2]
- 사용자 승인(`진행해...`) 확인, 20:43 (KST)

## [RESULT-SUMMARY-2]
- `PUT /api/user/profile-image` 추가로 프로필 이미지 업로드+DB 갱신 구현
- `GET /api/files/view` 추가로 URL 기반 이미지 출력 경로 제공
- FinancePage 헤더/드롭다운 아바타 표시 및 이미지 변경 UI 반영

## [VERIFY-SUMMARY-2]
- `./gradlew compileJava`: 성공
- `cd src/moneylog && npm run build`: 실패 (`vite: not found`)
- `cd src/moneylog && node node_modules/vite/bin/vite.js build`: 실패 (`@rollup/rollup-linux-x64-gnu` 누락)
- 상세 실행 로그: `docs/2026-02-26/WORKLOG_2026-02-26.md`

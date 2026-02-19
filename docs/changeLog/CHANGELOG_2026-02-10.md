# MoneyLog 변경 이력 — 2026-02-10

> 본 문서는 2026-02-10 에 수행한 **QC(품질 검증)**, **백엔드 유효성 검사 강화**, **대시보드·캘린더 API 신규 구현** 작업의 전체 내역을 기록합니다.

---

## 1. 백엔드 DTO 유효성 검사 (`@Valid`) 강화

### 1-1. TransactionController

| 메서드 | 변경 전 | 변경 후 |
|---|---|---|
| `saveTransaction` | `@RequestBody TransactionReqDto` | `@RequestBody @Valid TransactionReqDto` |
| `updateTransaction` | `@RequestBody TransactionReqDto` | `@RequestBody @Valid TransactionReqDto` |
| `deleteTransaction` | `TransactionDto` 객체 사용 | `@RequestParam Integer transactionId` 직접 수신 |

- **파일**: `transaction/controller/TransactionController.java`
- **핵심**: 모든 POST/PUT 요청에 `@Valid`를 추가하여 백엔드 유효성 검사 활성화. DELETE는 불필요한 DTO 제거.

### 1-2. TransactionService — `deleteTransaction` 시그니처 변경

| 항목 | 변경 전 | 변경 후 |
|---|---|---|
| 매개변수 | `TransactionDto transactionDto` | `Integer transactionId, Integer userId` |
| import | `TransactionDto` 사용 | `TransactionDto` import 제거 |

- **파일**: `transaction/service/TransactionService.java`
- **이유**: Controller에서 ID만 받아 넘기므로, Service도 동일하게 정리.

### 1-3. UserController — `LoginReqDto` 도입

| 메서드 | 변경 전 | 변경 후 |
|---|---|---|
| `login` | `@RequestBody UserDto` | `@RequestBody @Valid LoginReqDto` |
| `signup` | `@Valid @ModelAttribute UserDto` | 변경 없음 (기존 유지) |

- **파일**: `user/controller/UserController.java`
- **이유**: `UserDto`를 로그인에 사용하면 회원가입 전용 필드(`name`, `email` 등)까지 검증 대상이 되어 충돌 발생. 로그인 전용 `LoginReqDto`를 분리.

### 1-4. LoginReqDto (신규)

```java
public class LoginReqDto {
    @NotBlank(message = "아이디를 입력해주세요")
    private String id;

    @NotBlank(message = "비밀번호를 입력해주세요")
    private String password;
}
```

- **파일**: `user/dto/LoginReqDto.java` **(NEW)**
- **어노테이션**: `@Getter`, `@AllArgsConstructor`, `@NoArgsConstructor`

### 1-5. UserService — `login` 시그니처 변경

| 항목 | 변경 전 | 변경 후 |
|---|---|---|
| 매개변수 | `UserDto userDto` | `LoginReqDto loginReqDto` |
| 필드 접근 | `userDto.getId()` | `loginReqDto.getId()` |

- **파일**: `user/service/UserService.java`

### 1-6. 기타 Controller 확인 결과 (변경 없음)

| Controller | 상태 |
|---|---|
| `AccountController` | ✅ 이미 `@Valid` + `ReqDto` 적용 |
| `CategoryController` | ✅ 이미 `@Valid` + `ReqDto` 적용 |
| `BudgetController` | ✅ 이미 `@Valid` + `ReqDto` 적용 |
| `PaymentController` | ✅ 이미 `@Valid` + `ReqDto` 적용 |
| `FixedController` | ✅ 이미 `@Valid` + `ReqDto` 적용 |

---

## 2. GlobalExceptionHandler 보강 (사용자 직접 수정)

사용자가 직접 추가한 예외 핸들러:

```java
// 4-2. InternalAuthenticationServiceException → 401 Unauthorized
@ExceptionHandler(InternalAuthenticationServiceException.class)
public ResponseEntity<ErrorResponse> handleInternalAuthException(...) {
    return new ResponseEntity<>(
        new ErrorResponse("LOGIN_FAILED", "아이디 또는 비밀번호가 일치하지 않습니다."),
        HttpStatus.UNAUTHORIZED
    );
}
```

- **파일**: `global/exception/GlobalExceptionHandler.java`
- **목적**: `BadCredentialsException`이 `InternalAuthenticationServiceException`으로 감싸져 나올 수 있는 케이스 대응.
- **들여쓰기 수정**: `handleResourceNotFound` 메서드 내 `ErrorResponse` 생성 라인의 들여쓰기 정리.

---

## 3. 대시보드 & 캘린더 뷰 API (신규 구현)

### 3-1. 신규 DTO

| DTO | 용도 | 주요 필드 |
|---|---|---|
| `DailySummaryResDto` | 캘린더 일자별 합계 | `date`, `totalIncome`, `totalExpense` |
| `CategoryStatsResDto` | 카테고리별 지출 통계 | `categoryName`, `totalAmount`, `ratio` |
| `DashboardResDto` | 대시보드 종합 응답 | `totalIncome`, `totalExpense`, `totalBalance`, `categoryStats` |

- **위치**: `transaction/dto/res/` 패키지

### 3-2. MyBatis Mapper 확장

#### TransactionMapper.java

```java
List<DailySummaryResDto> getDailySummaries(SelectTransactionByUserIdQuery selectQuery);
List<CategoryStatsResDto> getCategoryStats(SelectTransactionByUserIdQuery selectQuery);
```

#### TransactionMapper.xml — 추가 쿼리

| 쿼리 ID | 설명 | GROUP BY |
|---|---|---|
| `getDailySummaries` | 일자별 수입/지출 합계 (`CASE WHEN` 사용) | `TRADING_AT` |
| `getCategoryStats` | 카테고리별 지출 합계 (지출만 필터) | `CATEGORY_ID, NAME` |

### 3-3. TransactionService — 신규 메서드

| 메서드 | 반환 타입 | 설명 |
|---|---|---|
| `getCalendarData(userId, year, month)` | `List<DailySummaryResDto>` | 월별 일자 데이터 조회 |
| `getDashboardData(userId, year, month)` | `DashboardResDto` | 수입/지출 합계 + 카테고리별 비율 계산 |

- **비율 계산 로직**: `totalExpense > 0`일 때 각 카테고리의 `totalAmount / totalExpense * 100`으로 퍼센트 산출.

### 3-4. TransactionController — 신규 엔드포인트

| HTTP | URL | 파라미터 | 설명 |
|---|---|---|---|
| `GET` | `/api/transaction/calendar` | `year` (선택), `month` (선택) | 캘린더 뷰 데이터 |
| `GET` | `/api/transaction/dashboard` | `year` (선택), `month` (선택) | 대시보드 통계 |

- 파라미터 미전달 시 **현재 연월** 기본값 사용.

---

## 4. 전체 수정 파일 목록

### 수정 (MODIFY)
| # | 파일 | 변경 내용 |
|---|---|---|
| 1 | `TransactionController.java` | `@Valid` 추가, `deleteTransaction` 리팩토링, 캘린더·대시보드 API 추가 |
| 2 | `TransactionService.java` | `deleteTransaction` 시그니처 변경, `getCalendarData`·`getDashboardData` 신규 |
| 3 | `TransactionMapper.java` | `getDailySummaries`, `getCategoryStats` 메서드 선언 추가 |
| 4 | `TransactionMapper.xml` | 통계 SQL 쿼리 2건 추가, `resultType` 명시 |
| 5 | `UserController.java` | `login` → `LoginReqDto` 사용, `@Valid` 추가 |
| 6 | `UserService.java` | `login` → `LoginReqDto` 매개변수로 변경 |
| 7 | `GlobalExceptionHandler.java` | `InternalAuthenticationServiceException` 핸들러 추가 |

### 신규 (NEW)
| # | 파일 | 설명 |
|---|---|---|
| 1 | `LoginReqDto.java` | 로그인 전용 요청 DTO |
| 2 | `DashboardResDto.java` | 대시보드 종합 응답 DTO |
| 3 | `DailySummaryResDto.java` | 캘린더 일자별 요약 DTO |
| 4 | `CategoryStatsResDto.java` | 카테고리별 지출 통계 DTO |

---

## 5. QC 검증 결과

| 검증 항목 | 결과 |
|---|---|
| Controller ↔ Service 시그니처 일치 | ✅ 통과 |
| `@Valid` 어노테이션 전 Controller 적용 확인 | ✅ 통과 |
| ReqDto 유효성 어노테이션 (`@NotNull`, `@NotBlank`, `@Min`, `@Size`) | ✅ 적절히 설정됨 |
| MyBatis XML ↔ Mapper 인터페이스 메서드명 일치 | ✅ 통과 |
| MyBatis `resultType` 패키지 경로 정확성 | ✅ 통과 |
| DTO Lombok 어노테이션 (`@Getter`, `@Builder`, `@AllArgs/NoArgs`) | ✅ 통과 |
| `TransactionService` private 메서드 존재 확인 | ✅ 4개 모두 정상 |
| `GlobalExceptionHandler` 예외 처리 체계 | ✅ 정상 |
| import 정리 (미사용 import 제거) | ✅ 완료 |

---

## 6. 향후 작업 권고

1. **프론트엔드 캘린더·대시보드 연동**: 신규 API(`/api/transaction/calendar`, `/api/transaction/dashboard`)를 프론트에서 호출하여 UI 구현.
2. **통합 테스트**: 유효성 검사(`@Valid`) 동작을 실제 API 호출로 테스트 (유효/무효 데이터 모두).
3. **프론트엔드 `categoryType` 정리**: 프론트에서 Transaction 요청 시 전송하는 `categoryType` 필드는 백엔드에서 불필요하므로 제거 고려.

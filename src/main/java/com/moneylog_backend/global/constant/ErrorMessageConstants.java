package com.moneylog_backend.global.constant;

public final class ErrorMessageConstants {
    public static final String USER_NOT_FOUND = "존재하지 않는 회원입니다.";
    public static final String BANK_NOT_FOUND = "존재하지 않는 은행입니다.";
    public static final String ACCOUNT_NOT_FOUND = "존재하지 않는 계좌입니다.";
    public static final String CATEGORY_NOT_FOUND = "존재하지 않는 카테고리입니다.";
    public static final String PAYMENT_NOT_FOUND = "존재하지 않는 결제수단입니다.";
    public static final String BUDGET_NOT_FOUND = "존재하지 않는 예산입니다.";
    public static final String TRANSACTION_NOT_FOUND = "존재하지 않는 지출 내역입니다.";
    public static final String DUPLICATE_LOGIN_ID = "이미 가입된 아이디입니다.";
    public static final String DUPLICATE_EMAIL = "이미 가입된 이메일입니다.";
    public static final String ACCESS_DENIED = "접근 권한이 없습니다.";
    public static final String INVALID_JSON = "올바르지 않은 데이터 형식입니다.";
    public static final String LOGIN_FAILED = "아이디 또는 비밀번호가 일치하지 않습니다.";
    public static final String INTERNAL_SERVER_ERROR = "서버 내부 오류가 발생했습니다.";
    public static final String BAD_REQUEST = "잘못된 요청입니다.";
    public static final String CONFLICT = "요청이 현재 상태와 충돌합니다.";
    public static final String NOT_FOUND = "요청한 리소스를 찾을 수 없습니다.";
    public static final String SCHEDULE_RESCHEDULE_FAILED = "스케줄 재등록에 실패했습니다.";
    public static final String SCHEDULE_WEEKLY_DAY_OF_WEEK_REQUIRED = "주간 스케줄은 요일(1~7)이 필요합니다.";
    public static final String SCHEDULE_MONTHLY_DAY_OF_MONTH_REQUIRED = "월간 스케줄은 실행일(1~31)이 필요합니다.";
    public static final String FILE_NOT_FOUND = "파일을 찾을 수 없습니다.";
    public static final String INVALID_FILE_URL = "올바르지 않은 파일 경로입니다.";
    public static final String FILE_EXTENSION_REQUIRED = "파일 확장자는 필수입니다.";
    public static final String FILE_EXTENSION_NOT_ALLOWED = "허용되지 않은 파일 확장자입니다.";
    public static final String FILE_SIZE_EXCEEDED = "파일 크기가 허용 범위를 초과했습니다.";
    public static final String FILE_REQUIRED = "업로드 파일은 필수입니다.";
    public static final String INVALID_UPLOAD_DIRECTORY = "올바르지 않은 업로드 디렉터리입니다.";
    public static final String FILE_DELETE_FAILED = "파일 삭제 중 오류가 발생했습니다.";
    public static final String S3_STORAGE_NOT_ENABLED = "S3 저장소가 활성화되어 있지 않습니다.";

    private static final String UNKNOWN_STATUS_FORMAT = "알 수 없는 상태 코드입니다: %s";
    private static final String UNKNOWN_FREQUENCY_FORMAT = "알 수 없는 스케줄 빈도입니다: %s";
    private static final String UNKNOWN_ROLE_FORMAT = "알 수 없는 권한 코드입니다: %s";
    private static final String UNKNOWN_PROVIDER_FORMAT = "알 수 없는 로그인 제공자 코드입니다: %s";
    private static final String UNKNOWN_PAYMENT_TYPE_FORMAT = "알 수 없는 결제수단 유형입니다: %s";
    private static final String UNKNOWN_CATEGORY_TYPE_FORMAT = "알 수 없는 카테고리 유형입니다: %s";
    private static final String UNKNOWN_ACCOUNT_TYPE_FORMAT = "알 수 없는 계좌 유형입니다: %s";
    private static final String SCHEDULE_JOB_NOT_FOUND_FORMAT = "존재하지 않는 스케줄 작업입니다: %s";

    private ErrorMessageConstants() {
    }

    public static String unknownStatus(String code) {
        return UNKNOWN_STATUS_FORMAT.formatted(String.valueOf(code));
    }

    public static String unknownFrequency(String code) {
        return UNKNOWN_FREQUENCY_FORMAT.formatted(String.valueOf(code));
    }

    public static String unknownRole(String code) {
        return UNKNOWN_ROLE_FORMAT.formatted(String.valueOf(code));
    }

    public static String unknownProvider(String code) {
        return UNKNOWN_PROVIDER_FORMAT.formatted(String.valueOf(code));
    }

    public static String unknownPaymentType(String code) {
        return UNKNOWN_PAYMENT_TYPE_FORMAT.formatted(String.valueOf(code));
    }

    public static String unknownCategoryType(String code) {
        return UNKNOWN_CATEGORY_TYPE_FORMAT.formatted(String.valueOf(code));
    }

    public static String unknownAccountType(String code) {
        return UNKNOWN_ACCOUNT_TYPE_FORMAT.formatted(String.valueOf(code));
    }

    public static String scheduleJobNotFound(String jobName) {
        return SCHEDULE_JOB_NOT_FOUND_FORMAT.formatted(String.valueOf(jobName));
    }
}

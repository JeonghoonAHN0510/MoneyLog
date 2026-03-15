export const AUTH_PLACEHOLDERS = {
  loginId: '4~20자 아이디',
  signupName: '예: 홍길동',
  email: 'name@example.com',
  bankSelect: '은행을 선택해 주세요',
  accountNumber: '계좌번호를 입력해 주세요',
  phone: '01012345678',
} as const;

export const AUTH_HELPER_TEXT = {
  password: '최소 6자 이상 입력해 주세요.',
  phone: '하이픈 포함/미포함 모두 입력할 수 있습니다.',
  accountNumber: '하이픈 포함/미포함 모두 가능하며 저장 시 은행 형식으로 정리됩니다.',
} as const;
